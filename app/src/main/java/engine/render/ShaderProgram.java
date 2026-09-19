package engine.render;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;

public class ShaderProgram {
    private String vertexSource = "";
    private String fragmentSource = "";
    private int vertexHandle = 0;
    private int fragmentHandle = 0;

    // NOTE TO FUTURE CALEB --> remember that this throws the IOException to Main
    public ShaderProgram(String vFilePath, String fFilePath) throws IOException
    {
      Path vertPath = Path.of(vFilePath);
      Path fragPath = Path.of(fFilePath);
      
      this.vertexSource = Files.readString(vertPath);
      this.fragmentSource = Files.readString(fragPath);
    }

    public int CompileShader(int shaderType, String shaderSource) {
      int shaderHandle = glCreateShader(shaderType);
      glShaderSource(shaderHandle, shaderSource);
      glCompileShader(shaderHandle);

      long shaderCompiled;
      glGetShaderiv(shaderHandle, GL_COMPILE_STATUS, shaderCompiled);
      if (shaderCompiled != GL_TRUE) {
        System.out.println("The shader did not compile!");
      }
    }
}
