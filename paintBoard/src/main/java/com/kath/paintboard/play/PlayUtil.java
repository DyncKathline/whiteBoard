package com.kath.paintboard.play;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.kath.paintboard.bean.Ink;
import com.kath.paintboard.bean.Point;
import com.kath.paintboard.bean.Shape;
import com.kath.paintboard.config.MotionElement;
import com.kath.paintboard.util.JsonOperation;
import com.kath.paintboard.widget.PaintView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayUtil implements LifecycleObserver {

    private final String TAG = "PlayUtil";
    private PaintView mPlayView;
    private List<Shape> mShapes;
    private AnimatorSet animatorSet;

    public PlayUtil(FragmentActivity activity, PaintView playView) {
        mPlayView = playView;
        activity.getLifecycle().addObserver(this);
    }

    public void start(String fileName) {
        mPlayView.loadPreImage(fileName);
        mShapes = mPlayView.getSaveShapeList();
        //这里要清空，不然会多出一份数据
        mPlayView.setSaveShapeList(new ArrayList<Shape>());

        animatorSet = new AnimatorSet();
        Animator animator = null;
        List<Shape> shapes = mShapes;
        long lastTime = 0;
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            List<Point> points = shape.getPointList().subList(1, shape.getPointList().size() - 1);

            if (points.size() > 0) {
                long startTime = points.get(0).getTime();
                long endTime = points.get(points.size() - 1).getTime();
                ValueAnimator valueAnimator = ValueAnimator.ofObject(new TypeEvaluator<Point>() {
                    @Override
                    public Point evaluate(float fraction, Point startValue, Point endValue) {
                        System.out.println("---" + fraction + ", " + startValue.toString() + ", " + endValue.toString());
                        mPlayView.onTouchEventV(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, endValue.getX(), endValue.getY(), 0));
                        return endValue;
                    }
                }, points.toArray());
                valueAnimator.setDuration(endTime - startTime);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mPlayView.setCurrentKind(shape.getKind());
                        mPlayView.setBrushSize(shape.getWidth());
                        mPlayView.setBrushColor(shape.getColor());
                        Point point = shape.getPointList().get(0);
                        mPlayView.onTouchEventV(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, point.getX(), point.getY(), 0));
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Point point = shape.getPointList().get(shape.getPointList().size() - 1);
                        mPlayView.onTouchEventV(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, point.getX(), point.getY(), 0));
                    }
                });


                Point startPoint = shape.getPointList().get(0);
                long firstTime = startPoint.getTime();

                if (animator != null) {
                    Shape preShape = shapes.get(i - 1);
                    Point preEndPoint = preShape.getPointList().get(preShape.getPointList().size() - 1);
                    lastTime = preEndPoint.getTime();
                    System.out.println("firstTime: " + firstTime + ", lastTime: " + lastTime + ",time: " + (firstTime - lastTime));
                    valueAnimator.setStartDelay(firstTime - lastTime);
                    animatorSet.play(valueAnimator).after(animator);
                } else {
                    animatorSet.play(valueAnimator);
                }
                animator = valueAnimator;
            }
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if(listener != null) {
                    listener.onAnimationCancel(animation);
                }
                mPlayView.setFingerEnable(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if(listener != null) {
                    listener.onAnimationEnd(animation);
                }
                mPlayView.setFingerEnable(true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if(listener != null) {
                    listener.onAnimationStart(animation);
                }
                mPlayView.setFingerEnable(false);
            }
        });
        animatorSet.start();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stop() {
        if (animatorSet != null) {
            animatorSet.cancel();
            animatorSet.end();
        }
    }

    private Animator.AnimatorListener listener;

    public void addListener(Animator.AnimatorListener listener) {
        this.listener = listener;
    }
}
