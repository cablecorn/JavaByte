package engine.maths;

import java.util.Arrays;

/*
 * MATRIX4F — a hand-written 4x4 matrix of floats, plus the factory methods
 * the engine needs to move, turn, and project things in 3D. This is the
 * hardcoded math version, written first so the math is understood before
 * it is swapped to JOML's Matrix4f later.
 *
 * LAYOUT: the 16 values live in one float array, stored row by row.
 * get(x, y) and set(x, y, value) take x = the COLUMN and y = the ROW, and
 * they read element number (y * 4 + x) of the array. So set(3, 0, value)
 * writes row 0, column 3, which is the last column of the top row. That is
 * where a translation (a move) is stored.
 *
 * OPENGL: Shader.setUniform(String, Matrix4f) uploads this array with the
 * transpose flag set to true. That is the correct choice, because this array
 * is stored row by row and OpenGL expects column by column, so it must be
 * flipped on the way in.
 *
 * HOW THE MATRICES ARE USED: the Renderer builds three of them every frame
 * and sends them to the shader:
 *   model      = transform(...)   places one object in the world
 *   view       = view(...)        places the camera
 *   projection = projection(...)  flattens the 3D scene onto the screen
 */
public class Matrix4f {
	public static final int SIZE = 4; // a matrix is SIZE x SIZE, so 4 x 4 = 16 values
	private float[] elements = new float[SIZE * SIZE]; // starts as all zeros
	
