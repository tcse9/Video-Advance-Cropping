package com.taufiq.videoadvancecropping;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.taufiq.videoadvancecropping.adapters.VideoTrimmerAdapter;
import com.taufiq.videoadvancecropping.binders.UiManager;
import com.taufiq.videoadvancecropping.databinding.ActivityMainBinding;
import com.taufiq.videoadvancecropping.listeners.IVideoViewActionListener;
import com.taufiq.videoadvancecropping.listeners.SingleCallback;
import com.taufiq.videoadvancecropping.utils.BackgroundExecutor;
import com.taufiq.videoadvancecropping.utils.DeviceUtil;
import com.taufiq.videoadvancecropping.utils.UiThreadExecutor;
import com.taufiq.videoadvancecropping.utils.VideoTrimmerUtil;
import com.taufiq.videoadvancecropping.viewhelpers.PaddingItemDecoration;

import static com.taufiq.videoadvancecropping.utils.VideoTrimmerUtil.VIDEO_FRAMES_WIDTH;

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
    private int mScaledTouchSlop;
    private boolean isOverScaledTouchSlop;
    private long scrollPos = 0;
    private float mAverageMsPx;
    private float averagePxMs;
    private long mLeftProgressPos;
    private long mRightProgressPos;
    private long mRedProgressBarPos = 0;
    private int mMaxWidth = VIDEO_FRAMES_WIDTH;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        uiManager = new UiManager();
        binding.setUimanager(uiManager);


        init();

    }


    private void init() {

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new VideoTrimmerAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.addOnScrollListener(mOnScrollListener);



        setPrepare(this, uiManager, binding, position);

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
            //达不到滑动的距离
            if (Math.abs(lastScrollX - scrollX) < mScaledTouchSlop) {
                isOverScaledTouchSlop = false;
                return;
            }
            isOverScaledTouchSlop = true;
            //初始状态,why ? 因为默认的时候有35dp的空白！
            if (scrollX == -VideoTrimmerUtil.RECYCLER_VIEW_PADDING) {
                scrollPos = 0;
            } else {
                isSeeking = true;
                scrollPos = (long) (mAverageMsPx * (VideoTrimmerUtil.RECYCLER_VIEW_PADDING + scrollX));
                //mLeftProgressPos = mRangeSeekBarView.getSelectedMinValue() + scrollPos;
                //mRightProgressPos = mRangeSeekBarView.getSelectedMaxValue() + scrollPos;
                //Log.d(TAG, "onScrolled >>>> mLeftProgressPos = " + mLeftProgressPos);
                //Log.d(TAG, "onScrolled >>>> mRightProgressPos = " + mRightProgressPos);
                mRedProgressBarPos = mLeftProgressPos;
                /*if (mVideoView.isPlaying()) {
                    mVideoView.pause();
                    setPlayPauseViewIcon(false);
                }*/
                //mRedProgressIcon.setVisibility(GONE);
                seekTo(mLeftProgressPos);
                //mRangeSeekBarView.setStartEndTime(mLeftProgressPos, mRightProgressPos);
                //mRangeSeekBarView.invalidate();
            }
            lastScrollX = scrollX;
        }
    };

    private int calcScrollXDistance() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
        int position = layoutManager.findFirstVisibleItemPosition();
        View firstVisibleChildView = layoutManager.findViewByPosition(position);
        int itemWidth = firstVisibleChildView.getWidth();
        return (position) * itemWidth - firstVisibleChildView.getLeft();
    }

    private void seekTo(long msec) {
        binding.videoView.seekTo((int) msec);
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

                mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                        /*mediaController = new MediaController(MainActivity.this, false);
                        binding.videoView.setMediaController(mediaController);
                        mediaController.setAnchorView(binding.videoView);*/
                    }
                });

                uiManager.setLoadingProgressBarVisibility(View.GONE);

                binding.videoView.seekTo(position);
                if (position == 0) {
                    binding.videoView.start();
                } else {
                    binding.videoView.pause();
                }

                initSlicer();

            }
        });

    }



    private void initSlicer(){

        if (mDuration <= VideoTrimmerUtil.MAX_SHOOT_DURATION) {
            mThumbsTotalCount = VideoTrimmerUtil.MAX_COUNT_RANGE;

        } else {
            mThumbsTotalCount = (int) (mDuration * 1.0f / (VideoTrimmerUtil.MAX_SHOOT_DURATION * 1.0f) * VideoTrimmerUtil.MAX_COUNT_RANGE);

        }


        mDuration = binding.videoView.getDuration();

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
}
