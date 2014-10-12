package net.shuttler.alliant.client.graphics.render;

import net.shuttler.alliant.client.graphics.Camera;
import net.shuttler.alliant.client.graphics.Light;
import net.shuttler.alliant.client.graphics.shaders.Shaders;
import net.shuttler.alliant.util.BufferTools;
import net.shuttler.alliant.world.World;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3;

public class RenderGlobal implements Renderable
{
    private World world;
    public List<Renderable> renderers;

    private FloatBuffer ambientLight;

    public List<Light> lights;

    public Camera camera;

    public RenderGlobal()
    {
        renderers = new ArrayList<Renderable>();
        ambientLight = BufferTools.asFlippedBuffer(1f, 1f, 1f);
        lights = new ArrayList<Light>();
        camera = new Camera();
        camera.applyOptimalStates();
    }

    public void sendLightsToShader(int shader)
    {
        int ambient = glGetUniformLocation(shader, "lightAmbient"); //Eventually make string not so hard coded with Shader class

        glUniform3(ambient, ambientLight);
        glUniform1i(glGetUniformLocation(shader, "lightCount"), lights.size());

        for (int i = 0; i < lights.size(); i++)
        {
            lights.get(i).sendDataToShader(shader, "lights[" + i + "]");
        }
    }

    @Override
    public void render() {
        sendLightsToShader(Shaders.VBO_TEXTURED_PHONG_LIGHTING);
        for (Renderable r : renderers)
        {
            r.render();
        }
    }

    public void update() {
        camera.applyTranslations();
        camera.processMouse(2);
        camera.processKeyboard(0.16f, 6);

    }
}
