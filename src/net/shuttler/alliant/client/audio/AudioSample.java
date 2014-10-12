package net.shuttler.alliant.client.audio;

import java.io.IOException;

public class AudioSample {

    private int buffer;
    private String path;
    private boolean isStream;

    //returns the source used
    public int playAsEffectAt(float pitch, float volume, float x, float y, float z) {
        if (!isStream) {
            return AudioController.INSTANCE.playAsSoundAt(buffer, pitch, volume, false, x, y, z);
        } else {
            System.err.println("It is not advised to play a stream as a sound effect");
            return AudioController.INSTANCE.playAsStreamAt(path, pitch, volume, false, x, y, z);
        }
    }

    //returns the source used
    public int playAsMusic(float pitch, float volume, boolean loop) {
        if (!isStream) {
            System.err.println("It is not advised to play a static sound as music, use stream");
            return AudioController.INSTANCE.playAsSoundAt(buffer, pitch, volume, loop, 0, 0, 0);
        } else {
            return AudioController.INSTANCE.playAsStreamAt(path, pitch, volume, loop, 0, 0, 0);
        }
    }

    public static AudioSample loadAsStream(String path) throws IOException {
        AudioSample a = new AudioSample();
        a.buffer = -1;
        a.isStream = true;
        if (!path.endsWith(".ogg")) {
            throw new IOException("Cannot stream sound file: " + path);
        }
        a.path = path;
        return a;
    }

    public static AudioSample loadAsStatic(String path) {
        AudioSample a = new AudioSample();
        a.buffer = AudioController.INSTANCE.getLoadedALBuffer(path);
        a.isStream = false;
        a.path = path;
        return a;
    }
}
