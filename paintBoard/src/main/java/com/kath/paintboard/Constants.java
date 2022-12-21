package com.kath.paintboard;

public class Constants {

    /**
     * 笔锋控制值,越小笔锋越粗,越不明显
     */
    public static final float DIS_VEL_CAL_FACTOR = 0.008f;

    public static final String[] colors = new String[]{
            "#242424",
            "#FF0000",
            "#9ACD32",
            "#473C8B",
            "#EEEE00",
            "#EE8262",
            "#EE3A8C",
            "#836FFF",
            "#CDCDB4",
            "#FF7F24"

    };

    public static final int INK = 1;//曲线笔迹
    public static final int LINE = 2;//直线
    public static final int RECT = 3;//矩阵
    public static final int CIRCLE = 4;//圆
    public static final int OVAL = 5;//椭圆
    public static final int ARROW = 6;//箭头
}
