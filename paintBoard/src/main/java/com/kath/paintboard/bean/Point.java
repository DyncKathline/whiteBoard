package com.kath.paintboard.bean;

import android.os.SystemClock;

/**
 * 画布上触点类
 */
public class Point {
    float x;
    float y;
    /**
     * 压力值  物理设备决定的，和设计的设备有关系
     */
    float pressure;
    /**
     * 绘制的工具是否是手指或者是笔（触摸笔）
     */
    int toolType;
    long time;

    public Point() {
    }

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
        this.time = SystemClock.elapsedRealtime();
    }

    public Point(float x, float y, float pressure, int toolType) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.toolType = toolType;
    }

    public Point(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPressure() {
        return pressure;
    }

    public int getToolType() {
        return toolType;
    }

    public long getTime() {
        return time;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public void setToolType(int toolType) {
        this.toolType = toolType;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", pressure=" + pressure +
                ", toolType=" + toolType +
                ", time=" + time +
                '}';
    }
}
