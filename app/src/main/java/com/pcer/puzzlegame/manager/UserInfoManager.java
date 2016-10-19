package com.pcer.puzzlegame.manager;

import com.pcer.puzzlegame.engine.SocketRequest;
import com.pcer.puzzlegame.proto.PuzzleGameProto;
import com.pcer.puzzlegame.tool.StringUtil;

import java.util.ArrayList;

/**
 * Created by pcer on 2016.9.4.0004.
 * 用户信息数据中心，通过它实现用户信息的操作
 */
public class UserInfoManager {
    public static final int RESULT_LOCAL_NORMAL = 0;
    public static final int RESULT_LOCAL_USER_EMPTY = 1;
    public static final int RESULT_LOCAL_PWD_EMPTY = 2;
    public static final int RESULT_LOCAL_PWD_DIF = 3;
    public static final int RESULT_LOCAL_NICK_EMPTY = 4;
    public static final int RESULT_LOCAL_PWD_INVALID = 5;

    private static UserInfoManager singleTon;

    private boolean mIsUserSet;
    private PuzzleGameProto.PuzzleGameUserInfo.Builder mUserInfoBuilder;
    private PuzzleGameProto.PuzzleGameUserInfo.Builder mUserInfoBuilderTemp;

    private ArrayList<UserStateObserver> mObservers;

    private UserInfoManager() {
        mIsUserSet = false;
        mObservers = new ArrayList<>();
        mUserInfoBuilderTemp = PuzzleGameProto.PuzzleGameUserInfo.newBuilder();
        mUserInfoBuilder = PuzzleGameProto.PuzzleGameUserInfo.newBuilder();
    }

    public static UserInfoManager getInstance(){
        if(singleTon == null){
            singleTon = new UserInfoManager();
        }
        return singleTon;
    }

