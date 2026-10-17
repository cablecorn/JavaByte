package engine.graphics;

/*
 * RENDER — draws game objects to the screen using each mesh's previously
 * created VAO/IBO, the object's transform, and the camera's view.
 *
 * 1. Bind the shader and send the model, view, and projection matrices to it.
 * 2. For each mesh: bind its VAO, enable its attribute slots, bind its
 *    index buffer (IBO), and bind its texture.
 * 3. glDrawElements — the actual draw call: draws triangles using the index
 *    data, one unsigned int per index, starting at offset 0.
 * 4. Unbind everything afterward so it doesn't affect the next draw call.
 */

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import engine.io.Window;
import engine.maths.Matrix4f;
import engine.object.Camera;
import engine.object.GameObject;






// GL14 is needed for the glass blending in the multi-texture renderer
// (glBlendColor and the constant-alpha blend factors live in GL14).
import org.lwjgl.opengl.GL14;






/*
 * RENDERER — holds references to the window (for its projection matrix)
 * and the shader program, and knows how to draw any GameObject handed to
 * it, following the correct OpenGL bind -> draw -> unbind sequence each frame.
 */


public class Renderer {
	private Shader shader;
	private Window window;
	
	
	/*
	 * CONSTRUCTOR — stores the window and the shader this renderer will use
	 * for every object it draws. The shader must be a fully-created Shader
	 * before being passed in here, since this reference is reused on every
	 * renderMesh() call. The window is new in this version: it supplies the
	 * projection matrix, which depends on the window's size.
	 */
	
	
	public Renderer(Window window, Shader shader) {
		this.shader = shader;
		this.window = window;
	}
	
	
	
	
	/*
	 * RENDER MESH — draws one GameObject for the current frame.
	 *
	 * Order matters: the texture and shader are bound BEFORE the draw
	 * call, since glDrawElements uses whatever texture/shader state is
	 * currently active at the moment it's called — binding them after
	 * the draw would have no effect on what was just drawn.
	 *
	 * Two versions of this method exist below:
	 *   OLD (commented out) — single-texture version. It draws one mesh
	 *     (object.getMesh()), and the shader, matrices, and VAO setup are
	 *     all handled inside this one method.
	 *   NEW (active) — multi-texture version. A GameObject can now hold many
	 *     meshes (object.getMeshes()), each with its own texture, so the
	 *     per-mesh work moved into drawMesh() and this method loops over
	 *     the meshes. Glass meshes are drawn in a second pass.
	 */
	
	
		/*
		 * OLD VERSION — SINGLE TEXTURE RENDERER (kept for comparison).
		 *
		 * Differences from the new version:
		 *  - Draws exactly one mesh per object, so models with several
		 *    textures cannot be drawn with it.
		 *  - Binds the VAO and texture first, then the shader, then sends
		 *    the matrices. The new version binds the shader and sends the
		 *    matrices once, then loops over the meshes.
		 *  - Has no glass handling, so every mesh is drawn as opaque.
		 *  - The closing brace of the class is also commented out here,
		 *    because this block was the last method in the old file.
		 */

//	public void renderMesh(GameObject object, Camera camera) {
//		GL30.glBindVertexArray(object.getMesh().getVao());
//		GL30.glEnableVertexAttribArray(0);
//		GL30.glEnableVertexAttribArray(1);
//		GL30.glEnableVertexAttribArray(2);
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, object.getMesh().getIbo());
//		GL13.glActiveTexture(GL13.GL_TEXTURE0);
//		GL13.glBindTexture(GL11.GL_TEXTURE_2D, object.getMesh().getMaterial().getTextureID());
//		shader.bind();
//		shader.setUniform("model", Matrix4f.transform(object.getPosition(), object.getRotation(), object.getScale()));
//		shader.setUniform("view", Matrix4f.view(camera.getPosition(), camera.getRotation()));
//		shader.setUniform("projection", window.getProjectionMatrix());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, object.getMesh().getIndices().length, GL11.GL_UNSIGNED_INT, 0);
//		shader.unbind();
//		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
//		GL30.glDisableVertexAttribArray(0);
//		GL30.glDisableVertexAttribArray(1);
//		GL30.glDisableVertexAttribArray(2);
//		GL30.glBindVertexArray(0);
//	}
//}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * NEW VERSION — MULTI TEXTURE RENDERER (active).
	 *
	 * Steps:
	 *  1. Bind the shader once and send the three matrices to it:
	 *       model      = the object's position/rotation/scale in the world
	 *       view       = the camera's position and rotation
	 *       projection = the window's perspective projection
	 *     These are the same for every mesh in the object, so they are sent
	 *     once instead of once per mesh.
	 *  2. Pass 1 — draw every solid (non-glass) mesh with drawMesh().
	 *  3. Pass 2 — draw the glass meshes last. Blending is set to a constant
	 *     alpha (the glBlendColor value, currently 0.2, i.e. 20% opaque),
	 *     and depth writing is switched off so see-through glass doesn't
	 *     hide the things behind it. Glass is drawn after the solids so
	 *     there is already something behind it to blend with.
	 *  4. Restore depth writing and the normal blend function, so later
	 *     draws aren't affected, then unbind the shader.
	 */
	public void renderMesh(GameObject object, Camera camera) {
		shader.bind();
		shader.setUniform("model", Matrix4f.transform(object.getPosition(), object.getRotation(), object.getScale()));
		shader.setUniform("view", Matrix4f.view(camera.getPosition(), camera.getRotation()));
		shader.setUniform("projection", window.getProjectionMatrix());

		for (Mesh mesh : object.getMeshes()) {
			if (!mesh.isGlass()) drawMesh(mesh);
		}

		// Glass last: constant 35% opacity, and don't write depth so it doesn't hide things behind it
		// (Note: the code below uses 0.2f, which is 20% opacity, so this old comment is out of date.)
		GL14.glBlendColor(0, 0, 0, 0.2f);
		GL11.glBlendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
		GL11.glDepthMask(false);
		for (Mesh mesh : object.getMeshes()) {
			if (mesh.isGlass()) drawMesh(mesh);
		}
		GL11.glDepthMask(true);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		shader.unbind();
	}

	/*
	 * DRAW MESH — draws a single mesh. This is the per-mesh part that the
	 * old single-texture version did inside renderMesh().
	 *
	 * Steps:
	 *  1. Bind the mesh's VAO (restores its vertex attribute pointers).
	 *  2. Enable attribute slots 0, 1, and 2 (position, color, and
	 *     texture coordinate) so the GPU reads them.
	 *  3. Bind the index buffer (IBO) so the draw call knows which
	 *     indices to use to form triangles.
	 *  4. Activate texture unit 0 and bind this mesh's own texture to it.
	 *  5. glDrawElements — the actual draw call.
	 *  6. Unbind everything afterward so it doesn't leak into the next
	 *     mesh (index buffer, attribute slots, VAO).
	 * The shader is not bound or unbound here, because renderMesh()
	 * already bound it for the whole object.
	 */
	private void drawMesh(Mesh mesh) {
		GL30.glBindVertexArray(mesh.getVao());
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		GL30.glEnableVertexAttribArray(2);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIbo());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL13.glBindTexture(GL11.GL_TEXTURE_2D, mesh.getMaterial().getTextureID());
		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL30.glDisableVertexAttribArray(0);
		GL30.glDisableVertexAttribArray(1);
		GL30.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}
}