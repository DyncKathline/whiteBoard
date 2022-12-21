package com.seewo.palette.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.kath.paintboard.play.PlayHelper;
import com.kath.paintboard.widget.PaintView;
import com.seewo.palette.R;
import com.seewo.palette.callback.ColorCallBack;
import com.seewo.palette.callback.PageChangeCall;
import com.seewo.palette.callback.ShapeChangeCall;
import com.seewo.palette.callback.SizeChangeCall;
import com.seewo.palette.save.SaveOperation;
import com.seewo.palette.save.SavePngOperation;
import com.seewo.palette.save.SaveSvgOperation;
import com.seewo.palette.ui.ChooseUiManager;
import com.seewo.palette.ui.FootUiManager;
import com.seewo.palette.ui.SaveMenuManager;
import com.seewo.palette.ui.SwitchView;
import com.seewo.palette.util.Constants;
import com.seewo.palette.util.SaveUtil;

/**
 * Created by user on 2016/7/26.
 * 绘制图像的Activity
 */
public class PaintActivity extends AppCompatActivity implements View.OnClickListener, FootUiManager.SizeBtnOnclickListener, FootUiManager.ColorBtnOnclickListener, FootUiManager.ShapeBtnClickListener, FootUiManager.PageBtnOnClickListener, SaveMenuManager.SaveBtnClickListener, FootUiManager.EraserBtnOnClickListener, FootUiManager.ShapeChooseBtnOnclickListener {

    TextView mRecordTv;
    PaintView mPaintView;
    ImageView mExitBtn;
    ImageView mUndoBtn;
    ImageView mRedoBtn;
    ImageView mMoreBtn;
    SwitchView mSwitchView;
    LinearLayout mFootLayout;
    FootUiManager mFootUiManager;
    LinearLayout mSaveMenuLayout;
    SaveMenuManager mSaveMenuManager;

