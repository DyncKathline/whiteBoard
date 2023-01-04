package com.kath.paintboard.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

/**
 * 存储操作类
 * Example
 * <pre> {@code
 * //向文件夹存储json
 * for (int i = 0; i < mPaintView.getGallery().getPaintingList().size(); i++) {
 *   //创建json文件名
 *   String jsonFileName = StoreOperation.getJsonFileName(mPaintView.getContext(), this.mName, i + "", PaintConstants.savePrivate);
 *   try {
 *     //创建json文件
 *     JsonOperation.createJson(mPaintView.getGallery().getPaintingList().get(i), jsonFileName);
 *   } catch (IOException e) {
 *     e.printStackTrace();
 *   }
 * }
 * }</pre>
 */
public class StoreOperation {

    private static final String OBJECT_CACHE = "ObjectCache"; //缓存对象文件目录

    /**
     * 获取Json文件存储名字
     *
     * @return
     */
    public static String getJsonFileName(Context context, String dirName, String name, boolean isPrivate) {
        if (isPrivate) {
            //通过文件保存对象数据，通过清理app数据即可清理掉数据
            //目录:SDCard/Android/data/应用包名/data/files/ObjectCache
            File file = new File(context.getExternalFilesDir(OBJECT_CACHE).getAbsolutePath() + "/" + dirName);
            if (!file.exists()) {
                file.mkdirs();
            }
            String filename = file.getAbsolutePath() + "/" + name + ".json";
            return filename;
        } else {
            //目录:SDCard/Download/应用包名
            String filename = getFileStorePath(context, dirName) + "/" + name + ".json";
            return filename;
        }
    }

    /**
     * 获取文件夹
     *
     * @return
     */
    public static String getFileStorePath(Context context, String name) {
        String filepath = null;
        String parentPath = context.getPackageName();
        //获得完整的保存路径
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + parentPath + "/" + name);
        if (!file.exists()) {
            file.mkdirs();
        }
        filepath = file.getAbsolutePath();
        return filepath;
    }

    public static String getFileParentPath(Context context, boolean isPrivate) {
        if (isPrivate) {
            //目录:SDCard/Android/data/应用包名/data/files/ObjectCache
            return context.getExternalFilesDir(OBJECT_CACHE).getAbsolutePath();
        } else {
            String parentPath = context.getPackageName();
            //目录:SDCard/Download/应用包名
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + parentPath;
        }
    }

}
