package engine.graphics;

import java.io.IOException;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

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
	private Texture texture;
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
		try { (MemoryStack stack = MemoryStack.stackPush();
		       InputStream in = Material.class.getResourceAsStream(path)) {

texture = TextureLoader.getTexture(path.split("[.]")[1], Material.class.getResourceAsStream(path), GL11.GL_NEAREST);  // sharp, pixel-style filtering for Minecraft textures
			
			
			
			
			width = texture.getWidth();
			height = texture.getHeight();
			textureID = texture.getTextureID();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Can't find textire at " + path);
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
	public boolean hasAlpha() {
		return texture != null && texture.hasAlpha();
	}
	
	
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
