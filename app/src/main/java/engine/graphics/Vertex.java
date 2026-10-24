package engine.graphics;


import engine.maths.Vector2f;
//import org.joml.Vector3f; // JOML version — uncomment this + comment out the engine.maths.Vector3f import to swap


import engine.maths.Vector3f;


/*
 * NOTE: this is the hand-written (hardcoded) version of the vector math,
 * written first so the math is understood before it is swapped to JOML later.
 */
/*
* VERTEX — represents one corner point of a mesh, bundling together
* everything the GPU needs to know about that point: where it is in
* 3D space, which direction its surface faces (the normal), and where it
* samples from on the bound texture. One Vertex array entry = one row of
* per-vertex data uploaded to the GPU in Mesh.create().
*
* CHANGE FROM THE OLD VERSION: a vertex used to carry a color (r, g, b).
* It now carries a normal instead. Color now comes from the texture, which
* suits models that bring their own textures. The old color code is kept
* below, commented out, for comparison.
*/

public class Vertex {
	private Vector3f position, normal;
	private Vector2f textureCoord;
	
	
	/*
	 * CONSTRUCTOR — stores the three pieces of per-vertex data passed in
	 * (position, normal, and texture coordinate). Nothing is computed or
	 * validated here; this just bundles the values together for later use
	 * when building the GPU buffers.
	 *
	 * OLD signature: Vertex(position, color, textureCoord)
	 * NEW signature: Vertex(position, normal, textureCoord)
	 * The middle argument changed meaning, so any code that creates a
	 * Vertex must now pass a normal there instead of a color.
	 */
	
	public Vertex(Vector3f position, Vector3f normal ,Vector2f textureCoord) {
		this.position = position; 
		this.textureCoord = textureCoord;
		this.normal = normal;
	}
	
	
	/*
	 * GETTER — returns this vertex's 3D position (x, y, z).
	 */
	
	public Vector3f getPosition() {
		return position;
	}
	
	
	/*
	 * OLD GETTER (commented out) — returned this vertex's color (r, g, b),
	 * used for the per-vertex color attribute interpolated across each
	 * triangle. Removed along with the color field and the color buffer
	 * in Mesh.create().
	 */
	
	
//	public Vector3f getColor() {
//		return color;
//	}

	
	/*
	 * GETTER (replaces getColor) — returns this vertex's normal, a vector
	 * that points straight out from the surface at this corner. Normals
	 * are used for lighting, since the angle between the normal and a light
	 * direction decides how bright the surface looks.
	 */
	
	public Vector3f getNormal() {
		return normal;
	}
	
	

	/*
	 * GETTER — returns this vertex's texture coordinate (u, v), telling
	 * the fragment shader where on the bound texture this corner samples from.
	 */
	
	
	public Vector2f getTextureCoord() {
		return textureCoord;
	}
	
}