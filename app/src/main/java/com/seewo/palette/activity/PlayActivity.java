package com.seewo.palette.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.kath.paintboard.bean.Point;
import com.kath.paintboard.bean.Shape;
import com.kath.paintboard.play.PlayUtil;
import com.kath.paintboard.play.RecordPathAnimUtil;
import com.kath.paintboard.play.RecordPathView;
import com.kath.paintboard.util.JsonOperation;
import com.seewo.palette.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends AppCompatActivity {

    private RecordPathView mPlayView;
    Handler handlertest = new Handler();
    String filename;
    private RecordPathAnimUtil pathAnimUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mPlayView = findViewById(R.id.id_play_view);

        handlertest.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                filename = intent.getStringExtra("openfilename");
                if (filename != null) {
                    List<Shape> shapes = loadPreImage(filename);
                    pathAnimUtil = new RecordPathAnimUtil(getApplicationContext());
                    long[] times = new long[shapes.size()];
                    for (int i = 0; i < shapes.size(); i++) {
                        Shape shape = shapes.get(i);
                        List<Point> points = shape.getPointList();

                        long startTime = points.get(0).getTime();
                        long endTime = points.get(points.size() - 1).getTime();
                        times[i] = endTime - startTime;

                        for (int j = 1; j < shape.getPointList().size(); j++) {
                            Point prePoint = shape.getPointList().get(j - 1);
                            Point point = shape.getPointList().get(j);
                            pathAnimUtil.addPath(new android.graphics.Point((int)prePoint.getX(), (int)prePoint.getY()), new android.graphics.Point((int)point.getX(), (int)point.getY()), Color.parseColor(shape.getColor()), Color.parseColor(shape.getColor()));
                        }
                    }
                    long duration = 0;
                    for (int i = 0; i < times.length; i++) {
                        duration += times[i];
                    }
                    mPlayView.setPath(pathAnimUtil, duration);
                }
            }
        }, 500);
    }

    public List<Shape> loadPreImage(String filename) {
        List<Shape> shapes = new ArrayList<>();
        //遍历文件夹下每个xml文件
        File scanFilePath = new File(filename);
        if (scanFilePath.isDirectory()) {
            for (File file : scanFilePath.listFiles()) {
                String fileAbsolutePath = file.getAbsolutePath();
                System.out.println(fileAbsolutePath);
                if (fileAbsolutePath.endsWith(".json")) {
                    //将json解析放入mGallery中
                    try {
                        shapes = JsonOperation.transJsonToShape(fileAbsolutePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return shapes;
    }
}