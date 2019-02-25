package com.example.myanalogiccircletimer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomCircularProgressBar extends ConstraintLayout {

    private final float mStartAngle = -90;
    private Paint mPaint;
    private float mPad;
    private int mViewWidth;
    private int mViewHeight;
    private float mSweepAngle = 0;
    private float mMaxSweepAngle = 360;
    private float mStrokeWidth = 22;
    private int mProgressColor;
    private int mRemainingColor;
    private ImageView mPointer;
    private int mAnimationDuration = 400;
    private boolean isDigitalMode;
    private TextView mCounterText;

    public CustomCircularProgressBar(Context context) {
        super(context);
    }

    public CustomCircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomCircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(false);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CustomCircularProgressBar,
                0, 0);

        try {
            isDigitalMode = a.getBoolean(R.styleable.CustomCircularProgressBar_isDigitalMode, false);
            mProgressColor = a.getColor(R.styleable.CustomCircularProgressBar_timeProgressColor, getResources().getColor(R.color.colorAccent));
            mRemainingColor = a.getColor(R.styleable.CustomCircularProgressBar_timeRemainingColor, getResources().getColor(R.color.colorPrimaryDark));
            mStrokeWidth = a.getDimension(R.styleable.CustomCircularProgressBar_strokeWidth, 22);
            mPad = a.getFloat(R.styleable.CustomCircularProgressBar_pad, 2.0f);

        } finally {
            a.recycle();
        }

        if (isDigitalMode) {
            inflate(context, R.layout.custom_circle_digital_progress_bar, this);
            mCounterText = findViewById(R.id.counterText);
        } else {
            inflate(context, R.layout.custom_circle_analogic_progress_bar, this);
            mPointer = findViewById(R.id.pointer);
        }

    }

    public void setProgressTo(final int progressToCount, final Callback callback) {

        ValueAnimator animator;

        if (isDigitalMode) {
            animator = ValueAnimator.ofFloat(mSweepAngle, calcSweepAngleFromProgress(progressToCount));
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(mAnimationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSweepAngle = (progressToCount == 100 ? 0 : (float) animation.getAnimatedValue());
                    invalidate();
                    if (progressToCount >= 100) {
                        callback.countFinished();
                    }
                }
            });
        } else {
            animator = ValueAnimator.ofFloat(mSweepAngle, mMaxSweepAngle);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(progressToCount);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSweepAngle = (float) animation.getAnimatedValue();
                    mPointer.setRotation(mSweepAngle);
                    invalidate();
                    if (animation.getAnimatedFraction() == 1.0) {
                        callback.countFinished();
                    }
                }
            });
        }
        animator.start();
    }

    public TextView getCounterText() {
        return mCounterText;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initMeasurments();
        drawProgress(canvas);
    }

    private void initMeasurments() {
        mViewWidth = getWidth();
        mViewHeight = getHeight();
    }

    private void drawProgress(Canvas canvas) {

        final int diameter = Math.min(mViewWidth, mViewHeight);
        final float pad = mStrokeWidth / mPad;
        final RectF outerOval = new RectF(pad, pad, diameter - pad, diameter - pad);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        if (isDigitalMode) {
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);

            canvas.drawArc(outerOval, mStartAngle, mSweepAngle, false, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.FILL);

            // Painting remaining time
            mPaint.setColor(mRemainingColor);
            canvas.drawArc(outerOval, mSweepAngle, mMaxSweepAngle, true, mPaint);

            // Painting progress time
            mPaint.setColor(mProgressColor);
            canvas.drawArc(outerOval, mStartAngle, mSweepAngle, true, mPaint);
        }
    }

    private float calcSweepAngleFromProgress(int progress) {
        return (mMaxSweepAngle / 100) * progress;
    }

    public interface Callback {
        void countFinished();
    }
}
