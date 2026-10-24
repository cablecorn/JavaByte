package engine.graphics;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import engine.maths.Matrix4f;

import engine.maths.Vector2f;
import engine.maths.Vector3f;

import engine.utils.FileUtils;


/*
 * SHADER — wraps an OpenGL shader program built from one vertex shader and
 * one fragment shader (GLSL files stored in resources/shaders).
 *
 * The vertex shader runs once per vertex and decides where it lands on screen.
 * The fragment shader runs once per pixel and decides that pixel's color.
 * This class loads both files, compiles them, links them into one program,
 * and lets the rest of the engine send values (uniforms) into the running
 * program, such as the model, view, and projection matrices.
 *
 * Usage order: new Shader(...) -> create() (needs an OpenGL context) ->
 * bind() -> setUniform(...) -> draw -> unbind() -> destroy() on shutdown.
 */
public class Shader {
	private String vertexFile, fragmentFile;
	private int vertexID, fragmentID, programID;
	
	/*
	 * CONSTRUCTOR — reads both shader files from the classpath as text and
	 * stores the source code. No GPU work happens here, so it is safe to
	 * call before the window exists. Compiling is done later in create().
	 */
	public Shader(String vertexPath, String fragmentPath) {
		vertexFile = FileUtils.loadAsString(vertexPath);
		fragmentFile = FileUtils.loadAsString(fragmentPath);
	}
	
	/*
	 * CREATE — builds the shader program on the GPU. Must run after the
	 * OpenGL context exists (after the window is created).
	 *
	 * Steps:
	 *  1. Create an empty program and an empty vertex shader object.
	 *  2. Give the vertex shader its source code and compile it. If the
	 *     compile fails, print the GPU's error log and stop.
	 *  3. Do the same for the fragment shader.
	 *  4. Attach both shaders to the program and link them together. The
	 *     link step also checks that the vertex shader's outputs match the
	 *     fragment shader's inputs (the same variable names and types).
	 *  5. Validate the program and print the log if validation fails.
	 *
	 * Each failure prints a message and returns early, which leaves the
	 * program unusable, so watch the console for these messages.
	 */
	public void create() {
		programID = GL20.glCreateProgram();
		vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		
		GL20.glShaderSource(vertexID, vertexFile);
		GL20.glCompileShader(vertexID);
		
		if (GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Vertex Shader: " + GL20.glGetShaderInfoLog(vertexID));
			return;
		}
		
		fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		
		GL20.glShaderSource(fragmentID, fragmentFile);
		GL20.glCompileShader(fragmentID);
		
		if (GL20.glGetShaderi(fragmentID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Fragment Shader: " + GL20.glGetShaderInfoLog(fragmentID));
			return;
		}
		
		GL20.glAttachShader(programID, vertexID);	
		GL20.glAttachShader(programID, fragmentID);
		
		GL20.glLinkProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Program Linking: " + GL20.glGetProgramInfoLog(programID));
			return;
		}
		
		GL20.glValidateProgram(programID);
		if (GL20.glGetProgrami(programID, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
			System.err.println("Program Validation: " + GL20.glGetProgramInfoLog(programID));
			return;
		}
	}
	
	
	/*
	 * GET UNIFORM LOCATION — asks OpenGL for the slot number of a uniform
	 * variable by its name in the GLSL code (for example "model" or "tex").
	 * Every setUniform method below uses this to find where to write its value.
	 * OpenGL returns -1 if the name doesn't exist in the program, such as
	 * after a typo or when the shader never uses that variable.
	 */
	public int getUniformLocation(String name) {
		return GL20.glGetUniformLocation(programID, name);
	}
	
	/*
	 * SET UNIFORM (float) — sends one float to the named uniform.
	 */
	public void setUniform(String name, float value) {
		GL20.glUniform1f(getUniformLocation(name), value);
	}
	
	/*
	 * SET UNIFORM (int) — sends an int to the named uniform. Note that it
	 * currently calls the float version of the OpenGL function (glUniform1f).
	 */
	public void setUniform(String name, int value) {
		GL20.glUniform1f(getUniformLocation(name), value);
	}
	/*
	 * SET UNIFORM (boolean) — sends true or false to the named uniform as
	 * 1.0 or 0.0, since GLSL has no direct way to receive a Java boolean.
	 */
	public void setUniform(String name, boolean value) {
		GL20.glUniform1f(getUniformLocation(name), value ? 1 : 0);
	}
	/*
	 * SET UNIFORM (Vector2f) — sends a 2-component vector (x, y) to the
	 * named uniform, which should be a vec2 in the shader.
	 */
	public void setUniform(String name, Vector2f value) {
		GL20.glUniform2f(getUniformLocation(name), value.getX(), value.getY());
	}
	/*
	 * SET UNIFORM (Vector3f) — sends a 3-component vector (x, y, z) to the
	 * named uniform, which should be a vec3 in the shader.
	 */
	public void setUniform(String name, Vector3f value) {
		GL20.glUniform3f(getUniformLocation(name), value.getX(), value.getY(), value.getZ());
	}
	/*
	 * SET UNIFORM (Matrix4f) — sends a 4x4 matrix to the named uniform, which
	 * should be a mat4 in the shader. OpenGL needs the 16 values in a native
	 * FloatBuffer, so this copies the matrix's float array into a temporary
	 * buffer, uploads it, and then frees the buffer.
	 *
	 * Details:
	 *  - flip() resets the buffer's position to the start so OpenGL reads
	 *    from the first value instead of from the end of what was written.
	 *  - The "true" argument tells OpenGL to transpose the matrix, which is
	 *    needed because the matrix class stores its values row by row while
	 *    OpenGL expects column by column.
	 *  - memFree releases the native memory. Without it, the buffer would
	 *    leak every time this method runs, which is every frame.
	 */
	public void setUniform(String name, Matrix4f value) {
	    FloatBuffer matrix = MemoryUtil.memAllocFloat(Matrix4f.SIZE * Matrix4f.SIZE);
	    matrix.put(value.getAll()).flip();
	    GL20.glUniformMatrix4fv(getUniformLocation(name), true, matrix);
	    MemoryUtil.memFree(matrix);
	}
	
	/*
	 * BIND — makes this program the active one. Every draw call after this
	 * uses this program's shaders until unbind() or another bind() happens.
	 * Uniforms can only be set on the program that is currently bound.
	 */
	public void bind() {
		GL20.glUseProgram(programID);
	}
	
	/*
	 * UNBIND — deactivates the shader program by binding program 0
	 * ("no program"), so later draws don't accidentally use it.
	 */
	public void unbind() {
		GL20.glUseProgram(0);
	}
	
	/*
	 * DESTROY — frees the shader's GPU resources. It detaches both shaders
	 * from the program, deletes the two shader objects, and then deletes the
	 * program itself. Call this once when shutting the game down.
	 */
	public void destroy() {
		GL20.glDetachShader(programID, vertexID);
		GL20.glDetachShader(programID, fragmentID);
		GL20.glDeleteShader(vertexID);
		GL20.glDeleteShader(fragmentID);
		GL20.glDeleteProgram(programID);
	}
}
