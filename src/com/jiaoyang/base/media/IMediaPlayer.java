package com.jiaoyang.base.media;

import java.io.IOException;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

public interface IMediaPlayer {

    public void init(Context context);

    /**
     * Sets the {@link SurfaceHolder} to use for displaying the video portion of the media.
     * 
     * Either a surface holder or surface must be set if a display or video sink is needed. Not calling this method or
     * {@link #setSurface(Surface)} when playing back a video will result in only the audio track being played. A null
     * surface holder or surface will result in only the audio track being played.
     * 
     * @param sh
     *            the SurfaceHolder to use for video display
     */
    public void setDisplay(SurfaceHolder sh);

    /**
     * Sets the audio stream type for this MediaPlayer. See {@link AudioManager} for a list of stream types. Must call
     * this method before prepare() or prepareAsync() in order for the target stream type to become effective
     * thereafter.
     * 
     * @param streamtype
     *            the audio stream type
     * @see android.media.AudioManager
     */
    public void setAudioStreamType(int streamType);

    /**
     * Control whether we should use the attached SurfaceHolder to keep the screen on while video playback is occurring.
     * This is the preferred method over {@link #setWakeMode} where possible, since it doesn't require that the
     * application have permission for low-level wake lock access.
     * 
     * @param screenOn
     *            Supply true to keep the screen on, false to allow it to turn off.
     */
    public void setScreenOnWhilePlaying(boolean screenOn);

    /**
     * Sets the data source as a content Uri.
     * 
     * @param context
     *            the Context to use when resolving the Uri
     * @param uri
     *            the Content URI of the data you want to play
     * @param headers
     *            the headers to be sent together with the request for the data
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public void setDataSource(Context context, Uri uri, Map<String, String> headers)
            throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    /**
     * Prepares the player for playback, synchronously.
     * 
     * After setting the datasource and the display surface, you need to either call prepare() or prepareAsync(). For
     * files, it is OK to call prepare(), which blocks until MediaPlayer is ready for playback.
     * 
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public void prepare() throws IOException, IllegalStateException;

    /**
     * Prepares the player for playback, asynchronously.
     * 
     * After setting the datasource and the display surface, you need to either call prepare() or prepareAsync(). For
     * streams, you should call prepareAsync(), which returns immediately, rather than blocking until enough data has
     * been buffered.
     * 
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public void prepareAsync() throws IllegalStateException;

    /**
     * Starts or resumes playback. If playback had previously been paused, playback will continue from where it was
     * paused. If playback had been stopped, or never started before, playback will start at the beginning.
     * 
     * @throws IllegalStateException
     *             if it is called in an invalid state
     */
    public void start() throws IllegalStateException;

    /**
     * Pauses playback. Call start() to resume.
     * 
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public void pause() throws IllegalStateException;

    /**
     * Stops playback after playback has been stopped or paused.
     * 
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized.
     */
    public void stop() throws IllegalStateException;

    /**
     * Resets the MediaPlayer to its uninitialized state. After calling this method, you will have to initialize it
     * again by setting the data source and calling prepare().
     */
    public void reset();

    /**
     * Releases resources associated with this MediaPlayer object. It is considered good practice to call this method
     * when you're done using the MediaPlayer. In particular, whenever an Activity of an application is paused (its
     * onPause() method is called), or stopped (its onStop() method is called), this method should be invoked to release
     * the MediaPlayer object, unless the application has a special need to keep the object around. In addition to
     * unnecessary resources (such as memory and instances of codecs) being held, failure to call this method
     * immediately if a MediaPlayer object is no longer needed may also lead to continuous battery consumption for
     * mobile devices, and playback failure for other applications if no multiple instances of the same codec are
     * supported on a device. Even if multiple instances of the same codec are supported, some performance degradation
     * may be expected when unnecessary multiple instances are used at the same time.
     */
    public void release();

    /**
     * Seeks to specified time position.
     * 
     * @param msec
     *            the offset in milliseconds from the start to seek to
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized
     */
    public void seekTo(int msec) throws IllegalStateException;

    /**
     * Gets the current playback position.
     * 
     * @return the current position in milliseconds
     */
    public int getCurrentPosition();

    /**
     * Gets the duration of the file.
     * 
     * @return the duration in milliseconds
     */
    public int getDuration();

    /**
     * Returns the width of the video.
     * 
     * @return the width of the video, or 0 if there is no video, no display surface was set, or the width has not been
     *         determined yet. The OnVideoSizeChangedListener can be registered via
     *         {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)} to provide a notification when the
     *         width is available.
     */
    public int getVideoWidth();

