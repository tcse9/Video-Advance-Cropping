package com.taufiq.videoadvancecropping.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taufiq.videoadvancecropping.R;
import com.taufiq.videoadvancecropping.utils.VideoTrimmerUtil;

import java.util.ArrayList;
import java.util.List;

public class VideoTrimmerAdapter extends RecyclerView.Adapter {
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;
    private boolean isRightBarClicked = false;
    private boolean isLeftBarClicked = false;

    public VideoTrimmerAdapter(Context context) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TrimmerViewHolder(mInflater.inflate(R.layout.row_video_thumb, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ((TrimmerViewHolder) holder).thumbImageView.setImageBitmap(mBitmaps.get(position));
       /* if(isRightBarClicked){
            ((TrimmerViewHolder) holder).imageBarRight.setVisibility(View.VISIBLE);
        }

        if(isLeftBarClicked){
            ((TrimmerViewHolder) holder).imageBarRight.setVisibility(View.VISIBLE);
        }*/

    }


    public void setStartRightBarClicked(boolean isRightBarClicked){
        this.isRightBarClicked = isRightBarClicked;
        this.notifyDataSetChanged();
    }

    public void setStartLeftBarClicked(boolean isLeftBarClicked){
        this.isLeftBarClicked = isLeftBarClicked;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mBitmaps.size();
    }

    public void addBitmaps(Bitmap bitmap) {
        mBitmaps.add(bitmap);
        notifyDataSetChanged();
    }

    private final class TrimmerViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbImageView, imageBarRight, imageBarLeft;

        TrimmerViewHolder(View itemView) {
            super(itemView);
            thumbImageView = itemView.findViewById(R.id.thumb);
            imageBarRight = itemView.findViewById(R.id.imageBarRight);
            imageBarLeft = itemView.findViewById(R.id.imageBarLeft);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) thumbImageView.getLayoutParams();
            layoutParams.width = VideoTrimmerUtil.VIDEO_FRAMES_WIDTH / VideoTrimmerUtil.MAX_COUNT_RANGE;
            thumbImageView.setLayoutParams(layoutParams);
        }
    }
}
