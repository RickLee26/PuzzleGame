package com.pcer.puzzlegame.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.manager.UserInfoManager;
import com.pcer.puzzlegame.proto.PuzzleGameProto;
import com.pcer.puzzlegame.tool.ScreenUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RankDialogFragment extends DialogFragment {

    public static final String TAG = "RankDialogFragment";

    private List<PuzzleGameProto.PuzzleGameUserInfo> mPuzzleUsers;
    private View mRootView, mView;
    private ListView lvRank;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPuzzleUsers = new ArrayList<>();
        UserInfoManager.getInstance().rank(new UserInfoManager.IProtoCallback() {
            @Override
            public void onSuccess(Object object) {
                PuzzleGameProto.PuzzleGameMessage message = (PuzzleGameProto.PuzzleGameMessage)object;
                mPuzzleUsers.addAll(message.getPUserList());
                onRefresh();
            }

            @Override
            public void onFailed(Object object) {
                Toast.makeText(getActivity(), "获取失败", Toast.LENGTH_SHORT).show();;
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_rank_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        AnimationSet fadeInAnim = (AnimationSet) AnimationUtils.loadAnimation(getActivity(), R.anim.dialog_fade_in);
        mRootView = getDialog().getWindow().getDecorView().findViewById(android.R.id.content);
        mRootView.startAnimation(fadeInAnim);
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.rank_title);
        lvRank = (ListView) mView.findViewById(R.id.lvRank);
        onRefresh();
    }

    private void onRefresh(){

        ArrayList<HashMap<String, String>> myList = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("tvRank", "排名");
        map.put("tvRankUserNickName", "昵称");
        map.put("tvRankScores", "积分");
        myList.add(map);
        for(int i=0;i<mPuzzleUsers.size();i++)
        {
            map = new HashMap<>();
            map.put("tvRank", mPuzzleUsers.get(i).getPassword());
            map.put("tvRankUserNickName", mPuzzleUsers.get(i).getNickname());
            map.put("tvRankScores", mPuzzleUsers.get(i).getScores() + "");
            myList.add(map);
        }
        //生成适配器，数组===》ListItem
        SimpleAdapter mSchedule = new SimpleAdapter(getActivity(), //没什么解释
                myList,//数据来源
                R.layout.rank_items_list,//ListItem的XML实现

                //动态数组与ListItem对应的子项
                new String[] {"tvRank", "tvRankUserNickName", "tvRankScores"},

                //ListItem的XML文件里面的两个TextView ID
                new int[] {R.id.tvRank,R.id.tvRankUserNickName,R.id.tvRankScores});
        //添加并且显示
        lvRank.setAdapter(mSchedule);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout((int) (0.84 * ScreenUtil.getScreenSize(getActivity()).widthPixels),
                (int) (0.63 * ScreenUtil.getScreenSize(getActivity()).heightPixels));
    }
}
