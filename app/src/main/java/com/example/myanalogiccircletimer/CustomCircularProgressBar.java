package com.example.myanalogiccircletimer;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CustomCircularProgressBar extends ConstraintLayout {


    private static final float START_ANGLE = -90;
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
    private RectF outerOval;
    private Integer countProgress = 0;
    ProgressBar timeProgressBar;

    int diameter = 0;
    float pad = 0f;

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
        //region - Analogic Timer
        if (layoutMode == LayoutMode.ANALOGIC.index) {

            final ImageView radar = findViewById(R.id.radar);

            timeProgressBar.bringToFront();
            timeProgressBar.setMax(progressToCount);
            timeProgressBar.setInterpolator(new LinearInterpolator());
            mPointer.bringToFront();

            final ObjectAnimator progressAnimator = ObjectAnimator.ofInt(timeProgressBar, "progress", 0, progressToCount);
            final ValueAnimator firstColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), mRemainingColor, getResources().getColor(R.color.middleColor));
            final ValueAnimator secondColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.middleColor), getResources().getColor(R.color.colorAccent));
            final Animation radarRotationAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.clockwise_animation);
            progressAnimator.setDuration(progressToCount);
            progressAnimator.setInterpolator(new LinearInterpolator());
            progressAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    ValueAnimator timerAnimator = ValueAnimator.ofFloat(mSweepAngle, mMaxSweepAngle);
                    timerAnimator.setInterpolator(new LinearInterpolator());
                    timerAnimator.setDuration(progressToCount);
                    timerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mPointer.setRotation((float) animation.getAnimatedValue());
                            radar.setRotation((float) animation.getAnimatedValue() - 90);
                        }
                    });
                    timerAnimator.start();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    radar.clearAnimation();
                    radarRotationAnimation.cancel();
                    radar.setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            radarRotationAnimation.setFillAfter(true);
            radar.setAnimation(radarRotationAnimation);

            firstColorAnimation.setDuration(progressToCount / 2);
            firstColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRemainingColor = ((int) firstColorAnimation.getAnimatedValue());
                    findViewById(R.id.miolo).getBackground().mutate().setColorFilter(mRemainingColor, PorterDuff.Mode.SRC_IN);
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
            secondColorAnimation.setDuration(progressToCount / 2);
            secondColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mRemainingColor = ((int) secondColorAnimation.getAnimatedValue());
                    findViewById(R.id.miolo).getBackground().mutate().setColorFilter(mRemainingColor, PorterDuff.Mode.SRC_IN);
                }
            });
            secondColorAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    radarRotationAnimation.cancel();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            firstColorAnimation.start();
            radarRotationAnimation.start();
            progressAnimator.start();
        }

        //endregion

        //region - Digital Timer
        else {
            ValueAnimator timerAnimator = ValueAnimator.ofFloat(mSweepAngle, mMaxSweepAngle);
            timerAnimator.setInterpolator(new LinearInterpolator());
            timerAnimator.setDuration(progressToCount);
            timerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mTimeTextCount.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
                    timeProgressBar.setProgress(countProgress += 1);
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
                    callback.countFinished();
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            startTimerTextCount(progressToCount);
            timerAnimator.start();
        }
        //endregion
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
        timeProgressBar = findViewById(R.id.timerProgressBar);
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
        diameter = Math.min(mViewWidth, mViewHeight);
        pad = mStrokeWidth / mPad;
        outerOval = new RectF(pad, pad, diameter - pad, diameter - pad);
    }

    private void drawProgress(Canvas canvas) {

        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        if (layoutMode == LayoutMode.DIGITAL.index) {
            mPaint.setColor(mProgressColor);
            mPaint.setStyle(Paint.Style.STROKE);

            canvas.drawArc(outerOval, START_ANGLE, mSweepAngle, false, mPaint);
        } else {
            mPaint.setStyle(Paint.Style.FILL);

            // Painting remaining time
            /*mPaint.setColor(mRemainingColor);
            canvas.drawArc(outerOval, mSweepAngle, mMaxSweepAngle, true, mPaint);*/
            // Painting progress time
            /*mPaint.setColor(mProgressColor);
            canvas.drawArc(outerOval, START_ANGLE, mSweepAngle, true, mPaint);*/
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

        int index;

        LayoutMode(int index) {
            this.index = index;
        }
    }
}
