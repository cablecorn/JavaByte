package engine.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import engine.graphics.Vertex;
import engine.maths.Vector3f;


public class Mesh {

    private float [] rawVertices; // holds our raw vertex data from Main.java
    private int vertexTotalCount = 0; // total number of vertices
    private int [] uniqueIndices;
    private int indexCount = 0;
    private int vboHandle = 0; // handle that links to our created VBO
    private int vaoHandle = 0; // same idea as above but with VAO
    private int eboHandle = 0; // same idea but with EBO

    public Mesh(Vertex [] rawData, int [] indexData) {
        this.rawVertices = flatten(rawData); // flatten vertex data structs to float array
        vertexTotalCount = rawData.length; // div by 6 since array should be in position, rgb value order
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

    public float [] flatten(Vertex [] arr) {
        float [] temp = new float[arr.length * 6];
        
        for (int i = 0; i < arr.length; i++) {
            Vertex vertex = arr[i];
            int baseIndex = i * 6;

            temp[baseIndex] = vertex.getPosition().getX();
            temp[baseIndex + 1] = vertex.getPosition().getY();
            temp[baseIndex + 2] = vertex.getPosition().getZ();
            temp[baseIndex + 3] = vertex.getColor().getX();
            temp[baseIndex + 4] = vertex.getColor().getY();
            temp[baseIndex + 5] = vertex.getColor().getZ();
        }

        return temp;
    }

    public void Draw() {
        glBindVertexArray(this.vaoHandle);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
    }
}
