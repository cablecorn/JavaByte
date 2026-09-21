package engine.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class ShaderProgram {
    private String vertexSource = "";  // holds the text of the vertex shader file
    private String fragmentSource = ""; // holds the text of the fragment shader file
    private int vertexHandle = 0;  // stores the openGL int reference to the compiled v shader
    private int fragmentHandle = 0; // stores the openGL int reference of the compiled f handle
    private int programHandle = 0; // stores address to program handle

    // This function throws IOException if file cannot be found
    public ShaderProgram(String vFilePath, String fFilePath) throws IOException
    {
      // convert the filepath strings to actual paths then let Java Files read the file contents as strings
      Path vertPath = Path.of(vFilePath);
      Path fragPath = Path.of(fFilePath);
      this.vertexSource = Files.readString(vertPath);
      this.fragmentSource = Files.readString(fragPath);

      vertexHandle = CompileShader(GL_VERTEX_SHADER, vertexSource);
      fragmentHandle = CompileShader(GL_FRAGMENT_SHADER, fragmentSource);

      programHandle = LinkShaders(vertexHandle, fragmentHandle);
    }

    public int CompileShader(int shaderType, String shaderSource) {
      int shaderHandle = glCreateShader(shaderType); // create a handle for the compiled shader
      glShaderSource(shaderHandle, shaderSource); // feed the source to the handle
      glCompileShader(shaderHandle); // compile it!

      // This chunk of code is simply to check if the shaders compiled correctly
      int [] shaderCompiled = new int[1];
      glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, shaderCompiled);
      if (shaderCompiled[0] != GL_TRUE) {
          if (shaderType == GL_VERTEX_SHADER) {
              System.out.println("Vertex shader failed to compile!");
              return -1;
          } else {
              System.out.println("Fragment shader failed to compile!");
              return -1;
          }
     }
      else
          return shaderHandle;
    }

    public int LinkShaders(int vertexShaderHandle, int fragmentShaderHandle) {
        // Create handle for the program to be referenced to
        int programID = glCreateProgram();
        // Attach the shaders that we compiled to the program and link them
        glAttachShader(programID, vertexShaderHandle);
        glAttachShader(programID, fragmentShaderHandle);
        glLinkProgram(programID);

        // This chunk of code is simply also to check if the program was linked successfully.
        // Either way, we delete the shaders since they are no longer needed and return the handle.
        int [] programLinked = new int[1];
        glGetProgramiv(programID, GL_LINK_STATUS, programLinked);
        if (programLinked[0] != GL_TRUE) {
            System.out.println("Program not linked properly!");
            glDeleteShader(vertexShaderHandle);
            glDeleteShader(fragmentShaderHandle);
            return -1;
        }
        else {
            glDeleteShader(vertexShaderHandle);
            glDeleteShader(fragmentShaderHandle);
            return programID;
        }
    }

    public void Use() {
        glUseProgram(this.programHandle);
    }
}