	/*
	 * IDENTITY — returns the identity matrix: 1s down the diagonal and 0s
	 * everywhere else. It changes nothing when multiplied with another matrix
	 * (like multiplying a number by 1), so every other factory method below
	 * starts from it and then fills in only the entries it needs.
	 *
	 * The double loop that sets every entry to 0 first is not strictly needed,
	 * because a new float array already starts as zeros, but it does no harm.
	 */
	public static Matrix4f identity() {
		Matrix4f result = new Matrix4f();
		
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				result.set(i, j, 0);
			}
		}
		
		result.set(0, 0, 1);
		result.set(1, 1, 1);
		result.set(2, 2, 1);
		result.set(3, 3, 1);
		
		return result;
	}
	
	/*
	 * TRANSLATE — returns a matrix that moves things by the given amount along
	 * x, y, and z. The three offsets go in the last column (rows 0, 1, 2).
	 */
	public static Matrix4f translate(Vector3f translate) {
		Matrix4f result = Matrix4f.identity();
		
		result.set(3, 0, translate.getX());
		result.set(3, 1, translate.getY());
		result.set(3, 2, translate.getZ());
		
		return result;
	}
	
	/*
	 * ROTATE — returns a matrix that turns things by "angle" DEGREES around
	 * the given axis. It converts the angle to radians, computes the cosine
	 * and sine, and fills the top-left 3x3 block with the standard
	 * axis-angle rotation formula (C is "1 - cos", used to shorten it).
	 *
	 * The axis should be a unit vector (length 1). The calls in this file
	 * pass (1,0,0), (0,1,0), and (0,0,1), which already are.
	 */
	public static Matrix4f rotate(float angle, Vector3f axis) {
		Matrix4f result = Matrix4f.identity();
		
		float cos = (float) Math.cos(Math.toRadians(angle));
		float sin = (float) Math.sin(Math.toRadians(angle));
		float C = 1 - cos;
		
		result.set(0, 0, cos + axis.getX() * axis.getX() * C);
		result.set(0, 1, axis.getX() * axis.getY() * C - axis.getZ() * sin);
		result.set(0, 2, axis.getX() * axis.getZ() * C + axis.getY() * sin);
		result.set(1, 0, axis.getY() * axis.getX() * C + axis.getZ() * sin);
		result.set(1, 1, cos + axis.getY() * axis.getY() * C);
		result.set(1, 2, axis.getY() * axis.getZ() * C - axis.getX() * sin);
		result.set(2, 0, axis.getZ() * axis.getX() * C - axis.getY() * sin);
		result.set(2, 1, axis.getZ() * axis.getY() * C + axis.getX() * sin);
		result.set(2, 2, cos + axis.getZ() * axis.getZ() * C);
		
		return result;
	}
	
	/*
	 * SCALE — returns a matrix that stretches things by the given factor along
	 * x, y, and z. The factors go on the diagonal. (1, 1, 1) changes nothing,
	 * and (2, 2, 2) makes an object twice as big.
	 */
	public static Matrix4f scale(Vector3f scalar) {
		Matrix4f result = Matrix4f.identity();
		
		result.set(0, 0, scalar.getX());
		result.set(1, 1, scalar.getY());
		result.set(2, 2, scalar.getZ());
		
		return result;
	}
	
	/*
	 * TRANSFORM — builds the MODEL matrix for one object, combining its
	 * position, rotation (in degrees, around x, y, and z), and scale into
	 * one matrix.
	 *
	 * Steps: make a translation, three single-axis rotations, and a scale
	 * matrix. Multiply the three rotations together into one rotation matrix,
	 * then multiply translation, rotation, and scale together into the result.
	 * The order of the multiplications matters, because matrix multiplication
	 * gives different results when the order changes.
	 */
	public static Matrix4f transform(Vector3f position, Vector3f rotation, Vector3f scale) {
		Matrix4f result = Matrix4f.identity();
		
		Matrix4f translationMatrix = Matrix4f.translate(position);
		Matrix4f rotXMatrix = Matrix4f.rotate(rotation.getX(), new Vector3f(1, 0, 0));
		Matrix4f rotYMatrix = Matrix4f.rotate(rotation.getY(), new Vector3f(0, 1, 0));
		Matrix4f rotZMatrix = Matrix4f.rotate(rotation.getZ(), new Vector3f(0, 0, 1));
		Matrix4f scaleMatrix = Matrix4f.scale(scale);
		
		Matrix4f rotationMatrix = Matrix4f.multiply(rotXMatrix, Matrix4f.multiply(rotYMatrix, rotZMatrix));
		
		result = Matrix4f.multiply(translationMatrix, Matrix4f.multiply(rotationMatrix, scaleMatrix));
		
		return result;
	}
	
	/*
	 * PROJECTION — builds the PERSPECTIVE projection matrix, which makes
	 * things that are farther away look smaller, like a real camera.
	 *
	 * Parameters:
	 *   fov    = field of view in degrees (how wide the camera sees)
	 *   aspect = window width divided by window height
	 *   near   = the closest distance that is drawn
	 *   far    = the farthest distance that is drawn
	 *
	 * The entries set here are the standard OpenGL perspective values. The
	 * -1 in row 3 makes the GPU divide by depth, which is what shrinks
	 * distant objects, and the last diagonal entry is set to 0 instead of 1.
	 */
	public static Matrix4f projection(float fov, float aspect, float near, float far) {
		Matrix4f result = Matrix4f.identity();
		
		float tanFOV = (float) Math.tan(Math.toRadians(fov / 2));
		float range = far - near;
		
		result.set(0, 0, 1.0f / (aspect * tanFOV));
		result.set(1, 1, 1.0f / tanFOV);
		result.set(2, 2, -((far + near) / range));
		result.set(2, 3, -1.0f);
		result.set(3, 2, -((2 * far * near) / range));
		result.set(3, 3, 0.0f);
		
		return result;
	}
	
	/*
	 * VIEW — builds the VIEW matrix, which represents the camera.
	 *
	 * A camera is not really moved in 3D graphics. Instead, the whole world
	 * is moved the opposite way. That is why the position is negated here
	 * (a camera at x = 5 moves the world 5 units toward negative x). The
	 * rotations are then combined (in z, y, x order) and multiplied with the
	 * translation to give the final matrix.
	 */
	public static Matrix4f view(Vector3f position, Vector3f rotation) {
		Matrix4f result = Matrix4f.identity();
		
		Vector3f negative = new Vector3f(-position.getX(), -position.getY(), -position.getZ());
		Matrix4f translationMatrix = Matrix4f.translate(negative);
		Matrix4f rotXMatrix = Matrix4f.rotate(rotation.getX(), new Vector3f(1, 0, 0));
		Matrix4f rotYMatrix = Matrix4f.rotate(rotation.getY(), new Vector3f(0, 1, 0));
		Matrix4f rotZMatrix = Matrix4f.rotate(rotation.getZ(), new Vector3f(0, 0, 1));
		
		Matrix4f rotationMatrix = Matrix4f.multiply(rotZMatrix, Matrix4f.multiply(rotYMatrix, rotXMatrix));
		
		result = Matrix4f.multiply(translationMatrix, rotationMatrix);
		
		return result;
	}
	
	/*
	 * MULTIPLY — multiplies two 4x4 matrices and returns a new matrix, leaving
	 * both inputs unchanged. For every output entry (column i, row j), it
	 * adds up four products:
	 *   matrix(column i, row k) * other(column k, row j), for k = 0, 1, 2, 3.
	 *
	 * Matrix multiplication is NOT commutative: multiply(a, b) and
	 * multiply(b, a) generally give different matrices. The order of the
	 * arguments decides which transformation is applied first, so this is
	 * the first place to look when an object ends up in the wrong spot or
	 * turned the wrong way.
	 */
	public static Matrix4f multiply(Matrix4f matrix, Matrix4f other) {
		Matrix4f result = Matrix4f.identity();
		
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				result.set(i, j, matrix.get(i, 0) * other.get(0, j) +
								 matrix.get(i, 1) * other.get(1, j) +
								 matrix.get(i, 2) * other.get(2, j) +
								 matrix.get(i, 3) * other.get(3, j));
			}
		}
		
		return result;
	}
	
	/*
	 * HASH CODE — builds a hash from the 16 values so two matrices with the
	 * same values get the same hash. Required to go along with equals().
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(elements);
		return result;
	}

	/*
	 * EQUALS — two matrices are equal when they are the same kind of object
	 * and all 16 values match exactly. Because the values are floats, matrices
	 * that were built through different math can differ by tiny rounding
	 * amounts and then compare as not equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Matrix4f other = (Matrix4f) obj;
		if (!Arrays.equals(elements, other.elements))
			return false;
		return true;
	}

	/*
	 * GET — returns the value in column x, row y.
	 */
	public float get(int x, int y) {
		return elements[y * SIZE + x];
	}
	
	/*
	 * SET — writes a value into column x, row y.
	 */
	public void set(int x, int y, float value) {
		elements[y * SIZE + x] = value;
	}
	
	/*
	 * GET ALL — returns the raw 16-value array (not a copy), which is what
	 * Shader.setUniform copies into a buffer to send to OpenGL. Changing the
	 * returned array changes the matrix.
	 */
	public float[] getAll() {
		return elements;
	}
}