package com.pcer.puzzlegame.data;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by pcer on 2016.9.4.0004.
 * 九宫格管理器
 */
public class GridGameItemLab {
    private static GridGameItemLab sGridGameItemLab = null;
    private ArrayList<GridGameItem> mItems =null;
    private Context mContext;

    private GridGameItemLab(Context c) {
        mItems = new ArrayList<>();
        mContext = c;
    }

    public static GridGameItemLab getInstance (Context c) {
        if (sGridGameItemLab == null) {
            sGridGameItemLab = new GridGameItemLab(c.getApplicationContext());
        }
        return sGridGameItemLab;
    }

    public ArrayList<GridGameItem> getmItems() {
        return mItems;
    }

    public void addItems(GridGameItem i){
        mItems.add(i);
    }

    public void clearItems(){
        mItems.clear();
    }
}
