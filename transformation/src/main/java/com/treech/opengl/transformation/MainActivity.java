package com.treech.opengl.transformation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.treech.opengl.BuildConfig;
import com.treech.opengl.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView glSurfaceView;
    private MyGLRenderer myGLRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        glSurfaceView = (GLSurfaceView) findViewById(R.id.surface);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        myGLRenderer = new MyGLRenderer(this);
        glSurfaceView.setRenderer(myGLRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                /**
                 * Generally, GL Context will be destroyed after pause.
                 * So we destroy GL-related resources before pause.
                 */
                myGLRenderer.destroy();
            }
        });
        glSurfaceView.onPause();
    }

    private static class MyGLRenderer implements GLSurfaceView.Renderer {
        private static final boolean LOG_ENABLE = BuildConfig.DEBUG;
        private static final String TAG = MyGLRenderer.class.getSimpleName();

        /**
         * The size of a float is 4 bytes.
         */
        private static final int FLOAT_BYTES = 4;

        private static final String VERTEX_SHADER = "" +
                "attribute vec3 position;\n" +
                "attribute vec3 normal;\n" +
                "attribute vec2 inputTextureCoordinate;\n" +
                "varying vec3 faceNormal;\n" +
                "varying vec2 textureCoordinate;\n" +
                "uniform mat4 model;\n" +
                "uniform mat4 view;\n" +
                "uniform mat4 projection;\n" +
                "void main()\n" +
                "{\n" +
                "	faceNormal = normal;\n" +
                "	textureCoordinate = inputTextureCoordinate;\n" +
                "	gl_Position = projection * view * model * vec4(position.xyz, 1);\n" +
                "}";
        private static final String FRAGMENT_SHADER = "" +
                "precision mediump float;\n" +
                "varying highp vec3 faceNormal;\n" +
                "varying highp vec2 textureCoordinate;\n" +
                "uniform sampler2D inputImageTexture1;\n" +
                "void main()\n" +
                "{\n" +
                "     gl_FragColor = texture2D(inputImageTexture1, textureCoordinate);\n" +
                "}";

        private static final float CUBE[] = {
                //  ---- 位置 ----       ---- 颜色 ----     - 纹理坐标 -
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 1.0f, 1.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,

                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,

                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                -0.5f, -0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, -1.0f, 0.0f, 0.0f, 1.0f, 0.0f,

                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,
                0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, -0.5f, 0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f, 0.0f, -1.0f, 0.0f, 0.0f, 1.0f,

                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
                0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f,
                -0.5f, 0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                -0.5f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f
        };

        private int glProgram;
        private int glPositionAttr;
        private int glNormalAttr;
        private int glTexCoordAttr;

        private int glTexture1Uniform;
        private int glModelMatrixUniform;
        private int glViewMatrixUniform;
        private int glProjectionMatrixUniform;

        private int[] vbo;
        private int texture1 = GLUtils.TEXTURE_NONE;

        private Bitmap face1;
        private FloatBuffer verticesBuffer;
        private final float[] modelMatrix1 = new float[16];//OpenGL确保至少有16个包含4分量的顶点属性可用
        private final float[] modelMatrix2 = new float[16];
        private final float[] modelMatrix3 = new float[16];
        private final float[] viewMatrix = new float[16];
        private final float[] projectionMatrix = new float[16];

        private long initFrameDrawingTime;
        private float rotateSpeed = 1.0f / 10;

        public MyGLRenderer(Context context) {
            verticesBuffer = ByteBuffer.allocateDirect(CUBE.length * FLOAT_BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(CUBE);
            verticesBuffer.position(0);

            face1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.face1);

            //Cube 1 is in the origin of world space
            Matrix.setIdentityM(modelMatrix1, 0);

            Matrix.setLookAtM(viewMatrix, 0,
                    3.0f, 3.0f, 10.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);

            Matrix.perspectiveM(projectionMatrix, 0, 45.0f, width / (float) height, 0.1f, 100.0f);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            //初始化顶点缓冲对象(Vertex Buffer Objects, VBO)
            initVertexBufferObjectIfNeeded();
            //初始化纹理
            initTexturesIfNeeded();
            //使用着色器程序
            initGLProgramIfNeeded();

            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            GLES20.glUseProgram(glProgram);

            //解析顶点位置属性
            GLES20.glVertexAttribPointer(glPositionAttr, 3, GLES20.GL_FLOAT, false, 8 * FLOAT_BYTES, 0);
            //解析顶点颜色属性
            GLES20.glVertexAttribPointer(glNormalAttr, 3, GLES20.GL_FLOAT, false, 8 * FLOAT_BYTES, 3 * FLOAT_BYTES);
            //解析顶点纹理属性
            GLES20.glVertexAttribPointer(glTexCoordAttr, 2, GLES20.GL_FLOAT, false, 8 * FLOAT_BYTES, 6 * FLOAT_BYTES);

            GLES20.glEnableVertexAttribArray(glPositionAttr);
            GLES20.glEnableVertexAttribArray(glNormalAttr);
            GLES20.glEnableVertexAttribArray(glTexCoordAttr);

            //变换矩阵传递给着色器
            GLES20.glUniformMatrix4fv(glViewMatrixUniform, 1, false, viewMatrix, 0);
            GLES20.glUniformMatrix4fv(glProjectionMatrixUniform, 1, false, projectionMatrix, 0);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1);
            GLES20.glUniform1i(glTexture1Uniform, 0);

            //Draw cube 1
            GLES20.glUniformMatrix4fv(glModelMatrixUniform, 1, false, modelMatrix1, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

            float angle = getRotationAngle();

            //Draw cube 2. Rotating about y axis...
            //https://blog.csdn.net/z896435317/article/details/77715783
            //https://ejin66.github.io/2018/11/02/opengl-study-2.html
            Matrix.setIdentityM(modelMatrix2, 0);
            Matrix.translateM(modelMatrix2, 0, 0.5f, 1.0f, -1.5f);
            Matrix.rotateM(modelMatrix2, 0, angle, 0.0f, 1.0f, 0.0f);
            Matrix.scaleM(modelMatrix2, 0, 1.5f, 1.5f, 1.5f);
            GLES20.glUniformMatrix4fv(glModelMatrixUniform, 1, false, modelMatrix2, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

            //Draw cube 3. Rotating about z axis...
            Matrix.setIdentityM(modelMatrix3, 0);
            Matrix.translateM(modelMatrix3, 0, 1.0f, -1.0f, 1.0f);
            Matrix.rotateM(modelMatrix3, 0, angle, 0.0f, 0.0f, 1.0f);
            Matrix.scaleM(modelMatrix3, 0, 0.5f, 0.5f, 0.5f);
            GLES20.glUniformMatrix4fv(glModelMatrixUniform, 1, false, modelMatrix3, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
        }

        /**
         * Destroy the GL resources attached with the Renderer.
         * This must be call on GL thread.
         */
        public void destroy() {
            destroyGLProgramIfNeeded();
            destroyTexturesIfNeeded();
            destroyVertexBufferObjectIfNeeded();
        }


        private float getRotationAngle() {
            float angle;

            long now = System.currentTimeMillis();
            if (initFrameDrawingTime == 0L) {
                angle = 0.0f;
                initFrameDrawingTime = now;
            } else {
                long deltaTime = now - initFrameDrawingTime;
                angle = deltaTime * rotateSpeed;
            }

            return angle;
        }

        private void initGLProgramIfNeeded() {
            if (glProgram == 0) {
                glProgram = GLUtils.createGLProgram(VERTEX_SHADER, FRAGMENT_SHADER);

                glPositionAttr = GLES20.glGetAttribLocation(glProgram, "position");
                glTexCoordAttr = GLES20.glGetAttribLocation(glProgram, "inputTextureCoordinate");
                glNormalAttr = GLES20.glGetAttribLocation(glProgram, "normal");

                glTexture1Uniform = GLES20.glGetUniformLocation(glProgram, "inputImageTexture1");
                glModelMatrixUniform = GLES20.glGetUniformLocation(glProgram, "model");
                glViewMatrixUniform = GLES20.glGetUniformLocation(glProgram, "view");
                glProjectionMatrixUniform = GLES20.glGetUniformLocation(glProgram, "projection");

                if (LOG_ENABLE) {
                    Log.d(TAG, String.format("create GL program %d, attr & uniform locations: [%d,%d,%d|%d,%d,%d,%d]", glProgram, glPositionAttr, glNormalAttr, glTexCoordAttr, glModelMatrixUniform, glViewMatrixUniform, glProjectionMatrixUniform, glTexture1Uniform));
                }
            }
        }

        private void destroyGLProgramIfNeeded() {
            if (glProgram != 0) {
                GLES20.glDeleteProgram(glProgram);
                if (LOG_ENABLE) {
                    Log.d(TAG, String.format("destroy GL program %d", glProgram));
                }
                glProgram = 0;
            }
        }

        private void initVertexBufferObjectIfNeeded() {
            if (vbo == null) {
                vbo = new int[1];
                //生成顶点缓冲对象
                GLES20.glGenBuffers(1, vbo, 0);
                //绑定顶点缓冲对象
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0]);
                //顶点数据复制到缓冲的内存中
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.remaining() * FLOAT_BYTES, verticesBuffer, GLES20.GL_STATIC_DRAW);
                if (LOG_ENABLE) {
                    Log.d(TAG, String.format("create vbo %d, bind vertices data %d bytes", vbo[0], verticesBuffer.remaining() * FLOAT_BYTES));
                }
            }
        }

        private void destroyVertexBufferObjectIfNeeded() {
            if (vbo != null) {
                if (LOG_ENABLE) {
                    Log.d(TAG, String.format("destroy vbo %d", vbo[0]));
                }
                GLES20.glDeleteBuffers(1, vbo, 0);
                vbo = null;
            }
        }

        private void initTexturesIfNeeded() {
            if (texture1 == GLUtils.TEXTURE_NONE) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                texture1 = GLUtils.loadTextureFromBitmap(face1);
                if (LOG_ENABLE) {
                    Log.d(TAG, String.format("create texture1 %d", texture1));
                }
            }
        }

        private void destroyTexturesIfNeeded() {
            if (texture1 != GLUtils.TEXTURE_NONE) {
                GLES20.glDeleteTextures(1, new int[]{texture1}, 0);
                if (LOG_ENABLE) {
                    Log.d(TAG, String.format("destroy texture1 %d", texture1));
                }
                texture1 = GLUtils.TEXTURE_NONE;
            }
        }

    }
}
