package com.taufiq.videoadvancecropping;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.taufiq.videoadvancecropping.adapters.VideoTrimmerAdapter;
import com.taufiq.videoadvancecropping.binders.UiManager;
import com.taufiq.videoadvancecropping.databinding.ActivityMainBinding;
import com.taufiq.videoadvancecropping.listeners.IVideoViewActionListener;
import com.taufiq.videoadvancecropping.listeners.SingleCallback;
import com.taufiq.videoadvancecropping.listeners.VideoTrimListener;
import com.taufiq.videoadvancecropping.utils.StorageUtil;
import com.taufiq.videoadvancecropping.utils.UiThreadExecutor;
import com.taufiq.videoadvancecropping.utils.VideoTrimmerUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.taufiq.videoadvancecropping.utils.VideoTrimmerUtil.MAX_SHOOT_DURATION;
import static com.taufiq.videoadvancecropping.utils.VideoTrimmerUtil.THUMB_WIDTH;


public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;
    private UiManager uiManager;
    private int position = 0;
    private VideoTrimmerAdapter adapter;
    private Uri mSourceUri;
    private int mDuration = 0;
    private int mThumbsTotalCount;
    private boolean isSeeking;
    private int lastScrollX;
    private LinearLayoutManager linearLayoutManager;
    private static final int REQ_CODE_PERMISSIONS = 110;
    private ProgressDialog mProgressDialog;
    private static final String COMPRESSED_VIDEO_FILE_NAME = "compress.mp4";
    private Timer timer;
    private String cutLeftPortion;
    private String cutRightPortion;
    private String trimPortion;
    private List<String> videoStripList;


    private int currentFrame = 0;
    private boolean isStartPressedOnce = false;
    private boolean isEndPressedOnce = false;
    private long videoCutStartTime = 0;
    private long videoCutEndTime = 0;
    private int startThumbPos = 0;
    private int endThumbPos = 0;
    private boolean isTreamStateOn = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        uiManager = new UiManager();
        binding.setUimanager(uiManager);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQ_CODE_PERMISSIONS);


        //init();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_CODE_PERMISSIONS: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    init();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on thissetpre permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void init() {


        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new VideoTrimmerAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addOnScrollListener(mOnScrollListener);

        setPrepare(this, uiManager, binding, position);

        binding.imgViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        topLayoutButtonColorManagement();

        videoStripList = new ArrayList<>();
    }


    private void topLayoutButtonColorManagement() {

        binding.btnTrim.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
        binding.btnTrim.setBackgroundColor(ContextCompat.getColor(this, R.color.buttonTextToggleColor));

        binding.btnCut.setTextColor(ContextCompat.getColor(this, R.color.buttonTextToggleColor));
        binding.btnCut.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack));

        binding.btnTrim.setTag("TREAM_CLICKED");

        binding.btnTrim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.btnTrim.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorWhite));
                binding.btnTrim.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.buttonTextToggleColor));

                binding.btnCut.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.buttonTextToggleColor));
                binding.btnCut.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorBlack));

                binding.btnTrim.setTag("TREAM_CLICKED");
            }
        });

        binding.btnCut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.btnCut.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorWhite));
                binding.btnCut.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.buttonTextToggleColor));

                binding.btnTrim.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.buttonTextToggleColor));
                binding.btnTrim.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.colorBlack));

                binding.btnTrim.setTag("CUT_CLICKED");
            }
        });

    }

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            isSeeking = false;
            int scrollX = calcScrollXDistance();

            lastScrollX = scrollX;

           /* Log.e("SCRL_X", "is: "+lastScrollX);
            Log.e("DURATION", "is: "+mDuration);
            Log.e("", ""+MAX_SHOOT_DURATION);
            Log.e("SEEK_POS_CUR", "is: "+calcRelativeSeekPosition());
            Log.e("FRAME_RATE", "is: "+lastScrollX/THUMB_WIDTH);
            Log.e("DURATION", "----------------------------------------");*/

            currentFrame = lastScrollX / THUMB_WIDTH;

            seekTo(calcRelativeSeekPosition());

        }
    };

    private int calcScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }


    private long calcRelativeSeekPosition() {
        return (long) (currentFrame) * 1000L;
    }

    private void seekTo(long msec) {
        binding.videoView.seekTo((int) msec);
    }

    private Bitmap converBarIcon(int iconReource) {
        return BitmapFactory.decodeResource(getResources(), iconReource);
    }


    private void startShootVideoThumbs(final Context context, final Uri videoUri, int totalThumbsCount, long startPosition, long endPosition) {
        VideoTrimmerUtil.shootVideoThumbInBackground(context, videoUri, totalThumbsCount, startPosition, endPosition,
                new SingleCallback<Bitmap, Integer>() {
                    @Override
                    public void onSingleCallback(final Bitmap bitmap, final Integer interval) {
                        if (bitmap != null) {
                            UiThreadExecutor.runTask("", new Runnable() {
                                @Override
                                public void run() {
                                    adapter.addBitmaps(bitmap);
                                }
                            }, 0L);
                        }
                    }
                });


        binding.btnStartLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isStartPressedOnce = !isStartPressedOnce;
                if (isStartPressedOnce) {
                    binding.btnStartLeft.setImageResource(R.drawable.start_here_tap);
                    startThumbPos = currentFrame + 6;
                    adapter.addBar(currentFrame + 6, converBarIcon(R.drawable.lower_image_bar_right));

                    videoCutStartTime = currentFrame;
                } else {
                    binding.btnStartLeft.setImageResource(R.drawable.start_here_normal);
                    adapter.removeBar(startThumbPos);
                    //adapter.addBar(currentFrame + 6, converBarIcon(R.drawable.lower_image_bar_right));
                }


                Log.e("FRAME_RATE_START", "is: " + currentFrame);
            }
        });

        binding.btnEndRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEndPressedOnce = !isEndPressedOnce;
                if (isEndPressedOnce) {
                    binding.btnEndRight.setImageResource(R.drawable.end_here_tap);
                    endThumbPos = currentFrame + 7;
                    adapter.addBar(currentFrame + 7, converBarIcon(R.drawable.lower_image_bar_left));

                    videoCutEndTime = currentFrame;

                } else {
                    binding.btnEndRight.setImageResource(R.drawable.end_here_normal);
                    adapter.removeBar(endThumbPos);
                    //adapter.addBar(currentFrame + 7, converBarIcon(R.drawable.lower_image_bar_left));
                }


                Log.e("FRAME_RATE_END", "is: " + currentFrame);
            }
        });


        final InputStream ins = getResources().openRawResource(R.raw.bunny);


        binding.imgViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (binding.btnTrim.getTag().equals("TREAM_CLICKED")) {
                    trimVideo();
                } else {
                    cutVideo();
                }
            }
        });


    }

    private void trimVideo() {

        final InputStream ins = getResources().openRawResource(R.raw.bunny);
        VideoTrimmerUtil.trim(MainActivity.this,
                createFileFromInputStream(ins).getAbsolutePath(),
                getOutputFilePath(),
                videoCutStartTime * 1000L,
                videoCutEndTime * 1000L,
                new VideoTrimListener() {
                    @Override
                    public void onStartTrim() {
                        buildDialog("Compressing...").show();
                    }

                    @Override
                    public void onFinishTrim(String url) {
                        mProgressDialog.dismiss();
                        Toast.makeText(MainActivity.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
    }

    private void cutVideo() {
        final InputStream ins = getResources().openRawResource(R.raw.bunny);
        videoStripList.clear();

        if (videoCutStartTime > 0 && (videoCutEndTime > 0 && videoCutEndTime < mDuration)) {
            VideoTrimmerUtil.trim(MainActivity.this,
                    createFileFromInputStream(ins).getAbsolutePath(),
                    getOutputFilePath(),
                    0,
                    videoCutStartTime * 1000L,
                    new VideoTrimListener() {
                        @Override
                        public void onStartTrim() {
                            buildDialog("Compressing...").show();
                        }

                        @Override
                        public void onFinishTrim(String url) {

                            trimPortion = url;
                            videoStripList.add(url);
                            cutPortionMaintenance(trimPortion);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });

        } else if (videoCutStartTime == 0 && videoCutEndTime > 0) {

            VideoTrimmerUtil.trim(MainActivity.this,
                    createFileFromInputStream(ins).getAbsolutePath(),
                    getOutputFilePath(),
                    0,
                    videoCutEndTime * 1000L,
                    new VideoTrimListener() {
                        @Override
                        public void onStartTrim() {
                            buildDialog("Compressing...").show();
                        }

                        @Override
                        public void onFinishTrim(String url) {
                            mProgressDialog.dismiss();
                            Toast.makeText(MainActivity.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });

        }else if(videoCutStartTime > 0 && videoCutEndTime*1000L >= mDuration){
            VideoTrimmerUtil.trim(MainActivity.this,
                    createFileFromInputStream(ins).getAbsolutePath(),
                    getOutputFilePath(),
                    videoCutStartTime * 1000L,
                    mDuration,
                    new VideoTrimListener() {
                        @Override
                        public void onStartTrim() {
                            buildDialog("Compressing...").show();
                        }

                        @Override
                        public void onFinishTrim(String url) {
                            mProgressDialog.dismiss();
                            Toast.makeText(MainActivity.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
        }


    }


    private void cutPortionMaintenance(final String url) {
        final InputStream ins = getResources().openRawResource(R.raw.bunny);


        VideoTrimmerUtil.trim(MainActivity.this,
                createFileFromInputStream(ins).getAbsolutePath(),
                getOutputFilePath(),
                videoCutEndTime * 1000,
                mDuration,
                new VideoTrimListener() {
                    @Override
                    public void onStartTrim() {
                    }

                    @Override
                    public void onFinishTrim(String url) {

                        trimPortion = url;
                        videoStripList.add(url);
                        merge();


                    }

                    @Override
                    public void onCancel() {

                    }
                });


    }


    private void merge() {
        final InputStream ins = getResources().openRawResource(R.raw.bunny);
        VideoTrimmerUtil.cut(MainActivity.this, getOutputFilePath(), videoStripList, new VideoTrimListener() {
            @Override
            public void onStartTrim() {

            }

            @Override
            public void onFinishTrim(String url) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.save_success), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }
        });
    }


    private String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    private String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }


    private File createFileFromInputStream(InputStream inputStream) {

        try {
            File f = new File(StorageUtil.getCacheDir() + File.separator + COMPRESSED_VIDEO_FILE_NAME);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            //Logging exception
        }

        return null;
    }


    private String getOutputFilePath() {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "advance_vid_cutter");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }

        return folder.getAbsolutePath();
    }


    /**
     * Setup the whole {@link android.widget.VideoView} preparation and loading process
     *
     * @param context
     * @param uiManager
     * @param binding
     * @param position
     * @return
     */
    public void setPrepare(Context context, final UiManager uiManager, final ActivityMainBinding binding, final int position) {
        try {
            //binding.videoView.setMediaController(mediaController);
            mSourceUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.bunny);
            binding.videoView.setVideoPath("android.resource://" + context.getPackageName() + "/" + R.raw.bunny);

            /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 350);

            binding.videoView.setVideoSize(params.width, params.height);*/
            binding.videoView.requestFocus();
            binding.videoView.start();
            binding.videoView.setVideoViewListener(mVideoViewListener);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        uiManager.setLoadingProgressBarVisibility(View.VISIBLE);

        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(final MediaPlayer mediaPlayer) {

                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                mediaPlayer.setLooping(true);

                uiManager.setLoadingProgressBarVisibility(View.GONE);

                binding.videoView.seekTo(position);
                if (position == 0) {
                    binding.videoView.start();
                } else {
                    binding.videoView.pause();
                }


                showTimer(mediaPlayer);
                binding.txtEndTime.setText(getDurationString(mediaPlayer.getDuration() / 1000));
                initSlicer();

            }
        });

    }


    private void showTimer(final MediaPlayer mp) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (timer != null)
                            try {
                                binding.txtRunningTime.setText(getDurationString(mp.getCurrentPosition() / 1000));
                            } catch (Exception e) {
                                timer.cancel();
                                timer = null;
                            }
                    }
                });
            }
        }, 0, 1000);
    }


    private void initSlicer() {

        mDuration = binding.videoView.getDuration();


        if (mDuration <= MAX_SHOOT_DURATION) {
            mThumbsTotalCount = VideoTrimmerUtil.MAX_COUNT_RANGE;

        } else {
            mThumbsTotalCount = (int) (mDuration * 1.0f / (MAX_SHOOT_DURATION * 1.0f) * VideoTrimmerUtil.MAX_COUNT_RANGE);

        }

        startShootVideoThumbs(this, mSourceUri, mThumbsTotalCount, 0, mDuration);
    }

    /**
     * Assign a {@link IVideoViewActionListener}
     */

    public IVideoViewActionListener mVideoViewListener = new IVideoViewActionListener() {
        @Override
        public void onTimeBarSeekChanged(boolean ffwdrwd) {


        }

        @Override
        public void onResume() {
            Log.e("*** PLAY", "playing");

        }

        @Override
        public void onPause() {
            Log.e("*** PAUSE", "paused");

        }

    };

    private ProgressDialog buildDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(this, "", msg);
        }
        mProgressDialog.setMessage(msg);
        return mProgressDialog;
    }


    @Override
    protected void onResume() {
        binding.videoView.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        binding.videoView.suspend();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.videoView.stopPlayback();
        super.onDestroy();
    }


    public boolean isTreamStateOn() {
        return isTreamStateOn;
    }
}
