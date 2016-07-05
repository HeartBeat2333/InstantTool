package com.heartbeat.instanttool;

public class FloatAnimation extends Animation {

    private final float mFrom;
    private final float mTo;
    private float mCurrent;

    public FloatAnimation(float from, float to, int duration) {
        mFrom = from;
        mTo = to;
        mCurrent = from;
        setDuration(duration);
    }

    @Override
    protected void onCalculate(float progress) {
        mCurrent = mFrom + (mTo - mFrom) * progress;
    }

    public float get() {
        return mCurrent;
    }
}
