package com.kath.paintboard.bean;

import android.graphics.Canvas;
import android.os.SystemClock;

import com.kath.paintboard.PaintConstants;
import com.kath.paintboard.util.InterSectUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 矩形类
 */
public class Rectangle extends Shape {

    Point startPoint;
    Point endPoint;

    /**
     * **************************
     * construct methond
     * **************************
     */

    public Rectangle() {

    }


    /**
     * **************************
     * getter and setter methond
     * <p>
     * **************************
     */

    public Point getStartPoint() {
        return startPoint;
    }

    public Point getEndPoint() {
        return endPoint;
    }

    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }

    public void setEndPoint(Point endPoint) {
        this.endPoint = endPoint;
    }


    @Override
    public void draw(Canvas mCanvas) {
        if (startPoint != null && endPoint != null) {
            mCanvas.drawRect(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY(), paint);
        }
    }

    @Override
    public void touchDown(float x, float y) {
        touchDown(x, y, SystemClock.elapsedRealtime());
    }

    @Override
    public void touchDown(float x, float y, long time) {
        //设置初始点和终止点
        setStartPoint(new Point(x, y, time));
        setEndPoint(new Point(x, y, time));
        //保存点
        addPoint(x, y, time);
    }

    @Override
    public void touchMove(float mx, float my, float x, float y) {
        touchMove(mx, my, x, y, SystemClock.elapsedRealtime());
    }

    @Override
    public void touchMove(float mx, float my, float x, float y, long time) {
        //修改终止点
        setEndPoint(new Point(x, y, time));
        //保存点
        addPoint(x, y, time);
    }

    @Override
    public void touchUp(float x, float y) {
        touchUp(x, y, SystemClock.elapsedRealtime());
    }

    @Override
    public void touchUp(float x, float y, long time) {
        //设置终止点
        setEndPoint(new Point(x, y, time));
        //保存点
        addPoint(x, y, time);
    }

    @Override
    public int getKind() {
        return PaintConstants.RECT;
    }

    @Override
    public void setOwnProperty() {
        //获取关键点
        setStartPoint(pointList.get(0));
//        setEndPoint(pointList.get(1));
        setEndPoint(pointList.get(pointList.size() - 1));
    }


    @Override
    public boolean isInterSect(float lastx, float lasty, float x, float y) {
        //矩形逻辑和曲线也是一样的，一共四个点
        List<Point> JudgePointList = new ArrayList<>();
        JudgePointList.add(pointList.get(0));
        JudgePointList.add(new Point(pointList.get(1).getX(), pointList.get(0).getY()));
        JudgePointList.add(new Point(pointList.get(1).getX(), pointList.get(1).getY()));
        JudgePointList.add(new Point(pointList.get(0).getX(), pointList.get(1).getY()));
        JudgePointList.add(pointList.get(0));
        for (int i = 1; i < JudgePointList.size(); i++) {
            if (new InterSectUtil(new Point(lastx, lasty), new Point(x, y), JudgePointList.get(i - 1), JudgePointList.get(i)).segmentIntersect()) {
                return true;
            }
        }
        return false;
    }


}
