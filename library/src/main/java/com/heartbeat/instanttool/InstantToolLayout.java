package com.heartbeat.instanttool;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by zhouyuan on 2016/6/8.
 */
public class InstantToolLayout extends ViewGroup {
    public InstantToolLayout(Context context) {
        super(context);
    }

    public InstantToolLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InstantToolLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InstantToolLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

}
