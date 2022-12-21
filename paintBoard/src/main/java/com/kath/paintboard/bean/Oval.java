package com.kath.paintboard.bean;

import android.graphics.Canvas;

import com.kath.paintboard.Constants;
import com.kath.paintboard.util.InterSectUtil;

/**
 * Created by user on 2016/8/4.
 * 椭圆形类
 */
public class Oval extends Shape {

    Point startPoint;
    Point endPoint;

    /**
     * **************************
     * construct methond
     * **************************
     */

    public Oval() {

    }


    /**
     * **************************
     * getter and setter methond
     * <p/>
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
            mCanvas.drawOval(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY(), paint);
        }
    }

    @Override
    public void touchDown(float x, float y) {
        //设置初始点和终止点
        setStartPoint(new Point(x, y));
        setEndPoint(new Point(x, y));
    }

    @Override
    public void touchMove(float mx, float my, float x, float y) {
        //修改终止点
        setEndPoint(new Point(x, y));
        //保存点
        addPoint(x, y);
    }

    @Override
    public void touchUp(float x, float y) {
        //设置终止点
        setEndPoint(new Point(x, y));
    }

    @Override
    public int getKind() {
        return Constants.OVAL;
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
        Point center = new Point((endPoint.getX() + startPoint.getX()) / 2, (endPoint.getY() + startPoint.getY()) / 2);
        float radius = Math.abs(
                endPoint.getY() - startPoint.getY()) >= Math.abs(endPoint.getX() - startPoint.getX())
                ? Math.abs(endPoint.getX() - startPoint.getX()) / 2
                : Math.abs(endPoint.getY() - startPoint.getY()) / 2;
//        System.out.println("circlecenter:"+center.getX()+";"+center.getY());
//        System.out.println("radius:"+radius);
//        double lastd=getDistance(lastx,lasty,center.getX(),center.getY());
//        double contentd=getDistance(x,y,center.getX(),center.getY());
//        System.out.println("distance:"+lastd+";"+contentd);
//        if((getDistance(lastx,lasty,center.getX(),center.getY())>=radius && getDistance(x,y,center.getX(),center.getY())<=radius)
//                ||
//                (getDistance(lastx,lasty,center.getX(),center.getY())<=radius && getDistance(x,y,center.getX(),center.getY())>=radius)){
//            return  true;
//        }
//        return false;
        //TODO 目前采取这种方式
        if (getDistance(x, y, center.getX(), center.getY()) < radius) {
            return true;
        } else
            return false;
    }

    public static float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
