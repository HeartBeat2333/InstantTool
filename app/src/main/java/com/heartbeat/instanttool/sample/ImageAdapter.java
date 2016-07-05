package com.heartbeat.instanttool.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * 图片适配器
 *
 * Created by zhouyuan on 2016/6/8.
 */
public class ImageAdapter extends RecyclerView.Adapter {

    private List<Integer> mItems;
    private Context mContext;
    OnItemLongSelectedListener mLongListner;
    private GestureDetector mGestureDetector;
    private int curerntId;

    public ImageAdapter(Context context) {
        mContext = context;
        mGestureDetector = new GestureDetector(mContext, myGestureListener);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.list_item, parent, false);
        return new ImageHolder(inflate);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final int rid = mItems.get(position);
        ImageHolder imageHolder = ((ImageHolder)holder);
        imageHolder.ivImage.setImageDrawable(mContext.getResources().getDrawable(rid));

        imageHolder.ivImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                curerntId = rid;
                mGestureDetector.onTouchEvent(event);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(List<Integer> items) {
        mItems = items;
    }

    static class ImageHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;

        public ImageHolder(View itemView) {
            super(itemView);
            ivImage = (ImageView) itemView.findViewById(R.id.iv_image);
        }
    }

    public void setLongListner(OnItemLongSelectedListener longListner) {
        this.mLongListner = longListner;
    }

    public interface OnItemLongSelectedListener {
        void onItemLongSelected(MotionEvent e, int rid);
    }

    GestureDetector.OnGestureListener myGestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mLongListner != null) {
                mLongListner.onItemLongSelected(e, curerntId);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };
}
