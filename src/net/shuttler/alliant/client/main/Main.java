package net.shuttler.alliant.client.main;

import net.shuttler.alliant.client.Alliant;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Alliant alliant = new Alliant(640, 480, false);

        alliant.run();
    }
}
