package net.shuttler.alliant.client.graphics.opengl;

import org.lwjgl.BufferUtils;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class GLAllocation
{
    private static final Map<Integer, Integer> mapDisplayLists = new HashMap<Integer, Integer>();
    private static final List<Integer> textures = new ArrayList<Integer>();
    private static final List<Integer> buffers = new ArrayList<Integer>();

    public static synchronized int generateDisplayList(int range)
    {
        int i = glGenLists(range);
        mapDisplayLists.put(i, range);
        return i;
    }

    public static void deleteDisplayList(int displayList)
    {
        glDeleteLists(displayList, mapDisplayLists.get(displayList));
        mapDisplayLists.remove(displayList);
    }

    public static synchronized void generateTextures(IntBuffer out)
    {
        glGenTextures(out);
        while (out.hasRemaining()) {
            textures.add(out.get());
        }
        out.rewind();
    }

    public static synchronized int generateTexture()
    {
        int i = glGenTextures();
        textures.add(i);
        return i;
    }

    public static void deleteTexture(int i)
    {
        glDeleteTextures(i);
        textures.remove(new Integer(i));
    }

    public static void deleteTextures(IntBuffer buffer)
    {
        glDeleteTextures(buffer);
        while (buffer.hasRemaining()) {
            textures.remove(buffer.get());
        }
    }

    public static synchronized void generateBuffers(IntBuffer out)
    {
        glGenBuffers(out);
        while (out.hasRemaining()){
            buffers.add(out.get());
        }
        out.rewind();
    }

    public static synchronized int generateBuffer()
    {
        int i = glGenBuffers();
        buffers.add(i);
        return i;
    }

    public static void deleteBuffer(int i)
    {
        glDeleteBuffers(i);
        buffers.remove(new Integer(i));
    }

    public static void deleteBuffers(IntBuffer buffer)
    {
        glDeleteTextures(buffer);
        while (buffer.hasRemaining()){
            buffers.add(buffer.get());
        }
    }

    public static void deleteAllAllocations()
    {
        for (Object o : mapDisplayLists.entrySet()) {
            Entry entry = (Entry) o;
            glDeleteLists((Integer) entry.getKey(), (Integer) entry.getValue());
        }
        mapDisplayLists.clear();

        IntBuffer scratch = BufferUtils.createIntBuffer(textures.size());
        for (Integer i : textures)
        {
            scratch.put(i);
        }
        glDeleteTextures(scratch);
        textures.clear();

        scratch = BufferUtils.createIntBuffer(buffers.size());
        for (Integer i : buffers)
        {
            scratch.put(i);
        }
        glDeleteBuffers(scratch);
        buffers.clear();
    }
}
