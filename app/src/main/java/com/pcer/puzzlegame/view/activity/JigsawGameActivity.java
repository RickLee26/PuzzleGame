package com.pcer.puzzlegame.view.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pcer.puzzlegame.Configs;
import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.manager.UserInfoManager;
import com.pcer.puzzlegame.tool.ImageSpliter;
import com.pcer.puzzlegame.tool.Node;
import com.pcer.puzzlegame.tool.ScreenUtil;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

/**
 * 自由拼图（月芽片拼图）的activity
 */

public class JigsawGameActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "JigsawGameActivity";

    private Bitmap mBitmap;
    private PuzzleView mPuzzleView;
    private boolean mSuccess;
    private Handler mHandler;

    private Chronometer mTimer;
    private long mUsed;
    private int mGrade;
    private int mGameSize;


    private Button btnOriginalImage, btnResetGame, btnGamePause;
    private ImageView ivOriginalPause;
    private Bitmap mPauseBitmap;
    private Animation animShow;
    private Animation animHide;
    private boolean mIsShowImg = false;
    private boolean mIsOnPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jigsaw_game);

        initView();
        mPauseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_pic);
        mBitmap = Configs.sCurrentBitmap;
        mGameSize = Configs.sGameSize;

        mPuzzleView = new PuzzleView(this);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == PuzzleView.PUZZLE_FINISH) {
                    if (mSuccess) {
                        mTimer.stop();
                        mUsed = (SystemClock.elapsedRealtime() - mTimer.getBase() + 500) / 1000;
                        mGrade = (int) getGrade(mUsed, mGameSize);
                        btnOriginalImage.setClickable(false);
                        btnGamePause.setClickable(false);
                        Toast.makeText(JigsawGameActivity.this, "拼图成功", Toast.LENGTH_SHORT).show();
                        UserInfoManager.getInstance().addScore(mGrade);
                    }
                }
            }
        };

        mTimer = (Chronometer) findViewById(R.id.ch_game_state_time);
        mTimer.setBase(SystemClock.elapsedRealtime() + 0);
        mTimer.setFormat("时间: %s");
