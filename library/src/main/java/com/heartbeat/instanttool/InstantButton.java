package com.heartbeat.instanttool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by zhouyuan on 2016/6/8.
 */
public abstract class InstantButton {
    protected final Rect mBounds = new Rect();
    protected final Rect mPaddings = new Rect();
    private Context mContext;
    private float mScale;
    private int mPadding;
    protected boolean isPressed;
    private Drawable mIconResource;

    protected boolean isTextVisible;
    private String mText;
    private Paint mTextPaint;
    private int mTextSize;

    private FloatAnimation mAnimation;

    private Object mTag;


    public Rect bounds() {
        return mBounds;
    }

    protected boolean setBounds(int left, int top, int right, int bottom) {
        boolean sizeChanged = (right - left) != (mBounds.right - mBounds.left)
                || (bottom - top) != (mBounds.bottom - mBounds.top);
        mBounds.set(left, top, right, bottom);
        return sizeChanged;
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPaddings.set(left, top, right, bottom);
        mPadding = left;
    }

    public InstantButton(Context context) {
        mContext = context;
        mTextPaint = new Paint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void setText(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public void setTextSize(int size) {
        mTextSize = size;
        mTextPaint.setTextSize(mTextSize);
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
    }

    public void setTextVisible(boolean visible) {
        isTextVisible = visible;
    }

    public void layout(int left, int top, int right, int bottom) {
        setBounds(left, top, right, bottom);
    }

    public int getWidth() {
        return mBounds.right - mBounds.left;
    }

    public int getHeight() {
        return mBounds.bottom - mBounds.top;
    }

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    protected void render(Canvas canvas, float progress) {
        FloatAnimation anim = mAnimation;
        boolean more = isMoreRender(anim);

        canvas.save(Canvas.MATRIX_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        // 按钮缩放
        canvas.translate(mBounds.centerX(), mBounds.centerY());
        canvas.scale(mScale, mScale);
        canvas.translate(-mBounds.centerX(), -mBounds.centerY());

        // 绘制按钮图片
        int l = mBounds.left + mPaddings.left;
        int t = mBounds.top + mPaddings.top;
        int r = mBounds.right - mPaddings.right;
        int b = mBounds.bottom - mPaddings.top;
        if(mIconResource == null) return;
        mIconResource.setBounds(l, t, r, b);
        mIconResource.setAlpha((int) (255 * progress));
        mIconResource.draw(canvas);

        drawText(canvas, anim);
        canvas.restore();

        if (more)
            invalidateButton();
    }

    /**
     * 判断是否有动画需要刷新
     * @param anim
     * @return
     */
    private boolean isMoreRender(FloatAnimation anim) {
        boolean more = false;
        long animTime = SystemClock.uptimeMillis();
        if (anim != null) {
            more |= anim.calculate(animTime);
            mScale = 1 + (float) mPadding * 2 / getWidth() * anim.get();
            if(!anim.isActive()) {
                mAnimation = null;
                more |= false;
            }
        }
        return more;
    }

    private void drawText(Canvas canvas, FloatAnimation anim) {
        // 绘制文字
        if (mText != null) {
            int textX = mBounds.left + (mBounds.width() - getTextWidth()) / 2;
            int textY = mBounds.top - mPadding;

            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            if(anim != null && isTextVisible) { // 文字缩放效果
                float scaleCenterX = textX + getTextWidth() / 2;
                float scaleCenterY = textY + getTextHeight() / 2;
                canvas.translate(scaleCenterX, scaleCenterY);
                canvas.scale(anim.get(), anim.get());
                canvas.translate(-scaleCenterX, -scaleCenterY);
                canvas.drawText(mText, textX, textY, mTextPaint);
            }
            canvas.restore();
        }
    }

    public void setScale(float scale) {
        mScale = scale;
    }

    public void setIconResource(Drawable iconResource) {
        mIconResource = iconResource;
    }

    public boolean isPressed() {
        return isPressed;
    }

    public void setPressed(boolean pressed) {
        isPressed = pressed;
        setTextVisible(pressed);
        startScaleAnimation(pressed);
    }

    /**
     *  显示按钮缩放动画
     */
    public void startScaleAnimation(boolean bigger) {
        if (mAnimation != null) {
            mAnimation.forceStop();
            mAnimation = null;
        }
        if(bigger) {
            mAnimation = new FloatAnimation(0.0f, 1.0f, 500);
        } else {
            mAnimation = new FloatAnimation(1.0f, 0.0f, 500);
        }
        mAnimation.setInterpolator(new DecelerateInterpolator(5));
        mAnimation.start();
        invalidateButton();
    }

    //  获取字体串宽度
    private int getTextWidth() {
        return mText == null ? 0 : (int) mTextPaint.measureText(mText);
    }


    //  获取字体高度
    private int getTextHeight() {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.top) + 2;
    }

    public abstract void invalidateButton();
}
