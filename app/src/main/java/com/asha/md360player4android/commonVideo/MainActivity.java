package com.asha.md360player4android.commonVideo;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Surface;
import android.widget.Toast;

import com.asha.md360player4android.R;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    private GLSurfaceView glSurfaceView;
    private SurfaceTexture mSurface;
    int mTextureID = -1;
    private OpenglRender mDirectDrawer;
    private Surface surface;
    private Uri mVideoPathUri;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);


        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(this);
        //RENDERMODE_WHEN_DIRTY   RENDERMODE_CONTINUOUSLY
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mediaPlayer = new MediaPlayer();
        mVideoPathUri = Uri.parse("android.resource://" + this.getPackageName() + "/" + R.raw.tusdk_sample_splice_video);
        String url = "";

        try {
			 mediaPlayer.setDataSource(this, mVideoPathUri);
//            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        mediaPlayer.setLooping(true);
        Toast.makeText(this, "马上开始播放！", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mTextureID = createTextureID();
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);
        mDirectDrawer = new OpenglRender(mTextureID);
        surface = new Surface(mSurface);
        mediaPlayer.setSurface(surface);
        surface.release();
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mSurface.updateTexImage();
        mDirectDrawer.draw(mtx);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        glSurfaceView.requestRender();

    }

    public int createTextureID() {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.bringToFront();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        glSurfaceView.onPause();
    }
}
