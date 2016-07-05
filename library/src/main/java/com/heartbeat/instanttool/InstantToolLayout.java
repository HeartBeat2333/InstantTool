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
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Created by zhouyuan on 2016/6/8.
 */
public class InstantToolLayout extends LinearLayout {
    private static final String TAG = "InstantToolLayout";
    private static final int MSG_PICK_BUTTON = 10000;
    private static final int MAX_BUTTON_NUM = 5; // 最大按钮数量
    private static final int MIN_BUTTON_NUM = 0; // 最小按钮数量

    private Context mContext;
    private boolean isButtonsShown;

    private int mButtonRadius; // 按钮到点击中心的半径
    private int mCurrentBtnNum; // 当前按钮数量
    private int mAngleGap;  // 每个按钮间隔的角度

    private int mBackgrounColor;
    private int mButtonDpSize;
    private int mButtonPadding;
    private int mTextSize;
    private int mTextColor;

    private float mAlpha; // 当前透明度
    private Rect mLimitRect;
    private Point mCenterPoint;
    protected InstantToolButtonListener mButtonListener;
    private ArrayList<Box> mBoxPool = new ArrayList<>();

    private Drawable mCenterCircle;
    private Paint mPaint;
    private Paint mBackgroundPaint;

    private float mProgress;
    private ShowAnimation mAnimation;

    private boolean isHandle;
    private Box mCurrentBox;
    private Handler mHandler;

    private class Box { // button容器
        public InstantButton button;
        public Rect target;
        public Point targetPoint;
        public int slotIndex;

        public Box(int index) {
            slotIndex = index;

            button = new InstantButton(mContext) {
                @Override
                public void invalidateButton() {
                    invalidate();
                }
            };

            if(mButtonPadding > 0) {
                button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
            }
            button.setTextSize(mTextSize);
            button.setTextColor(mTextColor);
            target = new Rect();
            targetPoint = new Point();
        }
    }

    public interface InstantToolButtonListener {
        void onShowButton(InstantButton button, int slotIndex);
        void onItemButtonChecked(InstantButton button, int slotIndex);
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
        mButtonPadding = typedArray.getDimensionPixelSize(R.styleable.InstantToolLayout_buttonPadding, 0);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.InstantToolLayout_textSize, 0);
        mTextColor = typedArray.getColor(R.styleable.InstantToolLayout_textColor, 0);

        mCurrentBtnNum = Utils.clamp(mCurrentBtnNum, MIN_BUTTON_NUM, MAX_BUTTON_NUM);

