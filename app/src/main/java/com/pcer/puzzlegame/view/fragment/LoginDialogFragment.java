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
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.manager.UserInfoManager;
import com.pcer.puzzlegame.proto.PuzzleGameProto;

public class LoginDialogFragment extends DialogFragment implements View.OnClickListener
        , CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "LoginDialogFragment";

    private EditText mEtUsername, mEtPassword;
    private AnimationSet mFadeInAnim;
    private View mView, mRootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            mFadeInAnim = (AnimationSet) AnimationUtils.loadAnimation(getContext(), R.anim.dialog_fade_in);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login_dialog, container, false);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mRootView = getDialog().getWindow().getDecorView().findViewById(android.R.id.content);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mRootView.startAnimation(mFadeInAnim);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mView = getView();
        mEtUsername = (EditText) mView.findViewById(R.id.et_username);
        mEtPassword = (EditText) mView.findViewById(R.id.et_userpassword);
        mView.findViewById(R.id.btn_login).setOnClickListener(this);
        ((CheckBox) (mView.findViewById(R.id.cb_show_userpassword))).setOnCheckedChangeListener(this);
        mView.findViewById(R.id.tv_jmpto_signin).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                String username = mEtUsername.getText().toString();
                String password = mEtPassword.getText().toString();

                int checkResult = UserInfoManager.getInstance().login(username, password, new UserInfoManager.IProtoCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        showMsg("登陆成功！");
                        dismiss();
                    }

                    @Override
                    public void onFailed(Object object) {
                        showMsg("登录失败，请检查输入和网络");
                    }
                });

                handleResult(checkResult);

                break;
            case R.id.tv_jmpto_signin:
                dismiss();
                RegisterDialogFragment registerDialogFragment = new RegisterDialogFragment();
                registerDialogFragment.show(getFragmentManager(), TAG);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        } else {
            mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
    }

    Toast toast;

    private void showMsg(String msg) {
        if (toast != null) {
            toast.cancel();
            toast.setText(msg);
        } else {
            toast = Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    private void handleResult(int result) {
        if (result == UserInfoManager.RESULT_LOCAL_USER_EMPTY) {
            showMsg("请输入用户名");
        } else if (result == UserInfoManager.RESULT_LOCAL_PWD_EMPTY) {
            showMsg("请输入密码");
        } else if (result == UserInfoManager.RESULT_LOCAL_PWD_DIF) {
            showMsg("密码不一致，请重新输入");
        } else if (result == UserInfoManager.RESULT_LOCAL_NICK_EMPTY) {
            showMsg("请输入昵称");
        } else if (result == UserInfoManager.RESULT_LOCAL_NORMAL) {
            showMsg("正在联网……");
        } else if (result == UserInfoManager.RESULT_LOCAL_PWD_INVALID) {
            showMsg("密码错误");
        }
    }

}
