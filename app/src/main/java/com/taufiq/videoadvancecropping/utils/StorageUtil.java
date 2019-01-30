package com.taufiq.videoadvancecropping.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.taufiq.videoadvancecropping.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@SuppressWarnings({"ResultOfMethodCallIgnored", "FieldCanBeLocal"})
public class StorageUtil {

    private static final String TAG = "StorageUtil";
    private static String APP_DATA_PATH = "/Android/data/" + BuildConfig.APPLICATION_ID;
    private static String sDataDir;
    private static String sCacheDir;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getAppDataDir() {
        if (TextUtils.isEmpty(sDataDir)) {
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    sDataDir = Environment.getExternalStorageDirectory().getPath() + APP_DATA_PATH;
                    if (TextUtils.isEmpty(sDataDir)) {
                        sDataDir = BaseUtils.getContext().getFilesDir().getAbsolutePath();
                    }
                } else {
                    sDataDir = BaseUtils.getContext().getFilesDir().getAbsolutePath();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                sDataDir = BaseUtils.getContext().getFilesDir().getAbsolutePath();
            }
            File file = new File(sDataDir);
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        return sDataDir;
    }

    public static String getCacheDir() {
        if (TextUtils.isEmpty(sCacheDir)) {
            File file = null;
            Context context = BaseUtils.getContext();
            try {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    file = context.getExternalCacheDir();
                    if (file == null || !file.exists()) {
                        file = getExternalCacheDirManual(context);
                    }
                }
                if (file == null) {
                    file = context.getCacheDir();
                    if (file == null || !file.exists()) {
                        file = getCacheDirManual(context);
                    }
                }
                Log.w(TAG, "cache dir = " + file.getAbsolutePath());
                sCacheDir = file.getAbsolutePath();
            } catch (Throwable ignored) {
            }
        }
        return sCacheDir;
    }

    private static File getExternalCacheDirManual(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {//
                Log.w(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.i(TAG, "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    @SuppressLint("SdCardPath")
    private static File getCacheDirManual(Context context) {
        String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache";
        return new File(cacheDirPath);
    }

    public static boolean delFiles(String path) {
        File cacheFile = new File(path);
        if (!cacheFile.exists()) {
            return false;
        }
        File[] files = cacheFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].exists() && files[i].isFile()) {
                files[i].delete();
            } else if (files[i].exists() && files[i].isDirectory()) {
                delFiles(files[i].getAbsolutePath());
                files[i].delete();
            }
        }

        return true;
    }

    public static long sizeOfDirectory(File dir) {
        if (dir.exists()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                // Recursive call if it's a directory
                if (fileList[i].isDirectory()) {
                    result += sizeOfDirectory(fileList[i]);
                } else {
                    // Sum the file size in bytes
                    result += fileList[i].length();
                }
            }
            return result; // return the file size
        }
        return 0;
    }


    public static String getFileSize(long length) {
        int MB = 1024 * 1024;
        if (length < MB) {
            double resultKB = length * 1.0 / 1024;
            return String.format(Locale.getDefault(), "%.1f", resultKB) + "Kb";
        }
        double resultMB = length * 1.0 / MB;
        return String.format(Locale.getDefault(), "%.1f", resultMB) + "Mb";
    }

    public static boolean isFileExist(String path) {
        if (TextUtils.isEmpty(path)) return false;
        File file = new File(path);
        return file.exists();
    }


    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) return true;
        return deleteFile(new File(path));
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) return true;

        if (file.isFile()) {
            return file.delete();
        }

        if (!file.isDirectory()) {
            return false;
        }

        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f);
            }
        }
        return file.delete();
    }
}
