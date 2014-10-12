package net.shuttler.alliant.client.graphics;

import net.shuttler.alliant.client.graphics.opengl.GLMatrix;
import net.shuttler.alliant.client.graphics.opengl.GLMatrixMode;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GLContext;

import static java.lang.Math.*;
import static org.lwjgl.opengl.ARBDepthClamp.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

public class Camera {
    private final float MOUSE_DAMPENER = 0.16f;
    private float aspectRatio = 1;
    private float x = 0, y = 0, z = 0, pitch = 0, yaw = 0, roll = 0;
    private float zNear = 0.3f;
    private float zFar = 100;
    private float fov = 90;

    public void processMouse(float mouseSpeed, float maxLookUp, float maxLookDown) {
        float mouseDX = Mouse.getDX() * mouseSpeed * MOUSE_DAMPENER;
        float mouseDY = Mouse.getDY() * mouseSpeed * MOUSE_DAMPENER;
        if (yaw + mouseDX >= 360) {
            yaw = yaw + mouseDX - 360;
        } else if (yaw + mouseDX < 0) {
            yaw = 360 - yaw + mouseDX;
        } else {
            yaw += mouseDX;
        }

        if (pitch - mouseDY >= maxLookDown && pitch - mouseDY <= maxLookUp) {
            pitch += -mouseDY;
        } else if (pitch - mouseDY < maxLookDown) {
            pitch = maxLookDown;
        } else if (pitch - mouseDY > maxLookUp) {
            pitch = maxLookUp;
        }
    }
    public void processMouse(float mouseSpeed) {
        processMouse(mouseSpeed, 90, -90);
    }
    public void processMouse() {
        processMouse(1);
    }

     public void processKeyboard(float delta, float speed) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta " + delta + " is 0 or is smaller than 0");
        }

        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        if (keyUp && keyRight && !keyLeft && !keyDown) {
            moveFromLook(speed * delta * 0.003f, 0, -speed * delta * 0.003f);
        }
        if (keyUp && keyLeft && !keyRight && !keyDown) {
            moveFromLook(-speed * delta * 0.003f, 0, -speed * delta * 0.003f);
        }
        if (keyUp && !keyLeft && !keyRight && !keyDown) {
            moveFromLook(0, 0, -speed * delta * 0.003f);
        }
        if (keyDown && keyLeft && !keyRight && !keyUp) {
            moveFromLook(-speed * delta * 0.003f, 0, speed * delta * 0.003f);
        }
        if (keyDown && keyRight && !keyLeft && !keyUp) {
            moveFromLook(speed * delta * 0.003f, 0, speed * delta * 0.003f);
        }
        if (keyDown && !keyUp && !keyLeft && !keyRight) {
            moveFromLook(0, 0, speed * delta * 0.003f);
        }
        if (keyLeft && !keyRight && !keyUp && !keyDown) {
            moveFromLook(-speed * delta * 0.003f, 0, 0);
        }
        if (keyRight && !keyLeft && !keyUp && !keyDown) {
            moveFromLook(speed * delta * 0.003f, 0, 0);
        }
        if (flyUp && !flyDown) {
            y += speed * delta * 0.003f;
        }
        if (flyDown && !flyUp) {
            y -= speed * delta * 0.003f;
        }
    }

    public void moveFromLook(float dx, float dy, float dz) {
        this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
        this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
        this.y += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
    }

    public void applyOrthographicMatrix() {
        glPushAttrib(GL_TRANSFORM_BIT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-aspectRatio, aspectRatio, -1, 1, 0, zFar);
        glPopAttrib();
    }

    public void applyPerspectiveMatrix() {
        GLMatrix.setMatrixMode(GLMatrixMode.PROJECTION);
        GLMatrix.loadIdentity();
        GLMatrix.loadPerspectiveMatrix(fov, aspectRatio, zNear, zFar);
    }

    public void applyTranslations() {
        GLMatrix.setMatrixMode(GLMatrixMode.MODELVIEW);
        GLMatrix.loadIdentity();
        GLMatrix.rotate(pitch, 1, 0, 0);
        GLMatrix.rotate(yaw, 0, 1, 0);
        GLMatrix.rotate(roll, 0, 0, 1);
        GLMatrix.translate(-x, -y, -z);
    }

    public void applyOptimalStates() {
        if (GLContext.getCapabilities().GL_ARB_depth_clamp) {
            glEnable(GL_DEPTH_CLAMP);
        }
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float pitch() {
        return pitch;
    }

    public float yaw() {
        return yaw;
    }

    public float roll() {
        return roll;
    }

    public float fieldOfView() {
        return fov;
    }

    public float aspectRatio() {
        return aspectRatio;
    }

    public float nearClippingPane() {
        return zNear;
    }

    public float farClippingPane() {
        return zFar;
    }

    public Camera setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Camera setRotation(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        return this;
    }

    public Camera setNearClippingPane(float nearClippingPane) {
        if (nearClippingPane <= 0) {
            throw new IllegalArgumentException("nearClippingPane " + nearClippingPane + " is 0 or less");
        }
        this.zNear = nearClippingPane;
        return this;
    }

    public Camera setFarClippingPane(float farClippingPane) {
        if (farClippingPane <= 0) {
            throw new IllegalArgumentException("nearClippingPane " + farClippingPane + " is 0 or less");
        }
        this.zFar = farClippingPane;
        return this;
    }

    public Camera setFieldOfView(float fov) {
        this.fov = fov;
        return this;
    }

    public Camera setAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("aspectRatio " + aspectRatio + " was 0 or was smaller than 0");
        }
        this.aspectRatio = aspectRatio;
        return this;
    }

    @Override
    public String toString() {
        return "Camera [x= " + x + ", y= " + y + ", z= " + z + ", pitch= " + pitch + ", yaw= " + yaw + ", " +
                "roll= " + roll + ", fov= " + fov + ", aspectRatio= " + aspectRatio + ", zNear= " + zNear + ", " +
                "zFar= " + zFar + "]";
    }
}
