package com.tp.gl.demo001;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 圆形
 */

public class Demo003GLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private int mProgram;
    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n" +
                    "uniform mat4 vMatrix;\n" +
                    "void main() {\n" +
                    "  gl_Position = vMatrix*vPosition;\n" +
                    "}";
    //gl_Position和gl_FragColor都是Shader的内置变量，分别为定点位置和片元颜色。
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    " uniform vec4 vColor;\n" +
                    " void main() {\n" +
                    "     gl_FragColor = vColor;\n" +
                    " }";

    private FloatBuffer mVertexBuff;

    public Demo003GLSurfaceView(Context context) {
        this(context, null);
    }

    public Demo003GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private float[] vertexs = null;


    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        setRenderer(this);/**必须要先设置回调接口,在设置渲染模式*/
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        vertexs = createPositions();

    }


    private float[] createPositions() {
        ArrayList<Float> data = new ArrayList<>();
        data.add(0.0f);             //设置圆心坐标
        data.add(0.0f);
        data.add(0.0f);
        float angDegSpan = 1;
        for (float i = 0; i < 360 + angDegSpan; i += angDegSpan) {
            data.add((float) (1.0f * Math.sin(i * Math.PI / 180f)));
            data.add((float) (1.0f * Math.cos(i * Math.PI / 180f)));
            data.add(0.0f);
        }
        float[] f = new float[data.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = data.get(i);
        }
        return f;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);//设置清屏颜色,只有你调用  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);起作用
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexs.length * 4);//分配
        bb.order(ByteOrder.nativeOrder());//一定要用native的字节序 (大小端字节问题)
        mVertexBuff = bb.asFloatBuffer();


        mVertexBuff.put(vertexs);/*讲数据填入*/
        mVertexBuff.position(0);/**读/写的数据的索引*/

        int vertex_shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);/**创建一个Shader对象*/
        if (vertex_shader != 0) {
            GLES20.glShaderSource(vertex_shader, VERTEX_SHADER);/**绑定源码*/
            GLES20.glCompileShader(vertex_shader);/**编译*/
            int[] complied = new int[1];
            GLES20.glGetShaderiv(vertex_shader, GLES20.GL_COMPILE_STATUS, complied, 0);
            if (complied[0] == 0) {
                Log.e("GLES", "GL_VERTEX_SHADER Error!!!!");
                GLES20.glDeleteShader(vertex_shader);
                vertex_shader = 0;
            }

        }
        int fragment_shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragment_shader != 0) {
            GLES20.glShaderSource(fragment_shader, FRAGMENT_SHADER);
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

    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];


    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //计算宽高比
        float ratio = (float) width / height;
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    private static final float[] COLOR = {1.0f, 1.0f, 1.0f, 1.0f};//white

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);


        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, mVertexBuff);
        int aColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(aColorHandle, 1, COLOR, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexs.length / 3);

        GLES20.glDisableVertexAttribArray(positionHandle);

    }
}
