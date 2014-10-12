package net.shuttler.alliant.client.graphics.model;

import net.shuttler.alliant.client.graphics.opengl.GLAllocation;
import net.shuttler.alliant.client.graphics.render.FinalRenderer;
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
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Model extends FinalRenderer {

    private final List<Vector3f> vertices = new ArrayList<Vector3f>();
    private final List<Vector2f> textureCoordinates = new ArrayList<Vector2f>();
    private final List<Vector3f> normals = new ArrayList<Vector3f>();
    private final List<Face> faces = new ArrayList<Face>();
    private final List<Material> materials = new ArrayList<Material>();
    private boolean enableSmoothShading;

    public String name;
    private boolean compiled = false;
    private int displayList;
    private int vertexDataHandles;
    private int vboSize;

    public Model()
    {
        shaderProgram = GLContext.getCapabilities().OpenGL30 ? Shaders.VBO_TEXTURED_PHONG_LIGHTING : Shaders.TEXTURED_PHONG_LIGHTING;
    }

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
                        if (m.texture != -1) {
                            glBindTexture(GL_TEXTURE_2D, m.texture);
                        } else {
                            glBindTexture(GL_TEXTURE_2D, 0);
                        }

                        glMaterial(GL_FRONT, GL_DIFFUSE, BufferTools.asFlippedBuffer(m.diffuseColour[0], m.diffuseColour[1], m.diffuseColour[2], m.transparency));
                        glMaterial(GL_FRONT, GL_AMBIENT, BufferTools.asFlippedBuffer(m.ambientColour[0], m.ambientColour[1], m.ambientColour[2], 1));
                        glMaterial(GL_FRONT, GL_SPECULAR, BufferTools.asFlippedBuffer(m.specularColour[0], m.specularColour[1], m.specularColour[2], 1));
                        glMaterialf(GL_FRONT, GL_SHININESS, m.specularCoefficient);
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

        FloatBuffer data = BufferUtils.createFloatBuffer(faces.size() * 3 * (3 + 4 + 3) );

        int currentTexture = -1;
        int currentMaterial = 0;

        for (Model.Face face : faces)
        {
            int material = face.getMaterial();

            if (material > currentMaterial && materials.get(material).texture != -1) {
                currentTexture++;
                currentMaterial = material;
                System.out.println(currentTexture);
            }

            for (int i = 0; i < 3; i++) { //i < face.getVertexIndices().length
                Vector3f v = vertices.get(face.getVertexIndices()[i] - 1);
                data.put(new float[]{v.x, v.y, v.z});

                if (hasNormals()) {
                    Vector3f n = normals.get(face.getNormalIndices()[i] - 1);
                    data.put(new float[]{n.x, n.y, n.z, material});
                }
                if (hasTextureCoordinates()) {
                    Vector2f t = textureCoordinates.get(face.getTextureCoordinateIndices()[i] - 1);
                    data.put(new float[]{t.x, t.y, currentTexture});
                }
            }
        }

        data.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

        System.out.println(glGetError() == GL_NO_ERROR);

        for (int i = 0; i < 5; i++) {
            glEnableVertexAttribArray(i);
        }

        int stride = hasNormals() ? (hasTextureCoordinates() ? 40 : 28) : 0; //40 = (numVertexComponents + numNormalComponents + numTextureComponents) * sizeOfBufferTypeInBytes = (3 + 4 + 3) * 4

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
        if (hasNormals()) {
            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 12L);
            glVertexAttribPointer(2, 1, GL_FLOAT, false, stride, 24L);
        }
        if (hasTextureCoordinates()) {
            glVertexAttribPointer(3, 2, GL_FLOAT, false, stride, 28L);
            glVertexAttribPointer(4, 1, GL_FLOAT, false, stride, 36L);
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        compiled = true;
        return vao;
    }

    public void renderVBO()
    {
        if (!compiled) {
            vertexDataHandles = createVAO();
        }

        glPushAttrib(GL_ALL_ATTRIB_BITS);

        glEnableClientState(GL_VERTEX_ARRAY);

        int currentShader = glGetInteger(GL_CURRENT_PROGRAM);

        if (currentShader != 0)
        {
            int textureCount = 0;

            for (int i = 0; i < materials.size(); i++)
            {
                if (i > 19) {
                    break;
                }

                int shininess = glGetUniformLocation(currentShader, "materials[" +i+ "].shininess"); //Eventually make string not so hard coded with Shader class
                int diffuseColor = glGetUniformLocation(currentShader, "materials[" +i+ "].diffuseColor"); //Eventually make string not so hard coded with Shader class
                int ambientColor = glGetUniformLocation(currentShader, "materials[" +i+ "].ambientColor"); //Eventually make string not so hard coded with Shader class
                int specularColor = glGetUniformLocation(currentShader, "materials[" +i+ "].specularColor"); //Eventually make string not so hard coded with Shader class

                Material m = materials.get(i);
                glUniform1f(shininess, m.specularCoefficient);
                glUniform4(diffuseColor, BufferTools.asFlippedBuffer(m.diffuseColour[0], m.diffuseColour[1], m.diffuseColour[2], m.transparency));
                glUniform3(ambientColor, BufferTools.asFlippedBuffer(m.ambientColour[0], m.ambientColour[1], m.ambientColour[2]));
                glUniform3(specularColor, BufferTools.asFlippedBuffer(m.specularColour[0], m.specularColour[1], m.specularColour[2]));

                if (m.texture != -1) {
                    int textures = glGetUniformLocation(currentShader, "textures[" +textureCount+ "]"); //Eventually make string not so hard coded with Shader class

                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureCount);
                    glBindTexture(GL_TEXTURE_2D, m.texture);
                    glUniform1i(textures, textureCount);
                    ++textureCount;
                }
            }
        }

        glBindVertexArray(vertexDataHandles);

        glDrawArrays(GL_TRIANGLES, 0, vboSize);

        glBindVertexArray(0);
        glPopAttrib();
    }

    @Override
    public void renderAll() {
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
                    '}';
        }
        public String name;
        public float specularCoefficient = 100;
        public float transparency = 1;
        public float[] ambientColour = {0.2f, 0.2f, 0.2f};
        public float[] diffuseColour = {0.3f, 1, 1};
        public float[] specularColour = {1, 1, 1};
        public int texture = -1;
    }

    public static class Face {
        private final int[] vertexIndices;
        private final int[] normalIndices;
        private final int[] textureCoordinateIndices;
        private final int material;

        public int getMaterial() {
            return material;
        }

        public boolean hasNormals() {
            return normalIndices != null;
        }

        public boolean hasTextureCoordinates() {
            return textureCoordinateIndices != null;
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
            this.vertexIndices = vertexIndices;
            normalIndices = null;
            textureCoordinateIndices = null;
            material = -1;
        }

        public Face(int[] vertexIndices, int[] normalIndices) {
            this.vertexIndices = vertexIndices;
            this.normalIndices = normalIndices;
            textureCoordinateIndices = null;
            material = -1;
        }

        public Face(int[] vertexIndices, int[] normalIndices, int[] textureCoordinateIndices, int material) {
            this.vertexIndices = vertexIndices;
            this.textureCoordinateIndices = textureCoordinateIndices;
            this.normalIndices = normalIndices;
            this.material = material;
        }
    }
}
