package engine.maths;

/*
 * VECTOR2F — a hand-written 2-component float vector (x, y). In this engine
 * it holds texture coordinates (u, v), and it can also hold 2D positions or
 * directions. This is the hardcoded math version, written first so the math
 * is understood before it is swapped to JOML's Vector2f later.
 *
 * All the math methods are static and return a NEW vector, so the inputs are
 * never changed. JOML works differently: its methods change the vector they
 * are called on, so the swap will need call sites updated, not just imports.
 */
public class Vector2f {
	private float x, y;

	/*
	 * CONSTRUCTOR — stores the two components passed in.
	 */
	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/*
	 * ADD — returns a new vector where each component is the sum of the two
	 * inputs: (x1 + x2, y1 + y2).
	 */
	public static Vector2f add(Vector2f vector1, Vector2f vector2) {
		return new Vector2f(vector1.getX() + vector2.getX(), vector1.getY() + vector2.getY());
	}

	/*
	 * SUBTRACT — returns a new vector where each component is the first input
	 * minus the second: (x1 - x2, y1 - y2). The order of the arguments matters.
	 */
	public static Vector2f subtract(Vector2f vector1, Vector2f vector2) {
		return new Vector2f(vector1.getX() - vector2.getX(), vector1.getY() - vector2.getY());
	}

	/*
	 * MULTIPLY — multiplies the two vectors component by component, giving
	 * (x1 * x2, y1 * y2). This is NOT the dot product (see dot below), and it
	 * is useful for scaling one vector by another.
	 */
	public static Vector2f multiply(Vector2f vector1, Vector2f vector2) {
		return new Vector2f(vector1.getX() * vector2.getX(), vector1.getY() * vector2.getY());
	}

	/*
	 * DIVIDE — divides the first vector by the second, component by component:
	 * (x1 / x2, y1 / y2). Dividing by a zero component gives infinity or NaN.
	 */
	public static Vector2f divide(Vector2f vector1, Vector2f vector2) {
		return new Vector2f(vector1.getX() / vector2.getX(), vector1.getY() / vector2.getY());
	}

	/*
	 * LENGTH — returns the length (magnitude) of the vector, using the
	 * Pythagorean theorem: the square root of (x*x + y*y).
	 */
	public static float length(Vector2f vector) {
		return (float) Math.sqrt(vector.getX() * vector.getX() + vector.getY() * vector.getY());
	}

	/*
	 * NORMALIZE — returns a vector that points the same way as the input but
	 * has a length of exactly 1. It finds the length, then divides each
	 * component by it. This is how a direction is separated from a distance.
	 * A vector of length 0 cannot be normalized, because that divides by zero.
	 */
	public static Vector2f normalize(Vector2f vector) {
		float len = Vector2f.length(vector);
		return Vector2f.divide(vector, new Vector2f(len, len));
	}

	/*
	 * DOT — returns the dot product: (x1 * x2) + (y1 * y2). The result is a
	 * single number that shows how much two vectors point the same way. For
	 * two normalized vectors it is 1 when they point the same direction,
	 * 0 when they are at a right angle, and -1 when they are opposite.
	 */
	public static float dot(Vector2f vector1, Vector2f vector2) {
		return vector1.getX() * vector2.getX() + vector1.getY() * vector2.getY();
	}

	/*
	 * HASH CODE — builds a hash from the bits of x and y, so two vectors with
	 * the same values get the same hash. Required to go along with equals(),
	 * and used when vectors are stored in hash-based collections.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	/*
	 * EQUALS — two vectors are equal when they are the same kind of object and
	 * x and y match exactly. The values are compared by their raw bits, so
	 * vectors that were built through different math can differ by tiny rounding
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
		Vector2f other = (Vector2f) obj;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}

	/*
	 * GETTERS AND SETTERS — read or change the x and y components. For
	 * texture coordinates, x is u and y is v. ModelLoader uses setX and
	 * setY to fill in a texture coordinate after the vector is created.
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
}