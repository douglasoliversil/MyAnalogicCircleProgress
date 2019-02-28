package com.example.myanalogiccircletimer;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
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
    private static final Integer FINAL_COLOR_SATURATION = 255;
    private static final Integer INITIAL_COLOR_SATURATION = 0;
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

    private int redFactor;
    private int greenFactor;
    private int blueFactor;
    private boolean isTimerRunning = false;
    private long mProgressTime = 0;
    private long mTimeToCount = 30000;
    private int changeColorReferenceCount = 0;

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
            layoutMode = a.getInt(R.styleable.CustomCircularProgressBar_mode, LayoutMode.DIGITAL.index);
            mProgressColor = a.getColor(R.styleable.CustomCircularProgressBar_timeProgressColor, getResources().getColor(R.color.colorPrimary));
            mRemainingColor = a.getColor(R.styleable.CustomCircularProgressBar_timeRemainingColor, getResources().getColor(R.color.colorPrimaryDark));
            mStrokeWidth = a.getDimension(R.styleable.CustomCircularProgressBar_strokeWidth, 22);
            mPad = a.getFloat(R.styleable.CustomCircularProgressBar_pad, 2.0f);

        } finally {
            a.recycle();
        }

        setupLayout(context);

    }

    public void setProgressTo(final int progressToCount, final Callback callback) {

        mTimeToCount = progressToCount;

        ValueAnimator timerAnimator = ValueAnimator.ofFloat(mSweepAngle, mMaxSweepAngle);
        timerAnimator.setInterpolator(new LinearInterpolator());
        timerAnimator.setDuration(progressToCount);
        timerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgressTime = animation.getCurrentPlayTime();
                mSweepAngle = (float) animation.getAnimatedValue();
                if (layoutMode == LayoutMode.ANALOGIC.index) {
                    mPointer.setRotation(mSweepAngle);
                } else {
                    mTimeTextCount.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                }
                invalidate();
            }
        });
        timerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.cancel();
                isTimerRunning = false;
                callback.countFinished();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if (layoutMode == LayoutMode.DIGITAL.index) {
            startTimerTextCount(progressToCount);
        }
        if (layoutMode == LayoutMode.ANALOGIC.index) {
            final ValueAnimator firstColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mRemainingColor, getResources().getColor(R.color.middleColor));
            firstColorAnimation.setDuration(progressToCount / 2);
            final ValueAnimator secondColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.middleColor), getResources().getColor(R.color.colorAccent));
            secondColorAnimation.setDuration(progressToCount / 2);
            firstColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRemainingColor = ((int) firstColorAnimation.getAnimatedValue());
                }
            });
            firstColorAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    secondColorAnimation.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            secondColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRemainingColor = ((int) secondColorAnimation.getAnimatedValue());
                }
            });
            firstColorAnimation.start();
        }
        timerAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initMeasurments();
        drawProgress(canvas);
    }

    private void setupLayout(Context context) {
        if (this.layoutMode == LayoutMode.DIGITAL.index) {
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
        if (layoutMode == LayoutMode.DIGITAL.index) {
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);

            canvas.drawArc(outerOval, mStartAngle, mSweepAngle, false, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.FILL);

            // Painting remaining time
            mPaint.setColor(mRemainingColor);
//            mPaint.setColor(getNextColor(mRemainingColor));
            canvas.drawArc(outerOval, mSweepAngle, mMaxSweepAngle, true, mPaint);

            // Painting progress time
            mPaint.setColor(mProgressColor);
            canvas.drawArc(outerOval, mStartAngle, mSweepAngle, true, mPaint);
        }
    }

    private int getNextColor(int color) {
        if (!isTimerRunning) {
            redFactor = Color.red(color);
            greenFactor = Color.green(color);
            blueFactor = Color.blue(color);
            isTimerRunning = true;
        }
        int newColor;
        int alpha = Math.round(Color.alpha(color)/* * mSweepAngle / mMaxSweepAngle*/);
        newColor = Color.argb(alpha, redFactor, greenFactor, blueFactor);

        if (isTimerRunning) {
            //por simples contagem
            int changeColorReference = (int) (mTimeToCount / mMaxSweepAngle); // quantas vezes precisarei incrementar meu RGB até completar 360 graus
            if (mProgressTime > 0 && ((mProgressTime / changeColorReference) > changeColorReferenceCount)) { // verificar se do tempo decorrido é o momento de mudar o RGB da cor
                if (mSweepAngle >= 0f && mSweepAngle <= 120f) {
                    if (redFactor < FINAL_COLOR_SATURATION)
                        redFactor++;
                } else if (mSweepAngle >= 121f && mSweepAngle <= 240f) {
                    if (redFactor < FINAL_COLOR_SATURATION) {
                        redFactor++;
                        if (greenFactor < FINAL_COLOR_SATURATION)
                            greenFactor++;
                    }
                } else {
                    if (greenFactor > INITIAL_COLOR_SATURATION)
                        greenFactor--;
                }

                /*if (redFactor < FINAL_COLOR_SATURATION) {
                    redFactor++;
                }
                if (redFactor >= FINAL_COLOR_SATURATION && greenFactor > INITIAL_COLOR_SATURATION) {
                    greenFactor--;
                }
                if (redFactor >= FINAL_COLOR_SATURATION && greenFactor == INITIAL_COLOR_SATURATION && blueFactor > INITIAL_COLOR_SATURATION) {
                    blueFactor--;
                }*/
                changeColorReferenceCount++;
            }
        }

        return newColor;
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

        int index;

        LayoutMode(int index) {
            this.index = index;
        }
    }
}
