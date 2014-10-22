package net.shuttler.alliant.client.graphics.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.Stack;

public class GLMatrix
{
    private static Stack<Matrix4f> ModelViewMatrixStack = new Stack<Matrix4f>();
    private static Stack<Matrix4f> ProjectionMatrixStack = new Stack<Matrix4f>();

    private static Stack<Matrix4f> activeMatrixStack = ModelViewMatrixStack;

    private static Vector3f tempVec = new Vector3f();

    public static void init()
    {
        ModelViewMatrixStack.push( new Matrix4f() );
        ProjectionMatrixStack.push( new Matrix4f() );
    }

    public static void setMatrixMode(GLMatrixMode activeMatrix)
    {
        if (activeMatrix == GLMatrixMode.MODELVIEW) {
            activeMatrixStack = ModelViewMatrixStack;
        }
        else if (activeMatrix == GLMatrixMode.PROJECTION)
        {
            activeMatrixStack = ProjectionMatrixStack;
        }
    }

    public static void translate(float x, float y, float z)
    {
        tempVec.set(x, y, z);
        activeMatrixStack.peek().translate(tempVec);
    }

    public static void rotate(float amount, float x, float y, float z)
    {
        tempVec.set(x, y, z);
        activeMatrixStack.peek().rotate((float)Math.toRadians(amount), tempVec);
    }

    public static void scale(float x, float y, float z)
    {
        tempVec.set(x, y, z);
        activeMatrixStack.peek().scale(tempVec);
    }

    public static void pushMatrix()
    {
        activeMatrixStack.push( new Matrix4f().load(activeMatrixStack.peek()) );
    }

    public static void popMatrix()
    {
        if (activeMatrixStack.size() > 1) {
            activeMatrixStack.pop();
        }
    }

    public static void loadIdentity()
    {
        activeMatrixStack.peek().setIdentity();
    }

    public static Matrix4f getCurrentMatrix()
    {
        return activeMatrixStack.peek();
    }

    public static void loadOrthogonalMatrix(float left, float right, float bottom, float top, float zNear, float zFar)
    {
        Matrix4f activeMatrix = activeMatrixStack.peek();
        activeMatrix.setIdentity();

        activeMatrix.m00 = 2/(right - left); activeMatrix.m30 = -((right + left) / (right - left));
        activeMatrix.m11 = 2/(top - bottom); activeMatrix.m31 = -((top + bottom) / (top - bottom));
        activeMatrix.m22 = (-2)/(zFar - zNear); activeMatrix.m32 = -((zFar + zNear) / (zFar - zNear));
    }

    public static void loadOrthogonalMatrix(float left, float right, float bottom, float top)
    {
        loadOrthogonalMatrix(left, right, top, bottom, 0f, 10f);
    }

    public static void loadPerspectiveMatrix(float fovy, float aspect, float zNear, float zFar)
    {
        Matrix4f activeMatrix = activeMatrixStack.peek();
        activeMatrix.setZero();

        float f = (float)(1/Math.tan(Math.toRadians(fovy/2)));
        activeMatrix.m00 = f / aspect;
        activeMatrix.m11 = f;
        activeMatrix.m22 = ((zFar + zNear) / (zNear - zFar));
        activeMatrix.m32 = ((2 * zFar * zNear) / (zNear - zFar));
        activeMatrix.m23 = -1;
    }

    public static void sendMatricesToUniform(int ModelViewLocation, int ModelViewProjectionLocation, int NormalLocation)
    {
        sendMatricesToUniform(ModelViewLocation, ModelViewProjectionLocation);

        FloatBuffer matrices = BufferUtils.createFloatBuffer(9);

        Matrix3f NormalMatrix = createNormalMatrix();
        NormalMatrix.store(matrices);
        matrices.flip();
        GL20.glUniformMatrix3(NormalLocation, false, matrices);
    }

    public static void sendMatricesToUniform(int ModelViewLocation, int ModelViewProjectionLocation) {
        FloatBuffer matrices = BufferUtils.createFloatBuffer(16);

        ModelViewMatrixStack.peek().store(matrices);
        matrices.flip();
        GL20.glUniformMatrix4(ModelViewLocation, false, matrices);

        matrices.clear();

        Matrix4f ModelViewProjectionMatrix = Matrix4f.mul( ProjectionMatrixStack.peek(), ModelViewMatrixStack.peek(), null);
        ModelViewProjectionMatrix.store(matrices);
        matrices.flip();
        GL20.glUniformMatrix4(ModelViewProjectionLocation, false, matrices);
    }

    private static Matrix3f createNormalMatrix()
    {
        Matrix3f NormalMatrix = new Matrix3f();
        Matrix4f modelview = ModelViewMatrixStack.peek();

        NormalMatrix.m00 = modelview.m00; NormalMatrix.m10 = modelview.m10; NormalMatrix.m20 = modelview.m20;
        NormalMatrix.m01 = modelview.m01; NormalMatrix.m11 = modelview.m11; NormalMatrix.m21 = modelview.m21;
        NormalMatrix.m02 = modelview.m02; NormalMatrix.m12 = modelview.m12; NormalMatrix.m22 = modelview.m22;

        Matrix3f.invert(NormalMatrix, NormalMatrix);
        Matrix3f.transpose(NormalMatrix, NormalMatrix);

        return NormalMatrix;
    }
}
