package engine.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private float [] rawVertices; // holds our raw vertex data from Main.java
    private int vertexTotalCount = 0; // total number of vertices
    private int [] uniqueIndices;
    private int indexCount = 0;
    private int vboHandle = 0; // handle that links to our created VBO
    private int vaoHandle = 0; // same idea as above but with VAO
    private int eboHandle = 0; // same idea but with EBO

    public Mesh(float [] rawData, int [] indexData) {
        this.rawVertices = rawData; // equate these two arrays so we have an internal ref to the array data
        vertexTotalCount = rawData.length / 6; // div by 6 since array should be in position, rgb value order
        this.uniqueIndices = indexData;
        indexCount = indexData.length;

        this.vboHandle = CreateVBO();
        this.vaoHandle = CreateVAO();
    }

    public int CreateVBO() {
        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, this.rawVertices, GL_STATIC_DRAW);
        return VBO;
    }

    public int CreateVAO() {
        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle);
        this.eboHandle = CreateEBO();
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 12);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        return VAO;
    }

    public int CreateEBO() {
        int EBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.uniqueIndices, GL_STATIC_DRAW);
        return EBO;
    }

    public void Draw() {
        glBindVertexArray(this.vaoHandle);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
    }
}
