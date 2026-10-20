package engine.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * FILE UTILS — small helper for reading text files that are packaged with
 * the program (resources), such as the GLSL shader files. Shader.java calls
 * it to load the vertex and fragment shader source code.
 */
public class FileUtils {
	/*
	 * LOAD AS STRING — reads a whole text file and returns its contents as one
	 * String, with a newline kept at the end of every line.
	 *
	 * The path is a CLASSPATH path, not a normal file path on disk. A leading
	 * "/" means "start at the root of the resources folder", so
	 * "/shaders/mainVertex.glsl" finds resources/shaders/mainVertex.glsl.
	 *
	 * Steps:
	 *  1. Ask FileUtils.class to open the file as a stream. Using this
	 *     class (and not Class.class, which was the earlier bug) makes Java
	 *     search the project's own resources.
	 *  2. Wrap the stream in a reader so it can be read one line at a time.
	 *  3. Read lines until there are none left, adding each to the result.
	 *  4. The try-with-resources block closes the reader automatically.
	 *
	 * If reading fails partway, it prints an error and returns whatever was
	 * read so far. The shader would then fail to compile with a confusing
	 * message, so watch the console for the "Couldn't find the file" line.
	 */
	public static String loadAsString(String path) {
		StringBuilder result = new StringBuilder();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtils.class.getResourceAsStream(path)))) {
			String line = "";
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
		} catch (IOException e) {
			System.err.println("Couldn't find the file at " + path);
		}
		
		return result.toString();
	}
}