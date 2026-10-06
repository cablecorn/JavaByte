package engine.maths;

public class Vector3f {
	private float x, y ,z;
	
	public Vector3f(float x, float y, float z) { // initial constructor 
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(float x, float y, float z) { // second constructor 
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public float getX() { //getter
		return x;
	}

	public void setX(float x) {//setter
		this.x = x;
	}

	public float getY() {//getter
		return y;
	}

	public void setY(float y) {//setter
		this.y = y;
	}

	public float getZ() {//getter
		return z;
	}

	public void setZ(float z) { //setter
		this.z = z;
	}	
}