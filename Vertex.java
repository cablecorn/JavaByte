package engine.graphics;


//import org.joml.Vector3f; // JOML version — uncomment this + comment out the engine.maths.Vector3f import to swap


import engine.maths.Vector3f;


/// this is a trial hardcode version we will change it later to joml version but for learining we hard code first
public class Vertex {
	private Vector3f position;
	
	public Vertex(Vector3f position) {
		this.position = position; 
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	
}
