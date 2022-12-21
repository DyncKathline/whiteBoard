package com.kath.paintboard.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kath.paintboard.Constants;
import com.kath.paintboard.bean.Arrow;
import com.kath.paintboard.bean.Circle;
import com.kath.paintboard.bean.Gallery;
import com.kath.paintboard.bean.Ink;
import com.kath.paintboard.bean.Line;
import com.kath.paintboard.bean.Oval;
import com.kath.paintboard.bean.Point;
import com.kath.paintboard.bean.Rectangle;
import com.kath.paintboard.bean.Shape;
import com.kath.paintboard.config.MotionElement;
import com.kath.paintboard.pen.BasePen;
import com.kath.paintboard.pen.SteelPen;
import com.kath.paintboard.util.JsonOperation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by user on 2016/7/26.
 * 绘制的画布
 */
public class PaintView extends View {

    private float currentWidth = 15; //默认字体大小
    private String currentColor = Constants.colors[0]; //默认字体颜色
    private int currentKind = Constants.INK; //默认绘图类型

    private int CurrentPageNum = 1;//默认当前页数
    private int CurrentPageIndex = 1;//默认当前页序号

    private Paint mPaint;//画笔
    private Bitmap mBitmap;//画布的bitmap
    private Canvas mCanvas;//画布

    int canvasWidth; //画布的宽
    int canvasHeight;//画布的高
    float mx, my; //当前画笔位置

    List<Shape> SaveShapeList; //已保存笔迹List
    List<Shape> DeleteShapeList; //删除笔迹List

    Gallery mGallery;//画册类

    private BasePen mStokeBrushPen;

    private boolean IsFileExist = false;//是否有编辑过标志
    private int PreShapeListSize = 0; //最初list的长度

    private boolean EraserState = false;//是否处于橡皮擦状态
    private boolean MoveShapeState = false;//是否处于笔迹操作状态

    private boolean Selected = false;//已选中状态

