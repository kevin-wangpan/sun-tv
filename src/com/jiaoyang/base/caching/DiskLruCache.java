package com.jiaoyang.base.caching;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.jiaoyang.base.misc.JiaoyangConstants;
import com.jiaoyang.tv.util.Logger;

/**
 * A simple disk LRU bitmap cache to illustrate how a disk cache would be used for bitmap caching. A much more robust
 * and efficient disk LRU cache solution can be found in the ICS source code
 * (libcore/luni/src/main/java/libcore/io/DiskLruCache.java) and is preferable to this simple implementation.
 */
public class DiskLruCache {
    private static final Logger LOG = Logger.getLogger(DiskLruCache.class);

    private static final String CACHE_FILENAME_PREFIX = "cache_";
    private static final int MAX_REMOVALS = 4;
    private static final int INITIAL_CAPACITY = 32;
    private static final float LOAD_FACTOR = 0.75f;

    private final File mCacheDir;
    private static int cacheSize = 0;
    private static int cacheByteSize = 0;
    private final int maxCacheItemSize = 1024; // 1024 item default
    private static final int DEFAULT_CACHE_SIZE = 1024 * 1024 * 5; // 5MB default
    private long maxCacheByteSize = DEFAULT_CACHE_SIZE;
    private CompressFormat mCompressFormat = CompressFormat.JPEG;
    private int mCompressQuality = 70;

    private boolean mIsStrongCache = false;

    private final Map<String, String> mLinkedHashMap =
            Collections.synchronizedMap(new LinkedHashMap<String, String>(INITIAL_CAPACITY, LOAD_FACTOR, true));

