package com.seewo.palette;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.kath.paintboard.PaintConstants;
import com.qmai.crashlib.CrashHandler;
import com.qmai.crashlib.CrashListener;
import com.seewo.palette.activity.PaintActivity;
import com.seewo.palette.adapter.CommonAdapter;
import com.seewo.palette.adapter.ViewHolder;
import com.seewo.palette.util.PermissionUtil;
import com.kath.paintboard.util.StoreOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    GridView mGridview;
    ImageButton mImageButton;
    List<Bitmap> ImageList = new ArrayList<>();
    List<String> NameList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crash();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        requirePermission();
    }

    public void crash() {
        CrashHandler.Builder builder = new CrashHandler.Builder();
        builder.setMaxLogCount(10, true);
        builder.setOnCrashListener(new CrashListener() {
            @Override
            public void againStartApp() {

            }

            @Override
            public void recordException(Throwable ex) {
                Log.e("", "", ex);
            }
        });
        CrashHandler.getInstance().init(getApplication(), builder);
    }

    /**
     * 初始化数据
     */
    private void initDatas() {
        //从SD卡中读出图片数据
        File scanFilePath = new File(StoreOperation.getFileParentPath(this, PaintConstants.savePrivate));
        if (scanFilePath.isDirectory()) {
            for (File file : scanFilePath.listFiles()) {
                String filename = file.getAbsolutePath();
                if (file.isDirectory()) {
                    NameList.add(filename);
                }
            }
        }

    }

    /**
     * 初始化相关组件
     */
    private void initView() {

        initGridview();
        mImageButton = (ImageButton) findViewById(R.id.id_create_btn);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转绘图界面,传过去的值为null
                Intent intent = new Intent(MainActivity.this, PaintActivity.class);
                String message=null;
                intent.putExtra("openfilename",message);
                startActivity(intent);
            }
        });

    }

    /**
     * 初始化Gridview
     */
    private void initGridview() {
        //主要是完成缩略图的加载
        mGridview = (GridView) findViewById(R.id.id_gridview);
        mGridview.setAdapter(new CommonAdapter<String>(this, NameList, R.layout.gridview_item) {
            @Override
            protected void convert(ViewHolder holder, String item) {
                TextView tv=holder.getView(R.id.id_gallery_name);
                String name=item.substring(item.lastIndexOf("/")+1);
                tv.setText(name);
            }
        });

        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //取出当前对应文件夹名字
                String DirName=NameList.get(position);
                //跳转绘制界面之后实时加载，只是传文件名过去
                Intent intent = new Intent(MainActivity.this, PaintActivity.class);
                intent.putExtra("openfilename",DirName);
                startActivity(intent);
            }
        });
    }

    private void requirePermission() {
        PermissionUtil.getInstance().with(MainActivity.this).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, new PermissionUtil.PermissionListener() {
            @Override
            public void onGranted() {
                System.out.println("requirePermission onGranted");
                initDatas();
            }

            @Override
            public void onDenied(List<String> deniedPermission) {
                PermissionUtil.getInstance().showDialogTips(MainActivity.this, deniedPermission, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        activity.finish();
                    }
                });
            }

            @Override
            public void onShouldShowRationale(List<String> deniedPermission) {
//                requirePermission(callBack);
            }
        });
    }

}
