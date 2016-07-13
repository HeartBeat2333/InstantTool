package com.heartbeat.instanttool.sample;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

    public ImageAdapter(Context context) {
        mContext = context;

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
}