    /**
     * A filename filter to use to identify the cache filenames which have CACHE_FILENAME_PREFIX prepended.
     */
    private static final FilenameFilter cacheFileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return filename.startsWith(CACHE_FILENAME_PREFIX);
        }
    };

    private static DiskLruCache openCache(Context context, File cacheDir, long maxByteSize) throws IOException {
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        long usableSpace = CacheUtils.getUsableSpace(cacheDir);
        //如果要求的cache空间，大于SD剩余空间，则创建一个大小为DEFAULT_CACHE_SIZE的cache；
        //如果SD的剩余空间比DEFAULT_CACHE_SIZE还小，则直接throw InsufficientStorageException
        if (usableSpace <= DEFAULT_CACHE_SIZE) {
            Log.w("jiaoyang", "创建cache失败，空间不够：" + usableSpace);
        } else {
            if (usableSpace < maxByteSize) {
                maxByteSize = DEFAULT_CACHE_SIZE;
            }
        }
        if (!cacheDir.isDirectory() || !cacheDir.canWrite()) {
            throw new IOException();
        }
        if (cacheDir.isDirectory() && cacheDir.canWrite() && CacheUtils.getUsableSpace(cacheDir) > maxByteSize) {
            return new DiskLruCache(cacheDir, maxByteSize);
        }

        return null;
    }

    public static DiskLruCache openCacheSafe(Context context, File cacheDir, long maxByteSize) {
        return openCacheSafe(context, cacheDir, maxByteSize, false);
    }

    public static DiskLruCache openCacheSafe(Context context, File cacheDir, boolean isStrong) {
        return openCacheSafe(context, cacheDir, DEFAULT_CACHE_SIZE, isStrong);
    }

    /**
     * 创建一个Disk Cache。
     * @param context
     * @param cacheDir 创建cache的目录
     * @param maxByteSize cache最大能占用的空间（如果isStrong为true，那么该cache可占用的磁盘空间无限大，直到所在磁盘分区满为止
     * @param isStrong 是否是永久cache
     * @return
     */
    public static DiskLruCache openCacheSafe(Context context, File cacheDir, long maxByteSize, boolean isStrong) {
        DiskLruCache cache = null;

        try {
            cache = openCache(context, cacheDir, maxByteSize);
            cache.mIsStrongCache = isStrong;
        } catch (IOException e) {
            LOG.warn("open cache failed. err={}", e.toString());
        }

        return cache;
    }

    /**
     * Constructor that should not be called directly, instead use {@link DiskLruCache#openCache(Context, File, long)}
     * which runs some extra checks before creating a DiskLruCache instance.
     * 
     * @param cacheDir
     * @param maxByteSize
     */
    private DiskLruCache(File cacheDir, long maxByteSize) {
        putOldFileToLinkedHashMap(cacheDir);
        mCacheDir = cacheDir;
        maxCacheByteSize = maxByteSize;
    }

    /**
     * Add a bitmap to the disk cache.
     * 
     * @param key
     *            A unique identifier for the bitmap.
     * @param data
     *            The bitmap to store.
     */
    public void put(String key, Bitmap data) {
        synchronized (mLinkedHashMap) {
            if (mLinkedHashMap.get(key) == null) {
                try {
                    final String file = createFilePath(mCacheDir, key);
                    if (writeBitmapToFile(data, file)) {
                        put(key, file);
                        flushCache();
                    }
                } catch (FileNotFoundException e) {
                    LOG.warn(e);
                } catch (IOException e) {
                    LOG.warn(e);
                }
            }
        }
    }

    private void put(String key, String file) {
        mLinkedHashMap.put(key, file);
        cacheSize = mLinkedHashMap.size();
        cacheByteSize += new File(file).length();
    }

    /**
     * Flush the cache, removing oldest entries if the total size is over the specified cache size. Note that this isn't
     * keeping track of stale files in the cache directory that aren't in the HashMap. If the images and keys in the
     * disk cache change often then they probably won't ever be removed.
     */
    private void flushCache() {
        if (mIsStrongCache) {
            return;
        }
        Entry<String, String> eldestEntry;
        File eldestFile;
        long eldestFileSize;
        int count = 0;

        while (count < MAX_REMOVALS && (cacheSize > maxCacheItemSize || cacheByteSize > maxCacheByteSize)) {
            eldestEntry = mLinkedHashMap.entrySet().iterator().next();
            eldestFile = new File(eldestEntry.getValue());
            eldestFileSize = eldestFile.length();
            mLinkedHashMap.remove(eldestEntry.getKey());
            eldestFile.delete();
            cacheSize = mLinkedHashMap.size();
            cacheByteSize -= eldestFileSize;
            count++;
        }
    }

    /**
     * Get an image from the disk cache.
     * 
     * @param key
     *            The unique identifier for the bitmap
     * @return The bitmap or null if not found
     */
    public Bitmap get(String key) {
        synchronized (mLinkedHashMap) {
            final String file = mLinkedHashMap.get(key);
            if (file != null) {
                LOG.debug("disk cache hit.");

                return BitmapFactory.decodeFile(file);
            } else {
                final String existingFile = createFilePath(mCacheDir, key);
                if (new File(existingFile).exists()) {
                    put(key, existingFile);
                    LOG.debug("disk cache hit (existing file).");

                    return BitmapFactory.decodeFile(existingFile);
                }
            }
            return null;
        }
    }

    public File getFile(String key) {
        synchronized (mLinkedHashMap) {
            final String file = mLinkedHashMap.get(key);
            if (file != null) {
                LOG.debug("disk cache hit.");

                return new File(file);
            } else {
                final String existingFile = createFilePath(mCacheDir, key);
                if (new File(existingFile).exists()) {
                    put(key, existingFile);
                    LOG.debug("disk cache hit (existing file).");

                    return new File(existingFile);
                }
            }
            return null;
        }
    }
    /**
     * Checks if a specific key exist in the cache.
     * 
     * @param key
     *            The unique identifier for the bitmap
     * @return true if found, false otherwise
     */
    public boolean containsKey(String key) {
        // See if the key is in our HashMap
        if (mLinkedHashMap.containsKey(key)) {
            return true;
        }

        // Now check if there's an actual file that exists based on the key
        final String existingFile = createFilePath(mCacheDir, key);
        if (new File(existingFile).exists()) {
            // File found, add it to the HashMap for future use
            put(key, existingFile);
            return true;
        }
        return false;
    }

    /**
     * Removes all disk cache entries from this instance cache dir
     */
    public void clearCache() {
        DiskLruCache.clearCache(mCacheDir);
    }

    /**
     * Removes all disk cache entries from the application cache directory in the uniqueName sub-directory.
     * 
     * @param context
     *            The context to use
     * @param uniqueName
     *            A unique cache directory name to append to the app cache directory
     */
    public static void clearCache(Context context, String uniqueName) {
        File cacheDir = getDiskCacheDir(context, uniqueName);
        clearCache(cacheDir);
    }
    
    /**
     * Removes all disk cache entries from the given directory. This should not be called directly, call
     * {@link DiskLruCache#clearCache(Context, String)} or {@link DiskLruCache#clearCache()} instead.
     * 
     * @param cacheDir
     *            The directory to remove the cache files from
     */
    private static void clearCache(File cacheDir) {
        final File[] files = cacheDir.listFiles(cacheFileFilter);
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     * 
     * @param context
     *            The context to use
     * @param uniqueName
     *            A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {

        String cachePath = CacheUtils.getCachePath(context);
        return new File(cachePath + File.separator + uniqueName);
    }

    public static File getInternalDiskCacheDir(Context context, String uniqueName) {
        return new File(context.getCacheDir().getPath() + File.separator + uniqueName);
    }

    /**
     * Creates a constant cache file path given a target cache directory and an image key.
     * 
     * @param cacheDir
     * @param key
     * @return
     */
    public static String createFilePath(File cacheDir, String key) {
        try {
            // Use URLEncoder to ensure we have a valid filename, a tad hacky but it will do for
            // this example
            return cacheDir.getAbsolutePath() + File.separator + CACHE_FILENAME_PREFIX
                    + URLEncoder.encode(key.replace("*", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e);
        }

        return null;
    }

    /**
     * Create a constant cache file path using the current cache directory and an image key.
     * 
     * @param key
     * @return
     */
    public String createFilePath(String key) {
        return createFilePath(mCacheDir, key);
    }

    /**
     * Sets the target compression format and quality for images written to the disk cache.
     * 
     * @param compressFormat
     * @param quality
     */
    public void setCompressParams(CompressFormat compressFormat, int quality) {
        mCompressFormat = compressFormat;
        mCompressQuality = quality;
    }

    /**
     * Writes a bitmap to a file. Call {@link DiskLruCache#setCompressParams(CompressFormat, int)} first to set the
     * target bitmap compression and format.
     * 
     * @param bitmap
     * @param file
     * @return
     */
    private boolean writeBitmapToFile(Bitmap bitmap, String file) throws IOException, FileNotFoundException {

        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file), CacheUtils.IO_BUFFER_SIZE);
            CompressFormat format = mCompressFormat;
            if (file.endsWith("png") ||
                    file.endsWith("PNG")) {
                format = CompressFormat.PNG;
            } else if (file.endsWith("jpg") ||
                    file.endsWith("JPG") ||
                    file.endsWith("jpeg") ||
                    file.endsWith("JPEG")) {
                format = CompressFormat.JPEG;
            }
            return bitmap.compress(format, mCompressQuality, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    /**
     * 
     * put early files to mLinkedHashMap before sort cacheDir' files
     * @Title: putOldFileToLinkedHashMap
     * @param cacheDir
     * @return void
     * @date 2013年10月16日 下午3:21:30
     */
    @SuppressWarnings("unchecked")
    private void putOldFileToLinkedHashMap(File cacheDir) {
        if (!cacheDir.getAbsolutePath().endsWith(JiaoyangConstants.Cache.IMAGE_CACHE_NAME))
            return;
        final File files[] = cacheDir.listFiles(cacheFileFilter);
        try {
            Arrays.sort(files, new LastModifiedFileComparator());
            for (int i = 0; i < files.length; i++) {
                File tempFile = files[i];
                String fileName = tempFile.getName().replace(CACHE_FILENAME_PREFIX, "");
                String decodename = URLDecoder.decode(fileName.replace("*", ""), "UTF-8");
                String filePath = tempFile.getAbsolutePath();
                
                if (!mLinkedHashMap.containsKey(decodename)) 
                    synchronized (mLinkedHashMap) {
                        mLinkedHashMap.put(decodename, filePath);
                        cacheSize = mLinkedHashMap.size();
                        cacheByteSize += new File(filePath).length();
                    }
            }
            
        } catch (UnsupportedEncodingException e) {
            LOG.warn("decode url err{}", e);
        }
    }

    @SuppressWarnings("rawtypes")
    class LastModifiedFileComparator implements Comparator
    {
        @Override
        public int compare(Object object1, Object object2) {
            File file1 = (File) object1;
            File file2 = (File) object2;
            long result = file1.lastModified() - file2.lastModified();
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