    private final int REDRAW = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == REDRAW) {
                try {
                    redrawOnBitmap();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private boolean isMoving = false; //移动笔迹的标志
    private int toolType = 0;  //记录手写笔类型：触控笔/手指
    /**
     * 是否允许写字
     */
    private boolean isFingerEnable = true;
    /**
     * 是否开启笔刷
     */
    private boolean isBrushEnable = true;

    /**
     * **************************
     * construct methond
     * **************************
     */

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        SaveShapeList = new ArrayList<>();
        DeleteShapeList = new ArrayList<>();
        mGallery = new Gallery();

        mGallery = new Gallery();
        mStokeBrushPen = new SteelPen();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        canvasWidth = getMeasuredWidth();
        canvasHeight = getMeasuredHeight();
        initCanvas();
    }

    /**
     * 初始化画布
     */
    private void initCanvas() {
        //初始化画布
        mBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mCanvas.drawColor(Color.WHITE);
        //初始化画笔
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(currentWidth);
        mPaint.setColor(Color.parseColor(currentColor));

        mStokeBrushPen.setPaint(mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        toolType = event.getToolType(event.getActionIndex());
        if (!isFingerEnable && toolType != MotionEvent.TOOL_TYPE_STYLUS) {
            return false;
        }
        if (!EraserState) {
            if (isBrushEnable && currentShape instanceof Ink) {
                mStokeBrushPen.onTouchEvent(event, mCanvas);
            }
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(event, x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                break;
        }
        invalidate();
        return true;
    }

    public boolean onTouchEventV(MotionEvent event) {
        if (!EraserState) {
            if (isBrushEnable && currentShape instanceof Ink) {
                mStokeBrushPen.onTouchEvent(event, mCanvas);
            }
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(event, x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp(x, y);
                break;
        }
        invalidate();
        return true;
    }

    Shape currentShape;
    Path EraserPath;
    Path MovePath;

    /**
     * 按下操作对应处理
     *
     * @param x
     * @param y
     */
    private void touchStart(MotionEvent event, float x, float y) {
        //进入橡皮擦模式
        if (EraserState) {
            //准备画虚线需要的相关属性
            PathEffect effects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);//设置虚线的间隔和点的长度
            mPaint.setPathEffect(effects);
            mPaint.setColor(Color.parseColor(Constants.colors[1]));
            SweepList = new ArrayList<>();
            NeedHandleList = new ArrayList<>();
            //创建橡皮擦的path
            EraserPath = new Path();
            EraserPath.moveTo(x, y);
        }
        //进入选择线条模式
        else if (MoveShapeState) {
            if (Selected) {
                //判断点击的地方是否是在NeedRect内部如果
                if (!IsNotInside(x, y)) {//不在范围内，相关参数清零从头开始
                    NeedMoveRect = null;
                    MoveList = null;
                    NeedMoveList = null;
                    System.out.println("NeedMoveRect:" + NeedMoveRect);
                    System.out.println("MoveList:" + MoveList);
                    System.out.println("NeedMoveRect:" + NeedMoveRect);
                    System.out.println("Selected:" + Selected);
                    System.out.println("MoveShapeState:" + MoveShapeState);
                } else {
                    //在范围内采取相关措施
                    isMoving = true;
                    //TODO 移动和缩放相关
                    //脏区为NeedMoveRect

                }
            } else {
                //准备画虚线需要的相关属性
                PathEffect effects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);//设置虚线的间隔和点的长度
                mPaint.setPathEffect(effects);
                mPaint.setColor(Color.parseColor(Constants.colors[1]));

                MoveList = new ArrayList<>();
                NeedMoveList = new ArrayList<>();
                //创建选中笔迹的path
                MovePath = new Path();
                MovePath.moveTo(x, y);
            }
        } else {
            //判断当前类型，根据类型选择构造函数
            switch (currentKind) {
                case Constants.INK:
                    currentShape = new Ink();
                    break;
                case Constants.LINE:
                    currentShape = new Line();
                    break;
                case Constants.RECT:
                    currentShape = new Rectangle();
                    break;
                case Constants.CIRCLE:
                    currentShape = new Circle();
                    break;
                case Constants.ARROW:
                    currentShape = new Arrow();
                    break;
                case Constants.OVAL:
                    currentShape = new Oval();
                    break;
            }
            //执行对应操作
            currentShape.touchDown(x, y);
            if (isBrushEnable && currentShape instanceof Ink) {
                mStokeBrushPen.onDown(mStokeBrushPen.createMotionElement(event), mCanvas);
            }
            //设置画笔
            currentShape.setPaint(mPaint);
            //记录起始点
            currentShape.addPoint(x, y);
            //获得颜色和宽度数据
            currentShape.setColor(currentColor);
            currentShape.setWidth(currentWidth);
        }
        mx = x;
        my = y;
    }

    List<Integer> SweepList;
    List<Shape> NeedHandleList;

    List<Integer> MoveList;
    List<Shape> NeedMoveList;
    RectF NeedMoveRect;

    /**
     * 移动操作对应处理
     */
    private void touchMove(float x, float y) {

        if (EraserState) {
            EraserPath.quadTo(mx, my, x, y);
            //遍历笔迹
            for (int i = 0; i < SaveShapeList.size(); i++) {
                //判断进入对应的矩形
                if (SaveShapeList.get(i).isEnterShapeEdge(x, y)) {
                    System.out.println("----------->enter");
                    //判断是否发生相交
                    if (SaveShapeList.get(i).isInterSect(mx, my, x, y)) {
                        System.out.println("------------>isInterSect");
                        //记录当前shape的position
                        SweepList.add(i);
                    }
                }
            }
        } else if (MoveShapeState) {

            if (Selected) {
//                if(isMoving==true) {
//                    for (int k = 0; k < NeedMoveList.size(); k++) {
//                        //取出
//                        Shape shape = NeedMoveList.get(k);
//                        for (int j = 0; j < shape.getPointList().size(); j++) {
//                            //修改坐标
//                            float movex = shape.getPointList().get(j).getX() + (x - mx);
//                            float movey = shape.getPointList().get(j).getY() + (y - my);
//                            //保存坐标
//                            shape.getPointList().set(j, new Point(movex, movey));
//                        }
//                        //替换SaveList中的对应shape
//                        SaveShapeList.set(MoveList.get(k), shape);
//                    }
//                }
            } else {
                if (MovePath != null) {
                    MovePath.quadTo(mx, my, x, y);
                }
                //遍历笔迹
                for (int i = 0; i < SaveShapeList.size(); i++) {
                    //判断进入对应的矩形
                    if (SaveShapeList.get(i).isEnterShapeEdge(x, y)) {
                        System.out.println("----------->enter");
                        //判断是否发生相交
                        if (SaveShapeList.get(i).isInterSect(mx, my, x, y)) {
                            System.out.println("------------>isInterSect");
                            //记录当前shape的position
                            MoveList.add(i);
                        }
                    }
                }
            }
        } else {
            //执行相关操作
            currentShape.touchMove(mx, my, x, y);
        }
        //记录当前坐标点
        mx = x;
        my = y;
    }

    /**
     * 点击区域不在对应范围内
     *
     * @return
     */
    private boolean IsNotInside(float x, float y) {
        if (NeedMoveRect != null) {
            if (x >= NeedMoveRect.left && x <= NeedMoveRect.right && y >= NeedMoveRect.bottom && y <= NeedMoveRect.top)
                return true;
        }
        return false;
    }

    /**
     * 抬起操作对应处理
     */
    private void touchUp(float x, float y) {

        if (EraserState) {
            EraserPath.lineTo(x, y);
            EraserPath = null;
            if (SweepList.size() != 0) {
                //删除选中的shape
                for (int i = 0; i < SweepList.size(); i++) {
                    //根据下标取出对象
                    NeedHandleList.add(SaveShapeList.get(SweepList.get(i)));
                }
                //遍历对象依次删除
                for (int j = 0; j < NeedHandleList.size(); j++) {
                    Shape deleteObject = NeedHandleList.get(j);
                    Iterator<Shape> it = SaveShapeList.iterator();
                    while (it.hasNext()) {
                        Shape i = it.next();
                        if (i == deleteObject) {
                            //删除的笔迹放入DeleteList
                            DeleteShapeList.add(i);
                            it.remove();
                        }
                    }
                }
            }
            //相关参数清空
            SweepList = null;
            NeedHandleList = null;
            System.out.println(SaveShapeList.size());
            System.out.println(DeleteShapeList.size());
            //通知系统重绘
            Message msg = new Message();
            msg.what = REDRAW;
            handler.sendMessageDelayed(msg, 100);
        } else if (MoveShapeState) {
            if (Selected) {
                if (NeedMoveList == null)
                    Selected = false;
                redrawOnBitmap();
            } else {
                MovePath.lineTo(x, y);
                if (MoveList.size() != 0) {
                    //删除选中的shape
                    for (int i = 0; i < MoveList.size(); i++) {
                        //根据下标取出对象
                        NeedMoveList.add(SaveShapeList.get(MoveList.get(i)));
                    }
                    //遍历找到笔迹最大的Rect区域
                    NeedMoveRect = findBiggestRect(NeedMoveList);

                }
                //相关参数清空
                MovePath = null;
                //设置为已选中状态
                Selected = true;
                //现在NeedMoveList中有保存对应笔迹.NeedMoveRect不为空.MoveList也保存有笔迹对应下标

            }
            //通知系统重绘
            Message msg = new Message();
            msg.what = REDRAW;
            handler.sendMessageDelayed(msg, 100);

        } else {

            //执行相关操作
            currentShape.touchUp(x, y);
            //绘制到Bitmap上去
//            currentShape.draw(mCanvas);
            if (isBrushEnable && currentShape instanceof Ink) {
                mStokeBrushPen.draw(mCanvas);
            } else {
                currentShape.draw(mCanvas);
            }
            //保存终结点
            currentShape.addPoint(x, y);
            //将笔迹添加到栈中
            SaveShapeList.add(currentShape);
            //对象置空
            currentShape = null;
        }

//        System.out.println("MoveSate:"+MoveShapeState);
//        System.out.println("Selected:"+Selected);
//        System.out.println("EraserState:"+EraserState);

        System.out.println("NeedMoveRect:" + NeedMoveRect);
        System.out.println("MoveList:" + MoveList);
        System.out.println("NeedMoveRect:" + NeedMoveRect);
        System.out.println("Selected:" + Selected);
        System.out.println("MoveShapeState:" + MoveShapeState);
    }

    /**
     * 找到要移动的区域
     *
     * @param needMoveList
     * @return
     */
    private RectF findBiggestRect(List<Shape> needMoveList) {
        float minx = needMoveList.get(0).getPointList().get(0).getX();
        float miny = needMoveList.get(0).getPointList().get(0).getY();
        float maxx = needMoveList.get(0).getPointList().get(0).getX();
        float maxy = needMoveList.get(0).getPointList().get(0).getY();
        for (int k = 0; k < needMoveList.size(); k++) {
            List<Point> pointList = needMoveList.get(k).getPointList();
            for (int i = 1; i < pointList.size(); i++) {
                if (maxx < pointList.get(i).getX())
                    maxx = pointList.get(i).getX();
                if (minx > pointList.get(i).getX())
                    minx = pointList.get(i).getX();
                if (maxy < pointList.get(i).getY())
                    maxy = pointList.get(i).getY();
                if (miny > pointList.get(i).getY())
                    miny = pointList.get(i).getY();
            }
        }
        System.out.println(minx + "----" + maxy + "----" + maxx + "------" + miny);
        RectF rect = new RectF(minx, maxy, maxx, miny);
        return rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mBitmap, 0, 0, null);
        if (currentShape != null) {
            if (isBrushEnable && currentShape instanceof Ink) {
                mStokeBrushPen.draw(canvas);
            } else {
                currentShape.draw(canvas);
            }
        }
        if (EraserState) {
            if (EraserPath != null) {
                canvas.drawPath(EraserPath, mPaint);
            }
        }
        if (MoveShapeState) {
            if (MovePath != null) {
                canvas.drawPath(MovePath, mPaint);
            }
        }
    }


    /**
     * 撤销操作
     */
    public void Undo() {
        if (SaveShapeList != null && SaveShapeList.size() >= 1) {
            DeleteShapeList.add(SaveShapeList.get(SaveShapeList.size() - 1));
            SaveShapeList.remove(SaveShapeList.size() - 1);
            redrawOnBitmap();//重新绘制图案
        }

    }

    /**
     * 重做操作
     */
    public void Redo() {
        if (DeleteShapeList != null && DeleteShapeList.size() >= 1) {
            SaveShapeList.add(DeleteShapeList.get(DeleteShapeList.size() - 1));
            DeleteShapeList.remove(DeleteShapeList.size() - 1);
            redrawOnBitmap();//重新绘制图案
        }

    }

    /**
     * 加载之前的画
     *
     * @param filename
     */
    public void loadPreImage(String filename) {
        IsFileExist = true;
        //遍历文件夹下每个xml文件
        File scanFilePath = new File(filename);
        if (scanFilePath.isDirectory()) {
            for (File file : scanFilePath.listFiles()) {
                String fileAbsolutePath = file.getAbsolutePath();
                System.out.println(fileAbsolutePath);
                if (fileAbsolutePath.endsWith(".json")) {
                    //将json解析放入mGallery中
                    try {
                        mGallery.addPainting(
                                JsonOperation.transJsonToShape(fileAbsolutePath),
                                mBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            //添加名字属性
            String name = filename.substring(filename.lastIndexOf("/") + 1);
            mGallery.setName(name);
            //修改对应页数相关
            CurrentPageNum = mGallery.getNum();
            //载入当前第一页内容
            SaveShapeList.clear();
            SaveShapeList.addAll(mGallery.getPaintingList().get(0));
            System.out.println("载入成功");
            System.out.println("笔迹个数" + SaveShapeList.size());
//            redrawOnBitmap();
        }
    }

    public Canvas getCanvas() {
        return mCanvas;
    }

    /**
     * ************************************
     * getter and setter methond
     * <p/>
     * ************************************
     */

    public int getCurrentPageNum() {
        return CurrentPageNum;
    }

    public void setCurrentPageNum(int currentPageNum) {
        CurrentPageNum = currentPageNum;
    }

    public int getCurrentPageIndex() {
        return CurrentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        CurrentPageIndex = currentPageIndex;
    }

    /**
     * 设置笔迹宽度
     */
    public void setBrushSize(float brushSize) {
        currentWidth = brushSize;
        mPaint.setStrokeWidth(brushSize);
        mStokeBrushPen.setPaint(mPaint);
    }

    /**
     * 获取笔迹宽度
     *
     * @return
     */
    public float getBrushSize() {
        return mPaint.getStrokeWidth();
    }

    /**
     * 设置笔迹颜色
     *
     * @param color
     */
    public void setBrushColor(String color) {
        currentColor = color;
        mPaint.setColor(Color.parseColor(currentColor));
        mStokeBrushPen.setPaint(mPaint);
    }

    /**
     * 获取笔迹颜色
     *
     * @return
     */
    public String getBrushColor() {
        return currentColor;
    }

    /**
     * 设置笔迹类型
     *
     * @param currentKind
     */
    public void setCurrentKind(int currentKind) {
        this.currentKind = currentKind;
        switch (currentKind) {
            case Constants.INK:
                mStokeBrushPen = new SteelPen();
                break;
        }
        //设置
        if (mStokeBrushPen.isNullPaint()) {
            mStokeBrushPen.setPaint(mPaint);
        }
    }

    /**
     * 获取笔迹类型
     *
     * @return
     */
    public int getCurrentKind() {
        return currentKind;
    }

    /**
     * 获取SaveList
     *
     * @return
     */
    public List<Shape> getSaveShapeList() {
        return SaveShapeList;
    }

    /**
     * 设置SaveList
     *
     * @param saveShapeList
     */
    public void setSaveShapeList(List<Shape> saveShapeList) {
        SaveShapeList = saveShapeList;
    }

    /**
     * 获取Bitmap
     *
     * @return
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * 设置Bitmap
     *
     * @param mBitmap
     */
    public void setBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    /**
     * 设置画布宽度
     *
     * @param canvasWidth
     */
    public void setCanvasWidth(int canvasWidth) {
        this.canvasWidth = canvasWidth;
    }

    /**
     * 设置画布高度
     *
     * @param canvasHeight
     */
    public void setCanvasHeight(int canvasHeight) {
        this.canvasHeight = canvasHeight;
    }

    public Gallery getGallery() {
        return mGallery;
    }

    public void setGallery(Gallery mGallery) {
        this.mGallery = mGallery;
    }
    /**
     * **************************************
     * other methond
     *
     * **************************************
     */


    /**
     * 重新绘制Bitmap上的图案
     */
    public void redrawOnBitmap() {
        System.out.println("------------------->redraw");
        // 重新设置画布，相当于清空画布
        initCanvas();
        //依次遍历，绘制对应图案
        for (int i = 0; i < SaveShapeList.size(); i++) {
            Shape shape = SaveShapeList.get(i);
//            shape.draw(mCanvas);
            if (isBrushEnable && shape instanceof Ink) {
                for (int j = 0; j < shape.getPointList().size(); j++) {
                    Point point = shape.getPointList().get(j);
                    if (j == 0) {
                        mStokeBrushPen.onDown(new MotionElement(point.getX(), point.getY()), mCanvas);
                    } else if (j == shape.getPointList().size() - 1) {
                        mStokeBrushPen.onUp(new MotionElement(point.getX(), point.getY()), mCanvas);
                    } else {
                        mStokeBrushPen.onMove(new MotionElement(point.getX(), point.getY()), mCanvas);
                    }
                }
            } else {
                shape.draw(mCanvas);
            }
            //字体大小和颜色根据最后一个shape决定
            if (i == SaveShapeList.size() - 1) {
                setBrushSize(shape.getPaint().getStrokeWidth());
                setBrushColor(String.format("#%06X", 0xFFFFFF & shape.getPaint().getColor()));
            }
        }
        if (MoveShapeState) {
            if (NeedMoveList != null) {
                PathEffect effects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);//设置虚线的间隔和点的长度
                Paint newPaint = new Paint();
                newPaint.setPathEffect(effects);
                newPaint.setColor(Color.parseColor(Constants.colors[1]));
                newPaint.setStyle(Paint.Style.STROKE);
                mCanvas.drawRect(NeedMoveRect.left, NeedMoveRect.top, NeedMoveRect.right, NeedMoveRect.bottom, newPaint);
            }
        }
        invalidate();
    }

    /**
     * 判断是否有内容
     *
     * @return
     */
    public boolean isEmpty() {
        if (SaveShapeList.size() == 0)
            return true;
        else
            return false;
    }

    /**
     * 判断是否是在已存在文件上编辑
     *
     * @return
     */
    public boolean isFileExist() {

        return IsFileExist;
    }

    /**
     * 判断是否发生了编辑
     *
     * @return
     */
    public boolean isEdited() {
        if (PreShapeListSize == SaveShapeList.size()) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * 绘制新的图形
     */
    public void drawNewImage() {
        if (mGallery.getNum() != CurrentPageNum) {
            //保存当前笔迹集合及Bitmap
            mGallery.addPainting(SaveShapeList, mBitmap);

        }
        SaveShapeList.clear();
        DeleteShapeList.clear();
        //清空画布及相关数据
        initCanvas();

        invalidate();
    }

    /**
     * 返回到上一页
     */
    public void turnToPrePage() {

        //刚好处于最后一页要往前翻
        if (mGallery.getNum() == CurrentPageNum - 1) {
            //保存当前图形
            mGallery.addPainting(SaveShapeList, mBitmap);
        } else {
            //覆盖当前图形
            mGallery.coverPainting(SaveShapeList, mBitmap, CurrentPageIndex);
        }
        //清空画布及相关数据
        initCanvas();
        SaveShapeList.clear();
        DeleteShapeList.clear();
        //加载上一页内容
        SaveShapeList.addAll(mGallery.getPaintingList().get(CurrentPageIndex - 1));
        redrawOnBitmap();
    }

    /**
     * 跳转下一页
     */
    public void turnToNextPage() {

        //覆盖当前图形
        mGallery.coverPainting(SaveShapeList, mBitmap, CurrentPageIndex - 2);
        //清空画布及相关数据
        initCanvas();
        SaveShapeList.clear();
        DeleteShapeList.clear();
        //加载下一页内容
        SaveShapeList.addAll(mGallery.getPaintingList().get(CurrentPageIndex - 1));
        redrawOnBitmap();
    }

    public void clear() {
        //清空画布及相关数据
        initCanvas();
        SaveShapeList.clear();
        DeleteShapeList.clear();
        invalidate();
    }

    /**
     * 判断是否需要保存最后一张并处理
     */
    public void saveTheLast() {
        if (mGallery.getNum() == CurrentPageNum - 1) {
            mGallery.addPainting(SaveShapeList, mBitmap);
        } else {
            mGallery.coverPainting(SaveShapeList, mBitmap, CurrentPageIndex - 1);
        }
    }

    /**
     * 修改橡皮擦状态
     */
    public void changeEraserState() {
        if (MoveShapeState == true) {
            MoveShapeState = false;
        }
        EraserState = !EraserState;
        System.out.println(EraserState);
    }

    public boolean getEraserState() {
        return EraserState;
    }

    /**
     * 修改笔迹相关操作状态
     */
    public void ChangeCutState() {
        if (EraserState == true) {
            EraserState = false;
        }
        MoveShapeState = !MoveShapeState;
        System.out.println(MoveShapeState);
    }

    public boolean getMoveShapeState() {
        return MoveShapeState;
    }

    public boolean isFingerEnable() {
        return isFingerEnable;
    }

    public void setFingerEnable(boolean fingerEnable) {
        isFingerEnable = fingerEnable;
    }

    public boolean isBrushEnable() {
        return isBrushEnable;
    }

    public void setBrushEnable(boolean brushEnable) {
        isBrushEnable = brushEnable;
    }

    public void setFileExist(boolean fileExist) {
        IsFileExist = fileExist;
    }
}
