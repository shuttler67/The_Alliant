package net.shuttler.alliant.client.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.util.WaveData;
import org.newdawn.slick.openal.OggData;
import org.newdawn.slick.openal.OggDecoder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.openal.AL10.*;

public class AudioController extends OpenALChecker {

    public static final AudioController INSTANCE = new AudioController();

    private HashMap<String, Integer> loadedFiles = new HashMap<String, Integer>();
    private HashMap<String, Streamer> streamers = new HashMap<String, Streamer>(8);
    private OggDecoder oggDecoder = new OggDecoder();

    private FloatBuffer listenerPos;
    private FloatBuffer listenerVel;
    private FloatBuffer listenerOrient;
    private IntBuffer sources;
    private int maxSources = 64;
    private int sourceCount = 0;

    private FloatBuffer sourcePos;
    private FloatBuffer sourceVel;

    public static AudioController create() {
        try {
            AL.create();
            INSTANCE.init();
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
        return INSTANCE;
    }

    private void init() {
        sources = BufferUtils.createIntBuffer(maxSources);
        while (alGetError() == AL_NO_ERROR) {
            IntBuffer temp = BufferUtils.createIntBuffer(1);

            alGenSources(temp);

            if (alGetError() == AL_NO_ERROR) {
                sourceCount++;
                sources.put(temp.get(0));
                if (sourceCount > maxSources-1) {
                    break;
                }
            } else {
                break;
            }
        }
        listenerPos = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }); listenerPos.flip();
        listenerVel = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f }); listenerVel.flip();
        listenerOrient = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f }); listenerOrient.flip();
        alListener(AL_POSITION, listenerPos);
        alListener(AL_VELOCITY, listenerVel);
        alListener(AL_ORIENTATION, listenerOrient);
    }

    private int loadALBuffer(String path) {
		int buffer = alGenBuffers();

		checkForALError();

		if (path.toLowerCase().endsWith(".wav")) {
			try {
                BufferedInputStream file = new BufferedInputStream(new FileInputStream(path));
				WaveData waveFile = WaveData.create(file);
				alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
				waveFile.dispose();
			} catch (FileNotFoundException e) {
				throw new RuntimeException("No such file: " + path);
			}
		} else if (path.toLowerCase().endsWith(".ogg")) {
			try {
				OggData oggFile = oggDecoder.getData(new FileInputStream(path));

				alBufferData(buffer, oggFile.channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, oggFile.data, oggFile.rate);

			} catch (IOException e) {
				throw new RuntimeException("No such file: " + path);
			}
		}

		checkForALError();

		return buffer;
	}

    public void destroy() {
        IntBuffer scratch = BufferUtils.createIntBuffer(loadedFiles.size());
        for ( Integer i :loadedFiles.values()) {
            scratch.put(i);
        }
        alDeleteBuffers(scratch);
        alDeleteSources(sources);
        AL.destroy();
    }

	public int getLoadedALBuffer(String path) {

		if (loadedFiles.containsKey(path))
			return loadedFiles.get(path);

		int buffer = loadALBuffer(path);

		loadedFiles.put(path, buffer);
		return buffer;
	}

    private int findFreeSource() {
        for (int i = streamers.size(); i < sourceCount; ++i) {
            int state = alGetSourcei(sources.get(i),AL_SOURCE_STATE);
            if (state !=  AL_PLAYING && state != AL_PAUSED) {
                return i;
            }
        }
        return -1;
    }

    public int playAsSoundAt(int buffer, float pitch, float volume, boolean loop, float x, float y, float z) {
        int nextSource = findFreeSource();
        if (nextSource == -1) {
            return -1;
        }
        nextSource = sources.get(nextSource);
        alSourceStop(nextSource);
        alSourcei(nextSource, AL_BUFFER, buffer);
        alSourcef(nextSource, AL_PITCH, pitch);
        alSourcef(nextSource, AL_GAIN, volume);
        alSourcei(nextSource, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);

        sourcePos.clear();
        sourceVel.clear();
        sourcePos.put(new float[]{x, y, z});
        sourceVel.put(new float[]{0, 0, 0});
        sourcePos.flip();
        sourceVel.flip();
        alSource(nextSource, AL_POSITION, sourcePos);
        alSource(nextSource, AL_VELOCITY, sourceVel);

        alSourcePlay(nextSource);

        return nextSource;
    }

    public int playAsStreamAt(String path, float pitch, float volume, boolean loop, float x, float y, float z) {
        int nextSource;
        Streamer streamer = streamers.get(path);
        if (streamer == null) {
            streamers.put(path, new Streamer(sources.get(streamers.size()), path));
            streamer = streamers.get(path);
        }

        nextSource = streamer.getSource();

        try {
            streamer.play(loop);

        } catch (IOException e) {
            e.printStackTrace();
        }

        alSourcef(nextSource, AL_PITCH, pitch);
        alSourcef(nextSource, AL_GAIN, volume);
        alSourcei(nextSource, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);

        sourcePos.clear();
        sourceVel.clear();
        sourcePos.put(new float[]{x, y, z});
        sourceVel.put(new float[]{0, 0, 0});
        sourcePos.flip();
        sourceVel.flip();
        alSource(nextSource, AL_POSITION, sourcePos);
        alSource(nextSource, AL_VELOCITY, sourceVel);

        return nextSource;
    }

    public void setListenerPosition(float x, float y, float z) {
        listenerPos.clear();
        listenerPos.put(new float[]{x, y, z});
        listenerPos.flip();
        alListener(AL_POSITION, listenerPos);
    }

    public void setListenerVelocity(float x, float y, float z) {
        listenerVel.clear();
        listenerVel.put(new float[]{x, y, z});
        listenerVel.flip();
        alListener(AL_VELOCITY, listenerVel);
    }

    public void setListenerOrientation( float fx, float fy, float fz, char upaxis) {
        float ux = 0, uy = 0, uz = 0;

        if (upaxis == 'x')
            ux = 1;
        else if (upaxis == 'y')
            uy = 1;
        else if (upaxis == 'z')
            uz = 1;

        listenerOrient.clear();
        listenerOrient.put(new float[]{fx, fy, fz, ux, uy, uz});
        listenerOrient.flip();
        alListener(AL_ORIENTATION, listenerOrient);
    }

    public float[] getListenerPosition() {
        return listenerPos.array();
    }

    public float[] getListenerVelocity() {
        return listenerVel.array();
    }

    public float[] getListenerOrientation() {
        return listenerOrient.array();
    }

    public float getVolume() {
        return alGetListenerf(AL_GAIN);
    }

    public void setVolume( float volume) {
        alListenerf(AL_GAIN, volume);
    }

    public void stopAllSources() {
        for (Streamer streamer : streamers.values()) {
            try {
                streamer.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        alSourceStop(sources);
    }
    public void pauseAllSources() {
        alSourcePause(sources);
    }
    public void rewindAllSources() {
        for (Streamer streamer : streamers.values()) {
            try {
                streamer.rewind();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        alSourceRewind(sources);
    }
}
