package engine.graphics;

/*
 * RENDER — draws a Mesh to the screen using its previously created VAO/IBO.
 *
 * 1. Bind the mesh's VAO to restore its vertex attribute setup.
 * 2. Enable attribute slot 0 (position data) so the GPU reads it.
 * 3. Bind the index buffer (IBO) so the draw call knows which indices to use.
 * 4. glDrawElements — the actual draw call: draws triangles using the index
 *    data, one unsigned int per index, starting at offset 0.
 * 5. Unbind everything afterward so it doesn't affect the next draw call.
 */


import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class Renderer {
	public void renderMesh(Mesh mesh) {
		GL30.glBindVertexArray(mesh.getVao());
		GL30.glEnableVertexAttribArray(0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.getIbo());
		GL11.glDrawElements(GL11.GL_TRIANGLES, mesh.getIndices().length, GL11.GL_UNSIGNED_INT, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL30.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		
	}
}
