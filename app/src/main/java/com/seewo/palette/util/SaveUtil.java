package com.seewo.palette.util;

import android.os.Handler;
import android.os.Message;

import com.kath.paintboard.PaintConstants;
import com.kath.paintboard.util.JsonOperation;
import com.kath.paintboard.util.StoreOperation;
import com.kath.paintboard.widget.PaintView;

import java.io.IOException;

/**
 * 保存json线程
 */
public class SaveUtil extends Thread {

    private Handler mHandler;
    private String mName;
    private PaintView mCanvas;

    public SaveUtil(Handler mHandler, String mName, PaintView paintView) {
        this.mHandler = mHandler;
        this.mName = mName;
        this.mCanvas = paintView;
    }

    @Override
    public void run() {
        super.run();
        //向文件夹存储json
        for (int i = 0; i < mCanvas.getGallery().getPaintingList().size(); i++) {
            //创建json文件名
            String jsonFileName = StoreOperation.getJsonFileName(mCanvas.getContext(), this.mName, i + "", PaintConstants.savePrivate);
            try {
                //创建json文件
                JsonOperation.createJson(mCanvas.getGallery().getPaintingList().get(i),
                        jsonFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //向主线程发送信息
        Message msg = new Message();
        msg.what = Constants.MSG_EXIT;
        mHandler.sendMessage(msg);
    }

}
