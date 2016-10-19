package com.pcer.puzzlegame.view.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.pcer.puzzlegame.Configs;
import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.view.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigureDialogFragment extends DialogFragment
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
        ,RadioGroup.OnCheckedChangeListener{

    public static final String TAG = "ConfigureDialogFragment";

    private int mGamePieceNum = Configs.sGameSize;
    private int mGameMode = Configs.sGameMode;
    private boolean mIsMusicOn = Configs.sIsMusicOn;


    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_configure_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);

        RadioGroup rgPieceNums = (RadioGroup)mView.findViewById(R.id.rgGameLevel);
        RadioGroup rgMode = (RadioGroup)mView.findViewById(R.id.rgGameType);

        if(mGameMode == Configs.MODE_JIGSAW){
            ((RadioButton)mView.findViewById(R.id.modeJigSaw)).setChecked(true);
        } else {
            ((RadioButton)mView.findViewById(R.id.modeSudoku)).setChecked(true);
        }

        switch (mGamePieceNum){
            case 2:
                ((RadioButton)mView.findViewById(R.id.pieceNum2)).setChecked(true);
                break;
            case 3:
                ((RadioButton)mView.findViewById(R.id.pieceNum3)).setChecked(true);
                break;
            case 4:
                ((RadioButton)mView.findViewById(R.id.pieceNum4)).setChecked(true);
                break;
            case 5:
                ((RadioButton)mView.findViewById(R.id.pieceNum5)).setChecked(true);
                break;
            default:
                ((RadioButton)mView.findViewById(R.id.pieceNum2)).setChecked(true);
                break;
        }

        rgMode.setOnCheckedChangeListener(this);
        rgPieceNums.setOnCheckedChangeListener(this);
        ((CheckBox)mView.findViewById(R.id.cbBackgroundMusic)).setOnCheckedChangeListener(this);

        mView.findViewById(R.id.btnConfigureOK).setOnClickListener(this);

        return mView;
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch(radioGroup.getId()){
            case R.id.rgGameLevel:
                if(i == R.id.pieceNum2){
                    mGamePieceNum = 2;
                } else if(i == R.id.pieceNum3){
                    mGamePieceNum = 3;
                } else if(i == R.id.pieceNum4){
                    mGamePieceNum = 4;
                } else if(i == R.id.pieceNum5){
                    mGamePieceNum = 5;
                }
                break;
            case R.id.rgGameType:
                if(i == R.id.modeSudoku){
                    mGameMode = Configs.MODE_SUDOKU;
                } else {
                    mGameMode = Configs.MODE_JIGSAW;
                }
                break;
            default:break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Configs.sGameMode = mGameMode;
        Configs.sGameSize = mGamePieceNum;
        Configs.sIsMusicOn = mIsMusicOn;
    }

    @Override
    public void onClick(View view) {
        dismiss();
        Configs.sGameMode = mGameMode;
        Configs.sGameSize = mGamePieceNum;
        Configs.sIsMusicOn = mIsMusicOn;

        Intent bgmService = MainActivity.sBgmService;
        bgmService.removeExtra("ORDER");
        bgmService.putExtra("ORDER",  mIsMusicOn);

        getActivity().startService(bgmService);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.cbBackgroundMusic:
                mIsMusicOn = b;
                Configs.sIsMusicOn = mIsMusicOn;
                break;
            default:
                break;
        }
    }
}
