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


}
