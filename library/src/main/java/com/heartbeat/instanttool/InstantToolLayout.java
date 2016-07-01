package com.heartbeat.instanttool;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by zhouyuan on 2016/6/8.
 */
public class InstantToolLayout extends LinearLayout {
    private static final String TAG = "InstantToolLayout";
    private static final int MAX_BUTTON_NUM = 5; // 最大按钮数量

    private Context mContext;
    private boolean isButtonsShown;

    private int mButtonRadius; // 按钮到点击中心的半径
    private int mCurrentBtnNum; // 当前按钮数量
    private int mAngleGap;  // 每个按钮间隔的角度

    private int mBackgrounColor;
    private int mButtonDpSize;
//    private int mButtonPadding;

    private float mAlpha; // 当前透明度
    private Rect mLimitRect;
    private Point mCenterPoint;
    protected InstantToolButtonListener mButtonListener;
    private ArrayList<Box> mBoxPool = new ArrayList<>();

    private Drawable mCenterCircle;
    private Paint mPaint;
    private Paint mBackgroundPaint;
    private GestureDetector mGestureDetector;

    private class Box { // button容器
        public InstantButton button;
        public Rect target;
        public Point targetPoint;

        public Box() {
            button = new InstantButton(mContext);
//            button.setBackgroundColor(Color.BLUE);
//            button.setText("111");
//            button.setTextPane(mTextPanel);
            target = new Rect();
            targetPoint = new Point();
//
//            if(mButtonListener != null)
//                mButtonListener.onInitButton(button.getId(), button);
        }
    }

    public interface InstantToolButtonListener {
        void onInitButton(Button button);
        void onItemButtonChecked(Button button, int slotIndex);
    }

    public InstantToolLayout(Context context) {
        this(context, null);
    }

    public InstantToolLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InstantToolLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InstantToolLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mGestureDetector = new GestureDetector(mContext, myGestureListener);

        mPaint = new Paint();
        mPaint.setColor(Color.RED);// 设置红色

        mBackgrounColor = context.getResources().getColor(R.color.instanttool_bg);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgrounColor);

        mAlpha = 0.0f;
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.InstantToolLayout);

        mAngleGap = 180 / (MAX_BUTTON_NUM - 1);
        mCenterCircle = typedArray.getDrawable(R.styleable.InstantToolLayout_centerIcon);
        mCurrentBtnNum = typedArray.getInteger(R.styleable.InstantToolLayout_buttonNum, 0);
        mButtonRadius = typedArray.getDimensionPixelSize(R.styleable.InstantToolLayout_radius, 0);
        mButtonDpSize = typedArray.getDimensionPixelSize(R.styleable.InstantToolLayout_buttonSize, 0);
