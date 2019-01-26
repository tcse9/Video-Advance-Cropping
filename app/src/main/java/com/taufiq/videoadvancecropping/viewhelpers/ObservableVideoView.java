package com.taufiq.videoadvancecropping.viewhelpers;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

import com.taufiq.videoadvancecropping.listeners.IVideoViewActionListener;


public class ObservableVideoView extends VideoView {


    private IVideoViewActionListener mVideoViewListener;
    private boolean mIsOnPauseMode = false;

    private int mVideoWidth;
    private int mVideoHeight;


    /**
     * Sets the listener
     * @param listener
     */
    public void setVideoViewListener(IVideoViewActionListener listener) {
        mVideoViewListener = listener;
    }

    /**
     * Overriding
     */
    @Override
    public void pause() {
        super.pause();

        if (mVideoViewListener != null) {
            mVideoViewListener.onPause();
        }

        mIsOnPauseMode = true;
    }

    /**
     * Overriding
     */
    @Override
    public void start() {
        super.start();

        if (mIsOnPauseMode) {
            if (mVideoViewListener != null) {
                mVideoViewListener.onResume();
            }

            mIsOnPauseMode = false;
        }
    }

    /**
     * Overriding
     * @param msec
     */
    @Override
    public void seekTo(int msec) {

        boolean ffwdrwd;
        if (super.getCurrentPosition() <= msec)
            ffwdrwd = false;
        else
            ffwdrwd = true;
        if (mVideoViewListener != null) {
            mVideoViewListener.onTimeBarSeekChanged(ffwdrwd);
        }
        super.seekTo(msec);
    }

    public ObservableVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableVideoView(Context context) {
        super(context);
    }

    public ObservableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("@@@", "onMeasure");
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            if (mVideoWidth * height > width * mVideoHeight) {
                // Log.i("@@@", "image too tall, correcting");
                height = width * mVideoHeight / mVideoWidth;
            } else if (mVideoWidth * height < width * mVideoHeight) {
                // Log.i("@@@", "image too wide, correcting");
                width = height * mVideoWidth / mVideoHeight;
            } else {
                // Log.i("@@@", "aspect ratio is correct: " +
                // width+"/"+height+"="+
                // mVideoWidth+"/"+mVideoHeight);
            }
        }
        // Log.i("@@@", "setting size: " + width + 'x' + height);
        setMeasuredDimension(width, height);
    }
}
