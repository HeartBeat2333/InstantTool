package com.heartbeat.instanttool;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

/**
 * Created by zhouyuan on 2016/6/8.
 */
public class InstantToolLayout extends ViewGroup {
    private static final String TAG = "InstantToolLayout";
    private static final int MAX_BUTTON_NUM = 5; // 最大按钮数量

    private Context mContext;
    private boolean isShown;

    private int mButtonRadius; // 按钮到点击中心的半径
    private int mCurrentBtnNum; // 当前按钮数量
    private int mAngleGap;  // 每个按钮间隔的角度

    private int mBackgrounColor;
    private int mButtonLength;
    private int mButtonPadding;

    private float mAlpha; // 当前透明度
    private Rect mLimitRect;
    private Point mCenterPoint;
    protected InstantToolButtonListener mButtonListener;
    private ArrayList<Box> mBoxPool = new ArrayList<>();

    private Drawable mCenterCircle;

    private class Box { // button容器
        public Button button;
        public Rect target;
        public Point targetPoint;

        public Box(int btnId) {
//            button = new InstantButton(btnId, mContext);
//            button.setTextPane(mTextPanel);
//            target = new Rect();
//            targetPoint = new Point();
//
//            if(mButtonListener != null)
//                mButtonListener.onInitButton(button.getId(), button);
        }
    }

    public interface InstantToolButtonListener {
        public void onInitButton(int id, Button button);
        public void onItemButtonChecked(int id, Button button, int slotIndex);
    }

    public InstantToolLayout(Context context) {
        this(context, null);
    }

    public InstantToolLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InstantToolLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public InstantToolLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }


    private void init(Context context) {
        mContext = context;
        mBackgrounColor = context.getResources().getColor(
                R.color.instanttool_bg);
        mAlpha = 0.0f;

        mCenterCircle = mContext.getResources().getDrawable(R.drawable.circle_center);
        mButtonRadius = context.getResources().getDimensionPixelSize(R.dimen.intantTool_radius);
        mAngleGap = 180 / (MAX_BUTTON_NUM - 1);
        mButtonLength = context.getResources().getDimensionPixelSize(R.dimen.intantTool_button_length);
        mButtonPadding = context.getResources().getDimensionPixelSize(R.dimen.intantTool_button_padding);
//        mTextPanel = new NinePatchTexture(mContext, R.drawable.panel_undo_holo);;
        mCenterPoint = new Point();
        initBoxs();
    }

    private void initBoxs() {
        for(int i = 0; i < mCurrentBtnNum; i++) {
            Box box = new Box(i);
            mBoxPool.add(box);
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
        initLimitRect(left, top, right, bottom);
        for(Box box : mBoxPool) {
            box.button.layout(0, 0, mButtonLength, mButtonLength);
            box.button.setPadding(mButtonPadding, mButtonPadding, mButtonPadding, mButtonPadding);
        }
    }

    /**
     * 初始化中心点显示区域
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void initLimitRect(int left, int top, int right, int bottom) {
        int width = mCenterCircle.getIntrinsicWidth() / 2;
        int height = mCenterCircle.getIntrinsicHeight() / 2;
        mLimitRect = new Rect(left + width, top + height, right - width, bottom -  height);
    }

    public void setCenterPointAndInitBox(int x, int y) {
        mCenterPoint = getCenterPointByRectLimit(mCenterPoint, x, y);
        initBoxsTargetRect();
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

    private void initBoxsTargetRect() {
        int startAngle = getStartAngle();
        for(Box box : mBoxPool) {
            // TODO 初始化按钮状态
//            box.button.setScale(1.0f);
//            box.button.isPressed = false;
//            box.button.setTextVisible(false);
            // 设置目标矩形
            initBoxTargetRect(box, startAngle);
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
    private void initBoxTargetRect(Box box, int startAngle) {
        // 当前box的角度
        int currentAngle = startAngle + mAngleGap * box.button.getId();
        // 根据角和半径计算出box中心点
        int x = mCenterPoint.x - (int)(mButtonRadius * Math.cos(Math.toRadians(currentAngle)));
        int y = mCenterPoint.y - (int)(mButtonRadius * Math.sin(Math.toRadians(currentAngle)));
//		Log.i(TAG, "initBoxTargetRect x : " + x + ", y : " + y);
        int boxHalfWidth = mButtonLength / 2;
        int boxHalfHeight = mButtonLength / 2;
        box.target.set(x - boxHalfWidth, y - boxHalfHeight, x + boxHalfWidth, y + boxHalfHeight);
        box.targetPoint.set(x, y);
//		Log.i(TAG, "box " + box.button.getId() + ", targetRect : " + box.target.toString());
    }

    public int size() {
        return mBoxPool.size();
    }
}
