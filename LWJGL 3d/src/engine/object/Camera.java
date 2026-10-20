package engine.object;

import org.lwjgl.glfw.GLFW;

import engine.io.Input;
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
	private float moveSpeed = 0.05f, mouseSensitivity = 0.15f, distance = 2.0f, horizontalAngle = 0, verticalAngle = 0;
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
	 * UPDATE (FIRST PERSON) — moves and turns the camera like a player's head.
	 * Call once per frame.
	 *
	 * Movement: W, A, S, D move forward, left, back, and right, and Space and
	 * Left Shift move up and down. Forward and sideways movement follow the
	 * direction the camera is facing, so the code takes the sine and cosine of
	 * the yaw (rotation.y) to split one step of moveSpeed into x and z parts.
	 *
	 * Turning: the mouse movement since last frame (dx, dy) is multiplied by
	 * the sensitivity and added to the rotation. Both are negated, so moving
	 * the mouse right turns the view right and moving it up looks up.
	 *
	 * Each movement creates a new position vector with Vector3f.add instead of
	 * changing the old one in place.
	 */
	//first person
	public void update() {
		newMouseX = Input.getMouseX();
		newMouseY = Input.getMouseY();
		
		float x = (float) Math.sin(Math.toRadians(rotation.getY())) * moveSpeed;
		float z = (float) Math.cos(Math.toRadians(rotation.getY())) * moveSpeed;
		
		if (Input.isKeyDown(GLFW.GLFW_KEY_A)) position = Vector3f.add(position, new Vector3f(-z, 0, x));
		if (Input.isKeyDown(GLFW.GLFW_KEY_D)) position = Vector3f.add(position, new Vector3f(z, 0, -x));
		if (Input.isKeyDown(GLFW.GLFW_KEY_W)) position = Vector3f.add(position, new Vector3f(-x, 0, -z));
		if (Input.isKeyDown(GLFW.GLFW_KEY_S)) position = Vector3f.add(position, new Vector3f(x, 0, z));
		if (Input.isKeyDown(GLFW.GLFW_KEY_SPACE)) position = Vector3f.add(position, new Vector3f(0, moveSpeed, 0));
		if (Input.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) position = Vector3f.add(position, new Vector3f(0, -moveSpeed, 0));
		
		float dx = (float) (newMouseX - oldMouseX);
		float dy = (float) (newMouseY - oldMouseY);
		
		rotation = Vector3f.add(rotation, new Vector3f(-dy * mouseSensitivity, -dx * mouseSensitivity, 0));
		
		oldMouseX = newMouseX;
		oldMouseY = newMouseY;
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
		newMouseX = Input.getMouseX();
		newMouseY = Input.getMouseY();
		
		float dx = (float) (newMouseX - oldMouseX);
		float dy = (float) (newMouseY - oldMouseY);
		
		if (Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			verticalAngle -= dy * mouseSensitivity;
			horizontalAngle += dx * mouseSensitivity;
		}
		if (Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
			if (distance > 0) {
				distance += dy * mouseSensitivity / 4;
			} else {
				distance = 0.1f;
			}
		}
		
		float horizontalDistance = (float) (distance * Math.cos(Math.toRadians(verticalAngle)));
		float verticalDistance = (float) (distance * Math.sin(Math.toRadians(verticalAngle)));
		
		float xOffset = (float) (horizontalDistance * Math.sin(Math.toRadians(-horizontalAngle)));
		float zOffset = (float) (horizontalDistance * Math.cos(Math.toRadians(-horizontalAngle)));
		
		position.set(object.getPosition().getX() + xOffset, object.getPosition().getY() - verticalDistance, object.getPosition().getZ() + zOffset);
	
		rotation.set(verticalAngle, -horizontalAngle, 0);
		
		oldMouseX = newMouseX;
		oldMouseY = newMouseY;
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