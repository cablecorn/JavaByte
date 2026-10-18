package engine.object;

import engine.maths.Matrix4f;
import engine.maths.Vector3f;

/*
 * ISO CAMERA — an orthographic camera that looks at a target from a fixed angle, which
 * is what gives the isometric look. It plays the same role as Farran's Camera (the Renderer
 * / Main ask it for the view and projection matrices every frame), but instead of free
 * first-person or orbit movement, the angle is locked and only the target and zoom change.
 *
 * ANGLES (degrees): yaw is the turn around the vertical axis, pitch is how far the camera
 * looks down. True isometric is yaw 45 and pitch 35.264 (= atan(1 / sqrt(2))), which makes
 * the x and z axes appear exactly 30 degrees below horizontal on screen. A 2:1 pixel-art
 * style look uses pitch 30 instead.
 *
 * ZOOM is the half-height of what the camera sees, in world units, so a SMALLER zoom number
 * means closer. There is no field of view, because an orthographic camera has no perspective.
 *
 * World axes: +y is up, the ground is the x/z plane.
 */
public class IsoCamera {
	private Vector3f target = new Vector3f(0, 0, 0);
	private float yaw = 45.0f;
	private float pitch = 35.264f;
	private float distance = 50.0f; // only matters for clipping, an orthographic view doesn't shrink with distance
	private float zoom = 6.0f;
	private float minZoom = 2.0f, maxZoom = 20.0f;
	
	public void setTarget(float x, float y, float z) {
		target.set(x, y, z);
	}
	
	/*
	 * ZOOM — call with the amount the scroll wheel moved. Each scroll step changes the zoom by
	 * 10%, and the result is kept between minZoom and maxZoom.
	 */
	public void zoom(double scrollAmount) {
		zoom = (float) (zoom * Math.pow(0.9, scrollAmount));
		zoom = Math.max(minZoom, Math.min(maxZoom, zoom));
	}
	
	/*
	 * BACK — the unit direction from the target toward the camera. Everything else is built from
	 * this and the two vectors below (right and up on the screen, expressed in world space).
	 */
	private float[] back() {
		float y = (float) Math.toRadians(yaw), p = (float) Math.toRadians(pitch);
		return new float[] { (float) (Math.cos(p) * Math.sin(y)), (float) Math.sin(p), (float) (Math.cos(p) * Math.cos(y)) };
	}
	
	private float[] right() {
		float y = (float) Math.toRadians(yaw);
		return new float[] { (float) Math.cos(y), 0, (float) -Math.sin(y) };
	}
	
	private float[] up() {
		float y = (float) Math.toRadians(yaw), p = (float) Math.toRadians(pitch);
		return new float[] { (float) (-Math.sin(p) * Math.sin(y)), (float) Math.cos(p), (float) (-Math.sin(p) * Math.cos(y)) };
	}
	
	/*
	 * GROUND DIRECTIONS — the world-space x/z directions that point "up the screen" and "right
	 * on the screen". WASD uses these so W always moves toward the top of the screen, even
	 * though the camera is turned 45 degrees from the world axes.
	 */
	public Vector3f getForward() {
		return new Vector3f(-(float) Math.sin(Math.toRadians(yaw)), 0, -(float) Math.cos(Math.toRadians(yaw)));
	}
	
	public Vector3f getRight() {
		float[] r = right();
		return new Vector3f(r[0], r[1], r[2]);
	}
	
	/*
	 * GET POSITION / GET ROTATION — what Matrix4f.view(position, rotation) needs. The position is
	 * the target pushed back along the "back" direction. The rotation is (-pitch, yaw, 0): the
	 * signs are flipped compared to the usual (pitch, -yaw) because Matrix4f.rotate() turns the
	 * opposite way from the standard maths formula (checked by testing that the target lands in
	 * the middle of the screen).
	 */
	public Vector3f getPosition() {
		float[] b = back();
		return new Vector3f(target.getX() + distance * b[0], target.getY() + distance * b[1], target.getZ() + distance * b[2]);
	}
	
	public Vector3f getRotation() {
		return new Vector3f(-pitch, yaw, 0);
	}
	
	public Matrix4f getView() {
		return Matrix4f.view(getPosition(), getRotation());
	}
	
	/*
	 * GET PROJECTION — the orthographic box is 2 * zoom tall and 2 * zoom * aspect wide
	 * (aspect = window width / window height).
	 */
	public Matrix4f getProjection(float aspect) {
		return Matrix4f.orthographic(-zoom * aspect, zoom * aspect, -zoom, zoom, 0.1f, distance * 4);
	}
	
	/*
	 * PICK GROUND — finds the point on the flat plane y = planeY that lies under the mouse.
	 * mouseX / mouseY and windowWidth / windowHeight are in window pixels, with (0, 0) at the
	 * top-left (what Input.getMouseX/Y and Window.getWidth/getHeight give).
	 *
	 * Because the camera is orthographic every mouse ray points the same way (straight "into" the
	 * screen), and only its starting point changes: it starts on the camera's view plane, shifted
	 * right/up by how far the mouse is from the middle of the window. We then walk along the ray
	 * until it reaches planeY. Returns null if the ray never reaches the plane.
	 */
	public Vector3f pickGround(double mouseX, double mouseY, int windowWidth, int windowHeight, float planeY) {
		if (windowWidth <= 0 || windowHeight <= 0) return null;
		
		float aspect = (float) windowWidth / windowHeight;
		float ndcX = (float) (2.0 * mouseX / windowWidth - 1.0);
		float ndcY = (float) (1.0 - 2.0 * mouseY / windowHeight);
		
		float[] r = right(), u = up(), b = back();
		float shiftRight = ndcX * zoom * aspect;
		float shiftUp = ndcY * zoom;
		
		float ox = target.getX() + r[0] * shiftRight + u[0] * shiftUp;
		float oy = target.getY() + r[1] * shiftRight + u[1] * shiftUp;
		float oz = target.getZ() + r[2] * shiftRight + u[2] * shiftUp;
		
		if (Math.abs(b[1]) < 1e-6f) return null;
		float t = (oy - planeY) / b[1]; // distance to walk along -back (toward the ground)
		
		return new Vector3f(ox - b[0] * t, planeY, oz - b[2] * t);
	}
}
