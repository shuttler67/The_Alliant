package net.shuttler.alliant.util;

import org.lwjgl.BufferUtils;

import java.nio.*;

public class BufferTools {
    public static FloatBuffer asFlippedBuffer(float... buffer) {
        FloatBuffer b = BufferUtils.createFloatBuffer(buffer.length).put(buffer);
        b.flip();
        return b;
    }

    public static IntBuffer asFlippedBuffer(int... buffer) {
        IntBuffer b = BufferUtils.createIntBuffer(buffer.length).put(buffer);
        b.flip();
        return b;
    }

    public static ByteBuffer asFlippedBuffer(byte... buffer) {
        ByteBuffer b = BufferUtils.createByteBuffer(buffer.length).put(buffer);
        b.flip();
        return b;
    }

    public static CharBuffer asFlippedBuffer(char... buffer) {
        CharBuffer b = BufferUtils.createCharBuffer(buffer.length).put(buffer);
        b.flip();
        return b;
    }

    public static DoubleBuffer asFlippedBuffer(double... buffer) {
        DoubleBuffer b = BufferUtils.createDoubleBuffer(buffer.length).put(buffer);
        b.flip();
        return b;
    }
}
