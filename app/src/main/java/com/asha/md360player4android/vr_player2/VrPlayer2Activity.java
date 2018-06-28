package com.asha.md360player4android.vr_player2;

import android.app.Activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.Surface;

import android.view.ViewGroup;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;


/**
 * Created by desaco on 2018/4/25.
 *
 * https://www.jianshu.com/p/254837f663c4
 * opengl显示全景图片和opengl播放视频
 */

public class VrPlayer2Activity extends Activity {
    //com.asha.md360player4android.vr_player2.VrPlayer2Activity
    //
    GLSurfaceView glSurfaceView;
    public float mAngleX = 0;// 摄像机所在的x坐标
    public float mAngleY = 0;// 摄像机所在的y坐标
    public float mAngleZ = 1;// 摄像机所在的z坐标

    public static String VL = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";
    public static String FL = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform samplerExternalOES s_texture;" +

            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";

    MediaPlayer mediaPlayerWrapper;
    RenderListener renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        setContentView(glSurfaceView);

        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new RenderListener();
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        new Thread(new Runnable() {
            @Override
            public void run() {

                mediaPlayerWrapper = new MediaPlayer();
                //mediaPlayerWrapper.init();
                mediaPlayerWrapper.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.start();
                    }
                });

                mediaPlayerWrapper.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayerWrapper.setSurface(renderer.getSurface());

