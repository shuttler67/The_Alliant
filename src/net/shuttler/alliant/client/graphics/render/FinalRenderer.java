package net.shuttler.alliant.client.graphics.render;

import net.shuttler.alliant.client.graphics.opengl.GLMatrix;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.*;

public abstract class FinalRenderer implements Renderable {

    public int shaderProgram;

    public void render()
    {
        glUseProgram(shaderProgram);
        int MV = glGetUniformLocation(shaderProgram, "ModelViewMatrix");  //Eventually make string not so hard coded with Shader class
        int MVP = glGetUniformLocation(shaderProgram, "ModelViewProjectionMatrix"); //Eventually make string not so hard coded with Shader class
        int N = glGetUniformLocation(shaderProgram, "NormalMatrix"); //Eventually make string not so hard coded with Shader class

        GLMatrix.sendMatricesToUniform(MV, MVP, N);

        renderAll();
        glUseProgram(0);
    }

    public abstract void renderAll();
}
