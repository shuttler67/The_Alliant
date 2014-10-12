package net.shuttler.alliant.client.graphics;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class Light
{
    private FloatBuffer position = BufferUtils.createFloatBuffer(3);
    private FloatBuffer color = BufferUtils.createFloatBuffer(3);
    private float attenuation = 1;

    public Light(float[] position, float[] color, float attenuation)
    {
        this.position.put(position);
        this.color.put(color);
        this.attenuation = attenuation;
    }

    public void sendDataToShader(int shader, String structName)
    {
        glUseProgram(shader);
        int lightPos = glGetUniformLocation(shader, structName + ".position"); //Eventually make string not so hard coded with Shader class
        int lightColor = glGetUniformLocation(shader, structName + ".color"); //Eventually make string not so hard coded with Shader class
        int lightAtt = glGetUniformLocation(shader, structName + ".attenuation"); //Eventually make string not so hard coded with Shader class

        position.flip(); color.flip();

        glUniform3(lightPos, position);
        glUniform3(lightColor, color);
        glUniform1f(lightAtt, attenuation);

        position.flip(); color.flip();
    }

    public float[] getPosition() {
        return position.array();
    }

    public void setPosition(float[] position) {
        this.position.clear();
        this.position.put(position);
    }

    public float[] getColor() {
        return color.array();
    }

    public void setColor(float[] color) {
        this.color.clear();
        this.color.put(color);
    }

    public float getAttenuation() {
        return attenuation;
    }

    public void setAttenuation(float attenuation) {
        this.attenuation = attenuation;
    }
}
