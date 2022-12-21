package com.kath.paintboard.config;

/**
 * 触摸点信息
 */
public class MotionElement {

    public float x;
    public float y;
    /**
     * 压力值  物理设备决定的，和设计的设备有关系
     */
    public float pressure;
    /**
     * 绘制的工具是否是手指或者是笔（触摸笔）
     */
    public int toolType;

    public MotionElement(float mx, float my) {
        x = mx;
        y = my;
    }

    public MotionElement(float x, float y, float pressure, int toolType) {
        this.x = x;
        this.y = y;
        this.pressure = pressure;
        this.toolType = toolType;
    }
}
