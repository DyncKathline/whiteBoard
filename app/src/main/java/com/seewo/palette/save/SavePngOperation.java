package com.seewo.palette.save;

import android.graphics.Bitmap;
import android.os.Environment;

import com.kath.paintboard.widget.PaintView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 保存为png类
 */
public class SavePngOperation extends SaveOperation{

    Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void savePainting() {
        File file = new File(getAbsoluteFileName());
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)) {
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAbsoluteFileName() {
        String filepath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //获得完整的保存路径
            String fileAbsoluteName = this.filepath + "/" +filename+ ".png";
            File file = new File(fileAbsoluteName);
            if (!file.exists()) {
                file.mkdirs();
            }
            filepath = file.getAbsolutePath();
        }
        return filepath;
    }


    @Override
    public void getContent(PaintView paintView) {
        this.bitmap=Bitmap.createBitmap(paintView.getBitmap());
    }

}
