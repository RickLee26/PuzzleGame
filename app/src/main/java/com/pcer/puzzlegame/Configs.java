package com.pcer.puzzlegame;

import android.graphics.Bitmap;

/**
 * Created by 15525 on 2016.9.4.0004.
 */
public class Configs {
    public static String IP_USER_SERVER = "119.29.221.111";
    public static String IP_FILE_SERVER = "127.0.0.1";
    public static int PORT_USER_SERVER = 9057;
    public static int PORT_FILE_SERVER = 9527;

    public static Bitmap sCurrentBitmap;
    public static int sGameSize = 3;
    public static boolean sIsMusicOn = true;

    public static final int MODE_SUDOKU = 0;
    public static final int MODE_JIGSAW = 1;
    public static int sGameMode;

}
