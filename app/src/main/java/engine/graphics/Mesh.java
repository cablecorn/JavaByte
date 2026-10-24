package engine.graphics;


/*
 * MESH CREATION — uploads vertex/index data from Java (CPU) memory to the GPU.
 *
 * VAO (Vertex Array Object): saves the "recipe" of how vertex data is laid out,
 *      so we don't have to re-specify attribute pointers every frame.
 * VBO (Vertex Buffer Object): stores raw per-vertex data (positions) on the GPU.
 * IBO (Index Buffer Object): stores indices telling OpenGL which vertices form
 *      each triangle, so shared corners aren't duplicated in the VBO.
 *
 * Steps for each buffer: generate an ID -> bind it -> upload data -> unbind.
 *
 * ACTIVE attribute buffers right now: position (location 0) and texture
 * coordinates (location 2). The color buffer (location 1) is currently
 * commented out in create(), so per-vertex colors are no longer sent to the GPU.
 *
 * glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0):
 *   0     = attribute location (must match "layout(location = 0)" in the shader)
 *   3     = components per vertex for this attribute (x, y, z)
 *   FLOAT = data type of each component
 *   false = don't normalize (irrelevant for floats)
 *   0     = stride (0 = tightly packed, no gaps between vertices)
 *   0     = offset (0 = data starts at the beginning of the buffer)
 */


import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import java.nio.IntBuffer;




/*
 * MESH — holds a shape's geometry (vertices + indices) and its texture
 * (Material), and is responsible for uploading all of that data to the
 * GPU so the Renderer can draw it each frame. Also carries a "glass" flag
 * so the renderer can treat transparent meshes (such as windows in the
 * loaded Minecraft model) differently from solid ones.
 */


public class Mesh {
	// stores all the vertices points that make up the mesh like a mesh of triangle postion
	private Vertex[] vertices;
	
	
	private int[] indices; // tells what part of the are of triangle you are position / Stores numbers that tell OpenGL which vertices to use to make triangles
	private Material material;
	// VAO = remembers how the vertex data is set up so when we render no values and data is lost
	// VBO = stores the vertex data in an OpenGL buffer 
	// IBO = stores the index data in an OpenGL buffer
	private int vao, vbo, ibo, cbo, tbo;  //vbo = pbo
	
	
	
