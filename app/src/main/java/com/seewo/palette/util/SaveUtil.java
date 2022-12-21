package com.seewo.palette.util;

import android.os.Handler;
import android.os.Message;

import com.kath.paintboard.util.JsonOperation;
import com.kath.paintboard.widget.PaintView;

import java.io.IOException;

/**
 * Created by user on 2016/8/9.
 * 保存xml线程
 */
public class SaveUtil extends Thread {

    private Handler mHandler;
    private String mName;
    private PaintView mCanvas;

    public SaveUtil(Handler mHandler, String mName, PaintView mMycanvas) {
        this.mHandler = mHandler;
        this.mName = mName;
        this.mCanvas = mMycanvas;
    }

    @Override
    public void run() {
        super.run();
        //创建palette下的文件夹
        //向文件夹存储json
        for (int i = 0; i < mCanvas.getGallery().getPaintingList().size(); i++) {
            //创建json文件名
            String jsonfilename = StoreOperation.getJsonFileName(this.mName, i + "");
            try {
                //创建json文件
                JsonOperation.creatJson(mCanvas.getGallery().getPaintingList().get(i),
                        jsonfilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //向主线程发送信息
            Message msg = new Message();
            msg.what = Constants.MSG_EXIT;
            mHandler.sendMessage(msg);
        }
    }

}