    /**
     * Returns the height of the video.
     * 
     * @return the height of the video, or 0 if there is no video, no display surface was set, or the height has not
     *         been determined yet. The OnVideoSizeChangedListener can be registered via
     *         {@link #setOnVideoSizeChangedListener(OnVideoSizeChangedListener)} to provide a notification when the
     *         height is available.
     */
    public int getVideoHeight();

    /**
     * Checks whether the MediaPlayer is playing.
     * 
     * @return true if currently playing, false otherwise
     * @throws IllegalStateException
     *             if the internal player engine has not been initialized or has been released.
     */
    public boolean isPlaying();

    /**
     * Interface definition for a callback to be invoked when the media source is ready for playback.
     */
    public interface OnPreparedListener {
        /**
         * Called when the media file is ready for playback.
         * 
         * @param mp
         *            the MediaPlayer that is ready for playback
         */
        void onPrepared(IMediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when the media source is ready for playback.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnPreparedListener(OnPreparedListener listener);

    /**
     * Interface definition for a callback to be invoked when playback of a media source has completed.
     */
    public interface OnCompletionListener {
        /**
         * Called when the end of a media source is reached during playback.
         * 
         * @param mp
         *            the MediaPlayer that reached the end of the file
         */
        void onCompletion(IMediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when the end of a media source has been reached during playback.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnCompletionListener(OnCompletionListener listener);

    /**
     * Interface definition of a callback to be invoked indicating buffering status of a media resource being streamed
     * over the network.
     */
    public interface OnBufferingUpdateListener {
        /**
         * Called to update status in buffering a media stream received through progressive HTTP download. The received
         * buffering percentage indicates how much of the content has been buffered or played. For example a buffering
         * update of 80 percent when half the content has already been played indicates that the next 30 percent of the
         * content to play has been buffered.
         * 
         * @param mp
         *            the MediaPlayer the update pertains to
         * @param percent
         *            the percentage (0-100) of the content that has been buffered or played thus far
         */
        void onBufferingUpdate(IMediaPlayer mp, int percent);
    }

    /**
     * Interface definition of a callback to be invoked indicating buffering status of playback over the network.
     */
    public interface OnPlaybackBufferingUpdateListener {
        /**
         * @param mp
         *            the MediaPlayer the update pertains to
         * @param percent
         *            the percentage (0-100) of playback
         */
        void onPlaybackBufferingUpdate(IMediaPlayer mp, int percent);
    }

    /**
     * Register a callback to be invoked when the status of a network stream's buffer has changed.
     * 
     * @param listener
     *            the callback that will be run.
     */
    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener);

    /**
     * Register a callback to be invoked when the status of a playback's buffer has changed.
     * 
     * @param listener
     *            the callback that will be run.
     */
    public void setOnPlaybackBufferingUpdateListener(OnPlaybackBufferingUpdateListener listener);

    /**
     * Interface definition of a callback to be invoked indicating the completion of a seek operation.
     */
    public interface OnSeekCompleteListener {
        /**
         * Called to indicate the completion of a seek operation.
         * 
         * @param mp
         *            the MediaPlayer that issued the seek operation
         */
        public void onSeekComplete(IMediaPlayer mp);
    }

    /**
     * Register a callback to be invoked when a seek operation has been completed.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener);

    /**
     * Interface definition of a callback to be invoked when the video size is first known or updated
     */
    public interface OnVideoSizeChangedListener {
        /**
         * Called to indicate the video size
         * 
         * The video size (width and height) could be 0 if there was no video, no display surface was set, or the value
         * was not determined yet.
         * 
         * @param mp
         *            the MediaPlayer associated with this callback
         * @param width
         *            the width of the video
         * @param height
         *            the height of the video
         */
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height);
    }

    /**
     * Register a callback to be invoked when the video size is known or updated.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

    /*
     * Do not change these values without updating their counterparts in include/media/mediaplayer.h!
     */
    /**
     * Unspecified media player error.
     * 
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_UNKNOWN = 1;

    /**
     * Media server died. In this case, the application must release the MediaPlayer object and instantiate a new one.
     * 
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_SERVER_DIED = 100;

    /**
     * The video is streamed and its container is not valid for progressive playback i.e the video's index (e.g moov
     * atom) is not at the start of the file.
     * 
     * @see android.media.MediaPlayer.OnErrorListener
     */
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;

    /** File or network related operation errors. */
    public static final int MEDIA_ERROR_IO = -1004;
    /** Bitstream is not conforming to the related coding standard or file spec. */
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    /**
     * Bitstream is conforming to the related coding standard or file spec, but the media framework does not support the
     * feature.
     */
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    /** Some operation takes too long to complete, usually more than 3-5 seconds. */
    public static final int MEDIA_ERROR_TIMED_OUT = -110;

    /**
     * Interface definition of a callback to be invoked when there has been an error during an asynchronous operation
     * (other errors will throw exceptions at method call time).
     */
    public interface OnErrorListener {
        /**
         * Called to indicate an error.
         * 
         * @param mp
         *            the MediaPlayer the error pertains to
         * @param what
         *            the type of error that has occurred:
         *            <ul>
         *            <li>{@link #MEDIA_ERROR_UNKNOWN}
         *            <li>{@link #MEDIA_ERROR_SERVER_DIED}
         *            </ul>
         * @param extra
         *            an extra code, specific to the error. Typically implementation dependent.
         *            <ul>
         *            <li>{@link #MEDIA_ERROR_IO}
         *            <li>{@link #MEDIA_ERROR_MALFORMED}
         *            <li>{@link #MEDIA_ERROR_UNSUPPORTED}
         *            <li>{@link #MEDIA_ERROR_TIMED_OUT}
         *            </ul>
         * @return True if the method handled the error, false if it didn't. Returning false, or not having an
         *         OnErrorListener at all, will cause the OnCompletionListener to be called.
         */
        boolean onError(IMediaPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an error has happened during an asynchronous operation.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnErrorListener(OnErrorListener listener);

    /*
     * Do not change these values without updating their counterparts in include/media/mediaplayer.h!
     */
    /**
     * Unspecified media player info.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_UNKNOWN = 1;

    /**
     * The player was started because it was used as the next player for another player, which just completed playback.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     * @hide
     */
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;

    /**
     * The player just pushed the very first video frame for rendering.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;

    /**
     * The video is too complex for the decoder: it can't decode frames fast enough. Possibly only the audio plays fine
     * at this stage.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

    /**
     * MediaPlayer is temporarily pausing playback internally in order to buffer more data.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_BUFFERING_START = 701;

    /**
     * MediaPlayer is resuming playback after filling buffers.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_BUFFERING_END = 702;

    /**
     * Bad interleaving means that a media has been improperly interleaved or not interleaved at all, e.g has all the
     * video samples first then all the audio ones. Video is playing but a lot of disk seeks may be happening.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;

    /**
     * The media cannot be seeked (e.g live stream)
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;

    /**
     * A new set of metadata is available.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     */
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;

    /**
     * Failed to handle timed text track properly.
     * 
     * @see android.media.MediaPlayer.OnInfoListener
     * 
     *      {@hide}
     */
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;

    public static final int MEDIA_INFO_VIDEO_START = 1000;

    public static final int MEDIA_INFO_VIDEO_END = 1001;

    public static final int MEDIA_INFO_VIDEO_PLAYING_START = 1002;

    /**
     * Interface definition of a callback to be invoked to communicate some info and/or warning about the media or its
     * playback.
     */
    public interface OnInfoListener {
        /**
         * Called to indicate an info or a warning.
         * 
         * @param mp
         *            the MediaPlayer the info pertains to.
         * @param what
         *            the type of info or warning.
         *            <ul>
         *            <li>{@link #MEDIA_INFO_UNKNOWN}
         *            <li>{@link #MEDIA_INFO_VIDEO_TRACK_LAGGING}
         *            <li>{@link #MEDIA_INFO_VIDEO_RENDERING_START}
         *            <li>{@link #MEDIA_INFO_BUFFERING_START}
         *            <li>{@link #MEDIA_INFO_BUFFERING_END}
         *            <li>{@link #MEDIA_INFO_BAD_INTERLEAVING}
         *            <li>{@link #MEDIA_INFO_NOT_SEEKABLE}
         *            <li>{@link #MEDIA_INFO_METADATA_UPDATE}
         *            </ul>
         * @param extra
         *            an extra code, specific to the info. Typically implementation dependent.
         * @return True if the method handled the info, false if it didn't. Returning false, or not having an
         *         OnErrorListener at all, will cause the info to be discarded.
         */
        boolean onInfo(IMediaPlayer mp, int what, int extra);
    }

    /**
     * Register a callback to be invoked when an info/warning is available.
     * 
     * @param listener
     *            the callback that will be run
     */
    public void setOnInfoListener(OnInfoListener listener);

}
