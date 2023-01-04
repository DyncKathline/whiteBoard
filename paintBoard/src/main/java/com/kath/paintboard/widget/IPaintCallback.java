package com.kath.paintboard.widget;

import android.view.MotionEvent;

public interface IPaintCallback {
    void touchDown(MotionEvent event, float x, float y, long time);
    void touchMove(float mx, float my, float x, float y, long time);
    void touchUp(float x, float y, long time);
    void undo();
    void redo();
    void changeKind(int kind);
    void changeBrushColor(String color);
    void changeBrushSize(float size);
}
