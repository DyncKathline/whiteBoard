package com.seewo.palette.save;

import com.kath.paintboard.widget.PaintView;

/**
 * 抽象另存类
 */
public abstract class SaveOperation {
    String filepath;
    String filename=null;

    public String getFilepath() {
        return filepath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public abstract  String getAbsoluteFileName();
    public abstract void savePainting();
    public abstract void getContent(PaintView paintView);
}