                try {
                   // 点播： http://www.w3school.com.cn/example/html5/mov_bbb.mp4
                   // VR:  http://video.netwin.cn/a0315d42031144cca1062fcbfd533bcb/5b89d15323c24cdda1f7f72f077749d2-a5b7d8911cc7d347a9c9dd7e9b1d521b.mp4

//                    mediaPlayerWrapper.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/a_rtmp_video/vr1.mp4");
//                    String url = "http://www.w3school.com.cn/example/html5/mov_bbb.mp4";
                    String url = "http://video.netwin.cn/a0315d42031144cca1062fcbfd533bcb/5b89d15323c24cdda1f7f72f077749d2-a5b7d8911cc7d347a9c9dd7e9b1d521b.mp4";
                    mediaPlayerWrapper.setDataSource(url);

                    mediaPlayerWrapper.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    float startRawX;
    float startRawY;

    double xFlingAngle;
    double xFlingAngleTemp;

    double yFlingAngle;
    double yFlingAngleTemp;

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        //处理手指滑动事件，我这里的处理是判断手指在横向和竖向滑动的距离
        //这个距离隐射到球体上经度和纬度的距离，根据这个距离计算三维空间的两个
        //夹角，根据这个夹角调整摄像机所在位置
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            startRawX = me.getRawX();
            startRawY = me.getRawY();
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {

            float distanceX = startRawX - me.getRawX();
            float distanceY = startRawY - me.getRawY();

            //这里的0.1f是为了不上摄像机移动的过快
            distanceY = 0.1f * (distanceY) / getWindowManager().getDefaultDisplay().getHeight();

            yFlingAngleTemp = distanceY * 180 / (Math.PI * 3);

            if (yFlingAngleTemp + yFlingAngle > Math.PI / 2) {
                yFlingAngleTemp = Math.PI / 2 - yFlingAngle;
            }
            if (yFlingAngleTemp + yFlingAngle < -Math.PI / 2) {
                yFlingAngleTemp = -Math.PI / 2 - yFlingAngle;
            }
            //这里的0.1f是为了不上摄像机移动的过快
            distanceX = 0.1f * (-distanceX) / getWindowManager().getDefaultDisplay().getWidth();
            xFlingAngleTemp = distanceX * 180 / (Math.PI * 3);


            mAngleX = (float) (Math.cos(yFlingAngle + yFlingAngleTemp) * Math.sin(xFlingAngle + xFlingAngleTemp));

            mAngleY = -(float) (Math.sin(yFlingAngle + yFlingAngleTemp));


            mAngleZ = (float) (Math.cos(yFlingAngle + yFlingAngleTemp) * Math.cos(xFlingAngle + xFlingAngleTemp));

            glSurfaceView.requestRender();
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            xFlingAngle += xFlingAngleTemp;
            yFlingAngle += yFlingAngleTemp;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayerWrapper != null)
            mediaPlayerWrapper.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayerWrapper != null)
            mediaPlayerWrapper.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayerWrapper != null)
            mediaPlayerWrapper.start();
    }

    class RenderListener implements GLSurfaceView.Renderer {

        FloatBuffer verticalsBuffer;

        int CAP = 9;//绘制球体时，每次增加的角度
        float[] verticals = new float[(180 / CAP) * (360 / CAP) * 6 * 3];


        private final FloatBuffer mUvTexVertexBuffer;

        private final float[] UV_TEX_VERTEX = new float[(180 / CAP) * (360 / CAP) * 6 * 2];

        private int mProgram;
        private int mPositionHandle;
        private int mTexCoordHandle;
        private int mMatrixHandle;
        private int mTexSamplerHandle;


        private final float[] mProjectionMatrix = new float[16];
        private final float[] mCameraMatrix = new float[16];
        private final float[] mMVPMatrix = new float[16];


        private int[] mTexNames;

        private SurfaceTexture surfaceTexture;
        private boolean isAvailiable;
        public Surface surface;
        float r = 6f;//球体半径

        public RenderListener() {

            float x = 0;
            float y = 0;
            float z = 0;


            int index = 0;
            int index1 = 0;
            double d = CAP * Math.PI / 180;//每次递增的弧度
            for (int i = 0; i < 180; i += CAP) {
                double d1 = i * Math.PI / 180;
                for (int j = 0; j < 360; j += CAP) {
                    //获得球体上切分的超小片矩形的顶点坐标（两个三角形组成，所以有六点顶点）
                    double d2 = j * Math.PI / 180;
                    verticals[index++] = (float) (x + r * Math.sin(d1 + d) * Math.cos(d2 + d));
                    verticals[index++] = (float) (y + r * Math.cos(d1 + d));
                    verticals[index++] = (float) (z + r * Math.sin(d1 + d) * Math.sin(d2 + d));
                    //获得球体上切分的超小片三角形的纹理坐标
                    UV_TEX_VERTEX[index1++] = (j + CAP) * 1f / 360;
                    UV_TEX_VERTEX[index1++] = (i + CAP) * 1f / 180;

                    verticals[index++] = (float) (x + r * Math.sin(d1) * Math.cos(d2));
                    verticals[index++] = (float) (y + r * Math.cos(d1));
                    verticals[index++] = (float) (z + r * Math.sin(d1) * Math.sin(d2));

                    UV_TEX_VERTEX[index1++] = j * 1f / 360;
                    UV_TEX_VERTEX[index1++] = i * 1f / 180;

                    verticals[index++] = (float) (x + r * Math.sin(d1) * Math.cos(d2 + d));
                    verticals[index++] = (float) (y + r * Math.cos(d1));
                    verticals[index++] = (float) (z + r * Math.sin(d1) * Math.sin(d2 + d));

                    UV_TEX_VERTEX[index1++] = (j + CAP) * 1f / 360;
                    UV_TEX_VERTEX[index1++] = i * 1f / 180;

                    verticals[index++] = (float) (x + r * Math.sin(d1 + d) * Math.cos(d2 + d));
                    verticals[index++] = (float) (y + r * Math.cos(d1 + d));
                    verticals[index++] = (float) (z + r * Math.sin(d1 + d) * Math.sin(d2 + d));

                    UV_TEX_VERTEX[index1++] = (j + CAP) * 1f / 360;
                    UV_TEX_VERTEX[index1++] = (i + CAP) * 1f / 180;

                    verticals[index++] = (float) (x + r * Math.sin(d1 + d) * Math.cos(d2));
                    verticals[index++] = (float) (y + r * Math.cos(d1 + d));
                    verticals[index++] = (float) (z + r * Math.sin(d1 + d) * Math.sin(d2));

                    UV_TEX_VERTEX[index1++] = j * 1f / 360;
                    UV_TEX_VERTEX[index1++] = (i + CAP) * 1f / 180;

                    verticals[index++] = (float) (x + r * Math.sin(d1) * Math.cos(d2));
                    verticals[index++] = (float) (y + r * Math.cos(d1));
                    verticals[index++] = (float) (z + r * Math.sin(d1) * Math.sin(d2));

                    UV_TEX_VERTEX[index1++] = j * 1f / 360;
                    UV_TEX_VERTEX[index1++] = i * 1f / 180;


                }
            }
            verticalsBuffer = ByteBuffer.allocateDirect(verticals.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(verticals);
            verticalsBuffer.position(0);


            mUvTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(UV_TEX_VERTEX);
            mUvTexVertexBuffer.position(0);
        }

        private Surface getSurface() {
            if (surface != null) {
                return surface;
            }
            int externalTextureId = -1;
            mTexNames = new int[1];

            GLES20.glGenTextures(1, mTexNames, 0);
            externalTextureId = mTexNames[0];

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(
                    GL_TEXTURE_EXTERNAL_OES,
                    externalTextureId);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameterf(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(
                    GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);


            surfaceTexture = new SurfaceTexture(externalTextureId);
            surfaceTexture.setDefaultBufferSize(100, 100);
            surfaceTexture.setOnFrameAvailableListener(
                    new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            isAvailiable = true;
                            glSurfaceView.requestRender();
                        }
                    });

            surface = new Surface(surfaceTexture);
            return surface;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            getSurface();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {


            mProgram = GLES20.glCreateProgram();

            int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, VL);
            GLES20.glCompileShader(vertexShader);

            int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, FL);
            GLES20.glCompileShader(fragmentShader);

            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);

            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");


            if (width < height) {
                float ratio = height * 1f / width;
                Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 1f, 1000f);
            } else {
                float ratio = width * 1f / height;
                Matrix.perspectiveM(mProjectionMatrix, 0, 70f, ratio, 1, 1000f);
            }
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (isAvailiable) {
                surfaceTexture.updateTexImage();
                isAvailiable = false;
            }
            //调整摄像机焦点位置，使画面滚动
            Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 0, mAngleX, mAngleY, mAngleZ, 0, 1, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, verticalsBuffer);
            GLES20.glEnableVertexAttribArray(mTexCoordHandle);
            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                    mUvTexVertexBuffer);
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform1i(mTexSamplerHandle, 0);


            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (180 / CAP) * (360 / CAP) * 6);

            GLES20.glDisableVertexAttribArray(mPositionHandle);

        }
    }

}
