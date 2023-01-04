package com.seewo.palette.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.kath.paintboard.PaintConstants;
import com.seewo.palette.R;
import com.seewo.palette.callback.ColorChangeCall;

/**
 * 颜色盘组件
 */
public class ColorPalette extends LinearLayout {

    String color;
    GridView mGridview;
    Context mContext;
    ColorChangeCall mCall;
    private BaseAdapter adapter;

    public void setColorChangeCall(ColorChangeCall call) {
        this.mCall = call;
    }

    public ColorPalette(Context context) {

        this(context, null);
    }

    public ColorPalette(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public ColorPalette(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.color_palette, this);
        //相关组件绑定设计
        initView();
        initEvent();
    }

    private void initEvent() {
        mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //todo 回调使颜色改变
                setColor(PaintConstants.colors[position]);
                mCall.callByColorChange(PaintConstants.colors[position]);
            }
        });
    }

    public void setColor(String color) {
        this.color = color;
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        mGridview = (GridView) findViewById(R.id.id_color_gridview);
        adapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return PaintConstants.colors.length;
            }

            @Override
            public Object getItem(int position) {
                return PaintConstants.colors[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.color_palette_item, null);
                    holder = new ViewHolder();
                    holder.iv = (ColorView) convertView.findViewById(R.id.id_color_item);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                if (color.equals(PaintConstants.colors[position])) {
                    holder.iv.setColor(Color.parseColor(PaintConstants.colors[position]), true);
                } else {
                    holder.iv.setColor(Color.parseColor(PaintConstants.colors[position]), false);
                }
                return convertView;
            }
        };
        mGridview.setAdapter(adapter);

    }

    class ViewHolder {
        ColorView iv;
    }


}

