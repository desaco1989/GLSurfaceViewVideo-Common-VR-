package com.asha.md360player4android;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.asha.md360player4android.commonVideo.GLViewMediaActivity;
import com.asha.md360player4android.commonVideo.MainActivity;

/**
 * Created by hzqiujiadi on 16/1/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DemoActivity extends AppCompatActivity {

    public static final String sPath = "file:///mnt/sdcard/vr/";

    //public static final String sPath = "file:////storage/sdcard1/vr/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        final EditText et = (EditText) findViewById(R.id.edit_text_url);

        SparseArray<String> data = new SparseArray<>();

        data.put(data.size(), getDrawableUri(R.drawable.bitmap360).toString());
        data.put(data.size(), getDrawableUri(R.drawable.texture).toString());
        data.put(data.size(), getDrawableUri(R.drawable.dome_pic).toString());
        data.put(data.size(), getDrawableUri(R.drawable.stereo).toString());
        data.put(data.size(), getDrawableUri(R.drawable.multifisheye).toString());
        data.put(data.size(), getDrawableUri(R.drawable.multifisheye2).toString());
        data.put(data.size(), getDrawableUri(R.drawable.fish2sphere180sx2).toString());
        data.put(data.size(), getDrawableUri(R.drawable.fish2sphere180s).toString());

        data.put(data.size(), "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");
        data.put(data.size(), sPath + "ch0_160701145544.ts");
        data.put(data.size(), sPath + "videos_s_4.mp4");
        data.put(data.size(), sPath + "28.mp4");
        data.put(data.size(), sPath + "haha.mp4");
        data.put(data.size(), sPath + "halfdome.mp4");
        data.put(data.size(), sPath + "dome.mp4");
        data.put(data.size(), sPath + "stereo.mp4");
        data.put(data.size(), sPath + "look25fps3M.mp4");
        data.put(data.size(), "http://10.240.131.39/vr/570624aae1c52.mp4");
        data.put(data.size(), "http://192.168.5.106/vr/570624aae1c52.mp4");
        data.put(data.size(), sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4");

        data.put(data.size(), "http://cache.utovr.com/201508270528174780.m3u8");
        data.put(data.size(), sPath + "AGSK6416.jpg");
        data.put(data.size(), sPath + "IJUN2902.jpg");
        data.put(data.size(), sPath + "SUYZ2954.jpg");
        data.put(data.size(), sPath + "TEJD0097.jpg");
        data.put(data.size(), sPath + "WSGV6301.jpg");

        SpinnerHelper.with(this)
                .setData(data)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        et.setText(value);
                    }
                })
                .init(R.id.spinner_url);

        findViewById(R.id.video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//asVideo
                String url = et.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    url = "http://video.netwin.cn/a0315d42031144cca1062fcbfd533bcb/5b89d15323c24cdda1f7f72f077749d2-a5b7d8911cc7d347a9c9dd7e9b1d521b.mp4";
//                    url = "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp";
                    MD360PlayerActivity.startVideo(DemoActivity.this, Uri.parse(url));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bitmap_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//asBitmap
                String url = et.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    MD360PlayerActivity.startBitmap(DemoActivity.this, Uri.parse(url));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.ijk_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//ijkVideo
                String url = et.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    url = "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp";
                    IjkPlayerDemoActivity.start(DemoActivity.this, Uri.parse(url));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //play_common_video
        findViewById(R.id.play_common_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commonVideoIntent = new Intent();
                commonVideoIntent.setClass(DemoActivity.this, MainActivity.class);
                startActivity(commonVideoIntent);
            }
        });

        findViewById(R.id.play_common_video2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commonVideoIntent = new Intent();
                commonVideoIntent.setClass(DemoActivity.this, GLViewMediaActivity.class);
                startActivity(commonVideoIntent);
            }
        });
    }

    private Uri getDrawableUri(@DrawableRes int resId){
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }
}
