/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
 ***/
package wxplus.opengles2forandroid.programs;

import android.content.Context;

import wxplus.opengles2forandroid.utils.GLog;
import wxplus.opengles2forandroid.utils.TextureUtils;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public abstract class BaseShaderProgram {
    public static final String TAG = BaseShaderProgram.class.getSimpleName();

    // Uniform constants
    protected static final String U_MATRIX = "u_Matrix";
    protected static final String U_PROJECTION_MATRIX = "u_ProjectionMatrix";
    protected static final String U_VIEW_MATRIX = "u_ViewMatrix";
    protected static final String U_MODEL_MATRIX = "u_ModelMatrix";
    protected static final String U_COLOR = "u_Color";
    protected static final String U_LIGHT_COLOR = "u_LightColor";
    protected static final String U_LIGHT_POSITION = "u_LightPosition";
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";
    protected static final String U_VIEW_POSITION = "u_ViewPosition";

    // Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_COLOR = "a_Color";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";
    protected static final String A_NORMAL = "a_Normal";


    protected final int uMatrixHandle;
    protected final int uProjectionMatrixHandle;
    protected final int uViewMatrixHandle;
    protected final int uModelMatrixHandle;
    protected final int uTextureUnitHandle;
    protected final int uLightColorHandle;
    protected final int uLightPositionHandle;
    protected final int uViewPositionHandle;

    protected final int aPositionHandle;
    protected final int aNormalHandle;

    // Shader program
    public final int program;

    protected BaseShaderProgram(Context context, int vertexShaderResourceId,
                                int fragmentShaderResourceId) {
        // Compile the shaders and link the program.
        program = buildProgram(
                TextureUtils.readShaderCodeFromResource(context, vertexShaderResourceId),
                TextureUtils.readShaderCodeFromResource(context, fragmentShaderResourceId));

        uMatrixHandle = glGetUniformLocation(program, U_MATRIX);
        uProjectionMatrixHandle = glGetUniformLocation(program, U_PROJECTION_MATRIX);
        uViewMatrixHandle = glGetUniformLocation(program, U_VIEW_MATRIX);
        uModelMatrixHandle = glGetUniformLocation(program, U_MODEL_MATRIX);

        uTextureUnitHandle = glGetUniformLocation(program, U_TEXTURE_UNIT);
        uLightColorHandle = glGetUniformLocation(program, U_LIGHT_COLOR);
        uLightPositionHandle = glGetUniformLocation(program, U_LIGHT_POSITION);
        uViewPositionHandle = glGetUniformLocation(program, U_VIEW_POSITION);

        aPositionHandle = glGetAttribLocation(program, A_POSITION);
        aNormalHandle = glGetUniformLocation(program, A_NORMAL);
    }

    /**
     * Loads and compiles a vertex shader, returning the OpenGL object ID.
     */
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    /**
     * Loads and compiles a fragment shader, returning the OpenGL object ID.
     */
    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    /**
     * Compiles a shader, returning the OpenGL object ID.
     */
    private static int compileShader(int type, String shaderCode) {
        // Create a new shader object.
        final int shaderObjectId = glCreateShader(type);

        if (shaderObjectId == 0) {
            GLog.e(TAG, "Could not create new shader.");
            return 0;
        }

        // Pass in the shader source.
        glShaderSource(shaderObjectId, shaderCode);

        // Compile the shader.
        glCompileShader(shaderObjectId);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS,
                compileStatus, 0);

//        GLog.d(TAG, "Results of compiling source:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId));

        // Verify the compile status.
        if (compileStatus[0] == 0) {
            // If it failed, delete the shader object.
            glDeleteShader(shaderObjectId);
            GLog.e(TAG, "Compilation of shader failed.");
            return 0;
        }

        // Return the shader object ID.
        return shaderObjectId;
    }

    /**
     * Links a vertex shader and a fragment shader together into an OpenGL
     * program. Returns the OpenGL program object ID, or 0 if linking failed.
     */
    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {

        // Create a new program object.
        final int programObjectId = glCreateProgram();

        if (programObjectId == 0) {
            GLog.e(TAG, "Could not create new program");
            return 0;
        }

        // Attach the vertex shader to the program.
        glAttachShader(programObjectId, vertexShaderId);

        // Attach the fragment shader to the program.
        glAttachShader(programObjectId, fragmentShaderId);

        // Link the two shaders together into a program.
        glLinkProgram(programObjectId);

        // Get the link status.
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS,
                linkStatus, 0);

        GLog.d(TAG, "Results of linking program:\n" + glGetProgramInfoLog(programObjectId));

        // Verify the link status.
        if (linkStatus[0] == 0) {
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);

            GLog.e(TAG, "Linking of program failed.");

            return 0;
        }

        // Return the program object ID.
        return programObjectId;
    }

    /**
     * Validates an OpenGL program. Should only be called when developing the
     * application.
     */
    public static boolean validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS,
                validateStatus, 0);
        GLog.d(TAG, "Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));

        return validateStatus[0] != 0;
    }

    /**
     * Helper function that compiles the shaders, links and validates the
     * program, returning the program ID.
     */
    public static int buildProgram(String vertexShaderSource,
                                   String fragmentShaderSource) {
        int program;

        // Compile the shaders.
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);

        // Link them into a shader program.
        program = linkProgram(vertexShader, fragmentShader);

        validateProgram(program);

        return program;
    }
}
