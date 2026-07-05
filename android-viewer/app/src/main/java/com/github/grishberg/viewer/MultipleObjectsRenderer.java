package com.github.grishberg.viewer;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import android.os.Handler;
import android.os.Looper;
import com.github.grishberg.cad3d.R;
import com.github.grishberg.cad3d.common.DebugPoint;
import com.github.grishberg.cad3d.common.RawResourceReader;
import com.github.grishberg.cad3d.ui.ControlledRenderer;
import com.github.grishberg.cad3d.ui.DebugPointUi;
import com.github.grishberg.cad3d.ui.DebugPointsRenderer;
import com.github.grishberg.cad3d.ui.DebugVisualizer;
import com.github.grishberg.cad3d.ui.DebugVisualizerImpl;
import com.github.grishberg.cad3d.util.BuffersContainer;
import com.github.grishberg.cad3d.util.SceneBuilder;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import eu.printingin3d.javascad.coords.V3d;
import eu.printingin3d.javascad.vrl.VertexHolder;

/**
 * This class implements our custom renderer. Note that the GL10 parameter passed in is unused
 * for OpenGL ES 2.0
 * renderers -- the static class GLES20 is used instead.
 */
public class MultipleObjectsRenderer implements GLSurfaceView.Renderer, ControlledRenderer {

    /**
     * Store our model data in a float buffer.
     */
    private List<BuffersContainer> targetBuffers = new ArrayList<>();
    private List<DebugPoint> debugPoints = new ArrayList<>();
    private List<DebugPointUi> debugPointsUi = new ArrayList<>();
    private final SceneBuilder sceneBuilder;
    /**
     * Store the model matrix. This matrix is used to move models from object space (where each
     * model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    /**
     * Store the view matrix. This can be thought of as our camera. This matrix transforms world
     * space to eye space;
     * it positions things relative to our eye.
     */
    private float[] mViewMatrix = new float[16];

    /**
     * Store the projection matrix. This is used to project the scene onto a 2D viewport.
     */
    private float[] mProjectionMatrix = new float[16];

    /**
     * Allocate storage for the final combined matrix. This will be passed into the shader program.
     */
    private float[] mMVPMatrix = new float[16];

    /**
     * Храним последнюю вычисленную MVP-матрицу для использования вне onDrawFrame
     * (например, для преобразования координат).
     */
    private final float[] mCachedMVPMatrix = new float[16]; // Добавляем это поле
    private boolean isFirstRender = true;

    /**
     * This will be used to pass in the transformation matrix.
     */
    private int mMVPMatrixHandle;

    /**
     * This will be used to pass in the modelview matrix.
     */
    private int mMVMatrixHandle;

    /**
     * This will be used to pass in the light position.
     */
    private int mLightPosHandle;

    /**
     * This will be used to pass in model position information.
     */
    private int mPositionHandle;

    /**
     * This will be used to pass in model normal information.
     */
    private int mNormalHandle;
    /**
     * Size of the normal data in elements.
     */
    private final int mNormalDataSize = 3;


    /**
     * Used to hold a light centered on the origin in model space. We need a 4th coordinate so we
     * can get translations to work when
     * we multiply this by our transformation matrices.
     */
    private final float[] mLightPosInModelSpace = new float[]{0.0f, 0.0f, 0.0f, 1.0f};

    /**
     * Used to hold the current position of the light in world space (after transformation via
     * model matrix).
     */
    private final float[] mLightPosInWorldSpace = new float[4];

    /**
     * Used to hold the transformed position of the light in eye space (after transformation via
     * modelview matrix)
     */
    private final float[] mLightPosInEyeSpace = new float[4];

    /**
     * Store the accumulated rotation.
     */
    private final float[] mAccumulatedRotation = new float[16];

    /**
     * Store the current rotation.
     */
    private final float[] mCurrentRotation = new float[16];

