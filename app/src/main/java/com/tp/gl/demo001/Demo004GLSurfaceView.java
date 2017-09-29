package com.tp.gl.demo001;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * 立方体
 */

public class Demo004GLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private static final float[] vertexs = {
            -1.0f, 1.0f, 1.0f,//top left
            -1.0f, -1.0f, 1.0f,//bottom left
            1.0f, -1.0f, 1.0f,//bottom right
            1.0f, 1.0f, 1.0f,//top right


            -1.0f, 1.0f, -1.0f,//top left
            -1.0f, -1.0f, -1.0f,//bottom left
            1.0f, -1.0f, -1.0f,//bottom right
            1.0f, 1.0f, -1.0f//top right


    };
    private static final short[] index = {
            7, 4, 5, 7, 5, 6,//背面
            3, 7, 6, 3, 6, 2,//右面
            2, 6, 5, 2, 5, 1,//下面
            0, 3, 2, 0, 2, 1,//正面
            4, 0, 1, 4, 1, 5,//左面
            4, 7, 3, 4, 3, 0//上面
    };

    private static final float[] color = {
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,


            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,

    };



    private int mProgram;
    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n" +
                    "uniform mat4 vMatrix;\n" +
                    "varying vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {\n" +
                    "  gl_Position = vMatrix*vPosition;\n" +
                    "  vColor = aColor;" +
                    "}";
    //gl_Position和gl_FragColor都是Shader的内置变量，分别为定点位置和片元颜色。
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    " varying vec4 vColor;\n" +
                    " void main() {\n" +
                    "     gl_FragColor = vColor;\n" +
                    " }";

    private FloatBuffer mVertexBuff;
    private FloatBuffer mColorBuff;
    private ShortBuffer mIndexBuff;

    public Demo004GLSurfaceView(Context context) {
        this(context, null);
    }

    public Demo004GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);//88800->888160 depth size加上/*todo 坑爹呀 我屮艸芔茻*/
        setRenderer(this);/**必须要先设置回调接口,在设置渲染模式*/
        setRenderMode(RENDERMODE_CONTINUOUSLY);

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
       // GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);//设置清屏颜色,只有你调用  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);起作用
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);//setEGLConfigChooser一定要加DepthSize

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexs.length * 4);//分配
        bb.order(ByteOrder.nativeOrder());//一定要用native的字节序 (大小端字节问题)
        mVertexBuff = bb.asFloatBuffer();


        mVertexBuff.put(vertexs);/*讲数据填入*/
        mVertexBuff.position(0);/**读/写的数据的索引*/

        ByteBuffer ii = ByteBuffer.allocateDirect(index.length *2);//分配
        ii.order(ByteOrder.nativeOrder());//一定要用native的字节序 (大小端字节问题)
        mIndexBuff = ii.asShortBuffer();
        mIndexBuff.put(index);/*讲数据填入*/
        mIndexBuff.position(0);/**读/写的数据的索引*/


        ByteBuffer cc = ByteBuffer.allocateDirect(color.length * 4);//分配
        cc.order(ByteOrder.nativeOrder());//一定要用native的字节序 (大小端字节问题)
        mColorBuff = cc.asFloatBuffer();
        mColorBuff.put(color);/*讲数据填入*/
        mColorBuff.position(0);/**读/写的数据的索引*/

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
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 5, 5, 10.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    private static final float[] COLOR = {1.0f, 1.0f, 1.0f, 1.0f};//white

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);


        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuff);
        int aColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        GLES20.glEnableVertexAttribArray(aColorHandle);
        GLES20.glVertexAttribPointer(aColorHandle, 4, GLES20.GL_FLOAT, false, 0, mColorBuff);

        GLES20.glUniform4fv(aColorHandle, 1, COLOR, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.length, GLES20.GL_UNSIGNED_SHORT, mIndexBuff);
        GLES20.glDisableVertexAttribArray(positionHandle);

    }
}
