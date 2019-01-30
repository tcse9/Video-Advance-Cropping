package com.taufiq.videoadvancecropping.listeners;

public interface VideoTrimListener {
    void onStartTrim();
    void onFinishTrim(String url);
    void onCancel();
}
