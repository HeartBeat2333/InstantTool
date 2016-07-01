package com.heartbeat.instanttool;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Created by zhouyuan on 2016/6/8.
 */
public class InstantButton {
    private Context mContext;

    protected final Rect mBounds = new Rect();
    protected final Rect mPaddings = new Rect();

    public Rect bounds() {
        return mBounds;
    }

    protected boolean setBounds(int left, int top, int right, int bottom) {
        boolean sizeChanged = (right - left) != (mBounds.right - mBounds.left)
                || (bottom - top) != (mBounds.bottom - mBounds.top);
        mBounds.set(left, top, right, bottom);
        return sizeChanged;
    }

    public InstantButton(Context context) {
        mContext = context;
    }

    public void layout(int left, int top, int right, int bottom) {
        setBounds(left, top, right, bottom);
    }

    protected void render(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        // 绘制按钮图片
//        if(isPressed && mPressedEnable) {
//            mIconPressedTexture.draw(canvas, x, y, w, h);
//        } else if(mIconTexture != null) {
//            mIconTexture.draw(canvas, x, y, w, h);
//        }
        canvas.restore();
    }

}
