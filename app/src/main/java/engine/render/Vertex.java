package engine.graphics;


//import org.joml.Vector3f; // JOML version — uncomment this + comment out the engine.maths.Vector3f import to swap
import engine.maths.Vector3f;

/// this is a trial hardcode version we will change it later to joml version but for learining we hard code first
public class Vertex {
	private Vector3f position;
    private Vector3f color;
	
	public Vertex(Vector3f userPosition, Vector3f userColor) {
		this.position = userPosition;
        this.color = userColor; 
	}
	
	public Vector3f getPosition() {
		return position;
	}

    public Vector3f getColor() {
        return color;
    }
	
	
}