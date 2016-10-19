package com.pcer.puzzlegame.view.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.pcer.puzzlegame.BGMService;
import com.pcer.puzzlegame.Configs;
import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.manager.UserInfoManager;
import com.pcer.puzzlegame.view.fragment.ConfigureDialogFragment;
import com.pcer.puzzlegame.view.fragment.GameInfoFragment;
import com.pcer.puzzlegame.view.fragment.LoginDialogFragment;
import com.pcer.puzzlegame.view.fragment.UserInfoDialogFragment;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    public static Intent sBgmService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sBgmService = new Intent(MainActivity.this, BGMService.class);
        startService(sBgmService);
        initView();

        LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        loginDialogFragment.show(getSupportFragmentManager(), TAG);

        Configs.sCurrentBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic8);

    }

    private void initView(){
        findViewById(R.id.btn_start_game).setOnClickListener(this);
        findViewById(R.id.btn_select_image).setOnClickListener(this);
        findViewById(R.id.btn_about_game).setOnClickListener(this);
        findViewById(R.id.btn_configure_game).setOnClickListener(this);
        findViewById(R.id.btn_user_image).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start_game:
                Intent gameIntent;
                if(Configs.sGameMode == Configs.MODE_SUDOKU){
                    gameIntent = new Intent(MainActivity.this, SudokuGameActivity.class);
                } else {
                    gameIntent = new Intent(MainActivity.this, JigsawGameActivity.class);
                }
                startActivity(gameIntent);
                break;
            case R.id.btn_select_image:
                Intent selectIntent = new Intent(MainActivity.this, ImagePickActivity.class);
                startActivity(selectIntent);
                break;
            case R.id.btn_about_game:
                GameInfoFragment gameInfoFragment = new GameInfoFragment();
                gameInfoFragment.show(getSupportFragmentManager(),GameInfoFragment.TAG);
                break;
            case R.id.btn_configure_game:
                ConfigureDialogFragment configureGameFragment = new ConfigureDialogFragment();
                configureGameFragment.show(getSupportFragmentManager(),ConfigureDialogFragment.TAG);
                break;
            case R.id.btn_user_image:
                if(!UserInfoManager.getInstance().isUserSet()){
                    Toast.makeText(this,"您还没有登录哦",Toast.LENGTH_SHORT).show();
                    LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
                    loginDialogFragment.show(getSupportFragmentManager(), LoginDialogFragment.TAG);
                } else {
                    UserInfoDialogFragment userInfoDialogFragment = new UserInfoDialogFragment();
                    userInfoDialogFragment.show(getSupportFragmentManager(),UserInfoDialogFragment.TAG);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(MainActivity.this, BGMService.class);
        stopService(intent);
    }

}
