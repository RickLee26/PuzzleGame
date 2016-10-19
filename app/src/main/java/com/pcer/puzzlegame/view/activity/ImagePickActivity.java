package com.pcer.puzzlegame.view.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.pcer.puzzlegame.Configs;
import com.pcer.puzzlegame.R;
import com.pcer.puzzlegame.data.Photo;
import com.pcer.puzzlegame.data.PhotoLab;
import com.pcer.puzzlegame.tool.ScreenUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 选择拼图图片的activity，其中从服务器获取图片资源的行为待实现
 */

public class ImagePickActivity extends AppCompatActivity implements View.OnClickListener{

    private String mFilename = null;

    private final int REQUEST_CODE_CAMERA = 0;
    private final int REQUEST_CODE_ALBUM = 1;
    private final int REQUEST_CODE_CROP = 2;

    private ArrayList<Photo> mPhotos;
    private static Bitmap mBitmap;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pick);
        mPhotos = PhotoLab.get(getApplicationContext()).getPhotos();
        initView();
        mBitmap = Configs.sCurrentBitmap;
    }

    private void initView(){
        Button btnFromCamera, btnFromAlbum, btnFromServer;
        GridView gvImages = (GridView)findViewById(R.id.gvImages);
        btnFromAlbum = (Button)findViewById(R.id.btnFromAlbum);
        btnFromCamera = (Button)findViewById(R.id.btnFromCamera);
        btnFromServer = (Button)findViewById(R.id.btnFromServer);

        final List<Map<String, Object>> listItems = new ArrayList<>();
        for(int i = 0;i < mPhotos.size(); i++){
            Map<String, Object> map = new HashMap<>();
            map.put("ivItem", mPhotos.get(i).getId());
            map.put("tvItemTitle", mPhotos.get(i).getTitle());
            listItems.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.items,
                new String[]{"tvItemTitle", "ivItem"}, new int[]{R.id.tvItemTitle, R.id.ivItem});
        gvImages.setAdapter(adapter);

        gvImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mBitmap = BitmapFactory.decodeResource(getResources(), mPhotos.get(i).getId());
                Configs.sCurrentBitmap = mBitmap;
            }
        });

        btnFromAlbum.setOnClickListener(this);
        btnFromCamera.setOnClickListener(this);
        btnFromServer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnFromCamera:
                File outputImage = new File(Environment.getExternalStorageDirectory(), "output_image.jpg");
                try{
                    if(outputImage.exists()){
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (Exception e){
                    e.printStackTrace();
                }

                mUri = Uri.fromFile(outputImage);
                Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(cameraIntent,  REQUEST_CODE_CAMERA);
                break;
            case R.id.btnFromAlbum:
                Intent albumIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                albumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "images/*");
                startActivityForResult(albumIntent, REQUEST_CODE_ALBUM);
                break;
            case R.id.btnFromServer:
                Toast.makeText(getApplicationContext(), "待实现", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK){
            return;
        }

        switch (requestCode){
            case REQUEST_CODE_CAMERA:
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mUri, "image/*");
                intent.putExtra("scale", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                startActivityForResult(intent, REQUEST_CODE_CROP);
                break;
            case REQUEST_CODE_CROP:
                try {
                    mBitmap = handlerImage(BitmapFactory.decodeStream(getContentResolver().openInputStream(mUri)));
                    Configs.sCurrentBitmap = mBitmap;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case REQUEST_CODE_ALBUM:
                mUri = Uri.parse(data.getData().toString());
                mBitmap = handlerImage(compressBitmap(null, null, this, mUri, 4, false));
                Configs.sCurrentBitmap = mBitmap;
                break;
            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode,event);
        }
    }


    /**图片压缩处理，size参数为压缩比，比如size为2，则压缩为1/4**/
    private Bitmap compressBitmap(String path, byte[] data, Context context, Uri uri, int size, boolean width) {
        BitmapFactory.Options options = null;
        if (size > 0) {
            BitmapFactory.Options info = new BitmapFactory.Options();
/**如果设置true的时候，decode时候Bitmap返回的为数据将空*/
            info.inJustDecodeBounds = false;
            decodeBitmap(path, data, context, uri, info);
            int dim = info.outWidth;
            if (!width) dim = Math.max(dim, info.outHeight);
            options = new BitmapFactory.Options();
/**把图片宽高读取放在Options里*/
            options.inSampleSize = size;
        }
        Bitmap bm = null;
        try {
            bm = decodeBitmap(path, data, context, uri, options);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**把byte数据解析成图片*/
    private Bitmap decodeBitmap(String path, byte[] data, Context context, Uri uri, BitmapFactory.Options options) {
        Bitmap result = null;
        if (path != null) {
            result = BitmapFactory.decodeFile(path, options);
        }
        else if (data != null) {
            result = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        }
        else if (uri != null) {
            ContentResolver cr = context.getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = cr.openInputStream(uri);
                result = BitmapFactory.decodeStream(inputStream, null, options);
                inputStream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Bitmap handlerImage(Bitmap bitmap) {
        // 将图片放大到固定尺寸
        int screenWidth = ScreenUtil.getScreenSize(this).widthPixels;
        int screenHeigt = ScreenUtil.getScreenSize(this).heightPixels;
        Matrix matrix = new Matrix();
        matrix.postScale(screenWidth * 0.8f / bitmap.getWidth(), screenHeigt * 0.6f / bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
