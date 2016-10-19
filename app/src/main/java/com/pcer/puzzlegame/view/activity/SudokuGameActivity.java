package com.pcer.puzzlegame.view.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pcer.puzzlegame.Configs;
import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.data.GridGameItem;
import com.pcer.puzzlegame.data.GridGameItemLab;
import com.pcer.puzzlegame.manager.UserInfoManager;
import com.pcer.puzzlegame.tool.ImageSpliter;
import com.pcer.puzzlegame.tool.ScreenUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class SudokuGameActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "SudokuGameActivity";

    private Bitmap mBitmap;
    private boolean mSuccess;
    private Chronometer mTimer;
    private int mGameStep;
    private long mUsed;
    private TextView mTvSteps;
    private ArrayList<GridGameItem> mItems = null;
    private int mBlankPosition = 1;
    private Random mRandomBlankPos = new Random(47);
    private BaseAdapter mAdapter;
    private int mGrade = 0;

    private int mGameSize;

    private Button btnOriginalImage, btnResetGame, btnGamePause;
    private GridView mGvGame;
    private ImageView mIvOriginalPause;
    private Bitmap mPauseBitmap;
    private boolean mIsShowImg;
    private boolean mIsOnPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku_game);

        mGameSize = Configs.sGameSize;
        mPauseBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_pic);
        mBitmap = Configs.sCurrentBitmap;
        initView();

        ArrayList<Bitmap> al = ImageSpliter.regularSplit(mBitmap, mGameSize, ScreenUtil.getScreenSize(this).widthPixels);
        GridGameItemLab.getInstance(this).clearItems();
        for (int i = 0; i < al.size(); i++) {
            GridGameItemLab.getInstance(this).addItems(new GridGameItem(i, al.get(i)));
        }
        mItems = GridGameItemLab.getInstance(this).getmItems();

        mBlankPosition = mRandomBlankPos.nextInt(mGameSize * mGameSize);


        generateGame();
        gameShuffle();

        mGvGame.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                if (!mSuccess && isMovable(position)) {
                    mTvSteps.setText("步数： " + (++mGameStep));
                    //交换被点击item和空白item
                    Collections.swap(mItems, position, mBlankPosition);
                    mBlankPosition = position;
                    mAdapter.notifyDataSetChanged();
                    if (isSuccess()) {
                        mUsed = (SystemClock.elapsedRealtime() - mTimer.getBase() + 500) / 1000;
                        System.out.println("time mUsed: " + mUsed);
                        mGrade = (int) getGrade(mUsed, mGameSize, mGameStep);
                        mBlankPosition = -1;
                        mAdapter.notifyDataSetChanged();
                        mSuccess = true;
                        Toast.makeText(SudokuGameActivity.this, "拼图成功!", Toast.LENGTH_LONG).show();
                        btnOriginalImage.setClickable(false);
                        btnGamePause.setClickable(false);
                        mBlankPosition = mRandomBlankPos.nextInt(mGameSize * mGameSize);
                        UserInfoManager.getInstance().addScore(mGrade);
                    }
                }
            }
        });


        mTimer = (Chronometer) findViewById(R.id.ch_game_state_time_grid);
        mTimer.setBase(SystemClock.elapsedRealtime() + 0);
        mTimer.setFormat("时间: %s");
        mTimer.start();
        mTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (mSuccess) {
                    mTimer.stop();
                }
            }
        });

    }

    private void initView() {
        mGvGame = (GridView) findViewById(R.id.gv_gamimg);
        mGvGame.setNumColumns(Configs.sGameSize);
        mTvSteps = (TextView) findViewById(R.id.tv_game_state_steps);
        mIvOriginalPause = (ImageView) findViewById(R.id.ivOriginalAndPauseGrid);
        btnOriginalImage = (Button) findViewById(R.id.btnOriginalBitmapGrid);
        btnResetGame = (Button) findViewById(R.id.btnResetGameGrid);
        btnGamePause = (Button) findViewById(R.id.btnGamePauseGrid);

        btnOriginalImage.setOnClickListener(this);
        btnResetGame.setOnClickListener(this);
        btnGamePause.setOnClickListener(this);
    }


    private void gameShuffle() {
        for (int i = 0; i < 500; i++) {
            int temp = mRandomBlankPos.nextInt(50) % (mGameSize * mGameSize);
            if (isMovable(temp)) {
                Collections.swap(mItems, temp, mBlankPosition);
                mBlankPosition = temp;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private boolean isSuccess() {
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).getId() != i) {
                return false;
            }
        }
        return true;
    }

    private boolean isMovable(int position) {
        if (mBlankPosition == position) return false;
        if (Math.abs(mBlankPosition - position) == mGameSize) {
            return true;
        }
        if ((mBlankPosition / mGameSize == position / mGameSize) &&
                Math.abs(mBlankPosition - position) == 1) {
            return true;
        }
        return false;
    }


    private void generateGame() {
        mAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return mItems.size();
            }

            @Override
            public Object getItem(int position) {
                return mItems.get(position).getBitmap();
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ImageView iv_gridView_item;
                if (convertView == null) {
                    iv_gridView_item = new ImageView(SudokuGameActivity.this);
                    int height = mGvGame.getHeight();
                    int width = mGvGame.getWidth();
                    GridView.LayoutParams params = (new GridView.LayoutParams(width / mGameSize, height / mGameSize));
                    iv_gridView_item.setLayoutParams(params);
                    iv_gridView_item.setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    iv_gridView_item = (ImageView) convertView;
                    iv_gridView_item.setLayoutParams(new AbsListView.LayoutParams((int) (parent.getWidth() / mGameSize),
                            (int) (parent.getHeight() / mGameSize)));// 动态设置item的高度
                }
                iv_gridView_item.setImageBitmap(mItems.get(position).getBitmap());
                if (!mSuccess && position == mBlankPosition)
                    iv_gridView_item.setAlpha(0.5f);
                else iv_gridView_item.setAlpha(1.0f);
                return iv_gridView_item;
            }
        };
        mGvGame.setAdapter(mAdapter);
    }

    private double getGrade(long seconds, int level, int steps) //九宫格移动分数，输入：秒数，难度级别(1易-4难)，步数
    {
        if (seconds <= 0 || steps <= 0) return -1;
        double result = 10000.0;
        result *= level * level;
        result /= (double) (seconds + steps);
        return result / 50;
    }


    @Override
    public void onClick(View v) {
        Animation animShow;
        Animation animHide;
        switch (v.getId()) {
            case R.id.btnOriginalBitmapGrid:
                mIvOriginalPause.setImageBitmap(mBitmap);
                animShow = AnimationUtils.loadAnimation(
                        SudokuGameActivity.this, R.anim.image_show_anim);
                animHide = AnimationUtils.loadAnimation(
                        SudokuGameActivity.this, R.anim.image_hide_anim);
                if (mIsShowImg) {
                    mIvOriginalPause.startAnimation(animHide);
                    mIvOriginalPause.setVisibility(View.GONE);

                    mIsShowImg = false;
                } else {
                    mIvOriginalPause.startAnimation(animShow);
                    mIvOriginalPause.setVisibility(View.VISIBLE);
                    mIsShowImg = true;
                }
                break;
            case R.id.btnResetGameGrid:
                gameShuffle();
                mSuccess = false;
                mGameStep = 0;
                mTvSteps.setText("步数： " + mGameStep);      //reset steps
                mUsed = 0;
                mTimer.setBase(SystemClock.elapsedRealtime());      //reset time
                mTimer.start();
                btnGamePause.setClickable(true);
                btnOriginalImage.setClickable(true);
                break;
            case R.id.btnGamePauseGrid:
                mIvOriginalPause.setImageBitmap(mPauseBitmap);
                animShow = AnimationUtils.loadAnimation(
                        SudokuGameActivity.this, R.anim.image_show_anim);
                animHide = AnimationUtils.loadAnimation(
                        SudokuGameActivity.this, R.anim.image_hide_anim);
                if (mIsOnPause) {
                    mIvOriginalPause.startAnimation(animHide);
                    mIvOriginalPause.setVisibility(View.GONE);
                    mIsOnPause = false;
                    mTimer.setBase(SystemClock.elapsedRealtime() - mUsed);
                    mTimer.start();
                    btnGamePause.setText("暂停");
                    btnOriginalImage.setOnClickListener(this);
                    btnResetGame.setOnClickListener(this);
                } else {
                    mIvOriginalPause.startAnimation(animShow);
                    mIvOriginalPause.setVisibility(View.VISIBLE);
                    mIsOnPause = true;
                    mUsed = SystemClock.elapsedRealtime() - mTimer.getBase();
                    mTimer.stop();
                    btnGamePause.setText("继续");
                    btnOriginalImage.setClickable(false);
                    btnResetGame.setClickable(false);
                }
                break;
        }
    }

}
