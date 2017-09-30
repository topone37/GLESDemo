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
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.tp.gl.demo001.Demo002GLSurfaceView.index;

/**
 * 圆锥体 可以 一个顶点 (0,0,2),底部一个园, 画扇形圆柱体
 */

public class Demo005GLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {


    private int mProgram;
    private static final String VERTEX_SHADER =
            "attribute vec4 vPosition;\n" +
                    "uniform mat4 vMatrix;\n" +
                    "varying vec4 vColor;" +
                    "attribute vec4 aColor;" +
                    "void main() {\n" +
                    "  gl_Position = vMatrix*vPosition;\n" +
                    "  if(vPosition.z>0.0){" +/** 比较的时候 也必须是同类型 0 不行 得用 0.0*/
                    "       vColor = vec4(1.0,0.0,1.0,1.0);" +/**todo 浮点型 不需要带f 切记切记*/
                    "   }else" +
                    "   {" +
                    "       vColor = vec4(0.9,0.9,0.9,1.0);" +
                    "   }" +
                    "}";
    //gl_Position和gl_FragColor都是Shader的内置变量，分别为定点位置和片元颜色。
    private static final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    " varying vec4 vColor;\n" +
                    " void main() {\n" +
                    "     gl_FragColor = vColor;\n" +
                    " }";

    private FloatBuffer mVertexBuff;

    public Demo005GLSurfaceView(Context context) {
        this(context, null);
    }

    public Demo005GLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);//88800->888160 depth size加上/*todo 坑爹呀 我屮艸芔茻*/
        setRenderer(this);/**必须要先设置回调接口,在设置渲染模式*/
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        createVertex();
    }

    float radiu = 1;
    float height = 2;

    private void createVertex() {
        float preSpan = 360 * 1.0f / 360;
        ArrayList<Float> pos = new ArrayList<>();
        for (float i = 0; i < 360 + preSpan; i = i + preSpan) {
            pos.add((float) (1 * Math.cos(i * Math.PI / 180)));
            pos.add((float) (1 * Math.sin(i * Math.PI / 180)));
            pos.add(height);

            pos.add((float) (1 * Math.cos(i * Math.PI / 180)));
            pos.add((float) (1 * Math.sin(i * Math.PI / 180)));
            pos.add(0.0f);
        }
        vertexs = new float[pos.size()];
        for (int i = 0; i < vertexs.length; i++) {
            vertexs[i] = pos.get(i);
        }
    }

    private float[] vertexs;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);//设置清屏颜色,只有你调用  GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);起作用
        //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);//setEGLConfigChooser一定要加DepthSize


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
                Log.e("GLES", "GL_VERTEX_SHADER Error!!!!" + GLES20.glGetShaderInfoLog(vertex_shader));
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
        Matrix.setLookAtM(mViewMatrix, 0, 0, 10, 15.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);
    }

    private static final float[] COLOR = {1.0f, 1.0f, 1.0f, 1.0f};//white

    @Override
    public void onDrawFrame(GL10 gl) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgram);


        //获取变换矩阵vMatrix成员句柄
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0);
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuff);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexs.length / 3);
        GLES20.glDisableVertexAttribArray(positionHandle);

    }
}