    /**
     * A temporary matrix.
     */
    private float[] mTemporaryMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] mLightModelMatrix = new float[16];

    /**
     * This will be used to pass in model color information.
     */
    private int mColorHandle;

    /**
     * How many bytes per float.
     */
    private final int mBytesPerFloat = 4;

    /**
     * How many elements per vertex.
     */
    private final int mStrideBytes = 7 * mBytesPerFloat;

    /**
     * Offset of the position data.
     */
    private final int mPositionOffset = 0;

    /**
     * Size of the position data in elements.
     */
    private final int mPositionDataSize = 3;

    /**
     * Offset of the color data.
     */
    private final int mColorOffset = 3;

    /**
     * Size of the color data in elements.
     */
    private final int mColorDataSize = 4;
    private int pointMVPMatrixHandle;
    private int pointPositionHandle;

    private float scale = 1f;
    private float angleX = 0;
    private float angleY = 0;
    private float translateX = 0.0f;
    private float translateY = 0.0f;

    private final Context context;
    private final Runnable invalidator;
    private DebugVisualizerImpl debugVisualizer;

    private boolean wireframeOnly = false;
    private int viewportWidth, viewportHeight;
    private DebugPointsRenderer debugPointsRenderer;
    /**
     * Initialize the model data.
     */
    private final Handler handler = new Handler(Looper.getMainLooper());
    public MultipleObjectsRenderer(Context context, SceneBuilder builder, Runnable invalidator) {
        this.context = context;
        this.invalidator = invalidator;
        // Define points for equilateral triangles.
        sceneBuilder = builder;

        sceneBuilder.setListener(new SceneBuilder.ReadyListener() {
            @Override
            public void onReady(List<VertexHolder> buffers) {
                onReady(buffers, Collections.emptyList());
            }

            @Override
            public void onReady(List<VertexHolder> buffers, List<DebugPoint> newDebugPoints) {
                ArrayList<BuffersContainer> newBuffers = new ArrayList<>();
                for (VertexHolder vh: buffers) {
                    newBuffers.add(BuffersContainer.fromVertexHolder(vh));
                }
                targetBuffers = newBuffers;
                debugPoints = newDebugPoints;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        invalidator.run();
                    }
                });
            }

        });
    }

    public void setDebugPointsRenderer(DebugPointsRenderer debugPointsRenderer) {
        this.debugPointsRenderer = debugPointsRenderer;
    }
    
    /**
     * Запрашивает перерисовку сцены
     */
    public void requestRender() {
        if (invalidator != null) {
            invalidator.run();
        }
    }
    
    /**
     * Устанавливает визуализатор отладки
     */
    public void setDebugVisualizer(DebugVisualizerImpl visualizer) {
        this.debugVisualizer = visualizer;
    }


    public void setScale(float newScale) {
        scale = newScale;
        mapDebugPointsToScreen();
    }

    public float getScale() {
        return scale;
    }

    // Getter and setter methods for angles, scale factor, and translation
    public float getAngleX() {
        return angleX;
    }

    public void setAngleX(float angleX) {
        this.angleX = angleX;
        mapDebugPointsToScreen();
    }

    public float getAngleY() {
        return angleY;
    }

    public void setAngleY(float angleY) {
        this.angleY = angleY;
        mapDebugPointsToScreen();
    }

    //public void setScaleFactor(float scaleFactor) { this.scaleFactor = scaleFactor; }
    public void setTranslateX(float translateX) {
        this.translateX = translateX;
        mapDebugPointsToScreen();
    }

    public void setTranslateY(float translateY) {
        this.translateY = translateY;
        mapDebugPointsToScreen();
    }

    public void setWireframeOnly(boolean wireframeOnly) {
        this.wireframeOnly = wireframeOnly;
    }


    @Override
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        android.util.Log.w("CAMERA", "onSurfaceCreated eyeZ=500");
        // Set the background clear color to very dark.
        GLES20.glClearColor(0.25f, 0.25f, 0.25f, 1f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        //GLES20.glCullFace(GLES20.GL_BACK);
        //GLES20.glFrontFace(GLES20.GL_CCW);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Position the eye closer
        final float eyeX = 500.0f;
        final float eyeY = -500.0f;
        final float eyeZ = 500.0f;

        // We are looking at the center of the scene
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = 0.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader = RawResourceReader.readTextFileFromRawResource(
            context,
            R.raw.vertex_shader2
        );
        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(
            context,
            R.raw.fragment_shader2
        );

        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }

        // Create a program object and store the handle to it.
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_Normal");
            GLES20.glBindAttribLocation(programHandle, 2, "a_Color");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(programHandle, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mNormalHandle = GLES20.glGetAttribLocation(programHandle, "a_Normal");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");

        pointMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        pointPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
        // Initialize the accumulated rotation matrix
        Matrix.setIdentityM(mAccumulatedRotation, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        viewportWidth = width;
        viewportHeight = height;
        mapDebugPointsToScreen();

        // Set the OpenGL viewport to the same size as the surface.
        GLES20.glViewport(0, 0, width, height);

        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.

        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 3000.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }

    @Override
    public void onDrawFrame(GL10 glUnused) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        // Do a complete rotation every 10 seconds.
        float angleInDegrees = (360.0f / 10000.0f);

        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 200.0f, 200.0f);
        Matrix.rotateM(mLightModelMatrix, 0, angleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 3.5f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, translateX, translateY, -3.5f);

        // Set a matrix that contains the current rotation.
        Matrix.setIdentityM(mCurrentRotation, 0);
        Matrix.rotateM(mCurrentRotation, 0, angleX, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(mCurrentRotation, 0, angleY, 0.0f, 1.0f, 0.0f);

        Matrix.scaleM(mModelMatrix, 0, scale, scale, scale);
        angleX = 0;
        angleY = 0;

        // Multiply the current rotation by the accumulated rotation, and then set the
        // accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

        // Rotate the cube taking the overall rotation into account.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mAccumulatedRotation, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);

        // Кэшируем итоговую матрицу
        // Вычисляем MVP матрицу и кэшируем её
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0); // MVP = View * Model
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0); // MVP = Projection * (View *
        // Model)
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
        System.arraycopy(mMVPMatrix, 0, mCachedMVPMatrix, 0, 16);

        if (isFirstRender) {
            isFirstRender = false;
            mapDebugPointsToScreen();
        }

        for (BuffersContainer buffersContainer : targetBuffers) {
            if (wireframeOnly) {
                drawWireframe(buffersContainer);
            } else {
                drawTriangle(buffersContainer);
            }
        }

        drawLight();
        
        // Отрисовываем отладочные буферы поверх основных объектов
        if (debugVisualizer != null) {
            List<BuffersContainer> debugBuffers = debugVisualizer.getDebugBuffers();
            for (BuffersContainer debugBuffer : debugBuffers) {
                drawDebugTriangle(debugBuffer);
            }
        }

    }

    /**
     * Draws a triangle from the given vertex data.
     */
    private void drawTriangle(final BuffersContainer buffersContainer) {
        // Сбрасываем состояние атрибутов для предотвращения конфликтов
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        
        final FloatBuffer aTriangleBuffer = buffersContainer.verticesBuffers;
        // Pass in the position information
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        );
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        );
        GLES20.glEnableVertexAttribArray(mColorHandle);

        final FloatBuffer normals = buffersContainer.normals;
        // Pass in the normal information
        normals.position(0);
        GLES20.glVertexAttribPointer(mNormalHandle, mNormalDataSize, GLES20.GL_FLOAT, false,
            0, normals
        );

        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // This multiplies the view matrix by the model matrix, and stores the result in the MVP
        // matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);

        // This multiplies the modelview matrix by the projection matrix, and stores the result
        // in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(
            mLightPosHandle,
            mLightPosInEyeSpace[0],
            mLightPosInEyeSpace[1],
            mLightPosInEyeSpace[2]
        );
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, buffersContainer.vertexCount);
    }

    private void drawWireframe(final BuffersContainer buffersContainer) {
        // Сбрасываем состояние атрибутов для предотвращения конфликтов
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        
        GLES20.glLineWidth(5f); // Толщина линий
        final FloatBuffer aTriangleBuffer = buffersContainer.verticesBuffers;
        aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
            mStrideBytes, aTriangleBuffer
        );
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Цвет задаём белым для wireframe
        float[] white = {1f, 1f, 1f, 1f};
        GLES20.glVertexAttrib4fv(mColorHandle, white, 0);
        // Отключаем нормали
        GLES20.glDisableVertexAttribArray(mNormalHandle);
        // Матрицы (аналогично drawTriangle)
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mMVPMatrix, 0);
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        // Рисуем рёбра
        int vertexCount = buffersContainer.vertexCount;
        for (int i = 0; i < vertexCount; i += 3) {
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, i, 3);
        }
    }

    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight() {
        // Pass in the position.
        GLES20.glVertexAttrib3f(
            pointPositionHandle,
            mLightPosInModelSpace[0],
            mLightPosInModelSpace[1],
            mLightPosInModelSpace[2]
        );

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mTemporaryMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mMVPMatrix, 0, 16);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }
    
    /**
     * Отрисовывает отладочные треугольники
     */
    private void drawDebugTriangle(final BuffersContainer buffersContainer) {
        // Используем тот же метод, что и для обычных треугольников
        drawTriangle(buffersContainer);
    }

    public List<DebugPointUi> getDebugPoints() {
        return debugPointsUi;
    }

    private void mapDebugPointsToScreen() {
        debugPointsUi.clear();
        for (DebugPoint p : debugPoints) {
            int[] coords = convert3dToScreen(p.point);
            debugPointsUi.add(
                new DebugPointUi(coords[0], coords[1],
                    Color.valueOf(p.color.getRGB()),
                    p.title));
        }
        if (debugPointsRenderer != null) {
            debugPointsRenderer.renderDebugPoints(debugPointsUi);
        }
    }

    private int[] convert3dToScreen(V3d point3d) {
        if (point3d == null || viewportWidth <= 0 || viewportHeight <= 0) {
            // Невозможно преобразовать без данных
            return new int[]{0, 0}; // Или return null;
        }

        // 1. Преобразуем точку в однородные координаты (x, y, z, 1)
        float[] point4d = {
            (float) point3d.getX(),
            (float) point3d.getY(),
            (float) point3d.getZ(),
            1.0f
        };

        // 2. Применяем кэшированную MVP-матрицу: result_clip = MVP * point4d
        float[] result_clip = new float[4];
        Matrix.multiplyMV(result_clip, 0, mCachedMVPMatrix, 0, point4d, 0); // Используем mCachedMVPMatrix

        // 3. Проверка перспективной коррекции и отсечения
        // Если w равно 0, деление невозможно. Если w <= 0, точка находится за ближней плоскостью отсечения.
        if (result_clip[3] <= 0) {
            // Точка находится за ближней плоскостью отсечения или в невидимой области
            // Можно вернуть специальное значение или координаты вне экрана
            // Например, вернем (-1, -1) как признак невидимости
            return new int[]{-1, -1};
        }

        // 4. Преобразуем в нормализованные координаты устройства (NDC)
        // Делим компоненты x, y, z на w
        float ndc_x = result_clip[0] / result_clip[3];
        float ndc_y = result_clip[1] / result_clip[3];
        // float ndc_z = result_clip[2] / result_clip[3]; // Не используется для 2D-координат экрана

        // 5. Проверка, находится ли точка в пределах NDC (-1, 1)
        // Это проверка отсечения по краям экрана
        if (ndc_x < -1.0f || ndc_x > 1.0f || ndc_y < -1.0f || ndc_y > 1.0f) {
            // Точка находится вне области видимости камеры (за краями экрана)
            // Можно вернуть (-1, -1) или координаты за пределами экрана
            return new int[]{-1, -1};
        }

        // 6. Преобразуем NDC в координаты экрана (пиксели)
        // В OpenGL NDC: (-1, -1) - левый нижний угол, (1, 1) - правый верхний
        // В Android UI: (0, 0) - левый верхний угол, (width, height) - правый нижний
        // Поэтому нужно преобразовать и инвертировать Y

        int screenX = (int) ((ndc_x * 0.5f + 0.5f) * viewportWidth);
        // Инвертируем Y: NDC (-1..1) -> Screen (0..height), с учетом системы координат
        int screenY = (int) ((1.0f - (ndc_y * 0.5f + 0.5f)) * viewportHeight);

        return new int[]{screenX, screenY};
    }
}
