package net.shuttler.alliant.client.graphics.model;

import net.shuttler.alliant.client.graphics.opengl.GLAllocation;
import net.shuttler.alliant.client.graphics.render.Renderable;
import net.shuttler.alliant.client.graphics.render.Renderer;
import net.shuttler.alliant.client.graphics.shaders.Shaders;
import net.shuttler.alliant.util.BufferTools;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glMaterial;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Model implements Renderable {

    private final List<Vector3f> vertices = new ArrayList<Vector3f>();
    private final List<Vector2f> textureCoordinates = new ArrayList<Vector2f>();
    private final List<Vector3f> normals = new ArrayList<Vector3f>();
    private final List<Face> faces = new ArrayList<Face>();
    private final List<Material> materials = new ArrayList<Material>();
    private boolean enableSmoothShading;

    public String name;
    private boolean compiled = false;
    private int displayList;
    private int vertexArrayObject;
    private int vboSize;

    public void enableStates() {
        if (hasTextureCoordinates()) {
            glEnable(GL_TEXTURE_2D);
        }
        if (isSmoothShadingEnabled()) {
            glShadeModel(GL_SMOOTH);
        } else {
            glShadeModel(GL_FLAT);
        }
    }

    public boolean hasTextureCoordinates() {
        return getTextureCoordinates().size() > 0;
    }

    public boolean hasNormals() {
        return getNormals().size() > 0;
    }

    public List<Vector3f> getVertices() {
        return vertices;
    }

    public List<Vector2f> getTextureCoordinates() {
        return textureCoordinates;
    }

    public List<Vector3f> getNormals() {
        return normals;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public boolean isSmoothShadingEnabled() {
        return enableSmoothShading;
    }

    public void setSmoothShadingEnabled(boolean smoothShadingEnabled) {
        this.enableSmoothShading = smoothShadingEnabled;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public int compileDisplayList() {
        int list = GLAllocation.generateDisplayList(1);
        glNewList(list, GL_COMPILE);
        {
            glBegin(GL_TRIANGLES);
            for (Face face : getFaces()) {
                if (face.hasTextureCoordinates()) {
                    Material m = materials.get(face.getMaterial());
                    if (m != null) {
                        glEnd();
                        m.sendToGLMaterial();
                        glBegin(GL_TRIANGLES);
                    }
                }

                for (int i = 0; i < 3; ++i) {
                    if (face.hasNormals()) {
                        Vector3f n = getNormals().get(face.getNormalIndices()[i] - 1);
                        glNormal3f(n.x, n.y, n.z);
                    }
                    if (face.hasTextureCoordinates()) {
                        Vector2f t = getTextureCoordinates().get(face.getTextureCoordinateIndices()[i] - 1);
                        glTexCoord2f(t.x, t.y);
                    }
                    Vector3f v = getVertices().get(face.getVertexIndices()[i] - 1);
                    glVertex3f(v.x, v.y, v.z);
                }
            }
            glEnd();
        }
        glEndList();
        compiled = true;
        return list;
    }

    public void renderDisplayList() {
        if (!compiled) {
            displayList = compileDisplayList();
            materials.clear();
            freeUnnecessaryMemory();
        }
        glCallList(displayList);
    }

    public int createVAO()
    {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = GLAllocation.generateBuffer();
        System.out.println(glGetError() == GL_NO_ERROR);

        vboSize = faces.size() * 3;

        FloatBuffer data = BufferUtils.createFloatBuffer(faces.size() * 3 * (3 + 4 + 2) );

        for (Model.Face face : faces)
        {
            for (int i = 0; i < 3; i++) //i < face.getVertexIndices().length
            {
                Vector3f v = vertices.get(face.getVertexIndices()[i] - 1);
                data.put(new float[]{v.x, v.y, v.z});

                if (hasNormals())
                {
                    Vector3f n = normals.get(face.getNormalIndices()[i] - 1);
                    data.put(new float[]{n.x, n.y, n.z, face.getMaterial()});
                }
                if (hasTextureCoordinates())
                {
                    Vector2f t = textureCoordinates.get(face.getTextureCoordinateIndices()[i] - 1);
                    data.put(new float[]{t.x, t.y});
                }
            }
        }

        data.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        System.out.println(glGetError() == GL_NO_ERROR);

        for (int i = 0; i < 4; i++) {
            glEnableVertexAttribArray(i);
        }

        int stride = hasNormals() ? (hasTextureCoordinates() ? 36 : 28) : 0; //36 = (numVertexComponents + numNormalComponents + numTextureComponents) * sizeOfBufferTypeInBytes = (3 + 4 + 2) * 4

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
        if (hasNormals()) {
            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 12L);
            glVertexAttribPointer(2, 1, GL_FLOAT, false, stride, 24L);
        }
        if (hasTextureCoordinates()) {
            glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 28L);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        compiled = true;
        return vao;
    }

    public void renderVBO()
    {
        if (!compiled) {
            vertexArrayObject = createVAO();
            freeUnnecessaryMemory();
        }

        glPushAttrib(GL_ALL_ATTRIB_BITS);
        enableStates();

        glEnableClientState(GL_VERTEX_ARRAY);

        int currentShader = glGetInteger(GL_CURRENT_PROGRAM);

        if (currentShader != 0)
        {
            for (int i = 0; i < materials.size(); i++)
            {
                if (i > 19) { //eventually shader defined
                    break;
                }

                Material m = materials.get(i);

                m.sendToShader(currentShader, "materials["+i+"]"); //Eventually make string not so hard coded with Shader class
            }
        }

        Renderer.drawArray(vertexArrayObject, 0, vboSize);

        glPopAttrib();
    }

    private void freeUnnecessaryMemory()
    {
        vertices.clear();
        textureCoordinates.clear();
        normals.clear();
        faces.clear();
        System.gc();
    }

    @Override
    public void render() {
        if (GLContext.getCapabilities().OpenGL30) {
            renderVBO();
        } else {
            renderDisplayList();
        }
    }

    public static class Material {
        @Override
        public String toString() {
            return name + "{" +
                    "specularCoefficient = " + specularCoefficient +
                    ", specularColour = " + specularColour[0]+' '+specularColour[1]+' '+specularColour[2] +
                    ", ambientColour = " + ambientColour[0]+' '+ambientColour[1]+' '+ambientColour[2] +
                    ", diffuseColour = " + diffuseColour[0]+' '+diffuseColour[1]+' '+diffuseColour[2] +
                    ", texture = " + texture +
                    ", textureIndex = " + textureIndex +
                    '}';
        }
        public String name;
        public float specularCoefficient = 1;
        public float transparency = 1;
        public float[] ambientColour = {0.2f, 0.2f, 0.2f};
        public float[] diffuseColour = {0.3f, 1, 1};
        public float[] specularColour = {1, 1, 1};
        public int texture = -1;
        public int textureIndex = -1;

        public void sendToGLMaterial() {
            if (texture != -1) {
                glBindTexture(GL_TEXTURE_2D, texture);
            } else {
                glBindTexture(GL_TEXTURE_2D, 0);
            }

            glMaterial(GL_FRONT, GL_DIFFUSE, BufferTools.asFlippedBuffer(diffuseColour[0], diffuseColour[1], diffuseColour[2], transparency));
            glMaterial(GL_FRONT, GL_AMBIENT, BufferTools.asFlippedBuffer(ambientColour[0], ambientColour[1], ambientColour[2], 1));
            glMaterial(GL_FRONT, GL_SPECULAR, BufferTools.asFlippedBuffer(specularColour[0], specularColour[1], specularColour[2], 1));
            glMaterialf(GL_FRONT, GL_SHININESS, specularCoefficient);
        }

        public void sendToShader( int shader, String materialName)
        {
            int shininessLocation = glGetUniformLocation(shader, materialName + ".shininess"); //Eventually make string not so hard coded with Shader class
            int diffuseColorLocation = glGetUniformLocation(shader, materialName + ".diffuseColor"); //Eventually make string not so hard coded with Shader class
            int ambientColorLocation = glGetUniformLocation(shader, materialName + ".ambientColor"); //Eventually make string not so hard coded with Shader class
            int specularColorLocation = glGetUniformLocation(shader, materialName + ".specularColor"); //Eventually make string not so hard coded with Shader class
            int textureIndexLocation = glGetUniformLocation(shader, materialName + ".textureIndex"); //Eventually make string not so hard coded with Shader class;

            glUniform1f(shininessLocation, specularCoefficient);
            glUniform4(diffuseColorLocation, BufferTools.asFlippedBuffer(diffuseColour[0], diffuseColour[1], diffuseColour[2], transparency));
            glUniform3(ambientColorLocation, BufferTools.asFlippedBuffer(ambientColour[0], ambientColour[1], ambientColour[2]));
            glUniform3(specularColorLocation, BufferTools.asFlippedBuffer(specularColour[0], specularColour[1], specularColour[2]));
            glUniform1i(textureIndexLocation, textureIndex);

            if (textureIndex != -1) {
                int textures = glGetUniformLocation(shader, "textures[" +textureIndex+ "]"); //Eventually make string not so hard coded with Shader class

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureIndex);
                glBindTexture(GL_TEXTURE_2D, texture);
                glUniform1i(textures, textureIndex);
            }
        }
    }

    public static class Face {
        private final int[] vertexIndices = {-1, -1, -1};
        private final int[] normalIndices = {-1, -1, -1};
        private final int[] textureCoordinateIndices = {-1, -1, -1};
        private final int material;

        public int getMaterial() {
            return material;
        }

        public boolean hasNormals() {
            return normalIndices[0] != -1;
        }

        public boolean hasTextureCoordinates() {
            return textureCoordinateIndices[0] != -1;
        }

        public int[] getVertexIndices() {
            return vertexIndices;
        }

        public int[] getNormalIndices() {
            return normalIndices;
        }

        public int[] getTextureCoordinateIndices() {
            return textureCoordinateIndices;
        }

        public Face(int[] vertexIndices) {
            System.arraycopy(vertexIndices, 0, this.vertexIndices, 0, 3);
            material = -1;
        }

        public Face(int[] vertexIndices, int[] normalIndices) {
            System.arraycopy(vertexIndices, 0, this.vertexIndices, 0, 3);
            System.arraycopy(normalIndices, 0, this.normalIndices, 0, 3);
            material = -1;
        }

        public Face(int[] vertexIndices, int[] normalIndices, int[] textureCoordinateIndices, int material) {
            System.arraycopy(vertexIndices, 0, this.vertexIndices, 0, 3);
            System.arraycopy(normalIndices, 0, this.normalIndices, 0, 3);
            System.arraycopy(textureCoordinateIndices, 0, this.textureCoordinateIndices, 0, 3);
            this.material = material;
        }
    }
}
