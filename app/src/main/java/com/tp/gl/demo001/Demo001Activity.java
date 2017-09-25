package com.tp.gl.demo001;

import android.app.Activity;
import android.os.Bundle;

import com.tp.gl.R;

public class Demo001Activity extends Activity {

    private Demo001GLSurfaceView mGLSurfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo001);
        mGLSurfaceView = (Demo001GLSurfaceView) findViewById(R.id.gl_view);
    }


}
