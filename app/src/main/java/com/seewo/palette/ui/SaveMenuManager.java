package com.seewo.palette.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.seewo.palette.R;
import com.seewo.palette.util.Constants;

/**
 * 选择菜单Ui管理
 */
public class SaveMenuManager implements View.OnClickListener {

    Context mContext;
    View mView;
    TextView CurPaintClearTv;
    TextView SavePngTv;
    TextView SaveSvgTv;

    public SaveMenuManager(Context mContext, View mView) {
        this.mContext = mContext;
        this.mView = mView;
        initView();
        initEvent();
    }

    private void initEvent() {
        CurPaintClearTv.setOnClickListener(this);
        SaveSvgTv.setOnClickListener(this);
        SavePngTv.setOnClickListener(this);
    }

    private void initView() {
        CurPaintClearTv = mView.findViewById(R.id.cur_paint_clear);
        SavePngTv = mView.findViewById(R.id.save_png_btn);
        SaveSvgTv = mView.findViewById(R.id.save_svg_btn);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cur_paint_clear:
                if(mSaveBtnClickListener!=null)
                    mSaveBtnClickListener.SaveClick(Constants.CLEAR);
                break;
            case R.id.save_png_btn:
                if(mSaveBtnClickListener!=null)
                    mSaveBtnClickListener.SaveClick(Constants.PNG);
                break;
            case R.id.save_svg_btn:
                if(mSaveBtnClickListener!=null)
                    mSaveBtnClickListener.SaveClick(Constants.SVG);
                break;

        }
    }

    public interface SaveBtnClickListener{
        void SaveClick(int saveKind);
    }
    SaveBtnClickListener mSaveBtnClickListener;
    public void setSaveBtnClickListener(SaveBtnClickListener listener){
        this.mSaveBtnClickListener=listener;
    }
}
