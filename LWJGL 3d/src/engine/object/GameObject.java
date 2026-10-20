/*
 * GAME OBJECT — a thing that exists in the game world. It holds a position,
 * rotation, and scale (where and how it appears), plus the mesh or meshes
 * that give it a shape. The Renderer reads these every frame and combines
 * the position, rotation, and scale into the model matrix
 * (see Matrix4f.transform).
 *
 * Two versions of the class exist in this file:
 *   OLD (commented out, first) — holds ONE Mesh.
 *   NEW (active, second)       — holds an ARRAY of Meshes.
 *
 * Differences between them:
 *  - Field and constructor: the old one stores "Mesh mesh" and the new one
 *    stores "Mesh[] meshes". Any code that creates a GameObject must now
 *    pass an array, which is what ModelLoader.loadModel() returns.
 *  - Getter: getMesh() became getMeshes(), and it returns the whole array.
 *    The single-texture Renderer (also commented out) calls getMesh(), and
 *    the multi-texture Renderer loops over getMeshes().
 *  - Why the change: a model such as the Minecraft one is made of many
 *    meshes, each with its own texture, so one object has to be able to
 *    hold all of them.
 *  - update(): the old version moved the object forward on every call as a
 *    test (position z decreased by 0.05 each frame, and an older
 *    sine-wave animation is commented out inside it). The new version does
 *    nothing, so the model stays where it was placed.
 *  - The old version's javadoc mentions a "temp" animation counter, but the
 *    field itself is not declared, so only the commented-out sine-wave code
 *    inside update() would have needed it.
 */









//package engine.object;
//
//import engine.graphics.Mesh;
//import engine.maths.Vector3f;
//
///**
// * Something that exists in the game world: a mesh (the shape to draw)
// * plus a position, rotation, and scale describing where and how it appears.
// * The renderer combines position/rotation/scale into a transformation matrix.
// */
//public class GameObject {
//	/** World position (x, y, z). */
//	private Vector3f position;
//	/** Euler rotation in degrees around each axis. */
//	private Vector3f rotation;
//	/** Scale factor per axis (1 = original size). */
//	private Vector3f scale;
//
//	/** The geometry (vertices/indices) this object draws. */
//	private Mesh mesh;
//
//	/** Temporary animation counter used by update() for testing. */
//
//
//	/**
//	 * Creates a game object.
//	 *
//	 * @param position starting world position
//	 * @param rotation starting rotation in degrees
//	 * @param scale    starting scale per axis
//	 * @param mesh     the mesh to render for this object
//	 */
//	public GameObject(Vector3f position, Vector3f rotation, Vector3f scale, Mesh mesh) {
//		this.position = position;
//		this.rotation = rotation;
//		this.scale = scale;
//		this.mesh = mesh;
//	}
//
//	/**
//	 * Called once per frame to advance the object's state.
//	 * Right now this is a test animation: everything follows a sine wave
//	 * so you can see translation, rotation, and scaling working.
//	 */
//	public void update() {
////		// Advance the animation clock a fixed amount each call
////		temp += 0.02;
////
////		// Slide back and forth along the X axis between -1 and 1
////		position.setX((float) Math.sin(temp));
////
////		// Spin on all three axes; sin() * 360 sweeps between -360 and 360 degrees
////		rotation.set((float) Math.sin(temp) * 360, (float) Math.sin(temp) * 360, (float) Math.sin(temp) * 360);
////
////		// Pulse the size uniformly on all three axes
////		scale.set((float) Math.sin(temp), (float) Math.sin(temp), (float) Math.sin(temp));
//		
//		
//		position.setZ(position.getZ() - 0.05f);
//		
//		
//		
//	}
//
//	/** @return the live position vector (changes to it affect this object). */
//	public Vector3f getPosition() {
//		return position;
//	}
//
//	/** @return the live rotation vector, in degrees. */
//	public Vector3f getRotation() {
//		return rotation;
//	}
//
//	/** @return the live scale vector. */
//	public Vector3f getScale() {
//		return scale;
//	}
//
//	/** @return the mesh used to draw this object. */
//	public Mesh getMesh() {
//		return mesh;
//	}
//}
//
//
//



















//multi texture 

package engine.object;

import engine.graphics.Mesh;
import engine.maths.Vector3f;

/**
 * GAME OBJECT (ACTIVE, MULTI-MESH VERSION) — something that exists in the
 * game world: one or more meshes (the shapes to draw) plus a position,
 * rotation, and scale describing where and how it appears. The renderer
 * combines position/rotation/scale into a transformation matrix, then
 * draws every mesh in the array with that same matrix.
 */
public class GameObject {
	/** World position (x, y, z). */
	private Vector3f position;
	/** Euler rotation in degrees around each axis. */
	private Vector3f rotation;
	/** Scale factor per axis (1 = original size). */
	private Vector3f scale;

	/** The geometry (vertices/indices) this object draws. A model can have many meshes. */
	private Mesh[] meshes;

	/**
	 * CONSTRUCTOR — stores everything passed in. The vectors and the array are
	 * kept as they are (not copied), so the caller and this object share them.
	 *
	 * @param position starting world position
	 * @param rotation starting rotation in degrees
	 * @param scale    starting scale per axis
	 * @param meshes   the meshes to render for this object
	 */
	public GameObject(Vector3f position, Vector3f rotation, Vector3f scale, Mesh[] meshes) {
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.meshes = meshes;
	}

	/**
	 * UPDATE — called once per frame to advance the object's state.
	 * Currently empty, so the model stays where it was placed. The old
	 * version moved the object along z here as a test, and that line is kept
	 * below, commented out. Uncomment it to make the model drift away from
	 * the camera. Per-object game logic (movement, animation) would go here.
	 */
	public void update() {
//		position.setZ(position.getZ() - 0.05f);
	}

	/** GETTER — @return the live position vector (changes to it affect this object). */
	public Vector3f getPosition() {
		return position;
	}

	/** GETTER — @return the live rotation vector, in degrees. */
	public Vector3f getRotation() {
		return rotation;
	}

	/** GETTER — @return the live scale vector. */
	public Vector3f getScale() {
		return scale;
	}

	/** GETTER — @return all meshes used to draw this object. Replaces the old getMesh(). */
	public Mesh[] getMeshes() {
		return meshes;
	}
}