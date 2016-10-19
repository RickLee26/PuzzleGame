package com.pcer.puzzlegame.engine;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.pcer.puzzlegame.Configs;
import com.pcer.puzzlegame.proto.PuzzleGameProto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Objects;

/**
 * Created by pcer on 2016.9.4.0004.
 * 主要的异步信息获取类，通过自身定义的接口实现回调，高度解耦
 */
public class SocketRequest {
    private static final String TAG = "SocketRequest";
    public static void requestAsync(PuzzleGameProto.PuzzleGameUserInfo.Builder builder,
                                    PuzzleGameProto.PuzzleGameMessage.MesType mesType,
                                    final IRequestCallback requestCallback){

        final PuzzleGameProto.PuzzleGameMessage message = PuzzleGameProto.PuzzleGameMessage.newBuilder()
                .setMesType(mesType)
                .addPUser(builder).build();

        Thread requestThread = new Thread(){
            @Override
            public void run() {
                try {
                    Log.e(TAG, "run: ");
                    Socket socket = new Socket(Configs.IP_USER_SERVER,Configs.PORT_USER_SERVER);

                    Log.e(TAG, "socket: ");
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.write(message.toByteArray());

                    byte[] buf = null;
                    if(message.getMesType() == PuzzleGameProto.PuzzleGameMessage.MesType.RANKACK){
                        buf = new byte[200];
                    } else {
                        buf = new byte[2048];
                    }

                    int len = dis.read(buf);
                    Log.e(TAG, "read: ");

                    final byte[] msg = new byte[len];
                    for(int i = 0; i < len; i++){
                        msg[i] = buf[i];
                    }
                    final PuzzleGameProto.PuzzleGameMessage returnMessage = PuzzleGameProto.PuzzleGameMessage.parseFrom(msg);

                    Log.e(TAG, "parsed");

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            requestCallback.onSuccess(returnMessage);
                        }
                    });
                }catch (Exception e){
                    Log.e(TAG, e.getLocalizedMessage());
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            requestCallback.onFailed(null);
                        }
                    });
                }finally {
                    Log.e(TAG, "run: finally");
                }
            }
        };

        requestThread.start();

    }


    // 回调接口，调用这个函数的类只需要实现这个接口就可以做到数据更新，默认将在UI线程回调它，调用者不必考虑切换线程
    public interface IRequestCallback{
        void onSuccess(Object object);
        void onFailed(Object object);
    }
}
