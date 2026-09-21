package engine.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    private float [] rawVertices; // holds our raw vertex data from Main.java
    private int vertexCount = 0; // total number of vertices
    private int vboHandle = 0; // handle that links to our created VBO
    private int vaoHandle = 0; // same idea as above but with VAO

    public Mesh(float [] rawData) {
        this.rawVertices = rawData; // equate these two arrays so we have an internal ref to the array data
        vertexCount = rawData.length / 6; // div by 6 since array should be in position, rgb value order

        this.vboHandle = CreateVBO();
        this.vaoHandle = CreateVAO();
    }

    public int CreateVBO() {
        // First, we need to create our VBO and bind it with the GL_ARRAY_BUFFER
        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        // Feed the vertex data into the buffer and return the handle
        glBufferData(GL_ARRAY_BUFFER, this.rawVertices, GL_STATIC_DRAW);
        return VBO;
    }

    /*
     * The purpose of the VAO is essentially to act as an instruction manual on
     * how to handle and process our vertex data. With the data coming in with a
     * [position, color, position, color, ...] order, we can attribute which value
     * goes to which shader.
     */

    public int CreateVAO() {
        // First, create the VAO
        int VAO = glGenVertexArrays();
        // This starts our VAO recording -- almost as if pressing record on a camera
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboHandle); // bind our original VBO data
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * 4, 0); // make first pointer handle vertex data
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * 4, 12); // make second pointer handle fragment data

        // enable both pointers to both attributes in GLSL code
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        return VAO;
    }

    public void Draw() {
        // activate the VAO recording we made
        glBindVertexArray(this.vaoHandle);
        // process and tell the GPU to draw the arrays
        glDrawArrays(GL_TRIANGLES, 0, this.vertexCount);
    }
}
