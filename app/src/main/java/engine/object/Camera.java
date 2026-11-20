package engine.object;

import engine.maths.Vector3f;

/*
 * CAMERA — holds the viewer's position and rotation, and moves them based on
 * keyboard and mouse input. The Renderer reads getPosition() and
 * getRotation() every frame to build the view matrix (see Matrix4f.view).
 *
 * The class has two modes, and each one is its own update method:
 *   FIRST PERSON  — update():             the camera is the player's eyes.
 *   THIRD PERSON  — update(GameObject):   the camera orbits around an object.
 * The game calls whichever one it wants once per frame, and only one of them
 * should be called in a frame. Both share the same mouse-tracking fields.
 *
 * Rotation is stored in DEGREES: x is looking up and down (pitch), y is
 * turning left and right (yaw), and z is tilting sideways (roll, unused).
 */
public class Camera {
	private Vector3f position, rotation;
	/*
	 * TUNING VALUES AND STATE:
	 *   moveSpeed        = how far the camera moves per frame in first person
	 *   mouseSensitivity = how many degrees one pixel of mouse movement turns
	 *   distance         = how far the third person camera sits from its object
	 *   horizontalAngle  = third person orbit angle around the object (degrees)
	 *   verticalAngle    = third person orbit angle above or below it (degrees)
	 *   oldMouse / newMouse = the mouse position last frame and this frame, so
	 *                      the difference between them is how far it moved
	 */
	private float moveSpeed = 0.05f, mouseSensitivity = 0.15f, distance = 50.0f, horizontalAngle = -45, verticalAngle = -35.264f;
	private double oldMouseX = 0, oldMouseY = 0, newMouseX, newMouseY;

	/*
	 * CONSTRUCTOR — stores the starting position and rotation. The vectors
	 * are kept as they are (not copied), so the caller and the camera share
	 * the same objects.
	 */
	public Camera(Vector3f position, Vector3f rotation) {
		this.position = position;
		this.rotation = rotation;
	}
	
	
	
	/*
	 * UPDATE (THIRD PERSON) — orbits the camera around a GameObject and keeps
	 * it looking at that object. Call once per frame, passing the object to
	 * follow.
	 *
	 * Controls: dragging with the LEFT mouse button turns the camera around
	 * the object (horizontalAngle and verticalAngle). Dragging with the RIGHT
	 * mouse button moves the camera closer or farther (distance), and the
	 * distance is not allowed to reach 0.
	 *
	 * Position: the orbit angles and distance are turned into an offset from
	 * the object using sine and cosine (this is a point on a sphere around
	 * the object). The camera position is the object's position plus that
	 * offset. The rotation is set to match the angles, so the camera faces
	 * the object.
	 *
	 * Unlike the first person update, this one changes position and rotation
	 * IN PLACE with set(...), instead of replacing them with new vectors.
	 */
	//third person 
	
	public void update(GameObject object) {
		
		float dx = (float) (newMouseX - oldMouseX);
		float dy = (float) (newMouseY - oldMouseY);
			
		float horizontalDistance = (float) (distance * Math.cos(Math.toRadians(verticalAngle)));
		float verticalDistance = (float) (distance * Math.sin(Math.toRadians(verticalAngle)));
		
		float xOffset = (float) (horizontalDistance * Math.sin(Math.toRadians(-horizontalAngle)));
		float zOffset = (float) (horizontalDistance * Math.cos(Math.toRadians(-horizontalAngle)));
		
		position.set(object.getPosition().getX() + xOffset, object.getPosition().getY() - verticalDistance, object.getPosition().getZ() + zOffset);
	
		rotation.set(verticalAngle, -horizontalAngle, 0);
		
		oldMouseX = newMouseX;
		oldMouseY = newMouseY;
	}

	public Vector3f walkUp() {
		Vector3f up = new Vector3f((float)-Math.sin(Math.toRadians(-horizontalAngle)), 0, (float)-Math.cos(Math.toRadians(-horizontalAngle)));
		return up;
	}


	public Vector3f walkRight() {
		Vector3f right = new Vector3f((float) Math.cos(Math.toRadians(-horizontalAngle)) , 0, (float) -Math.sin(Math.toRadians(-horizontalAngle)));
		return right;
	}	
	
	

	/*
	 * GETTERS — return the camera's position and rotation. The Renderer
	 * passes both to Matrix4f.view() each frame to build the view matrix.
	 */
	public Vector3f getPosition() {
		return position;
	}

	public Vector3f getRotation() {
		return rotation;
	}
}
