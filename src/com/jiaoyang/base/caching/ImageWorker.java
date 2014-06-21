package com.jiaoyang.base.caching;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadPoolExecutor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.jiaoyang.tv.util.Logger;

/**
 * <p>This class wraps up completing some arbitrary long running work when
 * loading a bitmap to an ImageView.
 * <p>It handles things like using a memory and disk cache,
 * running the work in a background thread and setting a placeholder image.
 */
public abstract class ImageWorker {
    private static final Logger LOG = Logger.getLogger(ImageCache.class);

    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = false;
    private boolean mExitTasksEarly = false;

    protected Context mContext;
    protected OnLoadImageListener mOnLoadImageListener;

    protected int mLoadingImageResId;

    private static ThreadPoolExecutor sExecutor;
    static {
        sExecutor = (ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR;
        sExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    }
    protected ImageWorker(Context context) {
        mContext = context;
    }

    public void loadImage(Object data, ImageView imageView) {
        loadImage(data, imageView, false);
    }

    /**
     * <p>Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(Object)} to define the processing logic).
     * <p>A memory and disk cache will be used if an {@link ImageCache} has been set using
     * {@link ImageWorker#setImageCache(ImageCache)}. If the image is found
     * in the memory cache, it is set immediately, otherwise an {@link AsyncTask} will be
     * created to asynchronously load the bitmap.
     * 
     * @param data
     *            The URL of the image to download.
     * @param imageView
     *            The ImageView to bind the downloaded image to.
     * @param permanentImage
     *            save this image locally for offline browsing or not.
     */
    public void loadImage(Object data, ImageView imageView, boolean permanentImage) {
        if(data == null) {
            return;
        }
        cancelPotentialWork(data, imageView);
        String key = String.valueOf(data);
        Bitmap bitmap = null;
        if (mImageCache != null) {
            bitmap = mImageCache.getBitmapFromMemCache(key);
        }

        if (bitmap != null) {
            // Bitmap found in memory cache
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
            if (mOnLoadImageListener != null) {
                mOnLoadImageListener.onLoadCompleted(bitmap);
            }
            if (permanentImage) {
                //this bitmap may only exist in memory cache.
                //add this bitmap to StrongImageCache
                mImageCache.addBitmapToCache(key, bitmap, permanentImage);
            }
        } else {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView, permanentImage);
            if (imageView != null) {
                imageView.setImageResource(mLoadingImageResId);
                imageView.setTag(new WeakReference<BitmapWorkerTask>(task));
            }
            task.executeOnExecutor(sExecutor, data);
        }
    }

    /**
     * <p>只是加载一个Bitmap，并不把Bitmap放到任何imageview中。
     * <p>该方法是非阻塞调用，加载Bitmap在异步线程中进行，需要实现 
     * {@link OnLoadImageListener}以获取加载出来的Bitmap
     * @param data
     * @param permanentImage 是否把图片永久缓存到本地磁盘中
     */
    public void loadImage(Object data, boolean permanentImage) {
        loadImage(data, null, permanentImage);
    }
    public Uri getImage(String url) {
        if (mImageCache != null) {
            File f = mImageCache.getFileFromDiskCache(url);
            if (f == null) {
                return null;
            }
            return Uri.fromFile(f);
        }
        return null;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     * 
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        if (null != bitmap) {
            mLoadingBitmap = null;
            mLoadingBitmap = bitmap;
            bitmap = null;
        }
    }


    public Bitmap getLoadingImage() {
        return mLoadingBitmap;
    }

    /**
     * Set the {@link ImageCache} object to use with this ImageWorker.
     * 
     * @param cacheCallback
     */
    public void setImageCache(ImageCache cacheCallback) {
        mImageCache = cacheCallback;
    }

    public ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     * 
     * @param fadeIn
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce the final bitmap.
     * This will be executed in a background thread and be long running. For example, you could resize a large bitmap
     * here, or pull down an image from the network.
     * 
     * @param data
     *            The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Object, ImageView)}
     * @return The processed bitmap
     */
    protected abstract Bitmap processBitmap(Object data);

    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
            final Object bitmapData = bitmapWorkerTask.data;
            LOG.debug("{} canceled." + bitmapData);
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not stopped in that case.
     */
    private boolean cancelPotentialWork(Object data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Object bitmapData = bitmapWorkerTask.data;
            if (bitmapData == null || !bitmapData.equals(data)) {
                bitmapWorkerTask.cancel(true);
                LOG.debug("{} cancelled.", data);
            } else {
                return false;
            }
        }

        return true;
    }

    /**
     * @param imageView
     *            Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView. null if there is no such
     *         task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            Object o = imageView.getTag();
            if (o != null) {
                return ((WeakReference<BitmapWorkerTask>) o).get();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
        private Object data;
        private final WeakReference<ImageView> imageViewReference;
        private boolean permanentImage;
        private boolean loadBitmapWithoutImageView;

        public BitmapWorkerTask(ImageView imageView, boolean permanentImage) {
            if (imageView == null) {
                loadBitmapWithoutImageView = true;
            }
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.permanentImage = permanentImage;
        }

        /**
         * Background processing.
         */
        @Override
        protected Bitmap doInBackground(Object... params) {
            if (mExitTasksEarly) {
                return null;
            }
            data = params[0];
            final String dataString = String.valueOf(data);
            Bitmap bitmap = null;

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && imageViewValid() && !mExitTasksEarly) {
                bitmap = mImageCache.getBitmapFromDiskCache(dataString, permanentImage);
            }

            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && imageViewValid() && !mExitTasksEarly) {
                bitmap = processBitmap(params[0]);
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null && mImageCache != null) {
                mImageCache.addBitmapToCache(dataString, bitmap, permanentImage);
            }

            return bitmap;
        }

        private boolean imageViewValid() {
            return getAttachedImageView() != null || loadBitmapWithoutImageView;
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                LOG.debug("{} Cancelled/ExitTasksEarly, Ignore display.", data);

                bitmap = null;
            }

            if (bitmap == null) {
                return;
            }
            if (mOnLoadImageListener != null) {
                mOnLoadImageListener.onLoadCompleted(bitmap);
            }
            final ImageView imageView = getAttachedImageView();
            if (imageView != null) {
                setImageBitmap(imageView, bitmap);
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still points to this task as
         * well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            } else {
                LOG.debug("BitmapWorkerTask atttached to this ImagerView is not matched to current task.");
            }

            return null;
        }
    }

    /**
     * Called when the processing is complete and the final bitmap should be set on the ImageView.
     * 
     * @param imageView
     * @param bitmap
     */
    private void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drwabale and the final bitmap
            final TransitionDrawable td = new TransitionDrawable(
                    new Drawable[] { new ColorDrawable(android.R.color.transparent),
                            new BitmapDrawable(mContext.getResources(), bitmap) });
            // Set background to loading bitmap
            imageView.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    public void setImageLoadListener(OnLoadImageListener listener) {
        this.mOnLoadImageListener = listener;
    }
}