//        mTimer.start();

        ArrayList<Node> al = ImageSpliter.irregularSplit(mBitmap, mGameSize, ScreenUtil.getScreenSize(this).widthPixels);
        mPuzzleView.startWork(al, mGameSize, mHandler);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.rlFreeGameContainer);
        rl.addView(mPuzzleView, 0);
        onClick(btnResetGame);
    }


    private void initView() {
        ivOriginalPause = (ImageView) findViewById(R.id.ivOriginalAndPause);
        btnOriginalImage = (Button) findViewById(R.id.btnOriginalBitmap);
        btnResetGame = (Button) findViewById(R.id.btnResetGame);
        btnGamePause = (Button) findViewById(R.id.btnGamePause);

        btnOriginalImage.setOnClickListener(this);
        btnResetGame.setOnClickListener(this);
        btnGamePause.setOnClickListener(this);
    }

    private double getGrade(long seconds, int level) //自由移动的分数，输入：秒数，难度级别(1易-4难)
    {
        if (seconds <= 0) return -1;
        double result = 10000.0;
        result *= level * level;
        result /= (double) (seconds);
        return result / 50;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnOriginalBitmap:
                ivOriginalPause.setImageBitmap(mBitmap);
                animShow = AnimationUtils.loadAnimation(
                        JigsawGameActivity.this, R.anim.image_show_anim);
                animHide = AnimationUtils.loadAnimation(
                        JigsawGameActivity.this, R.anim.image_hide_anim);
                if (mIsShowImg) {
                    ivOriginalPause.startAnimation(animHide);
                    ivOriginalPause.setVisibility(View.GONE);

                    mIsShowImg = false;
                } else {
                    ivOriginalPause.startAnimation(animShow);
                    ivOriginalPause.setVisibility(View.VISIBLE);
                    mIsShowImg = true;
                }
                break;
            case R.id.btnResetGame:
                mPuzzleView.shuffle();
                mSuccess = false;
                mUsed = 0;
                mTimer.setBase(SystemClock.elapsedRealtime());      //reset time
                mTimer.start();
                btnGamePause.setClickable(true);
                btnOriginalImage.setClickable(true);
                break;
            case R.id.btnGamePause:
                ivOriginalPause.setImageBitmap(mPauseBitmap);
                animShow = AnimationUtils.loadAnimation(
                        JigsawGameActivity.this, R.anim.image_show_anim);
                animHide = AnimationUtils.loadAnimation(
                        JigsawGameActivity.this, R.anim.image_hide_anim);
                if (mIsOnPause) {
                    ivOriginalPause.startAnimation(animHide);
                    ivOriginalPause.setVisibility(View.GONE);
                    mIsOnPause = false;
                    mTimer.setBase(SystemClock.elapsedRealtime() - mUsed);
                    mTimer.start();
                    btnGamePause.setText("暂停");
                    btnOriginalImage.setOnClickListener(this);
                    btnResetGame.setOnClickListener(this);
                } else {
                    ivOriginalPause.startAnimation(animShow);
                    ivOriginalPause.setVisibility(View.VISIBLE);
                    mIsOnPause = true;
                    mUsed = SystemClock.elapsedRealtime() - mTimer.getBase();
                    mTimer.stop();
                    btnGamePause.setText("继续");
                    btnOriginalImage.setOnClickListener(null);
                    btnResetGame.setOnClickListener(null);
                }
                break;
        }
    }

    /**
     * 月芽片拼图的view，所有的游戏过程自治
     */
    public class PuzzleView extends View {
        int k;
        int resetCount = 0;
        int preX, preY;
        static final int MAX_DIF = 30;

        ArrayList<Node> elements = null;
        ArrayList<Node> parent = null;
        Node curNode = null;
        Handler handler = null;

        public PuzzleView(Context context) {
            super(context);
        }


        public void startWork(ArrayList<Node> al, int pieceCount, Handler hdl) {
            elements = al;
            parent = (ArrayList<Node>) elements.clone();
            k = pieceCount;
            handler = hdl;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            int eX = (int) event.getX();
            int eY = (int) event.getY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:   // 手指按下
                    preX = eX;
                    preY = eY;
                    findBitmap(eX, eY); // 通过位图算法找到被点到的碎片（可能不存在）
                    if (curNode != null)
                        groupActionWithNoUp(0, 0, 0, curNode.id);   // 标记所有已经跟当前片连接的碎片，作为【移动整体】
                    break;
                case MotionEvent.ACTION_MOVE:   // 手指移动
                    if (curNode == null) break;
                    int xDiff = eX - preX;
                    int yDiff = eY - preY;
                    preX = eX;
                    preY = eY;
                    groupActionWithNoUp(1, xDiff, yDiff, curNode.id);   // 移动当前的【移动整体】
                    break;
                case MotionEvent.ACTION_UP:     // 手指释放
                    if (curNode == null) break;

                    groupActionUp(curNode.id);      // 处理释放之后，【移动整体】的后续操作，包括判断邻接片的相对位置，磁贴附着效果等
                    curNode = null;
                    if (resetCount == elements.size()) {    // 是否所有碎片已经整合成一块
                        if (!mSuccess) {
                            mSuccess = true;
                            Message msg = new Message();
                            msg.what = PUZZLE_FINISH;
                            handler.sendMessage(msg);       // 通知父元素，游戏结束
                        }
                    }
                    break;
                default:
                    break;
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < elements.size(); i++) {
                Node node = elements.get(i);
                canvas.drawBitmap(node.bitmap, node.curX, node.curY, null);
            }
        }


        int getParent(int n) {

            if (n == parent.get(n).setId) return n;
            int ans = parent.get(n).setId = getParent(parent.get(n).setId);
            return ans;
        }

        int getParent(Node node) {
            return getParent(node.id);
        }

        void groupActionUp(int srcIndex) {
            //boolean upReset = (Math.abs(curNode.curX - curNode.x) <= MAX_DIF && Math.abs(curNode.curY - curNode.y) <= MAX_DIF);
            TreeSet<Integer> ts = new TreeSet<>();

            for (int i = 0; i < parent.size(); i++) {
                if (getParent(i) == getParent(srcIndex)) {
                    Node node = parent.get(i);
                    node.role = 1;
                    /*
                    if (upReset) {
                        node.curX = node.x;
                        node.curY = node.y;
                        node.reset = true;
                        elements.remove(node);
                        elements.add(0, node);
                    }*/

                    // 并查集算法，将所有碎片分组，同一组的碎片是已经连接成一块的，所以只需要对每组碎片的根碎片进行相对位置判断就可以知道是否可以相互附着，避免了复杂的逻辑处理

                    if (isLinkable(node, node.up)) ts.add(getParent(node.up));
                    if (isLinkable(node, node.dn)) ts.add(getParent(node.dn));
                    if (isLinkable(node, node.lt)) ts.add(getParent(node.lt));
                    if (isLinkable(node, node.rt)) ts.add(getParent(node.rt));
                }
            }
            Node src = parent.get(srcIndex);

            for (int idx : ts) {
                Node tar = parent.get(idx);
                int xDiff = src.curX - src.x + tar.x - tar.curX;
                int yDiff = src.curY - src.y + tar.y - tar.curY;
                groupActionWithNoUp(3, xDiff, yDiff, idx);
                parent.get(idx).setId = srcIndex;
            }

            resetCount = 0;
            for (Node node : parent) {
                node.role = 0;
                if (getParent(node) == getParent(curNode)) resetCount++;
            }
        }

        void groupActionWithNoUp(int action, int xDiff, int yDiff, int srcIndex) {
            for (int i = 0; i < parent.size(); i++) {
                if (getParent(i) == getParent(srcIndex)) {
                    Node node = parent.get(i);
                    node.role = 1;
                    switch (action) {
                        case 0://down
                            elements.remove(node);
                            elements.add(node);
                            break;
                        case 1://move
                        case 3:
                            node.curX += xDiff;
                            node.curY += yDiff;
                            /*
                            if (action == 3 && curNode.reset) {
                                node.reset = true;
                                elements.remove(node);
                                elements.add(0, node);
                            }*/
                            break;
                    }
                }
            }

        }


        // 对位图进行 & 运算，找出当前点击位置对应的碎片
        void findBitmap(int eX, int eY) {
            for (int i = elements.size() - 1; i >= 0; i--) {
                Node node = elements.get(i);
                Bitmap bmp = node.bitmap;
                int color = -1;
                int ySize = bmp.getHeight();
                int xSize = bmp.getWidth();
                if (eX - node.curX < 0 || eX - node.curX >= xSize || eY - node.curY < 0 || eY - node.curY >= ySize)
                    continue;
                try {
                    color = bmp.getPixel(eX - node.curX, eY - node.curY);
                } catch (Exception e) {
                    Log.d("whatPixel:", "deltaX:" + (eX - node.curX) + "\tdeltaY:" + (eY - node.curY));
                }

                if ((color & 0xff000000) != 0) {
                    if (node.reset) return;
                    curNode = node;
                    elements.remove(i);
                    elements.add(curNode);
                    return;
                }
            }
        }


        public void shuffle() {

            int totalHeight = parent.get(0).bitmap.getWidth() * k / 2;
            totalHeight *= 0.9f;
            int totalWidth = totalHeight;
            Random rdn = new Random();
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < k; j++) {
                    Node node = elements.get(i * k + j);
                    node.curX = rdn.nextInt(totalWidth);
                    node.curY = rdn.nextInt(totalHeight);
                    node.setId = node.id;
                    node.reset = false;
                }
            }
            resetCount = 0;
            invalidate();
        }

        boolean isLinkable(Node src, Node tar) {
            if (tar == null) return false;
            if (tar.reset) return false;
            if (tar.role != 0) return false;
            if (getParent(src) == getParent(tar)) return false;
            return Math.abs((src.curX - tar.curX) - (src.x - tar.x)) <= MAX_DIF &&
                    Math.abs((src.curY - tar.curY) - (src.y - tar.y)) <= MAX_DIF;
        }

        public static final int PUZZLE_FINISH = 0x13349057;
    }
}
