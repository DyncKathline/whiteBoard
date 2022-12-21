package com.seewo.palette.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kath.paintboard.Constants;
import com.seewo.palette.R;

/**
 * Created by user on 2016/8/3.
 * 图形选择UI
 */
public class ShapeSelectView extends LinearLayout implements View.OnClickListener {

    private final Context mContext;
    Button mSelectInkBtn;
    Button mSelectLineBtn;
    Button mSelectRectBtn;
    Button mSelectCircleBtn;
    Button mSelectArrowBtn;
    Button mSelectOvalBtn;

    //默认选择的是曲线
    public int kind;

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
        updateUIState(kind);
    }


    public ShapeSelectView(Context context) {
        this(context, null);
    }

    public ShapeSelectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShapeSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(mContext).inflate(R.layout.shape_palette, this);
        initView();
        initEvent();
    }


    private void initView() {
        mSelectInkBtn = findViewById(R.id.id_select_ink);
        mSelectLineBtn = findViewById(R.id.id_select_line);
        mSelectRectBtn = findViewById(R.id.id_select_rect);
        mSelectCircleBtn = findViewById(R.id.id_select_circle);
        mSelectArrowBtn = findViewById(R.id.id_select_arrow);
        mSelectOvalBtn = findViewById(R.id.id_select_oval);
    }

    private void initEvent() {
        mSelectInkBtn.setOnClickListener(this);
        mSelectLineBtn.setOnClickListener(this);
        mSelectRectBtn.setOnClickListener(this);
        mSelectCircleBtn.setOnClickListener(this);
        mSelectArrowBtn.setOnClickListener(this);
        mSelectOvalBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_select_ink:
                setKind(Constants.INK);
                break;
            case R.id.id_select_line:
                setKind(Constants.LINE);
                break;
            case R.id.id_select_rect:
                setKind(Constants.RECT);
                break;
            case R.id.id_select_circle:
                setKind(Constants.CIRCLE);
                break;
            case R.id.id_select_arrow:
                setKind(Constants.ARROW);
                break;
            case R.id.id_select_oval:
                setKind(Constants.OVAL);
                break;
        }
        if (mKindBtnClickedListener != null) {
            mKindBtnClickedListener.onKindBtnClicked(kind);
        }
    }

    public void updateUIState(int kind) {
        mSelectInkBtn.setTextColor(Color.BLACK);
        mSelectLineBtn.setTextColor(Color.BLACK);
        mSelectRectBtn.setTextColor(Color.BLACK);
        mSelectCircleBtn.setTextColor(Color.BLACK);
        mSelectArrowBtn.setTextColor(Color.BLACK);
        mSelectOvalBtn.setTextColor(Color.BLACK);
        switch (kind) {
            case Constants.INK:
                mSelectInkBtn.setTextColor(mContext.getResources().getColor(R.color.blue));
                break;
            case Constants.LINE:
                mSelectLineBtn.setTextColor(mContext.getResources().getColor(R.color.blue));
                break;
            case Constants.RECT:
                mSelectRectBtn.setTextColor(mContext.getResources().getColor(R.color.blue));
                break;
            case Constants.CIRCLE:
                mSelectCircleBtn.setTextColor(mContext.getResources().getColor(R.color.blue));
                break;
            case Constants.ARROW:
                mSelectArrowBtn.setTextColor(mContext.getResources().getColor(R.color.blue));
                break;
            case Constants.OVAL:
                mSelectOvalBtn.setTextColor(mContext.getResources().getColor(R.color.blue));
                break;
        }
    }

    public interface KindBtnClickedListener {
        void onKindBtnClicked(int kind);
    }

    private KindBtnClickedListener mKindBtnClickedListener;

    public void setKindBtnClickedListener(KindBtnClickedListener listener) {
        this.mKindBtnClickedListener = listener;
    }
}
