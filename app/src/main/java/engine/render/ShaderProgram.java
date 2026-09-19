package engine.render;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class ShaderProgram {
    private String vertexSource = "";  // holds the text of the vertex shader file
    private String fragmentSource = ""; // holds the text of the fragment shader file
    private int vertexHandle = 0;  // stores the address to the compiled v shader
    private int fragmentHandle = 0; // stores the address of the compiled f handle
    private int programHandle = 0;

    // NOTE TO FUTURE CALEB --> remember that this throws the IOException to Main
    public ShaderProgram(String vFilePath, String fFilePath) throws IOException
    {
      Path vertPath = Path.of(vFilePath);
      Path fragPath = Path.of(fFilePath);
      
      this.vertexSource = Files.readString(vertPath);
      this.fragmentSource = Files.readString(fragPath);

      vertexHandle = CompileShader(GL_VERTEX_SHADER, vertexSource);
      fragmentHandle = CompileShader(GL_FRAGMENT_SHADER, fragmentSource);

      programHandle = LinkShaders(vertexHandle, fragmentHandle);
    }

    public int CompileShader(int shaderType, String shaderSource) {
      int shaderHandle = glCreateShader(shaderType);
      glShaderSource(shaderHandle, shaderSource);
      glCompileShader(shaderHandle);

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
        int programID = glCreateProgram();
        glAttachShader(programID, vertexShaderHandle);
        glAttachShader(programID, fragmentShaderHandle);
        glLinkProgram(programID);

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
