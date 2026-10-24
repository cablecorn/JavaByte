package engine.maths;

/*
 * VECTOR3F — a hand-written 3-component float vector (x, y, z). The engine
 * uses it for positions, rotations (in degrees), scales, normals, and colors.
 * This is the hardcoded math version, written first so the math is
 * understood before it is swapped to JOML's Vector3f later.
 *
 * It matches Vector2f, with one extra component. The math methods are static
 * and return a NEW vector, so the inputs are never changed. JOML works
 * differently: its methods change the vector they are called on, and it uses
 * public x, y, z fields instead of getters, so the swap needs call sites
 * updated, not just the import.
 */
public class Vector3f {
	private float x, y, z;

	/*
	 * CONSTRUCTOR — stores the three components passed in.
	 */
	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/*
	 * SET — overwrites all three components of this existing vector at once.
	 * Window uses it to change the background color without making a new vector.
	 */
	public void set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/*
	 * ADD — returns a new vector where each component is the sum of the two
	 * inputs: (x1 + x2, y1 + y2, z1 + z2).
	 */
	public static Vector3f add(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() + vector2.getX(), vector1.getY() + vector2.getY(), vector1.getZ() + vector2.getZ());
	}

	/*
	 * SUBTRACT — returns a new vector where each component is the first input
	 * minus the second. The order of the arguments matters.
	 */
	public static Vector3f subtract(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() - vector2.getX(), vector1.getY() - vector2.getY(), vector1.getZ() - vector2.getZ());
	}

	/*
	 * MULTIPLY — multiplies the two vectors component by component:
	 * (x1 * x2, y1 * y2, z1 * z2). This is NOT the dot product, and it is
	 * useful for scaling one vector by another.
	 */
	public static Vector3f multiply(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() * vector2.getX(), vector1.getY() * vector2.getY(), vector1.getZ() * vector2.getZ());
	}

	/*
	 * DIVIDE — divides the first vector by the second, component by component.
	 * Dividing by a zero component gives infinity or NaN.
	 */
	public static Vector3f divide(Vector3f vector1, Vector3f vector2) {
		return new Vector3f(vector1.getX() / vector2.getX(), vector1.getY() / vector2.getY(), vector1.getZ() / vector2.getZ());
	}

	/*
	 * LENGTH — returns the length (magnitude) of the vector: the square root
	 * of (x*x + y*y + z*z), which is the 3D version of the Pythagorean theorem.
	 */
	public static float length(Vector3f vector) {
		return (float) Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY() + vector.getZ() * vector.getZ());
	}

	/*
	 * NORMALIZE — returns a vector that points the same way as the input but
	 * has a length of exactly 1, by dividing each component by the length.
	 * Rotation axes and surface normals need unit-length vectors. A vector
	 * of length 0 cannot be normalized, because that divides by zero.
	 */
	public static Vector3f normalize(Vector3f vector) {
		float len = Vector3f.length(vector);
		return Vector3f.divide(vector, new Vector3f(len, len, len));
	}

	/*
	 * DOT — returns the dot product: (x1*x2) + (y1*y2) + (z1*z2). The result
	 * is a single number that shows how much two vectors point the same way.
	 * For two normalized vectors it is 1 when they point the same direction,
	 * 0 at a right angle, and -1 when they are opposite. Lighting uses it
	 * to compare a surface normal with the direction of a light.
	 */
	public static float dot(Vector3f vector1, Vector3f vector2) {
		return vector1.getX() * vector2.getX() + vector1.getY() * vector2.getY() + vector1.getZ() * vector2.getZ();
	}

	/*
	 * HASH CODE — builds a hash from the bits of x, y, and z, so two vectors
	 * with the same values get the same hash. Required to go along with
	 * equals().
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		result = prime * result + Float.floatToIntBits(z);
		return result;
	}

	/*
	 * EQUALS — two vectors are equal when they are the same kind of object and
	 * x, y, and z all match exactly. The values are compared by their raw bits,
	 * so vectors that were built through different math can differ by tiny
	 * rounding amounts and then compare as not equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vector3f other = (Vector3f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		if (Float.floatToIntBits(z) != Float.floatToIntBits(other.z))
			return false;
		return true;
	}

	/*
	 * GETTERS AND SETTERS — read or change one component at a time. For a
	 * color, x, y, and z are red, green, and blue.
	 */
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}
}

