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
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
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
 * 绘制的画布
 */
public class PaintView extends View {

    private float currentWidth = 15; //默认字体大小
    private String currentColor = Constants.colors[0]; //默认字体颜色
    private int currentKind = Constants.INK; //默认绘图类型

    private int currentPageNum = 1;//默认当前页数
    private int currentPageIndex = 1;//默认当前页序号

    private Paint mPaint;//画笔
    private Bitmap mBitmap;//画布的bitmap
    private Canvas mCanvas;//画布

    int canvasWidth; //画布的宽
    int canvasHeight;//画布的高
    float mx, my; //当前画笔位置

    List<Shape> saveShapeList; //已保存笔迹List
    List<Shape> deleteShapeList; //删除笔迹List

    Gallery mGallery;//画册类

    private BasePen mStokeBrushPen;

    private boolean isFileExist = false;//是否有编辑过标志
    private final int preShapeListSize = 0; //最初list的长度

    private boolean eraserState = false;//是否处于橡皮擦状态
    private boolean moveShapeState = false;//是否处于笔迹操作状态

    private boolean selected = false;//已选中状态

    private final int REDRAW = 0;

    private IPaintCallback iPaintCallback;

    Handler handler = new Handler(Looper.getMainLooper()) {
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
        saveShapeList = new ArrayList<>();
        deleteShapeList = new ArrayList<>();
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
        if (!eraserState) {
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
        if (!eraserState) {
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
    Path eraserPath;
    Path movePath;

    /**
     * 按下操作对应处理
     *
     * @param x
     * @param y
     */
    private void touchStart(MotionEvent event, float x, float y) {
        //进入橡皮擦模式
        if (eraserState) {
            //准备画虚线需要的相关属性
            PathEffect effects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);//设置虚线的间隔和点的长度
            mPaint.setPathEffect(effects);
            mPaint.setColor(Color.parseColor(Constants.colors[1]));
            sweepList = new ArrayList<>();
            needHandleList = new ArrayList<>();
            //创建橡皮擦的path
            eraserPath = new Path();
            eraserPath.moveTo(x, y);
        }
        //进入选择线条模式
        else if (moveShapeState) {
            if (selected) {
                //判断点击的地方是否是在NeedRect内部如果
                if (!IsNotInside(x, y)) {//不在范围内，相关参数清零从头开始
                    needMoveRect = null;
                    moveList = null;
                    needMoveList = null;
                    System.out.println("NeedMoveRect:" + needMoveRect);
                    System.out.println("MoveList:" + moveList);
                    System.out.println("NeedMoveRect:" + needMoveRect);
                    System.out.println("Selected:" + selected);
                    System.out.println("MoveShapeState:" + moveShapeState);
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

                moveList = new ArrayList<>();
                needMoveList = new ArrayList<>();
                //创建选中笔迹的path
                movePath = new Path();
                movePath.moveTo(x, y);
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
            long time = SystemClock.elapsedRealtime();
            currentShape.touchDown(x, y, time);
            if (isBrushEnable && currentShape instanceof Ink) {
                mStokeBrushPen.onDown(mStokeBrushPen.createMotionElement(event), mCanvas);
            }
            if(iPaintCallback != null) {
                iPaintCallback.touchDown(event, x, y, time);
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

    List<Integer> sweepList;
    List<Shape> needHandleList;

    List<Integer> moveList;
    List<Shape> needMoveList;
    RectF needMoveRect;

    /**
     * 移动操作对应处理
     */
    private void touchMove(float x, float y) {

        if (eraserState) {
            eraserPath.quadTo(mx, my, x, y);
            //遍历笔迹
            for (int i = 0; i < saveShapeList.size(); i++) {
                //判断进入对应的矩形
                if (saveShapeList.get(i).isEnterShapeEdge(x, y)) {
                    System.out.println("----------->enter");
                    //判断是否发生相交
                    if (saveShapeList.get(i).isInterSect(mx, my, x, y)) {
                        System.out.println("------------>isInterSect");
                        //记录当前shape的position
                        sweepList.add(i);
                    }
                }
            }
        } else if (moveShapeState) {

            if (selected) {
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
//                        saveShapeList.set(MoveList.get(k), shape);
//                    }
//                }
            } else {
                if (movePath != null) {
                    movePath.quadTo(mx, my, x, y);
                }
                //遍历笔迹
                for (int i = 0; i < saveShapeList.size(); i++) {
                    //判断进入对应的矩形
                    if (saveShapeList.get(i).isEnterShapeEdge(x, y)) {
                        System.out.println("----------->enter");
                        //判断是否发生相交
                        if (saveShapeList.get(i).isInterSect(mx, my, x, y)) {
                            System.out.println("------------>isInterSect");
                            //记录当前shape的position
                            moveList.add(i);
                        }
                    }
                }
            }
        } else {
            //执行相关操作
            long time = SystemClock.elapsedRealtime();
            currentShape.touchMove(mx, my, x, y, time);
            if(iPaintCallback != null) {
                iPaintCallback.touchMove(mx, my, x, y, time);
            }
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
        if (needMoveRect != null) {
            if (x >= needMoveRect.left && x <= needMoveRect.right && y >= needMoveRect.bottom && y <= needMoveRect.top)
                return true;
        }
        return false;
    }

    /**
     * 抬起操作对应处理
     */
    private void touchUp(float x, float y) {

        if (eraserState) {
            eraserPath.lineTo(x, y);
            eraserPath = null;
            if (sweepList.size() != 0) {
                //删除选中的shape
                for (int i = 0; i < sweepList.size(); i++) {
                    //根据下标取出对象
                    needHandleList.add(saveShapeList.get(sweepList.get(i)));
                }
                //遍历对象依次删除
                for (int j = 0; j < needHandleList.size(); j++) {
                    Shape deleteObject = needHandleList.get(j);
                    Iterator<Shape> it = saveShapeList.iterator();
                    while (it.hasNext()) {
                        Shape i = it.next();
                        if (i == deleteObject) {
                            //删除的笔迹放入DeleteList
                            deleteShapeList.add(i);
                            it.remove();
                        }
                    }
                }
            }
            //相关参数清空
            sweepList = null;
            needHandleList = null;
            System.out.println(saveShapeList.size());
            System.out.println(deleteShapeList.size());
            //通知系统重绘
            Message msg = new Message();
            msg.what = REDRAW;
            handler.sendMessageDelayed(msg, 100);
        } else if (moveShapeState) {
            if (selected) {
                if (needMoveList == null)
                    selected = false;
                redrawOnBitmap();
            } else {
                movePath.lineTo(x, y);
                if (moveList.size() != 0) {
                    //删除选中的shape
                    for (int i = 0; i < moveList.size(); i++) {
                        //根据下标取出对象
                        needMoveList.add(saveShapeList.get(moveList.get(i)));
                    }
                    //遍历找到笔迹最大的Rect区域
                    needMoveRect = findBiggestRect(needMoveList);

                }
                //相关参数清空
                movePath = null;
                //设置为已选中状态
                selected = true;
                //现在NeedMoveList中有保存对应笔迹.NeedMoveRect不为空.MoveList也保存有笔迹对应下标

            }
            //通知系统重绘
            Message msg = new Message();
            msg.what = REDRAW;
            handler.sendMessageDelayed(msg, 100);

        } else {
            //执行相关操作
            long time = SystemClock.elapsedRealtime();
            currentShape.touchUp(x, y, time);
            if(iPaintCallback != null) {
                iPaintCallback.touchUp(x, y, time);
            }
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
            saveShapeList.add(currentShape);
            //对象置空
            currentShape = null;
        }

//        System.out.println("MoveSate:"+MoveShapeState);
//        System.out.println("Selected:"+Selected);
//        System.out.println("EraserState:"+EraserState);

        System.out.println("NeedMoveRect:" + needMoveRect);
        System.out.println("MoveList:" + moveList);
        System.out.println("NeedMoveRect:" + needMoveRect);
        System.out.println("Selected:" + selected);
        System.out.println("MoveShapeState:" + moveShapeState);
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
        if (eraserState) {
            if (eraserPath != null) {
                canvas.drawPath(eraserPath, mPaint);
            }
        }
        if (moveShapeState) {
            if (movePath != null) {
                canvas.drawPath(movePath, mPaint);
            }
        }
    }


    /**
     * 撤销操作
     */
    public void undo() {
        if (saveShapeList != null && saveShapeList.size() >= 1) {
            deleteShapeList.add(saveShapeList.get(saveShapeList.size() - 1));
            saveShapeList.remove(saveShapeList.size() - 1);
            redrawOnBitmap();//重新绘制图案
            if(iPaintCallback != null) {
                iPaintCallback.undo();
            }
        }

    }

    /**
     * 重做操作
     */
    public void redo() {
        if (deleteShapeList != null && deleteShapeList.size() >= 1) {
            saveShapeList.add(deleteShapeList.get(deleteShapeList.size() - 1));
            deleteShapeList.remove(deleteShapeList.size() - 1);
            redrawOnBitmap();//重新绘制图案
            if(iPaintCallback != null) {
                iPaintCallback.redo();
            }
        }

    }

    /**
     * 加载之前的画
     *
     * @param filename
     */
    public void loadPreImage(String filename) {
        isFileExist = true;
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
            currentPageNum = mGallery.getNum();
            //载入当前第一页内容
            saveShapeList.clear();
            saveShapeList.addAll(mGallery.getPaintingList().get(0));
            System.out.println("载入成功");
            System.out.println("笔迹个数" + saveShapeList.size());
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
        return currentPageNum;
    }

    public void setCurrentPageNum(int currentPageNum) {
        this.currentPageNum = currentPageNum;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        this.currentPageIndex = currentPageIndex;
    }

    public void setPaintCallback(IPaintCallback callback) {
        iPaintCallback = callback;
    }

    /**
     * 设置笔迹宽度
     */
    public void setBrushSize(float brushSize) {
        currentWidth = brushSize;
        mPaint.setStrokeWidth(brushSize);
        mStokeBrushPen.setPaint(mPaint);
        if(iPaintCallback != null) {
            iPaintCallback.changeBrushSize(brushSize);
        }
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
        if(iPaintCallback != null) {
            iPaintCallback.changeBrushColor(color);
        }
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
        if(iPaintCallback != null) {
            iPaintCallback.changeKind(currentKind);
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
        return saveShapeList;
    }

    /**
     * 设置SaveList
     *
     * @param saveShapeList
     */
    public void setSaveShapeList(List<Shape> saveShapeList) {
        this.saveShapeList = saveShapeList;
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
        for (int i = 0; i < saveShapeList.size(); i++) {
            Shape shape = saveShapeList.get(i);
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
            if (i == saveShapeList.size() - 1) {
                setBrushSize(shape.getPaint().getStrokeWidth());
                setBrushColor(String.format("#%06X", 0xFFFFFF & shape.getPaint().getColor()));
            }
        }
        if (moveShapeState) {
            if (needMoveList != null) {
                PathEffect effects = new DashPathEffect(new float[]{8, 8, 8, 8}, 1);//设置虚线的间隔和点的长度
                Paint newPaint = new Paint();
                newPaint.setPathEffect(effects);
                newPaint.setColor(Color.parseColor(Constants.colors[1]));
                newPaint.setStyle(Paint.Style.STROKE);
                mCanvas.drawRect(needMoveRect.left, needMoveRect.top, needMoveRect.right, needMoveRect.bottom, newPaint);
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
        if (saveShapeList.size() == 0)
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

        return isFileExist;
    }

    /**
     * 判断是否发生了编辑
     *
     * @return
     */
    public boolean isEdited() {
        if (preShapeListSize == saveShapeList.size()) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * 绘制新的图形
     */
    public void drawNewImage() {
        if (mGallery.getNum() != currentPageNum) {
            //保存当前笔迹集合及Bitmap
            mGallery.addPainting(saveShapeList, mBitmap);

        }
        saveShapeList.clear();
        deleteShapeList.clear();
        //清空画布及相关数据
        initCanvas();

        invalidate();
    }

    /**
     * 返回到上一页
     */
    public void turnToPrePage() {

        //刚好处于最后一页要往前翻
        if (mGallery.getNum() == currentPageNum - 1) {
            //保存当前图形
            mGallery.addPainting(saveShapeList, mBitmap);
        } else {
            //覆盖当前图形
            mGallery.coverPainting(saveShapeList, mBitmap, currentPageIndex);
        }
        //清空画布及相关数据
        initCanvas();
        saveShapeList.clear();
        deleteShapeList.clear();
        //加载上一页内容
        saveShapeList.addAll(mGallery.getPaintingList().get(currentPageIndex - 1));
        redrawOnBitmap();
    }

    /**
     * 跳转下一页
     */
    public void turnToNextPage() {

        //覆盖当前图形
        mGallery.coverPainting(saveShapeList, mBitmap, currentPageIndex - 2);
        //清空画布及相关数据
        initCanvas();
        saveShapeList.clear();
        deleteShapeList.clear();
        //加载下一页内容
        saveShapeList.addAll(mGallery.getPaintingList().get(currentPageIndex - 1));
        redrawOnBitmap();
    }

    public void clear() {
        //清空画布及相关数据
        initCanvas();
        saveShapeList.clear();
        deleteShapeList.clear();
        invalidate();
    }

    /**
     * 判断是否需要保存最后一张并处理
     */
    public void saveTheLast() {
        if (mGallery.getNum() == currentPageNum - 1) {
            mGallery.addPainting(saveShapeList, mBitmap);
        } else {
            mGallery.coverPainting(saveShapeList, mBitmap, currentPageIndex - 1);
        }
    }

    /**
     * 修改橡皮擦状态
     */
    public void changeEraserState() {
        if (moveShapeState == true) {
            moveShapeState = false;
        }
        eraserState = !eraserState;
        System.out.println(eraserState);
    }

    public boolean getEraserState() {
        return eraserState;
    }

    /**
     * 修改笔迹相关操作状态
     */
    public void changeCutState() {
        if (eraserState == true) {
            eraserState = false;
        }
        moveShapeState = !moveShapeState;
        System.out.println(moveShapeState);
    }

    public boolean getMoveShapeState() {
        return moveShapeState;
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
        isFileExist = fileExist;
    }
}
