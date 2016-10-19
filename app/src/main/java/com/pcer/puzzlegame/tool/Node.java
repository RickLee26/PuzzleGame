package com.pcer.puzzlegame.tool;

import android.graphics.Bitmap;

/**
 * Created by pcer on 2016.9.4.0004.
 * 月芽片拼图快的各类信息
 */
public class Node {
    public Bitmap bitmap = null;
//    public Bitmap model = null;
    public int x = 0;
    public int y = 0;
    public int curX = 0;
    public int curY = 0;
    public int setId = 0;
    public int id = 0;
    public int role = 0;
    public Node lt = null, rt = null, up = null, dn = null;
    public boolean reset = false;

    public Node(Bitmap bmp, int nid) {
        bitmap = bmp;
        this.id = nid;
        this.setId = nid;
    }
}
