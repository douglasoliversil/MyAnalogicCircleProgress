package com.example.myanalogiccircletimer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CustomCircularProgressBar extends ConstraintLayout {


    private static final Integer INITIAL_REGRESSIVE_COUNT_TIME = 59;
    private static final Integer FINAL_REGRESSIVE_COUNT_TIME = 0;
    private int hours = FINAL_REGRESSIVE_COUNT_TIME;
    private int minutes = FINAL_REGRESSIVE_COUNT_TIME;
    private int seconds = FINAL_REGRESSIVE_COUNT_TIME;
    private Timer mTimer;

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
    private int layoutMode;
    private TextView mTimeTextCount;

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
            layoutMode = a.getInt(R.styleable.CustomCircularProgressBar_mode, LayoutMode.DIGITAL.modeNumber);
            mProgressColor = a.getColor(R.styleable.CustomCircularProgressBar_timeProgressColor, getResources().getColor(R.color.colorAccent));
            mRemainingColor = a.getColor(R.styleable.CustomCircularProgressBar_timeRemainingColor, getResources().getColor(R.color.colorPrimaryDark));
            mStrokeWidth = a.getDimension(R.styleable.CustomCircularProgressBar_strokeWidth, 22);
            mPad = a.getFloat(R.styleable.CustomCircularProgressBar_pad, 2.0f);

        } finally {
            a.recycle();
        }

        setupLayout(context);

    }

    public void setProgressTo(final int progressToCount, final Callback callback) {

        ValueAnimator animator = ValueAnimator.ofFloat(mSweepAngle, mMaxSweepAngle);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(progressToCount);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSweepAngle = (float) animation.getAnimatedValue();
                if (layoutMode == LayoutMode.ANALOGIC.modeNumber) {
                    mPointer.setRotation(mSweepAngle);
                } else {
                    mTimeTextCount.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
                invalidate();
                if (animation.getAnimatedFraction() == 1.0) {
                    callback.countFinished();
                }
            }
        });
        if (layoutMode == LayoutMode.DIGITAL.modeNumber) {
            startTimerTextCount(progressToCount);
        }
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initMeasurments();
        drawProgress(canvas);
    }

    private void setupLayout(Context context) {
        if (this.layoutMode == LayoutMode.DIGITAL.modeNumber) {
            inflate(context, R.layout.custom_circle_digital_progress_bar, this);
            mTimeTextCount = findViewById(R.id.counterText);
        } else {
            inflate(context, R.layout.custom_circle_analogic_progress_bar, this);
            mPointer = findViewById(R.id.pointer);
        }
    }

    private void startTimerTextCount(int progressToCount) {
        hours = (int) TimeUnit.MILLISECONDS.toHours(progressToCount);
        minutes = (int) TimeUnit.MILLISECONDS.toMinutes(progressToCount) - (hours * 60);

        int auxMinutes = (int) TimeUnit.MILLISECONDS.toSeconds(progressToCount) - (minutes * 60);
        seconds = auxMinutes > INITIAL_REGRESSIVE_COUNT_TIME ? FINAL_REGRESSIVE_COUNT_TIME : auxMinutes;

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshTime();
            }
        }, 1000, 1000);
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

        float mStartAngle = -90;
        if (layoutMode == LayoutMode.DIGITAL.modeNumber) {
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

    private void refreshTime() {

        boolean isSecondsReseted = false;
        boolean isMinutesReseted = false;

        if (hours > FINAL_REGRESSIVE_COUNT_TIME && minutes == FINAL_REGRESSIVE_COUNT_TIME) {
            minutes = INITIAL_REGRESSIVE_COUNT_TIME;
            isMinutesReseted = true;
            hours--;
        }
        if (minutes > FINAL_REGRESSIVE_COUNT_TIME && seconds == FINAL_REGRESSIVE_COUNT_TIME) {
            seconds = INITIAL_REGRESSIVE_COUNT_TIME;
            isSecondsReseted = true;
            if (!isMinutesReseted) {
                minutes--;
            }
        }
        if (!isSecondsReseted) {
            seconds--;
        }
        if (hours == FINAL_REGRESSIVE_COUNT_TIME && minutes == FINAL_REGRESSIVE_COUNT_TIME && seconds == FINAL_REGRESSIVE_COUNT_TIME) {
            mTimer.purge();
            mTimer.cancel();
        }
    }

    public interface Callback {
        void countFinished();
    }

    public enum LayoutMode {
        ANALOGIC(0), DIGITAL(1);

        int modeNumber;

        LayoutMode(int modeNumber) {
            this.modeNumber = modeNumber;
        }
    }
}
