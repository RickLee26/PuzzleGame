package com.pcer.puzzlegame.view.fragment;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.manager.UserInfoManager;
import com.pcer.puzzlegame.tool.ScreenUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterDialogFragment extends DialogFragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "RegisterDialogFragment";

    private View mView, mRootView;
    private AnimationSet mFadeInAnim;

    private EditText mEtUsername, mEtNickname, mEtPassword, mEtPasswordConfirm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getContext() != null){
            mFadeInAnim = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.dialog_fade_in);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_register_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mRootView = getDialog().getWindow().getDecorView().findViewById(android.R.id.content);
        getDialog().setCanceledOnTouchOutside(false);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setLayout((int)(0.84* ScreenUtil.getScreenSize(getActivity()).widthPixels),
                (int)(0.75* ScreenUtil.getScreenSize(getActivity()).heightPixels));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mView = getView();
        mEtUsername = (EditText)mView.findViewById(R.id.et_username_signin);
        mEtNickname = (EditText)mView.findViewById(R.id.et_nickname);
        mEtPassword = (EditText)mView.findViewById(R.id.et_userpassword_signin);
        mEtPasswordConfirm = (EditText)mView.findViewById(R.id.et_userpassword_confirm);
        mView.findViewById(R.id.btn_signin).setOnClickListener(this);
        mView.findViewById(R.id.tv_back_to_login).setOnClickListener(this);
        ((CheckBox)mView.findViewById(R.id.cb_show_userpassword_signin)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b){
            mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mEtPasswordConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mEtPasswordConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_signin:
                String username = mEtUsername.getText().toString().trim();
                String nickname = mEtNickname.getText().toString().trim();
                String password = mEtPassword.getText().toString().trim();
                String passwordconfirm = mEtPasswordConfirm.getText().toString().trim();

                int checkResult = UserInfoManager.getInstance().register(username, nickname,
                        password, passwordconfirm, new UserInfoManager.IProtoCallback() {
                            @Override
                            public void onSuccess(Object object) {
                                showMsg("注册成功");
                                dismiss();
                            }

                            @Override
                            public void onFailed(Object object) {
                                showMsg("注册失败，请检查输入和网络");
                            }
                        });
                handleResult(checkResult);
                break;
            case R.id.tv_back_to_login:
                dismiss();
                LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
                loginDialogFragment.show(getFragmentManager(), TAG);
                break;
            default:break;
        }
    }
    private void showMsg(String msg){
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
    private void handleResult(int result){
        if (result == UserInfoManager.RESULT_LOCAL_USER_EMPTY){
            showMsg("请输入用户名");
        } else if (result == UserInfoManager.RESULT_LOCAL_PWD_EMPTY) {
            showMsg("请输入密码");
        } else if (result == UserInfoManager.RESULT_LOCAL_PWD_DIF) {
            showMsg("密码不一致，请重新输入");
        } else if (result == UserInfoManager.RESULT_LOCAL_NICK_EMPTY) {
            showMsg("请输入昵称");
        } else if (result == UserInfoManager.RESULT_LOCAL_NORMAL){
            showMsg("正在联网……");
        } else if (result == UserInfoManager.RESULT_LOCAL_PWD_INVALID){
            showMsg("密码错误");
        }
    }
}
