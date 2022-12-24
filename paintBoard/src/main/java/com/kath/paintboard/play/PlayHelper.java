package com.kath.paintboard.play;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.kath.paintboard.bean.Point;
import com.kath.paintboard.bean.Shape;
import com.kath.paintboard.widget.PaintView;

import java.util.ArrayList;
import java.util.List;

public class PlayHelper implements LifecycleObserver {

    private final Handler mHandler;
    private final PaintView mPlayView;
    private List<Shape> mShapes;
    private final Object obj;
    private PlayRunnable playRunnable;

    public PlayHelper(FragmentActivity activity, PaintView playView) {
        activity.getLifecycle().addObserver(this);
        mPlayView = playView;
        mHandler = new Handler(Looper.getMainLooper());
        obj = new Object();
    }

    public void start(String fileName) {
        mPlayView.loadPreImage(fileName);
        mShapes = mPlayView.getSaveShapeList();
        //这里要清空，不然会多出一份数据，因为是重新绘制了一遍
        mPlayView.setSaveShapeList(new ArrayList<Shape>());

        playRunnable = new PlayRunnable();
        new Thread(playRunnable).start();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void stop() {
        if(playRunnable != null) {
            playRunnable.stop();
        }
    }

    public interface PlayListener {
        void onStart();

        /**
         * @param flag true:播放完成 false:终止播放
         */
        void onEnd(boolean flag);
    }

    private PlayListener playListener;

    public void setPlayListener(PlayListener listener) {
        playListener = listener;
    }

    public class PlayRunnable implements Runnable {

        private boolean mStopping;
        private final Object mMonitor = new Object();

        @Override
        public void run() {
            process();
        }

        private void process() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlayView.setFingerEnable(false);
                    if (playListener != null) {
                        playListener.onStart();
                    }
                }
            });
            int index = 0;
            while (!mStopping && index < mShapes.size()) {
                long differ = 0;
                boolean loop = true;
                int nextTick = 0;
                Shape shape = mShapes.get(index);
                List<Point> points = shape.getPointList();
                if (index > 0) {//这里是为了两段笔画之间可能存在停留间隙，不然会画完上一个就直接画下一个了
                    Point point = points.get(0);
                    List<Point> prePoints = mShapes.get(index - 1).getPointList();
                    Point lastPoint = prePoints.get(prePoints.size() - 1);
                    long differTwo = point.getTime() - lastPoint.getTime();
                    long tick = getTick();
                    while (!mStopping && frameNeedWait( tick + differTwo, getTick())) {
                        synchronized (obj) {
                            try {
                                obj.wait(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                while (loop && !mStopping) {
                    Point point = points.get(nextTick);
                    if (nextTick == 0) {
                        mPlayView.setCurrentKind(shape.getKind());
                        mPlayView.setBrushSize(shape.getWidth());
                        mPlayView.setBrushColor(shape.getColor());
                        mPlayView.onTouchEventV(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, point.getX(), point.getY(), 0));
                        differ = getTick() - point.getTime();
                    } else if (nextTick == points.size() - 1) {
                        mPlayView.onTouchEventV(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, point.getX(), point.getY(), 0));
                    } else {
                        mPlayView.onTouchEventV(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, point.getX(), point.getY(), 0));
                    }
                    while (!mStopping && frameNeedWait(point.getTime() + differ, getTick())) {
                        synchronized (obj) {
                            try {
                                obj.wait(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    nextTick++;
                    loop = (nextTick != points.size());
                }
                index++;
            }
            mStopping = true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlayView.setFingerEnable(true);
                    if (playListener != null) {
                        playListener.onEnd(true);
                    }
                }
            });
            synchronized (mMonitor) {
                mMonitor.notify();
            }
        }

        private boolean frameNeedWait(long tick, long curTick) {
            System.out.println("frameNeedWait--tick:" + tick + ", curTick:" + curTick + ", tick - curTick:" + (tick - curTick));
            return tick - 100 > curTick;
        }

        public final void stop() {
            if(mStopping) {
               return;
            }
            mStopping = true;
            mPlayView.setFingerEnable(true);
            if (playListener != null) {
                playListener.onEnd(false);
            }
            requestProcess();
            synchronized (mMonitor) {
                try {
                    mMonitor.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public boolean waitProcess() {
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return !mStopping;
        }

        public final void requestProcess() {
            synchronized (this) {
                this.notify();
            }
        }
    }

    public static long getTick() {
        return SystemClock.elapsedRealtime();
    }
}
