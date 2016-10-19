package com.pcer.puzzlegame.view.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.pcer.puzzlegame.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GameInfoFragment extends DialogFragment implements View.OnClickListener{

    public static final String TAG = "ameInfoFragment";

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_game_info, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        return mView;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }
}