//        mButtonPadding = context.getResources().getDimensionPixelSize(R.dimen.intantTool_button_padding);
//        mTextPanel = new NinePatchTexture(mContext, R.drawable.panel_undo_holo);;
        mCenterPoint = new Point();
        initBoxs();
        setWillNotDraw(false);

    }

    private void initBoxs() {
        for(int i = 0; i < mCurrentBtnNum; i++) {
            Box box = new Box();
            mBoxPool.add(box);
        }
    }

    private void showButtons(MotionEvent e) {
        if(!isButtonsShown) {
            setCenterPointAndInitBox((int) e.getX(), (int) e.getY());
            isButtonsShown = true;
            invalidate();
        }
    }

    private void hideButtons() {
        if(isButtonsShown) {
            isButtonsShown = false;
            invalidate();
        }
    }

    /**
     * 设置按键选择监听
     * @param listener
     */
    public void setButtonListener(InstantToolButtonListener listener) {
        mButtonListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initLimitRect(left, top, right, bottom);
    }

    /**
     * 初始化中心点显示区域
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void initLimitRect(int left, int top, int right, int bottom) {
        int width = mCenterCircle != null ? mCenterCircle.getIntrinsicWidth() / 2 : 0;
        int height = mCenterCircle != null ? mCenterCircle.getIntrinsicHeight() / 2 : 0;
        mLimitRect = new Rect(left + width, top + height, right - width, bottom -  height);
    }

    public void setCenterPointAndInitBox(int x, int y) {
        mCenterPoint = getCenterPointByRectLimit(mCenterPoint, x, y);
        initBoxesTargetRect();
    }

    /**
     * 从限制区域内获取中心点
     * @param x
     * @param y
     */
    private Point getCenterPointByRectLimit(Point point, int x, int y) {
        x = Utils.clamp(x, mLimitRect.left, mLimitRect.right);
        y = Utils.clamp(y, mLimitRect.top, mLimitRect.bottom);
        point.set(x, y);
//		Log.i(TAG, "getCenterPointByRectLimit " + point.toString());
        return point;
    }

    private void initBoxesTargetRect() {
        int startAngle = getStartAngle();
        int i = 0;
        for(Box box : mBoxPool) {
            // TODO 初始化按钮状态
//            box.button.setScale(1.0f);
//            box.button.isPressed = false;
//            box.button.setTextVisible(false);
            // 设置目标矩形
            initBoxTargetRect(box, startAngle, i);
            i++;
        }
    }

    /**
     * 获得起始Button角度
     * @return 弹出BUTTON的起始角度
     */
    private int getStartAngle() {
        int width = getWidth();
        int height = getHeight();
        if(width == 0 || height == 0 || mCurrentBtnNum == 0) return 0;
        // 当点击位置在中心时的起始角度
        int startAngle = (180 - (mCurrentBtnNum - 1) * mAngleGap) / 2;
        // 在屏幕上X轴移动时角度范围
        int angleRange = (mCurrentBtnNum - 1) * mAngleGap / 2;
        // 根据点击的X轴位置获取移动的角度
        float offsetAngle = angleRange - (mCenterPoint.x - mLimitRect.left) * ((float) angleRange / (width / 2 - mLimitRect.left)) ;
        // 获取在X轴上最后的起始角度
        startAngle += offsetAngle;
        // 起始角度
        return startAngle;
    }

    /**
     * 获得每个按钮的目标位置
     * @param box 需要定位的BOX
     * @param startAngle 第一个按钮起始角度
     */
    private void initBoxTargetRect(Box box, int startAngle, int btnPos) {
        // 当前box的角度
        int currentAngle = startAngle + mAngleGap * btnPos;
        // 根据角和半径计算出box中心点
        int x = mCenterPoint.x - (int)(mButtonRadius * Math.cos(Math.toRadians(currentAngle)));
        int y = mCenterPoint.y - (int)(mButtonRadius * Math.sin(Math.toRadians(currentAngle)));
        int boxHalfWidth = mButtonDpSize / 2;
        int boxHalfHeight = mButtonDpSize / 2;
        box.target.set(x - boxHalfWidth, y - boxHalfHeight, x + boxHalfWidth, y + boxHalfHeight);
        box.targetPoint.set(x, y);
    }

    public int size() {
        return mBoxPool.size();
    }

    // ---------------------------- Draw --------------------------------------------

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if(isButtonsShown) {
            renderBackground(canvas);
            canvas.save(Canvas.MATRIX_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
            drawCenterCircle(canvas);
            if(mBoxPool.size() > 0) {
                for (Box box : mBoxPool) {
//                    canvas.drawCircle(box.target.centerX(), box.target.centerY(), box.target.width() / 2, mPaint);// 小圆
                }
            }
            canvas.restore();
        }
    }

    /**
     * 绘制背景遮罩
     * @param canvas
     */
    protected void renderBackground(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
//        mAlpha = Utils.clamp(mProgress, 0.0f, 0.5f);
        mBackgroundPaint .setAlpha(127);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mBackgroundPaint);
        canvas.restore();
    }

    /**
     * 绘制中心圆
     * @param canvas
     */
    private void drawCenterCircle(Canvas canvas) {
        int x , y , w, h;
        x = y = 0;
        w = mCenterCircle.getIntrinsicWidth();
        h = mCenterCircle.getIntrinsicHeight();
        canvas.save(Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
//        if(mAnimation != null && mAnimation.getType() == ShowAnimation.SHOWING) {
//            // 显示动画放大后缩小
//            w = (int) (mCenterCircle.getWidth() + (1 - mProgress) * mCenterCircle.getWidth());
//            h = (int) (mCenterCircle.getHeight() + (1 - mProgress) * mCenterCircle.getWidth());
//        }
        x = mCenterPoint.x - w / 2;
        y = mCenterPoint.y - h / 2;
//        mCenterCircle.draw(canvas, x, y, w, h);
        mCenterCircle.setBounds(x, y,  x + w, y + h);
        mCenterCircle.draw(canvas);
        canvas.restore();
    }

    // ---------------------------- Gesture --------------------------------------------

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
            showButtons(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
//                for (Box box : mBoxPool) {
//                    if(box.target.contains((int) event.getX(), (int) event.getY())) {
//                        if(!box.button.isPressed()) {
//                            box.button.setPressed(true);
//                            mCurrentButton = box.button;
//                        }
//                    } else {
//                        if(box.button.isPressed()) {
//                            box.button.setPressed(false);
//                            if(mCurrentButton == box.button) {
//                                mCurrentButton = null;
//                            }
//                        }
//                    }
//                }
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
                // 抬起时隐藏
                if(isButtonsShown) {
                    hideButtons();
                }
//                startHideAnimation();
//                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_BUTTON), 500);
                break;
        }
        return isButtonsShown ? true : super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
//                for (Box box : mBoxPool) {
//                    if(box.target.contains((int) event.getX(), (int) event.getY())) {
//                        if(!box.button.isPressed()) {
//                            box.button.setPressed(true);
//                            mCurrentButton = box.button;
//                        }
//                    } else {
//                        if(box.button.isPressed()) {
//                            box.button.setPressed(false);
//                            if(mCurrentButton == box.button) {
//                                mCurrentButton = null;
//                            }
//                        }
//                    }
//                }
//                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
                // 抬起时隐藏
                if(isButtonsShown) {
                    hideButtons();
                }
//                startHideAnimation();
//                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_BUTTON), 500);
                break;
        }
        return super.onTouchEvent(event);
    }
}
