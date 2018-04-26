在MD360 VR- https://github.com/ashqal/MD360Player4Android，非常感谢MD360的作者，在此基础上新增GLSurfaceView播放普通视频。

## NOTICE
* OpenGLES 2.0 required
* Android 4.0.3 (Ice Cream Sandwich  API-15) required
* Compatible with all Players which have `setSurface` api.
* This library do nothing but render the image of video frame, so you may deal with the issues about `MediaPlayer` or `IjkMediaPlayer` (e.g. play local file, rtmp, hls) by yourself;
* 这个库只负责视频帧画面的渲染，所有的视频文件播放、控制的工作都交给了`MediaPlayer`或者`IjkMediaPlayer`，你可能需要自己处理使用Player过程中出现的问题(比如播放本地文件、rtmp、hls).
* [Working with vlc](https://github.com/ashqal/MD-vlc-sample)

## Gradle
```java
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
```java
dependencies {
    compile 'com.github.ashqal:MD360Player4Android:2.4.0'
}
```

## USAGE
### Using with GLSurfaceView
**STEP1** Define `GLSurfaceView` in the layout xml.
```java
<android.opengl.GLSurfaceView
   android:id="@+id/surface_view"
   android:layout_width="match_parent"
   android:layout_height="match_parent" />
```

**STEP2** Init the `MDVRLibrary` in the Activity.
```java
public class MDVRLibraryDemoActivity extends Activity {

    private MDVRLibrary mVRLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md_render);

        // init VR Library
        initVRLibrary();
    }

    private void initVRLibrary(){
        // new instance
        mVRLibrary = MDVRLibrary.with(this)
                    .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                    .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                    .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                        @Override
                        public void onSurfaceReady(Surface surface) {
                            // IjkMediaPlayer or MediaPlayer
                            getPlayer().setSurface(surface);
                        }
                    })
                    .build(R.id.surface_view);
    }
}
```

**STEP3** Addition call in `onResume` `onPause` `onDestroy` `onConfigurationChanged`.
```java
public class MDVRLibraryDemoActivity extends MediaPlayerActivity {

    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }
}
```