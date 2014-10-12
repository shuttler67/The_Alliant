package net.shuttler.alliant.client.graphics.shaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class ShaderLoader {

    public static int loadShaderPair(String shaderLocation) {
        return loadShaderPair(shaderLocation, ".vs", ".fs");
    }

    public static int loadShaderPair(String shaderLocation, String vertExten, String fragExten ) {
        int shaderProgram = glCreateProgram();
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);

        StringBuilder vertexShaderSource = new StringBuilder();
        StringBuilder fragmentShaderSource = new StringBuilder();
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(shaderLocation + vertExten));
            String line;
            while ((line = fileReader.readLine()) != null) {
                vertexShaderSource.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(shaderLocation + fragExten));
            String line;
            while ((line = fileReader.readLine()) != null) {
                fragmentShaderSource.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex shader wasn't able to be compiled correctly. Error log:");
            System.err.println(glGetShaderInfoLog(vertexShader, 1024));
            return -1;
        }
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment shader wasn't able to be compiled correctly. Error log:");
            System.err.println(glGetShaderInfoLog(fragmentShader, 1024));
            glDeleteShader(vertexShader);
            return -1;
        }

        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            System.err.println("Shader program wasn't linked correctly, a.k.a. you got some problem in your shader codes");
            System.err.println(glGetProgramInfoLog(shaderProgram, 1024));
            glDeleteProgram(shaderProgram);
            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            return -1;
        }
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        return shaderProgram;
    }
}