    public int login(String userName, String password, final IProtoCallback protoCallback){
        if(StringUtil.isEmpty(userName)){
            return RESULT_LOCAL_USER_EMPTY;
        }
        if(StringUtil.isEmpty(password)){
            return RESULT_LOCAL_PWD_EMPTY;
        }

        mUserInfoBuilderTemp.setUsername(userName).setPassword(password);

        SocketRequest.requestAsync(mUserInfoBuilderTemp,
                PuzzleGameProto.PuzzleGameMessage.MesType.LOGREQ,
                new SocketRequest.IRequestCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        PuzzleGameProto.PuzzleGameMessage message = (PuzzleGameProto.PuzzleGameMessage) object;
                        if(message.getMesType() != PuzzleGameProto.PuzzleGameMessage.MesType.LOGACK){
                            onFailed(null);
                        } else {
                            mIsUserSet = true;
                            mUserInfoBuilder.mergeFrom(message.getPUser(0));
                            protoCallback.onSuccess(message.getPUser(0));
                            notifyUserStateChange();
                        }
                    }

                    @Override
                    public void onFailed(Object object) {
                        mIsUserSet = false;
                        protoCallback.onFailed(null);
                        notifyUserStateChange();
                    }
                });
        return RESULT_LOCAL_NORMAL;
    }

    public int register(String userName, String nickName, String password1,
                        String password2, final IProtoCallback protoCallback){
        if(StringUtil.isEmpty(userName)){
            return RESULT_LOCAL_USER_EMPTY;
        }
        if(StringUtil.isEmpty(nickName)){
            return RESULT_LOCAL_NICK_EMPTY;
        }
        if(StringUtil.isEmpty(password1) || StringUtil.isEmpty(password2)){
            return RESULT_LOCAL_PWD_EMPTY;
        }
        if(!password1.equals(password2)){
            return RESULT_LOCAL_PWD_EMPTY;
        }

        mUserInfoBuilderTemp.setUsername(userName).setNickname(nickName).setPassword(password1);
        SocketRequest.requestAsync(mUserInfoBuilderTemp,
                PuzzleGameProto.PuzzleGameMessage.MesType.REGREQ,
                new SocketRequest.IRequestCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        PuzzleGameProto.PuzzleGameMessage message = (PuzzleGameProto.PuzzleGameMessage)object;
                        if(message.getMesType() == PuzzleGameProto.PuzzleGameMessage.MesType.REGACK){
                            mIsUserSet = true;
                            mUserInfoBuilder.mergeFrom(message.getPUser(0));
                            protoCallback.onSuccess(message.getPUser(0));
                            notifyUserStateChange();
                        } else {
                            onFailed(null);
                        }
                    }

                    @Override
                    public void onFailed(Object object) {
                        mIsUserSet = false;
                        protoCallback.onFailed(null);
                        notifyUserStateChange();
                    }
                });

        return RESULT_LOCAL_NORMAL;
    }

    public int modify(String nickname, String oldPassword,
                      String newPassword1, String newPassword2,
                      final IProtoCallback protoCallback){
        if(StringUtil.isEmpty(nickname)){
            return RESULT_LOCAL_NICK_EMPTY;
        }
        if(StringUtil.isEmpty(newPassword1) || StringUtil.isEmpty(newPassword2)
                || StringUtil.isEmpty(oldPassword)){
            return RESULT_LOCAL_PWD_EMPTY;
        }
        if(!mUserInfoBuilder.getPassword().equals(oldPassword)){
            return RESULT_LOCAL_PWD_INVALID;
        }
        if(!newPassword1.equals(newPassword2)){
            return RESULT_LOCAL_PWD_DIF;
        }

        mUserInfoBuilderTemp.setNickname(nickname).setPassword(newPassword1)
                .setUsername(mUserInfoBuilder.getUsername());

        SocketRequest.requestAsync(mUserInfoBuilderTemp,
                PuzzleGameProto.PuzzleGameMessage.MesType.MODREQ,
                new SocketRequest.IRequestCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        PuzzleGameProto.PuzzleGameMessage message = (PuzzleGameProto.PuzzleGameMessage) object;
                        if(message.getMesType() == PuzzleGameProto.PuzzleGameMessage.MesType.MODACK){
                            mUserInfoBuilder.mergeFrom(message.getPUser(0));
                            mIsUserSet = true;
                            protoCallback.onSuccess(message.getPUser(0));
                            notifyUserStateChange();
                        } else {
                            onFailed(null);
                        }
                    }

                    @Override
                    public void onFailed(Object object) {
                        mIsUserSet = false;
                        protoCallback.onFailed(null);
                        notifyUserStateChange();
                    }
                });
        return RESULT_LOCAL_NORMAL;
    }

    public int rank(final IProtoCallback protoCallback){
        if(mIsUserSet){
            mUserInfoBuilderTemp.setUsername(mUserInfoBuilder.getUsername());
        } else {
            mUserInfoBuilderTemp.clearUsername();
        }

        SocketRequest.requestAsync(mUserInfoBuilderTemp,
                PuzzleGameProto.PuzzleGameMessage.MesType.RANKREQ,
                new SocketRequest.IRequestCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        protoCallback.onSuccess(object);
                    }

                    @Override
                    public void onFailed(Object object) {
                        protoCallback.onFailed(null);
                    }
                });
        return RESULT_LOCAL_NORMAL;
    }

    public void addScore(int plusScore){
        if(!mIsUserSet){
            return;
        }
        plusScore += mUserInfoBuilder.getScores();
        submit(plusScore);
    }

    public void submit(int newScore){
        if(!mIsUserSet){
            return;
        }
        mUserInfoBuilderTemp.setUsername(mUserInfoBuilder.getUsername()).setScores(newScore);

        SocketRequest.requestAsync(mUserInfoBuilderTemp,
                PuzzleGameProto.PuzzleGameMessage.MesType.SUBREQ,
                new SocketRequest.IRequestCallback() {
                    @Override
                    public void onSuccess(Object object) {
                        // TODO: add code as you like
                    }

                    @Override
                    public void onFailed(Object object) {

                    }
                });
    }

    public void logout(){
        mIsUserSet = false;
        notifyUserStateChange();
    }


    public boolean isUserSet(){
        return mIsUserSet;
    }

    public PuzzleGameProto.PuzzleGameUserInfo.Builder getUserInfo(){
        return mUserInfoBuilder;
    }


    private void notifyUserStateChange(){

    }

    private void notifyUserInfoInvalid(){

    }


    public void registerObserver(UserStateObserver observer){
        mObservers.remove(observer);
        mObservers.add(observer);
    }

    public void unRegisterObserver(UserStateObserver observer){
        mObservers.remove(observer);
    }

    public interface UserStateObserver{
        int STATE_LOGIN_SUCCESS = 0;
        int STATE_LOGIN_NO_NET = 1;
        int STATE_LOGIN_FAILED = 2;
        int STATE_LOGOUT = 3;

        void onUserStateChange(int state);
    }

    // 用户信息的回调，通过实现和传递这个接口完成对数据的制定操作，默认在UI线程回调
    public interface IProtoCallback{
        void onSuccess(Object object);
        void onFailed(Object object);
    }
}
