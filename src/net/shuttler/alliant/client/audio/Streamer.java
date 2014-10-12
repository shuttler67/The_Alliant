package net.shuttler.alliant.client.audio;

import org.lwjgl.BufferUtils;
import org.newdawn.slick.openal.OggInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.*;

public class Streamer extends OpenALChecker {

    private int STREAMBUFFER_SIZE;
    private int STREAMBUFFER_COUNT;
    private String path;

    private OggInputStream oggStream;
    private IntBuffer streamBuffers;
    private int source;

    private byte[] readingBuffer; //reading readingBuffer
    private ByteBuffer bufferData; //give to openAL
    private int remainingBufferCount;
    private IntBuffer unqueued = BufferUtils.createIntBuffer(1);
    private boolean done;
    private boolean loop;
    private float positionOffset;

    private void initStreams() throws IOException {
        if (oggStream != null) {
            oggStream.close();
        }

        oggStream = new OggInputStream( new BufferedInputStream(new FileInputStream(new File(path))) );
        positionOffset = 0;
    }

    public void play(boolean loop) throws IOException {
        stop();

        done = false;
        this.loop = loop;

        playback();
    }

    public void stop() throws IOException {
        initStreams();

        done = true;

        alSourceStop(source);
        removeBuffers();
    }

    public void rewind() throws IOException {
        if (done)
            return;

        if (alGetSourcei(source, AL_SOURCE_STATE) == AL_PAUSED) {
            play(loop);
            alSourcePause(source);
        } else {
            play(loop);
        }
    }

    private void playback() {
        alSourcei(source, AL_LOOPING, AL_FALSE);

        remainingBufferCount = STREAMBUFFER_COUNT;

        for (int i = 0; i < STREAMBUFFER_COUNT; i++) {
            stream(streamBuffers.get(i));
        }

        alSourceQueueBuffers(source, streamBuffers);
        alSourcePlay(source);
        checkForALError();
    }

    public boolean stream(int buffer) {
        try {
            int size = oggStream.read(readingBuffer);

            if (size != -1) {
                bufferData.clear();
                bufferData.put(readingBuffer, 0, size);
                bufferData.flip();

                int format = oggStream.getChannels() > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;

                alBufferData(buffer, format, bufferData, oggStream.getRate());

                return alGetError() == AL_NO_ERROR;

            } else {
                if (loop) {
                    initStreams();
                    stream(buffer);
                } else {
                    done = true;
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void removeBuffers() {
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        int queued = alGetSourcei(source, AL_BUFFERS_QUEUED);

        while (queued > 0) {
            alSourceUnqueueBuffers(source, buffer);
            --queued;
        }
    }

    public void update() {
        if (done)
            return;

        float sampleRate = oggStream.getRate();
        float sampleSize;
        if (oggStream.getChannels() > 1)
            sampleSize = 4;
        else
            sampleSize = 2;

        int processed = alGetSourcei(source, AL_BUFFERS_PROCESSED);

        while (processed > 0) {
            unqueued.clear();

            alSourceUnqueueBuffers(source, unqueued);
            checkForALError();

            int bufferIndex = unqueued.get(0);

            float bufferLength = (alGetBufferi(bufferIndex, AL_SIZE) / sampleSize) / sampleRate;
            positionOffset += bufferLength;

            if (stream(bufferIndex)) {
                alSourceQueueBuffers(source, unqueued);
                checkForALError();
            } else {
                remainingBufferCount--;
                if (remainingBufferCount == 0) {
                    done = true;
                }
            }

            processed--;
        }

        if (alGetSourcei(source, AL_SOURCE_STATE) != AL_PLAYING) {
            alSourcePlay(source);
        }
    }

    public boolean done() {
        return done;
    }

    public boolean setPosition(float position) {
        try {
            if (getPosition() > position || oggStream == null ) {
                initStreams();
            }

            float sampleRate = oggStream.getRate();
            float sampleSize;
            if (oggStream.getChannels() > 1) {
                sampleSize = 4; // AL10.AL_FORMAT_STEREO16
            } else {
                sampleSize = 2; // AL10.AL_FORMAT_MONO16
            }

            while (positionOffset < position) {
                int count = oggStream.read(readingBuffer);
                if (count != -1) {
                    float bufferLength = (count / sampleSize) / sampleRate;
                    positionOffset += bufferLength;
                } else {
                    if (loop) {
                        initStreams();
                    } else {
                        done = true;
                    }
                    return false;
                }
            }

            playback();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public float getPosition() {
        return positionOffset;
    }

    public int getSource() {
        return source;
    }

    public Streamer(int sourceid, String streamPath, int bufferSize, int bufferCount) {
        STREAMBUFFER_SIZE = bufferSize;
        STREAMBUFFER_COUNT = bufferCount;
        this.source = sourceid;

        path = streamPath;
        streamBuffers = BufferUtils.createIntBuffer(STREAMBUFFER_COUNT);

        alGenBuffers(streamBuffers);
        checkForALError();

        readingBuffer = new byte[STREAMBUFFER_SIZE];
        bufferData = BufferUtils.createByteBuffer(STREAMBUFFER_SIZE);
    }

    public Streamer(int sourceid, String path) {
        this(sourceid, path, 4096*20, 3);
    }
}