    boolean IsMenuShow = false;


    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Constants.MSG_EXIT)
                finish();
            if (msg.what == Constants.MSG_REDRAW) {
                mPaintView.redrawOnBitmap();
            }
        }
    };

    Handler handlertest = new Handler();
    String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paint_layout);
        //初始化相关绘图工具及画布
        mRecordTv = findViewById(R.id.id_recording);
        mPaintView = findViewById(R.id.id_paint_view);
        mExitBtn = findViewById(R.id.id_exit_btn);
        mUndoBtn = findViewById(R.id.id_undo_btn);
        mRedoBtn = findViewById(R.id.id_redo_btn);
        mMoreBtn = findViewById(R.id.id_save_btn);
        mSwitchView = findViewById(R.id.id_switch_view);

        mFootLayout = findViewById(R.id.id_foot_layout);
        mChooseLayout = findViewById(R.id.id_choose_layout);
        mSaveMenuLayout = findViewById(R.id.save_menu_layout);
        mSaveMenuManager = new SaveMenuManager(this, mSaveMenuLayout);
        mFootUiManager = new FootUiManager(this, mFootLayout);
        mChooseUiManager = new ChooseUiManager(this, mChooseLayout);

        mRecordTv.setVisibility(View.GONE);

        //TODO 装载之前的画，有就装载没有就不做处理,暂时以这种方式
        handlertest.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                filename = intent.getStringExtra("openfilename");
                if (filename != null) {
                    mSwitchView.setChecked(false);
//                    mPaintView.loadPreImage(filename);
//                    mPaintView.redrawOnBitmap();

                    PlayHelper playHelper = new PlayHelper(mPaintView);
                    playHelper.setPlayListener(new PlayHelper.PlayListener() {
                        @Override
                        public void onStart() {
                            mRecordTv.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onEnd(boolean flag) {
                            mRecordTv.setVisibility(View.GONE);
                        }
                    });
                    playHelper.start(filename);
                }
            }
        }, 500);
        //相关控件事件事件监听
        initEvent();
    }

    /**
     * 初始化监听事件
     */
    private void initEvent() {
        mExitBtn.setOnClickListener(this);
        mUndoBtn.setOnClickListener(this);
        mRedoBtn.setOnClickListener(this);
        mMoreBtn.setOnClickListener(this);

        mFootUiManager.setSizeBtnListener(this);//选择画笔大小事件监听
        mFootUiManager.setColorBtnOnclickListener(this);//选择画笔颜色事件监听
        mFootUiManager.setShapeBtnClickListener(this);//选择形状事件监听
        mFootUiManager.setPageBtnOnClickListener(this);//加页监听
        mFootUiManager.setEraserBtnOnClickListener(this);//橡皮擦监听
        mFootUiManager.setShapeChooseBtnOnclickListener(this);//笔迹操作监听
        mSaveMenuManager.setSaveBtnClickListener(this); //保存监听

        mSwitchView.setOnClickCheckedListener(new SwitchView.onClickCheckedListener() {
            @Override
            public void onClick() {
                mPaintView.setBrushEnable(mSwitchView.isChecked());
                mPaintView.redrawOnBitmap();
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_exit_btn:
                //退出保存操作
                ExitLogic();
                break;
            case R.id.id_undo_btn:
                //撤销操作
                mPaintView.undo();
                break;
            case R.id.id_redo_btn:
                //重做操作
                mPaintView.redo();
                break;
            case R.id.id_save_btn:
                //另存为相关操作
                ShowMenuLogic();
                break;
        }

    }

    /**
     * 菜单显示逻辑
     */
    private void ShowMenuLogic() {
        if (IsMenuShow) {
            mSaveMenuLayout.setVisibility(View.VISIBLE);
        } else {
            mSaveMenuLayout.setVisibility(View.GONE);
        }
        IsMenuShow = !IsMenuShow;
    }

    /**
     * 退出逻辑执行
     */
    private void ExitLogic() {
        //判断是否有内容
        if (!mPaintView.isEmpty()) {
            //判断是否处于已存在的文件
            if (mPaintView.isFileExist()) {
                //是否发生了修改
                if (mPaintView.isEdited()) {
                    //保存最后一张
                    mPaintView.saveTheLast();
                    //进入覆盖保存
                    ChooseCoverOrNot();
                } else {
                    //直接退出
                    finish();
                }
            } else {
                //保存最后一张
                mPaintView.saveTheLast();
                //进入选择名字保存
                ChooseSaveOrNot();
            }
        } else {
            finish();
        }

    }

    /**
     * 选择是否覆盖已存在图片
     */
    private void ChooseCoverOrNot() {
        new AlertDialog.Builder(this)
                .setMessage("是否覆盖当前图片")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message msg = new Message();
                        msg.what = Constants.MSG_EXIT;
                        handler.sendMessage(msg);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String pathname = mPaintView.getGallery().getName();
                        SaveUtil suThread = new SaveUtil(handler, pathname, mPaintView);
                        suThread.start();
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 是否选择保存绘制图案
     *
     * @return
     */
    private void ChooseSaveOrNot() {

        final EditText et = new EditText(PaintActivity.this);
        new AlertDialog.Builder(this)
                .setMessage("是否保存？")
                .setView(et)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (et.getText().toString().equals("")) {
                            Toast.makeText(PaintActivity.this, "请输入保存文件的名字", Toast.LENGTH_SHORT).show();
                        } else {
                            //输入的名字作为文件夹的名字
                            String name = et.getText().toString();
                            //TODO 开启子线程处理保存相关
                            SaveUtil suThread = new SaveUtil(handler, name, mPaintView);
                            suThread.start();
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Message msg = new Message();
                        msg.what = Constants.MSG_EXIT;
                        handler.sendMessage(msg);
                        dialog.dismiss();
                    }
                }).show();
    }

    LinearLayout mChooseLayout;
    ChooseUiManager mChooseUiManager;

    /**
     * 画笔粗细点击事件
     */
    @Override
    public void Clicked(boolean isShow) {
        if (isShow) {
            mChooseLayout.removeAllViews();
            mChooseLayout.setVisibility(View.GONE);
        } else {
            mChooseLayout.setVisibility(View.VISIBLE);
            mChooseUiManager.ShowSizeUi(mPaintView.getBrushSize());
            mChooseUiManager.setSizeCallback(new SizeChangeCall() {
                @Override
                public void callBySizeChange(float size) {
                    mPaintView.setBrushSize(size);
                }
            });
        }
    }

    /**
     * 颜色点击事件
     *
     * @param isShow
     */
    @Override
    public void ColorClicked(boolean isShow) {
        if (isShow) {
            mChooseLayout.removeAllViews();
            mChooseLayout.setVisibility(View.GONE);
        } else {
            mChooseLayout.setVisibility(View.VISIBLE);
            mChooseUiManager.ShowColorUi(mPaintView.getBrushColor());
            mChooseUiManager.setColorCallBack(new ColorCallBack() {
                @Override
                public void setChangeColor(String color) {
                    mPaintView.setBrushColor(color);
                }
            });
        }
    }


    /**
     * 图形点击事件
     *
     * @param isShow
     */
    @Override
    public void onShapeBtnClicked(boolean isShow) {
        if (isShow) {
            mChooseLayout.removeAllViews();
            mChooseLayout.setVisibility(View.GONE);
        } else {
            mChooseLayout.setVisibility(View.VISIBLE);
            mChooseUiManager.ShowShapeUi(mPaintView.getCurrentKind());
            mChooseUiManager.setShapeChangeCall(new ShapeChangeCall() {
                @Override
                public void CallByShapeChange(int kind) {
                    mPaintView.setCurrentKind(kind);
                }
            });
        }
    }

    /**
     * 多页点击事件
     *
     * @param isShow
     */
    @Override
    public void PageClicked(boolean isShow) {
        if (isShow) {
            mChooseLayout.removeAllViews();
            mChooseLayout.setVisibility(View.GONE);
        } else {
            mChooseLayout.setVisibility(View.VISIBLE);
            mChooseUiManager.ShowPageUi(mPaintView.getCurrentPageNum(), mPaintView.getCurrentPageIndex());
            //接口回调
            mChooseUiManager.setPageChangeCall(new PageChangeCall() {
                @Override
                public void PageAddCall(int pagenum, int pageindex) {
                    mPaintView.setCurrentPageNum(pagenum);
                    mPaintView.setCurrentPageIndex(pageindex);
                    mPaintView.drawNewImage();
                }

                @Override
                public void PagePreCall(int pageindex) {
                    mPaintView.setCurrentPageIndex(pageindex);
                    mPaintView.turnToPrePage();
                }

                @Override
                public void PageNextCall(int pageindex) {
                    mPaintView.setCurrentPageIndex(pageindex);
                    mPaintView.turnToNextPage();
                }
            });
        }
    }


    String saveFileName;
    SaveOperation so;

    /**
     * 另存点击事件
     *
     * @param saveKind
     */
    @Override
    public void SaveClick(int saveKind) {
        switch (saveKind) {
            case Constants.CLEAR:
                mPaintView.clear();
                return;
            case Constants.PNG:
                so = new SavePngOperation();
                break;
            case Constants.SVG:
                so = new SaveSvgOperation();
                break;
        }
        //获取需要保存的内容
        so.getContent(mPaintView);
        final EditText et = new EditText(PaintActivity.this);
        new AlertDialog.Builder(this)
                .setMessage("输入保存文件名")
                .setView(et)
                .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (et.getText().toString().equals("")) {
                            Toast.makeText(PaintActivity.this, "文件名不能为空", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                saveFileName = et.getText().toString();
                                //开启线程保存
                                new Thread(new MyPicSaveRunnable()).start();
                                Message msg = new Message();
                                msg.what = Constants.MSG_REDRAW;
                                handler.sendMessageDelayed(msg, 300);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 橡皮擦监听
     */
    @Override
    public boolean EraserClicked() {
        mPaintView.changeEraserState();
        return mPaintView.getEraserState();
    }

    /**
     * 笔迹操作监听
     */
    @Override
    public boolean ChooseOnClicked() {
        mPaintView.changeCutState();
        return mPaintView.getMoveShapeState();
    }


    class MyPicSaveRunnable implements Runnable {

        @Override
        public void run() {
            if (so != null) {
                so.setFilepath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/palette");
                so.setFilename(saveFileName);
                so.getAbsoluteFileName();
                so.savePainting();
            }
        }
    }


}
