package net.shuttler.alliant.client.audio;

import org.lwjgl.openal.OpenALException;

import static org.lwjgl.openal.AL10.*;

public class OpenALChecker {

    public String getALErrorString(int err) {
		switch (err)
		{
		case AL_NO_ERROR:
			return "AL_NO_ERROR";
		case AL_INVALID_NAME:
			return "AL_INVALID_NAME";
		case AL_INVALID_ENUM:
			return "AL_INVALID_ENUM";
		case AL_INVALID_VALUE:
			return "AL_INVALID_VALUE";
		case AL_INVALID_OPERATION:
			return "AL_INVALID_OPERATION";
		case AL_OUT_OF_MEMORY:
			return "AL_OUT_OF_MEMORY";
		default:
			return "No such error code";
		}
	}

	public void checkForALError() {
		int result;
		if ((result = alGetError()) != AL_NO_ERROR) {
			throw new OpenALException(getALErrorString(result));
		}
    }

}
