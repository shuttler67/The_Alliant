package net.shuttler.alliant.client;

import net.shuttler.alliant.client.audio.AudioController;
import net.shuttler.alliant.client.graphics.Light;
import net.shuttler.alliant.client.graphics.model.Model;
import net.shuttler.alliant.client.graphics.model.OBJLoader;
import net.shuttler.alliant.client.graphics.opengl.GLAllocation;
import net.shuttler.alliant.client.graphics.opengl.GLMatrix;
import net.shuttler.alliant.client.graphics.opengl.GLMatrixMode;
import net.shuttler.alliant.client.graphics.render.RenderGlobal;
import net.shuttler.alliant.client.graphics.shaders.Shaders;
import net.shuttler.alliant.entity.EntityPlayer;
import net.shuttler.alliant.world.World;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL20;

import java.io.File;

import static org.lwjgl.opengl.GL11.*;

public class Alliant {

    private static final String TITLE = "The Alliant";
    private static Alliant theAlliant;

    public Timer timer = new Timer();
    public AudioController audioController;
    public GameSettings gameSettings;

    public World theWorld;
    public EntityPlayer thePlayer;

    public RenderGlobal renderEngine;

//    public GuiScreen currentScreen;
//    public FontRenderer fontRenderer;

    public boolean skipRenderWorld;
    private boolean isGamePaused;

    public int displayWidth;
    public int displayHeight;
    public boolean fullscreen;
    public boolean inGameHasFocus;
    public boolean hasCrashed = false;
    private boolean running = true;

    private Model testModel;

    public Alliant(int displayWidth, int displayHeight, boolean fullscreen) {
        theAlliant = this;

        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.fullscreen = fullscreen;
    }

    private void startGame() {
        this.audioController = AudioController.create();
        skipRenderWorld = true;

        createWindow();

        glEnable(GL_SMOOTH);
        glShadeModel(GL_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glEnable(GL_ALPHA_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

        GLMatrix.init();

        GLMatrix.setMatrixMode(GLMatrixMode.PROJECTION);
        GLMatrix.loadPerspectiveMatrix(90f, (float) displayWidth / (float) displayHeight, 0.001f, 1000);
        GLMatrix.setMatrixMode(GLMatrixMode.MODELVIEW);

        try {
            testModel = OBJLoader.loadTexturedModel(new File("res/models/Wraith Raider Starship.obj"));
        } catch (Exception e) {
            e.printStackTrace();
            shutdownTheAlliant();
        }

        renderEngine = new RenderGlobal();
        renderEngine.lights.add(new Light(new float[]{0, 10.0f, 0}, new float[]{1, 1, 1}, 0.0f));
        renderEngine.renderers.add(testModel);

        glViewport(0, 0, displayWidth, displayHeight);

    }

    public void run() {

        try
        {
            this.startGame();
        } catch (Throwable throwable)
        {
            throwable.printStackTrace();//CRASH REPORT
            return;
        }

        try
        {
            while (this.running)
            {
                if (!this.hasCrashed)
                {
                    try
                    {
                        this.runGameLoop();
                    }
                    catch (OutOfMemoryError outOfMemoryError)
                    {
                        //this.freeMemory();
                        //DISPLAY ERROR SCREEN
                        outOfMemoryError.printStackTrace();
                        System.gc();
                    }
                }
            }
        }
        catch (AlliantError alliantError)
        {
            alliantError.printStackTrace();
            //CRASH REPORT
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
            //CRASH REPORT

        }
        finally
        {
            this.shutdownTheAlliant();
        }
    }

    private void runGameLoop()
    {
        this.update();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        GLMatrix.translate(0, -1, -1);
        GLMatrix.scale(0.006f, 0.006f, 0.006f);

        renderEngine.render();
    }

    private void shutdown() {
        running = false;
    }

    public void shutdownTheAlliant() {

        try
        {
            try
            {
                this.loadWorld(null);
                GL20.glDeleteProgram(Shaders.VBO_TEXTURED_PHONG_LIGHTING);
            }
            catch (Throwable throwable1) {}

            try
            {
                GLAllocation.deleteAllAllocations();
            }
            catch (Throwable throwable2) {}

            this.audioController.destroy();
        }
        finally
        {
            Display.destroy();

            if (!hasCrashed) {
                System.exit(0);
            }
        }

        System.gc();
    }

    private void loadWorld(World world) {
        if (world == null) {

        }
    }

    public void createWindow() {
        try {
			Display.setResizable(true);
			Display.setTitle(TITLE);
			Display.setLocation(100, 100);
			Display.setDisplayMode(new DisplayMode(displayWidth, displayHeight));
            Display.setFullscreen(fullscreen);
			Display.create();

			Mouse.create();
			Keyboard.create();
		} catch (LWJGLException e) {
			System.err.println("The display wasn't initialised properly D:");
            Display.destroy();
            System.exit(1);
		}
    }

    private void loadScreen() {
        //load Loading screen
    }

    public void update()
    {
        Display.update();
        Display.sync(120);
        timer.update();

        if (Mouse.isButtonDown(0)) {
            Mouse.setGrabbed(true);
        } else if (Mouse.isButtonDown(1)) {
            Mouse.setGrabbed(false);
        }
        renderEngine.update();
        if (Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            shutdown();
        }
    }
}
