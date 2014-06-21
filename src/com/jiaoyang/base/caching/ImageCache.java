package com.jiaoyang.base.caching;

import java.io.File;
import java.io.IOException;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.util.LruCache;

import com.jiaoyang.tv.util.Logger;

/**
 * This class holds our bitmap caches (memory and disk).
 */
public class ImageCache {
    private static final Logger LOG = Logger.getLogger(ImageCache.class);

    // Default memory cache size
    private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

    // Default disk cache size
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 30; // 30MB

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    private static final int DEFAULT_COMPRESS_QUALITY = 80;

    // Constants to easily toggle various caches
    private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
    private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
    private static final boolean DEFAULT_CLEAR_DISK_CACHE_ON_START = false;

    private DiskLruCache mWeakDiskCache;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mStrongDiskCache;

    /**
     * Creating a new ImageCache object using the specified parameters.
     * 
     * @param context
     *            The context to use
     * @param cacheParams
     *            The cache parameters to use to initialize the cache
     * @throws InsufficientStorageException
     * @throws IOException
     */
    public ImageCache(Context context, ImageCacheParams cacheParams) throws IOException {
        init(context, cacheParams);
    }

    /**
     * Creating a new ImageCache object using the default parameters.
     * 
     * @param context
     *            The context to use
     * @param uniqueName
     *            A unique name that will be appended to the cache directory
     * @throws InsufficientStorageException
     * @throws IOException
     */
    public ImageCache(Context context, String uniqueName) throws IOException {
        init(context, new ImageCacheParams(uniqueName));
    }

