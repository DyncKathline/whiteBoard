package com.kath.paintboard.play;

import android.animation.Animator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.kath.paintboard.R;
import com.kath.paintboard.bean.Shape;

import java.util.ArrayList;

public class RecordPathView extends View {

    private Context context;
    private Paint paint, iconPaint;
    private Path dstPath, totalPath;
    private PathMeasure mPathMeasure, mDstPathMeasure;

    private boolean isDrawRecordPath = false;

    private float pathLength;

    private Bitmap startIcon, middleIcon;

    private Bitmap bitmap;//当前canvas生成的bitmap
    private Canvas bitmapCanvas;

    private float[] pathStartPoint = new float[2];
    private float[] pathEndPoint = new float[2];
    private float[] dstPathEndPoint = new float[2];

    private float value = 0;

    private long ANIM_DURATION;

    private ArrayList<RecordPathAnimUtil.RecordPathBean> recordPathList;

    private OnAnimEnd onAnimEnd;

    private int animIndex, lastAnimIndex;

    public RecordPathView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public RecordPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public RecordPathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        iconPaint = new Paint();
        iconPaint.setAntiAlias(true);

        dstPath = new Path();

        startIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.next);
        middleIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.pre);
    }

    public void setPath(RecordPathAnimUtil recordPathAnimUtil, long duration) {
        if (recordPathAnimUtil == null)
            return;
        if (!isDrawRecordPath) {
            pathLength = recordPathAnimUtil.getAllPathLength();
            ANIM_DURATION = duration != 0 ? duration : recordPathAnimUtil.getANIM_DURATION();
            recordPathList = recordPathAnimUtil.getRecordPathList();
            totalPath = recordPathAnimUtil.getTotalPath();
            mPathMeasure = new PathMeasure(totalPath, false);
            mPathMeasure.getPosTan(0, pathStartPoint, null);//轨迹的起点
            mPathMeasure.getPosTan(mPathMeasure.getLength(), pathEndPoint, null);//轨迹的终点
            if (recordPathList == null || recordPathList.size() == 0)
                return;
            startPathAnim();
            isDrawRecordPath = true;
        }
    }

    public void setOnAnimEnd(OnAnimEnd onAnimEnd) {
        this.onAnimEnd = onAnimEnd;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (recordPathList == null || recordPathList.size() == 0)
            return;
        if (value >= 1)
            return;
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(this.getMeasuredWidth(), this.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }

        bitmapCanvas.save();
        if (animIndex > lastAnimIndex && lastAnimIndex > 0) {//动画跳到下一段或下几段path
            for (int i = lastAnimIndex - 1; i < animIndex; i++) {
                RecordPathAnimUtil.RecordPathBean recordPathBean = recordPathList.get(i);
                paint.setColor(recordPathBean.getEndColor());
                paint.setShader(recordPathBean.getShader());
                paint.setStrokeWidth(10);
                paint.setStyle(Paint.Style.STROKE);
                bitmapCanvas.drawPath(recordPathBean.getPath(), paint);
                paint.setShader(null);
                paint.setStrokeWidth(1);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                bitmapCanvas.drawCircle(recordPathBean.getEndPoint().x, recordPathBean.getEndPoint().y, 5, paint);
            }
        }
        bitmapCanvas.restore();
        canvas.drawBitmap(bitmap, 0, 0, iconPaint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(recordPathList.get(animIndex).getShader());
        paint.setStrokeWidth(10);
        canvas.drawPath(dstPath, paint);

//        canvas.drawBitmap(startIcon, pathStartPoint[0] - startIcon.getWidth() / 2, pathStartPoint[1] - startIcon.getHeight() / 2, iconPaint);
//        canvas.drawBitmap(middleIcon, dstPathEndPoint[0] - middleIcon.getWidth() / 2, dstPathEndPoint[1] - middleIcon.getHeight() / 2, iconPaint);

        if (lastAnimIndex != animIndex)
            lastAnimIndex = animIndex;
    }

    /**
     * 当前动画执行进度对应的分段path index
     */
    private void caculateAnimPathData() {
        float length = value * pathLength;
        float caculateLength = 0;
        float offsetLength = 0;
        for (int i = 0, count = recordPathList.size(); i < count; i++) {
            caculateLength += recordPathList.get(i).getPathLength();
            if (caculateLength > length) {
                animIndex = i;
                offsetLength = caculateLength - length;
                break;
            }
        }
//        dstPath.reset();
        PathMeasure pathMeasure = new PathMeasure(recordPathList.get(animIndex).getPath(), false);
        pathMeasure.getSegment(0, recordPathList.get(animIndex).getPathLength() - offsetLength, dstPath, true);
        mDstPathMeasure = new PathMeasure(dstPath, false);
        mDstPathMeasure.getPosTan(mDstPathMeasure.getLength(), dstPathEndPoint, null);
    }

    private void startPathAnim() {
        ValueAnimator animator = ValueAnimator.ofObject(new DstPathEvaluator(), 0, mPathMeasure.getLength());
        animator.setDuration(ANIM_DURATION);
//        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                value = (float) animation.getAnimatedValue();
                caculateAnimPathData();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimEnd != null)
                    onAnimEnd.animEndCallback();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    class DstPathEvaluator implements TypeEvaluator {

        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            return fraction;
        }
    }

    public interface OnAnimEnd {
        void animEndCallback();
    }

    float x, y;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                if (Math.abs(event.getX() - x) > 0 || Math.abs(event.getY() - y) > 0) {
                    if (onAnimEnd != null)
                        onAnimEnd.animEndCallback();
                }
                break;
            default:
                break;
        }
        return true;
    }
}
