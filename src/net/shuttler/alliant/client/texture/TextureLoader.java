package net.shuttler.alliant.client.texture;

import net.shuttler.alliant.client.graphics.opengl.GLAllocation;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

public class TextureLoader {	
	public static int loadTextureFromFile(String src, boolean smooth) throws Exception
	{
        BufferedImage image = loadImageFromFile(src);
		byte[] imageData = getImageDataBytes(image);

		if((image != null) && (imageData != null))
		{
            IntBuffer textureReference = BufferUtils.createIntBuffer(1);
            ByteBuffer bb = BufferUtils.createByteBuffer(imageData.length);
            bb.put(imageData).flip();
            GLAllocation.generateTextures(textureReference);
            int textureID = textureReference.get(0);
            glPushAttrib(GL_TEXTURE_BIT);
            glBindTexture(GL_TEXTURE_2D,textureID);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            if(smooth) {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            } else {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            }
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
            glPopAttrib();
            return textureID;

		} else {
			throw new Exception("No image!");
		}
	}

	private static BufferedImage loadImageFromFile(String src)
	{
		BufferedImage img = null;
		InputStream in = null;
    	try {
    		in = new FileInputStream(src);
    		img = ImageIO.read(in);
    	}
    	catch (IOException ioe) {
                System.out.println(new File(src).getAbsolutePath());
    		ioe.printStackTrace();
    		if (in != null) {
    			try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    		return null;
    	}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -1*img.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		return op.filter(img, null);
	}
	
	private static byte[] getImageDataBytes(BufferedImage image)
	{
        if (image == null) {
			return null;
		}
		int imgw = image.getWidth(null);
		int imgh = image.getHeight(null);
		int[] pixelsARGB = new int[imgw * imgh];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, imgw, imgh, pixelsARGB, 0, imgw);
		try {
			pg.grabPixels();
		}
		catch (Exception e) {System.err.println("oops. " + e.getMessage());}

		int[] pixel;
		byte[] bytes = new byte[pixelsARGB.length * 4];
        for (int i = 0; i < 4 * pixelsARGB.length; i += 4) {
            pixel = convertPixelToRGBAArray(pixelsARGB[i/4]);
            bytes[i  ] = (byte)pixel[0];
            bytes[i+1] = (byte)pixel[1];
            bytes[i+2] = (byte)pixel[2];
            bytes[i+3] = (byte)pixel[3];
        }
        return bytes;
	}

	private static int[] convertPixelToRGBAArray(int pixel)
	{
		int[] pixelArr = new int[4];
		pixelArr[0] = (pixel >> 16) & 0xFF;	//red
		pixelArr[1] = (pixel >> 8 ) & 0xFF;	//green
		pixelArr[2] = (pixel      ) & 0xFF;	//blue
		pixelArr[3] = (pixel >> 24) & 0xFF; //alpha
		return pixelArr;
	}
}
