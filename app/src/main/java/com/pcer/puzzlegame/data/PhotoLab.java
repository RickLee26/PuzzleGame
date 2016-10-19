package com.pcer.puzzlegame.data;

import android.content.Context;

import com.pcer.puzzlegame.R;

import java.util.ArrayList;

/**
 * Created by pcer on 2016.9.4.0004.
 */
public class PhotoLab {

    private ArrayList<Photo> mPhotos;
    private static PhotoLab sPhotoLab;
    private Context mContext;

    private PhotoLab(Context appContext){
        mContext = appContext;
        mPhotos = new ArrayList<Photo>();
        int[] itemsId = new int[] {R.drawable.pic1, R.drawable.pic2,R.drawable.pic3,R.drawable.pic4,
                R.drawable.pic5,R.drawable.pic6,R.drawable.pic7,R.drawable.pic8,R.drawable.pic9};
        String[] itemTitle = new String[]{"1","2","3","4","5","6","7","8","9"};

        for (int i=0; i<itemsId.length; i++) {
            mPhotos.add(new Photo(itemsId[i],itemTitle[i]));
        }
    }

    public static PhotoLab get(Context c) {
        if (sPhotoLab == null) {
            sPhotoLab = new PhotoLab(c.getApplicationContext());
        }
        return sPhotoLab;
    }

    public ArrayList<Photo> getPhotos() {
        return mPhotos;
    }
}