	/*
	 * GLASS FLAG (multi-texture support) — marks this mesh as glass/transparent.
	 * Set by the model loader and read when drawing, so glass can be drawn
	 * with transparency instead of as an opaque surface. This is a workaround
	 * for the Minecraft model's glass not rendering correctly, and the whole
	 * block can be deleted if a better fix replaces it.
	 */
	/// MULTI TEXTURE CODE BELOW
	/// 	//ISSUE FIXING GLASS IN MINCEFEER LOADER CAN DELETE IF NEEDED 
	private boolean glass;
	public boolean isGlass() { return glass; }
	public void setGlass(boolean glass) { this.glass = glass; }
	
	
	
	
	/*
	 * CONSTRUCTOR — stores the raw CPU-side mesh data (vertices, indices,
	 * material) passed in. Nothing is sent to the GPU yet — that only
	 * happens once create() is called, after an OpenGL context exists.
	 */
	// Save the vertices passed into the constructor
	public Mesh(Vertex[] vertices, int[] indices, Material material) {
		this.vertices = vertices;
		this.indices = indices;
		this.material = material;
		
	}
	
	
	/*
	 * CREATE — the main setup method: loads the texture, then builds and
	 * uploads the active per-vertex attribute buffers (position and texture
	 * coordinate) plus the index buffer, all tied together under one VAO
	 * so they can be re-bound as a single unit every frame. The color
	 * buffer step is commented out below, so only two attribute buffers
	 * are created at the moment.
	 */
	public void create() {
		material.create();
		
		
		
		
		// this creates a new VAO and get its OpenGL ID so we can track it 
		vao = GL30.glGenVertexArrays();
		// Bind the VAO so it becomes the VAO we are currently working with
		GL30.glBindVertexArray(vao);

		/*
		 * POSITION ATTRIBUTE (location 0) — builds a flat float array of
		 * every vertex's x/y/z, then uploads it as the mesh's position buffer.
		 */
		//the 8 lines below before the // tells converts everthing so that it can be fitted into the buffer
		FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
		
		float[] positionData = new float[vertices.length * 3];
		for (int i = 0; i < vertices.length; i++) {
			positionData[i * 3] = vertices[i].getPosition().getX();
			positionData[i * 3 + 1] = vertices[i].getPosition().getY();
			positionData[i * 3 + 2] = vertices[i].getPosition().getZ();
			
			
			// JOML version (public fields, not getters) — uncomment once swapped:
			// positionData[i * 3] = vertices[i].getPosition().x;
			// positionData[i * 3 + 1] = vertices[i].getPosition().y;
			// positionData[i * 3 + 2] = vertices[i].getPosition().z;
			
			
		}
		positionBuffer.put(positionData).flip();

		vbo = storeData(positionBuffer,  0, 3);
		
		
		/*
		 * COLOR ATTRIBUTE (location 1) — DISABLED (commented out below).
		 *
		 * OLD behavior (this code): built a flat float array of every
		 * vertex's r/g/b and uploaded it as its own buffer, so each vertex
		 * carried its own color to the shader.
		 * NEW behavior (code removed): no color buffer is created or uploaded,
		 * so the cbo field is never assigned and stays 0. Color now comes
		 * entirely from the texture, which suits models loaded with their
		 * own textures. To bring vertex colors back, uncomment this whole
		 * block. The shader's color input and the Renderer's attribute
		 * enabling must match whichever version is active.
		 */
		//the 8 lines below before the // tells converts everthing so that it can be fitted into the buffer
//		FloatBuffer colorBuffer = MemoryUtil.memAllocFloat(vertices.length * 3);
//		
//		float[] colornData = new float[vertices.length * 3];
//		for (int i = 0; i < vertices.length; i++) {
//			colornData[i * 3]     = vertices[i].getColor().getX();
//			colornData[i * 3 + 1] = vertices[i].getColor().getY();
//			colornData[i * 3 + 2] = vertices[i].getColor().getZ();
//			
//			// JOML version (public fields, not getters) — uncomment once swapped:
//			// positionData[i * 3] = vertices[i].getPosition().x;
//			// positionData[i * 3 + 1] = vertices[i].getPosition().y;
//			// positionData[i * 3 + 2] = vertices[i].getPosition().z;
//			
//			
//		}
//		colorBuffer.put(colornData).flip();
//
//		cbo = storeData(colorBuffer,  1, 3);
		
		
		
		/*
		 * TEXTURE COORDINATE ATTRIBUTE (location 2) — builds a flat float
		 * array of every vertex's u/v. Each raw UV is scaled by the
		 * material's actual width/height ratio to correct for texture
		 * padding added when the image was loaded (see Material.java) —
		 * without this, UVs would map across the padded texture instead
		 * of just the real image region.
		 */
		
		
		FloatBuffer tectureBuffer = MemoryUtil.memAllocFloat(vertices.length * 2);
		
		float[] tectureData = new float[vertices.length * 2];
		for (int i = 0; i < vertices.length; i++) {
			tectureData[i * 2]     = vertices[i].getTextureCoord().getX() * material.getWidth();
			tectureData[i * 2 + 1] = vertices[i].getTextureCoord().getY()* material.getHeight();
			
			
			// JOML version (public fields, not getters) — uncomment once swapped:
			// positionData[i * 3] = vertices[i].getPosition().x;
			// positionData[i * 3 + 1] = vertices[i].getPosition().y;
			// positionData[i * 3 + 2] = vertices[i].getPosition().z;
			
			
		}
		tectureBuffer.put(tectureData).flip();

		tbo = storeData(tectureBuffer,  2, 2);
		
		
		
		
		/*
		 * INDEX BUFFER (IBO) — uploads the triangle index list separately
		 * from storeData() since it uses GL_ELEMENT_ARRAY_BUFFER, a
		 * different buffer target than the per-vertex attribute buffers above.
		 * The unbind line after the upload is commented out: it is safe to
		 * leave the index buffer bound here because the VAO is still bound,
		 * and unbinding it while a VAO is active would detach it from the VAO.
		 */
		IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
		indicesBuffer.put(indices).flip();
		ibo = GL15.glGenBuffers();
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
// might need to remove 		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		
	}
	
	
	

	/*
	 * STORE DATA — shared helper for uploading any per-vertex attribute
	 * buffer (position, color, or texture coordinate). Generates a buffer
	 * ID, binds it, uploads the float data, and configures the vertex
	 * attribute pointer at the given location/size so the shader can
	 * read it. Returns the buffer ID so it can be tracked for cleanup.
	 */
	
	private int storeData(FloatBuffer buffer, int index, int size ) {
		
		// Create a new buffer and get its OpenGL ID 
		// This buffer will be used to store the vertex data 
		int bufferID = GL15.glGenBuffers();
		
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(index , size, GL11.GL_FLOAT,false, 0, 0 );
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		return bufferID;
	}

	
	
	/*
	 * DESTROY — releases all GPU resources owned by this mesh (every
	 * buffer, the VAO, and the material's texture) to avoid leaking
	 * GPU memory when the mesh is no longer needed. Note that cbo is
	 * deleted here even though the color buffer is currently disabled;
	 * deleting buffer ID 0 is ignored by OpenGL, so this is harmless.
	 */
	
	public void destroy() {
		GL15.glDeleteBuffers(vbo);
		GL15.glDeleteBuffers(cbo);
		GL15.glDeleteBuffers(ibo);
		GL15.glDeleteBuffers(tbo);
		
		GL30.glDeleteVertexArrays(vao);
		
		material.destroy();
	}

	
	
	
	

	public Vertex[] getVertices() {
		return vertices;
	}


	public int[] getIndices() {
		return indices;
	}


	public int getVao() {
		return vao;
	}


	public int getVbo() {
		return vbo;
	}

	
	public int getCbo() {
		return cbo;
	}

	
	public int getTBO() {
		return tbo;
	}
	
	public int getIbo() {
		return ibo;
	}
	
	

	public Material getMaterial() {
		return material;
	}
	
	
	

	
	
	
	
}