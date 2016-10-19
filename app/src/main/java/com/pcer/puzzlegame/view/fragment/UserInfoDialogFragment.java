package com.pcer.puzzlegame.view.fragment;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.manager.UserInfoManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserInfoDialogFragment extends DialogFragment implements View.OnClickListener{

    public static final String TAG = "UserInfoDialogFragment";

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_user_info_dialog, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        return mView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(UserInfoManager.getInstance().isUserSet()){
            ((TextView)mView.findViewById(R.id.tvUserInfoName))
                    .setText(String.format(getResources().getString(R.string.user_info_name),
                            UserInfoManager.getInstance().getUserInfo().getUsername()));
            ((TextView)mView.findViewById(R.id.tvUserInfoNickName))
                    .setText(String.format(getResources().getString(R.string.user_info_nickname),
                            UserInfoManager.getInstance().getUserInfo().getNickname()));
            ((TextView)mView.findViewById(R.id.tvUserInfoMark))
                    .setText(String.format(getResources().getString(R.string.user_info_scores),
                            UserInfoManager.getInstance().getUserInfo().getScores()));
        }

        mView.findViewById(R.id.ivUserImage).setOnClickListener(this);
        mView.findViewById(R.id.btnCheckRank).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.ivUserImage:
                break;
            case R.id.btnCheckRank:
                RankDialogFragment rankDialogFragment = new RankDialogFragment();
                rankDialogFragment.show(getFragmentManager(), RankDialogFragment.TAG);
                break;
        }
    }
}
