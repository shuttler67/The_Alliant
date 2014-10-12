package net.shuttler.alliant.client.graphics.model;

import net.shuttler.alliant.client.texture.TextureLoader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class OBJLoader {

    public static Model loadTexturedModel(File f) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Model m = new Model();

        HashMap<String, Integer> materials = new HashMap<String, Integer>();
        int currentMaterial = -1;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            String[] split = line.split(" ");
            if (line.startsWith("mtllib ")) {

                String materialFileName = line.substring(7);
                File materialFile = new File(f.getParentFile().getAbsolutePath() + "/" + materialFileName);
                BufferedReader materialReader = new BufferedReader(new FileReader(materialFile));

                String materialLine;
                String parseMaterialName = "";

                Model.Material parseMaterial = new Model.Material();

                while ((materialLine = materialReader.readLine()) != null) {
                    if (materialLine.startsWith("#") || materialLine.isEmpty()) {
                        continue;
                    }
                    String[] mtlsplit = materialLine.split(" ");

                    if (materialLine.startsWith("newmtl ")) {
                        if (!parseMaterialName.equals("")) {
                            materials.put(parseMaterialName, m.getMaterials().size());
                            parseMaterial.name = parseMaterialName;
                            m.getMaterials().add(parseMaterial);
                            parseMaterial = new Model.Material();
                        }
                        parseMaterialName = materialLine.split(" ")[1];

                    } else if (materialLine.startsWith("Ns ")) {
                        parseMaterial.specularCoefficient = Float.valueOf(mtlsplit[1]);

                    } else if(materialLine.startsWith("d ")) {
                        parseMaterial.transparency = Float.valueOf(mtlsplit[1]);

                    } else if (materialLine.startsWith("Ka " )) {
                        parseFloats(parseMaterial.ambientColour, materialLine);

                    } else if (materialLine.startsWith("Ks " )) {
                        parseFloats(parseMaterial.specularColour, materialLine);

                    } else if (materialLine.startsWith("Kd " )) {
                        parseFloats(parseMaterial.diffuseColour, materialLine);

                    } else if (materialLine.startsWith("map_Kd " )) {
                        parseMaterial.texture = TextureLoader.loadTextureFromFile(f.getParent() + "/" + mtlsplit[1], true);
                    }
                }
                materials.put(parseMaterialName, m.getMaterials().size());
                parseMaterial.name = parseMaterialName;
                m.getMaterials().add(parseMaterial);
                materialReader.close();
            } else if (line.startsWith( "usemtl ")) {
                currentMaterial = materials.get(split[1]);

            } else if (line.startsWith("v ")) {
                m.getVertices().add( parseFloats(new Vector3f(), line));

            } else if (line.startsWith("vt ")) {
                m.getTextureCoordinates().add( parseFloats(new Vector2f(), line));

            } else if (line.startsWith("vn ")) {
                m.getNormals().add( parseFloats(new Vector3f(), line));

            } else if(line.startsWith("o ")) {
                m.name = line.substring(2);

            } else if (line.startsWith("f ")) {
                int[] vertexIndicesArray = new int[split.length-1];
                int[] textureCoordinateIndicesArray = null;
                int[] normalIndicesArray = new int[split.length-1];
                if (m.hasTextureCoordinates())
                    textureCoordinateIndicesArray = new int[split.length];

                for (int i = 0; i < split.length-1; ++i) {
                    vertexIndicesArray[i] = Integer.parseInt(split[i+1].split("/")[0]);
                    if (textureCoordinateIndicesArray != null)
                        textureCoordinateIndicesArray[i] = Integer.parseInt(split[i+1].split("/")[1]);
                    if (m.hasNormals())
                        normalIndicesArray[i] = Integer.parseInt(split[i+1].split("/")[2]);
                    else
                        normalIndicesArray[i] = 0;
                }

                m.getFaces().add(new Model.Face(vertexIndicesArray, normalIndicesArray, textureCoordinateIndicesArray, currentMaterial));

            } else if(line.startsWith("s ")) {
                m.setSmoothShadingEnabled(!line.contains("off"));
            }
        }
        System.out.println(materials.size());
        reader.close();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        return m;
    }

    private static float[] parseFloats(float[] output, String line) {
        String[] elements = line.split(" ");
        for (int i = 0; i < output.length; i++) {
            output[i] = Float.parseFloat(elements[i+1]);
        }
        return output;
    }
    private static Vector3f parseFloats(Vector3f output, String line) {
        String[] elements = line.split(" ");
        output.x = Float.parseFloat(elements[1]);
        output.y = Float.parseFloat(elements[2]);
        output.z = Float.parseFloat(elements[3]);
        return output;
    }
    private static Vector2f parseFloats(Vector2f output, String line) {
        String[] elements = line.split(" ");
        output.x = Float.parseFloat(elements[1]);
        output.y = Float.parseFloat(elements[2]);
        return output;
    }

}
