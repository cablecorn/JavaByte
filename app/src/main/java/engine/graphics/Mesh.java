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

public class Mesh {
	// stores all the vertices points that make up the mesh like a mesh of triangle postion
	private Vertex[] vertices;
	
	
	private int[] indices; // tells what part of the are of triangle you are position / Stores numbers that tell OpenGL which vertices to use to make triangles
	
	// VAO = remembers how the vertex data is set up so when we render no values and data is lost
	// VBO = stores the vertex data in an OpenGL buffer 
	// IBO = stores the index data in an OpenGL buffer
	private int vao, vbo, ibo;  //vbo = pbo
	
	
	// Save the vertices passed into the constructor
	public Mesh(Vertex[] vertices, int[] indices) {
		this.vertices = vertices;
		this.indices = indices;
		
	}
	
	
	public void create() {
		// this creates a new VAO and get its OpenGL ID so we can track it 
		vao = GL30.glGenVertexArrays();
		// Bind the VAO so it becomes the VAO we are currently working with
		GL30.glBindVertexArray(vao);

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
		
		// Create a new buffer and get its OpenGL ID 
		// This buffer will be used to store the vertex data 
		vbo = GL15.glGenBuffers();
		
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0,3, GL11.GL_FLOAT,false, 0, 0 );
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
		indicesBuffer.put(indices).flip();
		ibo = GL15.glGenBuffers();
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		
		
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


	public int getIbo() {
		return ibo;
	}
	
	
	
	
	
	
	
	
	
	
}
