package com.pcer.puzzlegame.data;

/**
 * Created by pcer on 2016.9.4.0004.
 */
public class Photo {
    private int mId;
    private String mTitle;

    public Photo(int mId, String mTitle) {
        this.mId = mId;
        this.mTitle = mTitle;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }
}
