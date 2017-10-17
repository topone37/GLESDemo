package com.tp.gl.demo002;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;

import com.tp.gl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by root on 17-10-10.
 * 加载图片作为纹理
 */

public class Demo012GLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final String VERREXT_SOURECE = "" +
            "attribute vec4 aPosition;" +
            "attribute vec2 aCoordinate;" +
            "varying vec2 vCoordinate;" +
            "uniform mat4 uMatrix;" +
            "varying mat4 vMatrix;" +
            "void main() {" +
            "   gl_Position = uMatrix * aPosition ;" +
            "   vCoordinate = aCoordinate ;" +
            "   vMatrix = uMatrix;" +
            "}";
    private static final String FRAGMENT_SOURCE = " precision mediump float;\n" +
            "    uniform sampler2D vTexture;\n" +
            "    varying vec2 vCoordinate;\n" +
            "    varying mat4 vMatrix;" +
            "    void main(){\n" +
//            " gl_FragColor = texture2D(vTexture,(vMatrix * vec4(vCoordinate - vec2(0.5), 0.0, 1.0)).xy + vec2(0.5));  " +
//            "        gl_FragColor=texture2D(vTexture,vCoordinate);\n" +
            "        gl_FragColor=vec4(1.0,1.0,0.0,1.0);\n" +
            "    }";
    private final float[] sPos = {
//            /************左上角************/
//            -1.0f, 1.0f,
//            -1.0f, 0.0f,
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            /************右上角************/
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            /************左下角************/
//            -1.0f, 0.0f,
//            -1.0f, -1.0f,
//            0.0f, 0.0f,
//            0.0f, -1.0f,
//            /************右下角************/
//            0.0f, 0.0f,
//            0.0f, -1.0f,
//            1.0f, 0.0f,
//            1.0f, -1.0f,
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f


    };
    private final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,

            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,


            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,


    };
    private final int[] index = {
            0, 1, 2,
            2, 1, 3,

            4, 5, 6,
            6, 5, 7,

            8, 9, 10,
            10, 9, 11,

            12, 13, 14,
            14, 13, 15

    };
    private Bitmap mBitmap;
    private float mProjectMatrix[] = new float[16];
    float[] mViewMatrix = new float[16];
    float[] mMVPMatrix = new float[16];
    private int textureId;
    private int mProgram;
    private FloatBuffer mVertexBuff;
    private FloatBuffer mColorBuff;
    private IntBuffer mIndexBuff;

    public Demo012GLSurfaceView(Context context) {
        this(context, null);
    }

    public Demo012GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 8, 0);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);//设置清屏颜色,只有你调用  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);起作用
        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);//分配
        bb.order(ByteOrder.nativeOrder());//一定要用native的字节序 (大小端字节问题)
        mVertexBuff = bb.asFloatBuffer();


        mVertexBuff.put(sPos);/*讲数据填入*/
        mVertexBuff.position(0);/*读/写的数据的索引*/

        ByteBuffer bc = ByteBuffer.allocateDirect(sCoord.length * 4);
        bc.order(ByteOrder.nativeOrder());
        mColorBuff = bc.asFloatBuffer();
        mColorBuff.put(sCoord);
        mColorBuff.position(0);

        ByteBuffer bi = ByteBuffer.allocateDirect(index.length * 4);
        bi.order(ByteOrder.nativeOrder());
        mIndexBuff = bi.asIntBuffer();
        mIndexBuff.put(index);
        mIndexBuff.position(0);


        int vertex_shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);/**创建一个Shader对象*/
        if (vertex_shader != 0) {
            GLES20.glShaderSource(vertex_shader, VERREXT_SOURECE);/**绑定源码*/
            GLES20.glCompileShader(vertex_shader);/**编译*/
            int[] complied = new int[1];
            GLES20.glGetShaderiv(vertex_shader, GLES20.GL_COMPILE_STATUS, complied, 0);
            if (complied[0] == 0) {
                Log.e("GLES", "GL_VERTEX_SHADER Error!!!!" + GLES20.glGetShaderInfoLog(vertex_shader));
                GLES20.glDeleteShader(vertex_shader);
                vertex_shader = 0;
            }

        }
        int fragment_shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragment_shader != 0) {
            GLES20.glShaderSource(fragment_shader, FRAGMENT_SOURCE);
            GLES20.glCompileShader(fragment_shader);
            int[] complied = new int[1];
            GLES20.glGetShaderiv(fragment_shader, GLES20.GL_COMPILE_STATUS, complied, 0);
            if (complied[0] == 0) {
                Log.e("GLES", "GL_FRAGMENT_SHADER Error!!!!");
                GLES20.glDeleteShader(fragment_shader);
                fragment_shader = 0;
            }

        }
        mProgram = GLES20.glCreateProgram();
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, vertex_shader);
            GLES20.glAttachShader(mProgram, fragment_shader);
            GLES20.glLinkProgram(mProgram);
            int linked[] = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked, 0);
            if (linked[0] == 0) {
                Log.e("GLES", "linked Error!!!!");
                GLES20.glDeleteProgram(mProgram);
                mProgram = 0;
            }
        }


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test);
        int w = mBitmap.getWidth();
        int h = mBitmap.getHeight();
        float sWH = w / (float) h;
        float sWidthHeight = width / (float) height;
        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }
        //设置相机位置

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
        requestRender();
    }

    /**
     * 旋转变形（ Matrix.multiplyMM 该方法来整合最初的 矩阵 以及投影矩阵）
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);
        int glHMatrix = GLES20.glGetUniformLocation(mProgram, "uMatrix");
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        float[] mRotationMatrix = new float[16];
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, result, 0);
        int glHPosition = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int glHCoordinate = GLES20.glGetAttribLocation(mProgram, "aCoordinate");
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        int glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");

        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuff);
        //传入纹理坐标
        GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, mColorBuff);
        createTexture(R.mipmap.test, 0);
        GLES20.glUniform1i(glHTexture, 0);
        //传入顶点坐标
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
//        Matrix.setIdentityM(mMVPMatrix, 0);
//        Matrix.setRotateM(mMVPMatrix, 0, 30, 0.5f, 0.5f, 0);
//        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
//        createTexture(R.mipmap.moto, 1);
//        GLES20.glUniform1i(glHTexture, 1);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 1 * 4, 4);
//        Matrix.setIdentityM(mMVPMatrix, 0);
//        Matrix.setRotateM(mMVPMatrix, 0, 30, 0.5f, 0.5f, 0);
//        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
//        createTexture(R.mipmap.moto, 2);
//        GLES20.glUniform1i(glHTexture, 2);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 2 * 4, 4);
//        Matrix.setIdentityM(mMVPMatrix, 0);
//        Matrix.setRotateM(mMVPMatrix, 0, 30, 0.5f, 0.5f, 0);
//        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
//        createTexture(R.mipmap.test, 3);
//        GLES20.glUniform1i(glHTexture, 3);
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 3 * 4, 4);
    }

    private void createTexture(int resId, int i) {
        int[] texture = new int[1];
        mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        if (mBitmap != null && !mBitmap.isRecycled()) {
            //生成纹理
            GLES20.glGenTextures(1, texture, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //根据以上指定的参数，生成一个2D纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        }
    }
}