    /**
     * Initialize the cache, providing all parameters.
     * 
     * @param context
     *            The context to use
     * @param cacheParams
     *            The cache parameters to initialize the cache
     * @throws IOException
     *             , InsufficientStorageException
     */
    private void init(Context context, ImageCacheParams cacheParams) throws IOException {

        // Set up disk cache
        if (cacheParams.diskCacheEnabled) {
            File weakDiskCacheDir = DiskLruCache.getDiskCacheDir(context, cacheParams.uniqueName);
            File strongDiskCacheDir = DiskLruCache.getDiskCacheDir(context, cacheParams.uniqueName + "_strong");
            //setup strong disk cache
            mStrongDiskCache = DiskLruCache.openCacheSafe(context, strongDiskCacheDir, true);
            if (mStrongDiskCache == null) {
                strongDiskCacheDir = DiskLruCache.getInternalDiskCacheDir(context, cacheParams.uniqueName + "_strong");
                mStrongDiskCache = DiskLruCache.openCacheSafe(context, strongDiskCacheDir, true);
            }
            mStrongDiskCache.setCompressParams(cacheParams.compressFormat, 100);

            //setup weak disk cache
            mWeakDiskCache = DiskLruCache.openCacheSafe(context, weakDiskCacheDir, cacheParams.diskCacheSize);
            if (mWeakDiskCache == null) {
                weakDiskCacheDir = DiskLruCache.getInternalDiskCacheDir(context, cacheParams.uniqueName);
                mWeakDiskCache = DiskLruCache.openCacheSafe(context, weakDiskCacheDir, cacheParams.diskCacheSize);
            }
            mWeakDiskCache.setCompressParams(cacheParams.compressFormat, cacheParams.compressQuality);
            if (cacheParams.clearDiskCacheOnStart) {
                mWeakDiskCache.clearCache();
            }
        }

        // Set up memory cache
        if (cacheParams.memoryCacheEnabled) {
            mMemoryCache = new LruCache<String, Bitmap>(cacheParams.memCacheSize) {
                /**
                 * Measure item size in bytes rather than units which is more practical for a bitmap cache
                 */
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return CacheUtils.getBitmapSize(bitmap);
                }
            };
        }
    }

    public void addBitmapToCache(String data, Bitmap bitmap) {
        addBitmapToCache(data, bitmap, false);
    }

    /**
     * 将bitmap保存到各级cache中：MemCache、WeakDiskCache、StrongDiskCache
     * 在cache中已经存在的，不会重复添加
     */
    public void addBitmapToCache(String data, Bitmap bitmap, boolean permanentImage) {
        if (data == null || bitmap == null) {
            return;
        }

        // Add to memory cache
        if (mMemoryCache != null && mMemoryCache.get(data) == null) {
            mMemoryCache.put(data, bitmap);
            LOG.debug("add to memory cache. current cache size: {}MB", mMemoryCache.size() / 1024 / 1024);
        }

        // Add to disk cache
        if (permanentImage && mStrongDiskCache != null) {
            if (!mStrongDiskCache.containsKey(data)) {
                mStrongDiskCache.put(data, bitmap);
            }
        } else {
            if (mWeakDiskCache != null && !mWeakDiskCache.containsKey(data)) {
                mWeakDiskCache.put(data, bitmap);
            }
        }
    }

    /**
     * Get from memory cache.
     * 
     * @param data
     *            Unique identifier for which item to get
     * @return The bitmap if found in cache, null otherwise
     */
    public Bitmap getBitmapFromMemCache(String data) {
        if (mMemoryCache != null) {
            final Bitmap memBitmap = mMemoryCache.get(data);
            if (memBitmap != null) {
                LOG.debug("memory cache hit. current cache size: {}MB", mMemoryCache.size() / 1024 / 1024);
                return memBitmap;
            }
        }
        return null;
    }

    public Bitmap getBitmapFromDiskCache(String data) {
        return getBitmapFromDiskCache(data, false);
    }
    /**
     * 从DiskCache中查找bitmap
     * @param data 所查找bitmap的url
     * @param permanentImage 是否要永久存储这张bitmap
     * @return
     */
    public Bitmap getBitmapFromDiskCache(String data, boolean permanentImage) {
        Bitmap result = null;
        if (mStrongDiskCache != null) {
            result = mStrongDiskCache.get(data);
        }
        if (result == null && mWeakDiskCache != null) {
            result = mWeakDiskCache.get(data);
            if(permanentImage && result != null) {
                addBitmapToCache(data, result, true);
            }
        }

        return result;
    }

    public File getFileFromDiskCache(String data) {
        File result = null;
        if (mStrongDiskCache != null) {
            result = mStrongDiskCache.getFile(data);
        }
        if (result == null && mWeakDiskCache != null) {
            result = mWeakDiskCache.getFile(data);
        }
        return result;
    }

    public void clearCaches() {
        //TODO
        //actually we should only clear weakDiskCache at the first step
        //but now disk caches will never be cleared, so anyway, clear 2 caches both.
        mWeakDiskCache.clearCache();
        mStrongDiskCache.clearCache();
        mMemoryCache.evictAll();
    }

    public void clearMemeoryCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

    public boolean removeFileFromCache(String url) {
        try {
            return mStrongDiskCache.getFile(url).delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * A holder class that contains cache parameters.
     */
    public static class ImageCacheParams {
        public String uniqueName;
        public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
        public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
        public int compressQuality = DEFAULT_COMPRESS_QUALITY;
        public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
        public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
        public boolean clearDiskCacheOnStart = DEFAULT_CLEAR_DISK_CACHE_ON_START;

        public ImageCacheParams(String uniqueName) {
            this.uniqueName = uniqueName;
        }
    }

    /**
     * Create a cache according to device's MemoryClass for an application. Some device is 16MB, some is 24MB, 32MB,
     * etc. By default this cache supports both Memory Cache and Disk Cache.
     * 
     * @param context
     * @param cacheName
     *            The name identify the cache, will be used it to create file folder on the disk.
     * @param sizeFactor
     *            is for calculate the CacheSize, if device's MemoryClass is 16MB, sizeFactor=4, the cache size is
     *            16MB/4=4MB.
     * @return New created Cache instance.
     */
    public static ImageCache getPerfectCache(Context context, String cacheName, int sizeFactor) {
        ImageCacheParams params = new ImageCacheParams(cacheName);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memClass = am.getMemoryClass();
        LOG.debug("memory size for this app is: {}MB", memClass);
        params.memCacheSize = 1024 * 1024 * memClass / sizeFactor;

        LOG.debug("try init image cache with memory & disk cache.");
        ImageCache cache = newImageCache(context, params);
        if (cache == null) {
            params.diskCacheEnabled = false;

            LOG.debug("try init image cache without disk cache.");
            cache = newImageCache(context, params);
            if (cache == null) {
                params.memoryCacheEnabled = false;

                LOG.debug("try init image cache without memory & disk cache.");
                cache = newImageCache(context, params);
            }
        }

        return cache;
    }

    private static ImageCache newImageCache(Context context, ImageCacheParams params) {
        ImageCache cache = null;

        try {
            cache = new ImageCache(context, params);
        } catch (IOException e) {
            LOG.warn("init image cache failed. err={}", e.toString());
        }

        return cache;
    }
}
