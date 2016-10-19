package com.pcer.puzzlegame.data;

import android.graphics.Bitmap;

/**
 * Created by pcer on 2016.9.4.0004.
 * 九宫格的格子内数据
 */
public class GridGameItem {
    private int mId;
    private Bitmap mBitmap;

    public GridGameItem(int id, Bitmap bm){
        mId = id;
        mBitmap = bm;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public int getId() {
        return mId;
    }
}
