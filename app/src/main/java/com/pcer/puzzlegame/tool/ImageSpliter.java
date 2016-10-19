package com.pcer.puzzlegame.tool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import java.util.ArrayList;

/**
 * Created by pcer on 2016.9.4.0004.
 * 拼图块的切割类，可实现九宫格的规律切割 和 自由拼图的无序切割
 */
public class ImageSpliter {
    /**
     * 对传进来的图片进行 n x n 的规律切割，通过数组返回
     * @param source 源文件
     * @param k 块数量参数
     * @param screenWidth 屏幕大小
     * @return 切割后的有序碎片
     */
    public static ArrayList<Bitmap> regularSplit(Bitmap source, int k, int screenWidth) {
        source = Bitmap.createScaledBitmap(source, (int) (screenWidth * 0.8f), (int) (screenWidth * 0.8f), true);

        ArrayList<Bitmap> al = new ArrayList<Bitmap>();

        int len = source.getWidth() / k;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                Bitmap bmp = Bitmap.createBitmap(source, j * len, i * len, len, len);
                al.add(bmp);
            }
        }
        return al;
    }


    /**
     * 月芽片切割
     * @param source 原图片
     * @param k 块数量参数
     * @param screenWidth 屏幕较短的长度
     * @return 有序的封装好的月芽碎片
     */
    public static ArrayList<Node> irregularSplit(Bitmap source, int k, int screenWidth) {
        source = Bitmap.createScaledBitmap(source, (int) (screenWidth * 0.9f), (int) (screenWidth * 0.9f), true);


        // 定义一些微常量
        int len = source.getWidth() / k;
        int offset = len / 2;
        float len1d2 = len * 0.5f;
        float len1d16 = len * 1.0f / 16;
        float len3d8 = len * 3.0f / 8;
        float len5d16 = len * 5.0f / 16;
        float len5d8 = len * 5.0f / 8;
        float len7d8 = len * 7.0f / 8;
        float len1d8 = len * 1.0f / 8;


        // 定义每个碎片的不规则偏移量
        int fx = offset;
        int fy = offset;
        int sx = fx + len;
        int sy = fy;
        int tx = sx;
        int ty = sy + len;
        int lx = tx - len;
        int ly = ty;
        ArrayList<Node> bitmaps = new ArrayList<Node>();


        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                // 定义路径画笔
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                Path path = new Path();

                path.moveTo(fx, fy);    //画笔就位

                int neg = dir(i, j);    //凹凸

                // 通过凹凸参数和边缘参数，用OpenGL画出四条路径边
                if (isLineTo(i, j, i + 1, j, k)) {
                    path.lineTo(sx, sy);
                } else {
                    path.quadTo(fx + len1d2, fy + neg * len1d8, fx + len3d8, fy - neg * len1d16);
                    path.cubicTo(fx + len1d8, fy - neg * len5d16, fx + len7d8, fy - neg * len5d16, fx + len5d8, fy - neg * len1d16);
                    path.quadTo(fx + len1d2, fy + neg * len1d8, sx, sy);
                }
                if (isLineTo(i + 1, j, i + 1, j + 1, k)) {
                    path.lineTo(tx, ty);
                } else {
                    path.quadTo(sx + neg * len1d8, sy + len1d2, sx - neg * len1d16, sy + len3d8);
                    path.cubicTo(sx - neg * len5d16, sy + len1d8, sx - neg * len5d16, sy + len7d8, sx - neg * len1d16, sy + len5d8);
                    path.quadTo(sx + neg * len1d8, sy + len1d2, tx, ty);
                }
                if (isLineTo(i + 1, j + 1, i, j + 1, k)) {
                    path.lineTo(lx, ly);
                } else {
                    path.quadTo(tx - len1d2, ty - neg * len1d8, tx - len3d8, ty + neg * len1d16);
                    path.cubicTo(tx - len1d8, ty + neg * len5d16, tx - len7d8, ty + neg * len5d16, tx - len5d8, ty + neg * len1d16);
                    path.quadTo(tx - len1d2, ty - neg * len1d8, lx, ly);
                }
                if (isLineTo(i, j + 1, i, j, k)) {
                    path.lineTo(fx, fy);
                } else {
                    path.quadTo(lx - neg * len1d8, ly - len1d2, lx + neg * len1d16, ly - len3d8);
                    path.cubicTo(lx + neg * len5d16, ly - len1d8, lx + neg * len5d16, ly - len7d8, lx + neg * len1d16, ly - len5d8);
                    path.quadTo(lx - neg * len1d8, ly - len1d2, fx, fy);
                }

                // 至此月芽片四条路径边完成，月芽片成型，可以通过其截取原图片上的碎片了

                Bitmap target = Bitmap.createBitmap(len * 2, len * 2, Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(target);
                canvas.drawPath(path, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));  // 定义SRC_IN透图模式
                canvas.drawBitmap(source, -i * len + offset, -j * len + offset, paint); // 截取月芽片图像

                int w = i * len;
                int h = j * len;

                // 保存截图的位置信息，用于在游戏中判断碎片相对位置的磁贴效果
                Node node = new Node(target, i * k + j);
                bitmaps.add(node);
                node.curX = node.x = w + (screenWidth - (k + 1) * len) / 2;
                node.curY = node.y = h + 300 - len / 2;

            }
        }

        // 定义碎片的四维相邻关系
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                Node cur = bitmaps.get(i * k + j);
                cur.lt = (i == 0) ? null : bitmaps.get((i - 1) * k + j);
                cur.up = (j == 0) ? null : bitmaps.get(i * k + j - 1);
                cur.rt = (i == k - 1) ? null : bitmaps.get((i + 1) * k + j);
                cur.dn = (j == k - 1) ? null : bitmaps.get(i * k + j + 1);
            }
        }
        return bitmaps;
    }


    // 是否是边缘片
    public static boolean isLineTo(int x1, int y1, int x2, int y2, int k) {
        if (x1 == 0 && x2 == 0) return true;
        if (y1 == 0 && y2 == 0) return true;
        if (x1 == k && x2 == k) return true;
        if (y1 == k && y2 == k) return true;
        return false;
    }

    // 判断月芽片的形状
    private static int dir(int x, int y) {
        if (((x + y) & 1) == 0) {
            return 1;
        }
        return -1;
    }
}
