package com.seewo.palette.util;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

/**
 * 存储操作类
 */
public class StoreOperation {

    private static final String parentPath = "palette";

    /**
     * 获取Json文件存储名字
     *
     * @return
     */
    public static String getJsonFileName(String dirName, String name) {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //自己命名名字
            String filename = getFileStorePath(dirName) + "/" + name + ".json";
            return filename;
        } else {
            return null;
        }
    }

    /**
     * 获取文件夹
     *
     * @return
     */
    public static String getFileStorePath(String name) {
        String filepath = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //获得完整的保存路径
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + parentPath + "/" + name);
            if (!file.exists()) {
                file.mkdirs();
            }
            filepath = file.getAbsolutePath();
        }
        return filepath;
    }

}
