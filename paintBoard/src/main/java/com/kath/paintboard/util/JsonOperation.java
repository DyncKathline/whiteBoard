package com.kath.paintboard.util;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.kath.paintboard.Constants;
import com.kath.paintboard.bean.Arrow;
import com.kath.paintboard.bean.Circle;
import com.kath.paintboard.bean.Ink;
import com.kath.paintboard.bean.Line;
import com.kath.paintboard.bean.Oval;
import com.kath.paintboard.bean.Point;
import com.kath.paintboard.bean.Rectangle;
import com.kath.paintboard.bean.Shape;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Created by user on 2016/7/29.
 * XML相关操作
 */
public class JsonOperation {

    /**
     * 将画图保存成Json格式文件
     *
     * @param shapeList
     */
    public static int creatJson(List<Shape> shapeList, String filename) throws IOException {

        int returnValue = 0; //创建结果返回，0失败，1成功

        JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"));
        writer.setIndent(" ");//换行
        writer.beginArray();
        for (int i = 0; i < shapeList.size(); i++) {
            Shape shape = shapeList.get(i);
            writer.beginObject();
            writer.name("Kind").value(String.valueOf(shape.getKind()));
            writer.name("Color").value(String.valueOf(shape.getColor()));
            writer.name("Width").value(String.valueOf(shape.getWidth()));
            writer.name("Pointlist").value(changeListDateToStr(shape.getPointList()));
            writer.endObject();
        }
        writer.endArray();
        writer.close();
        returnValue = 1;

        return returnValue;
    }


    /**
     * 将json文件解析成对应的list<shape>
     *
     * @param filename
     * @return
     */
    public static List<Shape> transJsonToShape(String filename) throws IOException {
        //得到Document对象
        FileInputStream fis = new FileInputStream(filename);
        JsonReader reader = new JsonReader(new InputStreamReader(fis, "UTF-8"));

        List<Shape> lists = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            Shape shape = null;

            reader.beginObject();
            while (reader.hasNext()) {
                String field = reader.nextName();
                if (field.equals("Kind")) {
                    switch (Integer.valueOf(reader.nextString())) {
                        case Constants.INK:
                            shape = new Ink();
                            break;
                        case Constants.LINE:
                            shape = new Line();
                            break;
                        case Constants.RECT:
                            shape = new Rectangle();
                            break;
                        case Constants.CIRCLE:
                            shape = new Circle();
                            break;
                        case Constants.ARROW:
                            shape = new Arrow();
                            break;
                        case Constants.OVAL:
                            shape = new Oval();
                            break;
                    }
                } else if (field.equals("Color")) {
                    shape.setColor(reader.nextString());
                } else if (field.equals("Width")) {
                    shape.setWidth(Float.valueOf(reader.nextString()));
                } else if (field.equals("Pointlist")) {
                    shape.setPointList(changeStrToListData(reader.nextString()));
                }
            }
            Paint newPaint = new Paint();
            newPaint.setStyle(Paint.Style.STROKE);
            newPaint.setStrokeWidth(shape.getWidth());
            newPaint.setColor(Color.parseColor(shape.getColor()));
            shape.setPaint(newPaint);
            //设置各自专属属性
            shape.setOwnProperty();
            //加到List当中
            lists.add(shape);
            reader.endObject();
        }
        reader.endArray();
        reader.close();
        return lists;
    }


    /**
     * 将String数据转化成笔迹点集合
     *
     * @param text
     * @return
     */
    private static List<Point> changeStrToListData(String text) {

        List<Point> lists = new ArrayList<>();
        String[] strArray = text.split(";");
        for (int i = 0; i < strArray.length; i++) {

            Point newPoint = new Point();
            String str = strArray[i].toString();
            String[] split = str.split(",");

            String str1 = split[0];
            newPoint.setX(Float.parseFloat(str1));

            String str2 = split[1];
            newPoint.setY(Float.parseFloat(str2));

            if(split.length > 2) {
                String str3 = split[2];
                newPoint.setTime(Long.valueOf(str3));
            }

            lists.add(newPoint);
        }
        return lists;
    }

    /**
     * 将笔迹点数据转成String格式
     *
     * @param pointList
     */
    private static String changeListDateToStr(List<Point> pointList) {

        StringBuffer sb = new StringBuffer();
        for (int j = 0; j < pointList.size(); j++) {
            sb.append(pointList.get(j).getX() + "," + pointList.get(j).getY() + "," + pointList.get(j).getTime() + ";");
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }


}
