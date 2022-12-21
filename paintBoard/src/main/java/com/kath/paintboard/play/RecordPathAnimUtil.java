package com.kath.paintboard.play;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.util.ArrayList;

public class RecordPathAnimUtil {

    private final long MAX_ANIM_DURATION = 5 * 1000;//动画最大执行时间

    private final long MIN_ANIM_DURATION = 2 * 1000;

    private final int SCREEN_WIDTH_DEBUG = 1080;//当前调试手机的屏幕宽度,作为计算动画执行时间的标准,无实际意义

    private int SCREEN_WIDTH_RELEASE;//用户使用手机屏幕的实际宽度

    private long ANIM_DURATION = MIN_ANIM_DURATION;//动画执行总时间

    private final float PATH_SCREEN_LENGTH_1_KM = 2000.0f;

    private ArrayList<RecordPathBean> recordPathList;

    private PathMeasure pathMeasure;

    private Path totalPath;

    public RecordPathAnimUtil(Context context) {
        recordPathList = new ArrayList<>();
        SCREEN_WIDTH_RELEASE = getScreenWidth(context);
    }

    public long getANIM_DURATION() {
        return ANIM_DURATION;
    }

    public void setANIM_DURATION(long ANIM_DURATION) {
        this.ANIM_DURATION = ANIM_DURATION;
    }

    public ArrayList<RecordPathBean> getRecordPathList() {
        return recordPathList;
    }

    /**
     * 创建坐标点对应的path 渐变
     *
     * @param start
     * @param end
     * @param startColor
     * @param endColor
     */
    public void addPath(Point start, Point end, int startColor, int endColor) {
        if (totalPath == null) {
            totalPath = new Path();
            totalPath.moveTo(start.x, start.y);
            totalPath.lineTo(end.x, end.y);
        }
        totalPath.lineTo(end.x, end.y);
        Path path = new Path();
        path.moveTo(start.x, start.y);
        path.lineTo(end.x, end.y);
        pathMeasure = new PathMeasure(path, false);
        Shader shader = new LinearGradient(start.x, start.y, end.x, end.y, new int[]{startColor, endColor}, null, Shader.TileMode.CLAMP);
        RecordPathBean recordPathBean = new RecordPathBean(path, pathMeasure.getLength(), shader);
        recordPathBean.setEndPoint(end);
        recordPathBean.setEndColor(endColor);
        recordPathList.add(recordPathBean);
        recordPathBean.setIndex(recordPathList.size() - 1);
    }

    /**
     * 所有path的总长度
     *
     * @return
     */
    public float getAllPathLength() {
        float pathLength = 0;
        if (recordPathList != null) {
            for (int i = 0, count = recordPathList.size(); i < count; i++) {
                pathLength += recordPathList.get(i).getPathLength();
            }
        }
        caculateAnimDuration(pathLength);
        return pathLength;
    }

    /**
     * 计算动画执行的总时长
     *
     * @param pathLength
     */
    private void caculateAnimDuration(float pathLength) {
        float pathScreenLength1KmRelease = SCREEN_WIDTH_RELEASE * PATH_SCREEN_LENGTH_1_KM / SCREEN_WIDTH_DEBUG;
        float durationScale = pathLength / pathScreenLength1KmRelease;
        if (durationScale <= 1)
            return;
        long durationRelease = (long) (durationScale * MIN_ANIM_DURATION);
        if (durationRelease >= MAX_ANIM_DURATION) {
            setANIM_DURATION(MAX_ANIM_DURATION);
            return;
        }

        setANIM_DURATION(durationRelease);
    }

    public Path getTotalPath() {
        return totalPath;
    }

    public class RecordPathBean {

        private Path path;//路径
        private Shader shader;//画笔渐变
        private float pathLength;
        private int index;
        private Point endPoint;
        private int endColor;

        public RecordPathBean(Path path, float pathLength, Shader shader) {
            this.path = path;
            this.pathLength = pathLength;
            this.shader = shader;
        }

        public Path getPath() {
            return path;
        }

        public Shader getShader() {
            return shader;
        }

        public float getPathLength() {
            return pathLength;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Point getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(Point endPoint) {
            this.endPoint = endPoint;
        }

        public int getEndColor() {
            return endColor;
        }

        public void setEndColor(int endColor) {
            this.endColor = endColor;
        }
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }
}
