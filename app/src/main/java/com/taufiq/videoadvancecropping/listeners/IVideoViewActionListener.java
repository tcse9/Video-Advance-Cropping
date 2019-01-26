package com.taufiq.videoadvancecropping.listeners;

public interface IVideoViewActionListener {

    void onPause();
    void onResume();
    void onTimeBarSeekChanged(boolean ffwdrwd);
}
