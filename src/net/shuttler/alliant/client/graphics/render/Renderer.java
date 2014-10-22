package net.shuttler.alliant.client.graphics.render;

import net.shuttler.alliant.client.graphics.opengl.GLMatrix;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Renderer{

    public static void drawArray( int array, int start, int count)
    {
        int currentShader = glGetInteger(GL_CURRENT_PROGRAM);

        int MV = glGetUniformLocation(currentShader, "ModelViewMatrix");  //Eventually make string not so hard coded with Shader class
        int MVP = glGetUniformLocation(currentShader, "ModelViewProjectionMatrix"); //Eventually make string not so hard coded with Shader class
        int N = glGetUniformLocation(currentShader, "NormalMatrix"); //Eventually make string not so hard coded with Shader class
        GLMatrix.sendMatricesToUniform(MV, MVP, N);

        glBindVertexArray(array);

        glDrawArrays(GL_TRIANGLES, start, count);

        glBindVertexArray(0);
    }
}
