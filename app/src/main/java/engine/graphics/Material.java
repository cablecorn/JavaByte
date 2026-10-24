package engine.graphics;

import java.io.IOException;
import java.io.InputStream;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;


import engine.utils.FileUtils;

/*
 * MATERIAL — loads an image file from the classpath and uploads it to
 * the GPU as an OpenGL texture, using the Slick-Util TextureLoader.
 * Holds the resulting texture ID (for binding), the real image's
 * width/height as a 0.0–1.0 ratio relative to the padded texture size
 * Slick allocates on the GPU (see getWidth/getHeight below), and whether
 * the image has an alpha (transparency) channel (see hasAlpha below).
 * Each Mesh owns one Material, and the Renderer binds its texture ID
 * before every draw call.
 */


public class Material {
	private String path;
	private float width, height;
	private int textureID;
	
	/*
	 * CONSTRUCTOR — just stores the classpath path to the image file.
	 * No GPU work happens here; that's deferred to create(), which must
	 * run after an OpenGL context exists.
	 */
	
	public Material(String path) {
		this.path = path;
	}
	
	
	/*
	 * CREATE — loads and decodes the image file, uploads it to the GPU
	 * as a texture, and stores the resulting texture ID and width/height
	 * ratio. The file extension is taken from the path (the text after
	 * the first '.') and passed to TextureLoader so it knows which decoder
	 * to use (jpg, png, etc.). Note that this only works if the path has
	 * exactly one '.', since path.split("[.]")[1] returns the second piece.
	 *
	 * Slick's TextureLoader may pad the texture up to the next
	 * power-of-two size internally; getWidth()/getHeight() return the
	 * fraction of that padded texture the real image actually occupies,
	 * not pixel dimensions — this is what Mesh.create() multiplies raw
	 * UV coordinates by to correct for that padding.
	 *
	 * FILTER COMPARISON (the last argument to getTexture):
	 *   GL_LINEAR  (old line, commented out below) — blends neighboring
	 *              pixels when the texture is scaled, so images look
	 *              smooth but soft or blurry when magnified.
	 *   GL_NEAREST (current line) — picks the single closest pixel with no
	 *              blending, so edges stay sharp and blocky. This is the
	 *              look needed for Minecraft-style pixel textures, which
	 *              would turn blurry under GL_LINEAR.
	 * Both lines load the same image the same way; only the scaling
	 * behavior differs, so switch between them depending on the art style.
	 */
	
	public void create() {
    try (MemoryStack stack = MemoryStack.stackPush();
         InputStream in = Material.class.getResourceAsStream(path)) {

        if (in == null) {
            System.err.println("Can't find texture at " + path);
            return;
        }
        byte[] bytes = in.readAllBytes();
        ByteBuffer file = MemoryUtil.memAlloc(bytes.length).put(bytes).flip();

        IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), c = stack.mallocInt(1);
        ByteBuffer pixels = STBImage.stbi_load_from_memory(file, w, h, c, 4); // force RGBA
        MemoryUtil.memFree(file);
        if (pixels == null) {
            System.err.println("Failed to decode " + path + ": " + STBImage.stbi_failure_reason());
            return;
        }

        textureID = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w.get(0), h.get(0), 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        STBImage.stbi_image_free(pixels);

        width = 1f;   // no power-of-two padding with STB, so his UV scaling becomes a no-op
        height = 1f;
    } catch (IOException e) {
        System.err.println("Can't read texture at " + path);
    }
}
	
	
	/*
	 * HAS ALPHA — returns true if the loaded image has an alpha
	 * (transparency) channel, such as a PNG with see-through areas.
	 * Returns false if the texture failed to load (texture is null) or
	 * the image has no transparency. This lets the code decide how to
	 * draw materials that need transparency, such as glass, which is
	 * needed once models with several textures are rendered.
	 */
	
	//for multi textuire
	//public boolean hasAlpha() {
//		return texture != null && texture.hasAlpha();
//	}
	
	
	/*
	 * DESTROY — releases this material's texture from GPU memory.
	 */
	
	public void destroy() {
		GL13.glDeleteTextures(textureID);
	}


	/*
	 * GETTER — returns the fraction (0.0–1.0) of the padded texture's
	 * width that the real image covers. Not a pixel count.
	 */
	public float getWidth() {
		return width;
	}


	/*
	 * GETTER — returns the fraction (0.0–1.0) of the padded texture's
	 * height that the real image covers. Not a pixel count.
	 */
	public float getHeight() {
		return height;
	}


	/*
	 * GETTER — returns the OpenGL texture ID, which the Renderer passes
	 * to glBindTexture before drawing.
	 */
	public int getTextureID() {
		return textureID;
	}
	
	
}
