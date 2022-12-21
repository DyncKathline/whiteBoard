package com.kath.paintboard.bean;

import android.graphics.Canvas;
import android.graphics.Path;

import com.kath.paintboard.Constants;
import com.kath.paintboard.util.InterSectUtil;

/**
 * Created by user on 2016/8/4.
 * 箭头类
 */
public class Arrow extends Shape {

    Point startPoint;
    Point endPoint;

    /**
     * **************************
     * construct methond
     * **************************
     */

    public Arrow() {

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
            float startX = startPoint.getX();
            float startY = startPoint.getY();
            float endX = endPoint.getX();
            float endY = endPoint.getY();

            Path mPath = new Path();

            double lineLength = Math.sqrt(Math.pow(Math.abs(endX - startX), 2) + Math.pow(Math.abs(endY - startY), 2));//线当前长度
            if (lineLength == 0) {
                // 画线
                mPath.moveTo(startX, startY);
                mPath.lineTo(endX, endY);
                // mPath.close();闭合线条
                mCanvas.drawPath(mPath, paint);// 实时的显示
            } else {
                double H = 0;// 箭头高度
                double L = 0;// 箭头长度
                if (lineLength < 320) {//防止箭头开始时过大
                    H = lineLength / 4;
                    L = lineLength / 6;
                } else { //超过一定线长箭头大小固定
                    H = 80;
                    L = 50;
                }

                double arrawAngle = Math.atan(L / H); // 箭头角度
                double arraowLen = Math.sqrt(L * L + H * H); // 箭头的长度
                double[] pointXY1 = rotateAndGetPoint(endX - startX, endY - startY, arrawAngle, true, arraowLen);
                double[] pointXY2 = rotateAndGetPoint(endX - startX, endY - startY, -arrawAngle, true, arraowLen);
                int x3 = (int) (endX - pointXY1[0]);//(x3,y3)为箭头一端的坐标
                int y3 = (int) (endY - pointXY1[1]);
                int x4 = (int) (endX - pointXY2[0]);//(x4,y4)为箭头另一端的坐标
                int y4 = (int) (endY - pointXY2[1]);
                // 画线
                mPath.moveTo(startX, startY);
                mPath.lineTo(endX, endY);
                mPath.moveTo(x3, y3);
                mPath.lineTo(endX, endY);
                mPath.lineTo(x4, y4);
                // mPath.close();闭合线条
                mCanvas.drawPath(mPath, paint);// 实时的显示
            }
        }
    }

    @Override
    public void touchDown(float x, float y) {
        //设置初始点和终结点
        setStartPoint(new Point(x, y));
        setEndPoint(new Point(x, y));
    }

    @Override
    public void touchMove(float mx, float my, float x, float y) {
        //修改终结点
        setEndPoint(new Point(x, y));
        //保存点
        addPoint(x, y);
    }

    @Override
    public void touchUp(float x, float y) {
        //设置终结点
        setEndPoint(new Point(x, y));
    }

    @Override
    public int getKind() {
        return Constants.ARROW;
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
        //直线从逻辑上来讲和曲线是一样的
        for (int i = 1; i < pointList.size(); i++) {
            if (new InterSectUtil(new Point(lastx, lasty), new Point(x, y), pointList.get(i - 1), pointList.get(i)).segmentIntersect()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 矢量旋转函数，计算末点的位置
     *
     * @param x       x分量
     * @param y       y分量
     * @param ang     旋转角度
     * @param isChLen 是否改变长度
     * @param newLen  箭头长度长度
     * @return 返回末点坐标
     */
    public double[] rotateAndGetPoint(float x, float y, double ang, boolean isChLen, double newLen) {
        double pointXY[] = new double[2];
        double vx = x * Math.cos(ang) - y * Math.sin(ang);
        double vy = x * Math.sin(ang) + y * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            pointXY[0] = vx / d * newLen;
            pointXY[1] = vy / d * newLen;
        }
        return pointXY;
    }
}
