package net.shuttler.alliant.client;

import net.shuttler.alliant.client.graphics.Camera;
import net.shuttler.alliant.client.graphics.model.Model;
import net.shuttler.alliant.client.graphics.model.OBJLoader;
import net.shuttler.alliant.client.graphics.shaders.ShaderLoader;
import net.shuttler.alliant.util.BufferTools;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;

import java.io.File;

import static org.lwjgl.opengl.ARBShadowAmbient.GL_TEXTURE_COMPARE_FAIL_VALUE_ARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_DEPTH_TEXTURE_MODE;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Loader {
    private static Camera camera = null;

    public static float[] lightPosition = new float[]{0, 0, 0, 1};
    public static float[] lightAmbiance = new float[]{0, 0, 0, 1};
    public static float[] lightDiffusion = new float[]{1.7F, 1.7F, 1.7F, 1};
    public static float[] lightModelAmbiance = new float[]{0, 0, 0, 1};
    public static String TITLE = "I'm a Window";
    public static DisplayMode DISPLAY_MODE = new DisplayMode(640, 480);

    public static final Matrix4f depthModelViewProjection = new Matrix4f();

    public static void createWindow() {
        try {
			Display.setResizable(true);
			Display.setTitle(TITLE);
			Display.setLocation(100, 100);
			Display.setDisplayMode(DISPLAY_MODE);
			Display.create();

			Mouse.create();
			Keyboard.create();
		} catch (LWJGLException e) {
			System.err.println("The display wasn't initialised properly D:");
            quit(true);
		}
    }

    public static void setupLighting() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glLightModel(GL_LIGHT_MODEL_AMBIENT, BufferTools.asFlippedBuffer(lightModelAmbiance));
        glLight(GL_LIGHT0, GL_POSITION, BufferTools.asFlippedBuffer(lightPosition));
        glLight(GL_LIGHT0, GL_AMBIENT, BufferTools.asFlippedBuffer(lightAmbiance));
        glLight(GL_LIGHT0, GL_DIFFUSE, BufferTools.asFlippedBuffer(lightDiffusion));
    }

    public static Camera setupCamera() {
        camera = new Camera().setAspectRatio( (float) Display.getWidth() / Display.getHeight() ).setFarClippingPane(100).setNearClippingPane(0.001f).setFieldOfView(70).setPosition(-2.19f, 1.36f, 11.45f);
        camera.applyPerspectiveMatrix();
        return camera;
    }

    public static void setupTexturedShader() {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_DIFFUSE);
        glUseProgram(ShaderLoader.loadShaderPair("res/shaders/textured_lighting"));
    }

    public static void setupShadows() {
        if (!GLContext.getCapabilities().OpenGL14 && !GLContext.getCapabilities().GL_ARB_shadow) {
            System.err.println("Can't create shadows at all. Requires OpenGL 1.4 or the GL_ARB_shadow extension");
            quit(true);
        }
        if (!GLContext.getCapabilities().GL_ARB_shadow_ambient) {
            System.err.println("GL_ARB_shadow_ambient extension not available");
            quit(true);
        }
        glEnable(GL_TEXTURE_2D);
        setupLighting();
        glEnable(GL_NORMALIZE);
        glEnable(GL_POLYGON_OFFSET_FACTOR);
        glPolygonOffset(2.5F, 0.0F);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_TEXTURE_INTENSITY_SIZE);

        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FAIL_VALUE_ARB, 0.5f);

        glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
    }

    public static void setupAA() {
        glEnable(GL_BLEND);
        glEnable(GL_ALPHA_TEST);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }

    public static Model createOBJ(String path) {
        try {
            Model model;
            model = OBJLoader.loadTexturedModel(new File(path));
            System.out.println(model.name);
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            quit(true);
        }
        return null;
    }

    public static void optimizeOpenGL() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        if (camera != null) {
            camera.applyOptimalStates();
        }
    }

    private static void quit(boolean asError) {
        Display.destroy();
        System.exit(asError ? 1: 0);
    }
}