        mCenterPoint = new Point();
        initBox();
        setWillNotDraw(false);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_PICK_BUTTON:
                        if(mButtonListener != null && mCurrentBox != null) {
                            Log.i(TAG, "MSG_PICK_BUTTON");
                            mButtonListener.onItemButtonChecked(mCurrentBox.button, mCurrentBox.slotIndex);
                            mCurrentBox = null;
                        }
                        break;
                    default :
                        throw new AssertionError(msg.what);
                }
            }
        };
    }

    private void initBox() {
        for(int i = 0; i < mCurrentBtnNum; i++) {
            Box box = new Box(i);
            mBoxPool.add(box);
        }
    }

    public void showButtons(MotionEvent e) {
        isHandle = true;
        if(!isButtonsShown) {
            int[] locationOnScreen = new int[2];
            getLocationOnScreen(locationOnScreen);
            setCenterPointAndInitBox((int) e.getRawX() - locationOnScreen[0], (int) e.getRawY() - locationOnScreen[1]);
            if(mButtonListener != null) {
                for(Box box : mBoxPool) {
                    box.button.layout(box.target.left, box.target.top, box.target.right, box.target.bottom);
                    mButtonListener.onShowButton(box.button, box.slotIndex);
                }
            }
            startShowAnimation();
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

    /**
     * 设置点击位置，初始化按钮位置
     * @param x
     * @param y
     */
    public void setCenterPointAndInitBox(int x, int y) {
        mCenterPoint = getCenterPointByRectLimit(mCenterPoint, x, y);
        initBoxesTargetRect();
    }

    /**
     * 判断点击位置是否在限制区域内， 限制区域根据中心按钮大小获得
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
            box.button.setScale(1.0f);
            box.button.isPressed = false;
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
        // 防止点击按钮显示超过屏幕边界
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
        long animTime = SystemClock.uptimeMillis();
        ShowAnimation anim = mAnimation;
        boolean more = false;
        if (anim != null) {
            more |= anim.calculate(animTime);
            if(!anim.isActive()) {
                mAnimation = null;
                if(anim.getType() == ShowAnimation.HIDING) {
                    isButtonsShown = false;
                }
            }
            mProgress = anim.get();
        }
        if(isButtonsShown) {
            renderBackground(canvas);
            canvas.save(Canvas.MATRIX_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);

            if(anim != null && anim.getType() == ShowAnimation.HIDING) {
                // 消失时往中心收缩
//                canvas.setAlpha(mProgress);
                canvas.translate(mCenterPoint.x, mCenterPoint.y);
                canvas.scale(mProgress, mProgress);
                canvas.translate(-mCenterPoint.x, -mCenterPoint.y);
            }

            drawCenterCircle(canvas);
            if(mBoxPool.size() > 0) {
                // 根据点击x位置判断显示层级， 防止弹出文字被遮挡
                if(mCenterPoint.x < getWidth() / 2) {
                    for(int i = 0; i < size(); i++) {
                        drawButtons(canvas, i);
                    }
                } else {
                    for(int i = size() -1; i >= 0; i--) {
                        drawButtons(canvas, i);
                    }
                }
            }
            canvas.restore();
        }

        if (more)
            invalidate();
    }

    private void drawButtons(Canvas canvas, int index) {
        Box box = mBoxPool.get(index);
        if (box.button == null)
            return;
        if (mAnimation != null  && mAnimation.isActive() && mAnimation.getType() == ShowAnimation.SHOWING) {
            mAnimation.apply(canvas, box);
        }
        box.button.render(canvas, mProgress);
    }

    /**
     * 绘制背景遮罩
     * @param canvas
     */
    protected void renderBackground(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
        mAlpha = Utils.clamp(mProgress, 0.0f, 0.5f);
        mBackgroundPaint .setAlpha((int) (255 * mAlpha));
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
        x = mCenterPoint.x - w / 2;
        y = mCenterPoint.y - h / 2;
        mCenterCircle.setBounds(x, y,  x + w, y + h);
        mCenterCircle.setAlpha((int) (255 * mProgress));
        mCenterCircle.draw(canvas);
        canvas.restore();
    }

    // ---------------------------- Gesture --------------------------------------------

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
                isHandle = false;
                startHideAnimation();
                break;
        }
        return isHandle ? true : super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                for (Box box : mBoxPool) {
                    if(box.target.contains((int) event.getX(), (int) event.getY())) {
                        if(!box.button.isPressed()) {
                            box.button.setPressed(true);
                            mCurrentBox = box;
                        }
                    } else {
                        if(box.button.isPressed()) {
                            box.button.setPressed(false);
                            if(mCurrentBox == box) {
                                mCurrentBox = null;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_DOWN:
                isHandle = false;
                startHideAnimation();
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PICK_BUTTON), 500);
                break;
        }
        return super.onTouchEvent(event);
    }

    // ---------------------------- Animation --------------------------------------------
    public void startShowAnimation() {
        if (isButtonsShown) return;
        if (mAnimation != null) {
            mAnimation.forceStop();
        }
        isButtonsShown = true;
        mAnimation = new ShowAnimation(ShowAnimation.SHOWING);
        mAnimation.start();
        invalidate();
    }

    public void startHideAnimation() {
        if (!isButtonsShown) return;
        if (mAnimation != null) {
            mAnimation.forceStop();
        }
        mAnimation = new ShowAnimation(ShowAnimation.HIDING);
        mAnimation.start();
        invalidate();
    }

    private class ShowAnimation extends Animation {
        private final float mFrom;
        private final float mTo;
        private float mCurrent;

        public static final int SHOWING = 0;
        public static final int HIDING = 1;
        private final int mType;

        public ShowAnimation(int type) {
            mType = type;
            if(type == SHOWING) {
                mFrom = 0.0f;
                mTo = 1.0f;
            } else if(type == HIDING)  {
                mFrom = 1.0f;
                mTo = 0.0f;
            } else {
                mFrom = mTo = 0.0f;
            }
            mCurrent = mFrom;
            setDuration(500);
            setInterpolator(new DecelerateInterpolator(4));
        }

        @Override
        protected void onCalculate(float progress) {
            mCurrent = mFrom + (mTo - mFrom) * progress;
        }

        public int getType() {
            return mType;
        }

        public float get() {
            return mCurrent;
        }

        public void apply(Canvas canvas, Box box) {
            int x = (int) (mCenterPoint.x - (mCenterPoint.x - box.targetPoint.x) * mCurrent);
            int y = (int) (mCenterPoint.y - (mCenterPoint.y - box.targetPoint.y) * mCurrent);
            int left = x - mButtonDpSize / 2;
            int right = x + mButtonDpSize / 2;
            int top = y - mButtonDpSize / 2;
            int bottom = y + mButtonDpSize / 2;
            box.button.layout(left, top, right, bottom);
        }
    }
}
