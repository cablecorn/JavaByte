package main;

// Commented out random to remove the flashing that was done for testing purposes
//import java.util.Random;

// Changes made - CJ Gezley 9-19-26
// Added new includes for the dot logic
import org.lwjgl.opengl.GL11;
import engine.graphics.Mesh;
import engine.graphics.Renderer;
import engine.graphics.Vertex;
import engine.maths.Vector3f;
// End of new includes

import org.lwjgl.glfw.GLFW;

import engine.io.Input;
import engine.io.Window;

public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public  final int WIDTH = 1280, HEIGHT = 760;
	
	private Renderer renderer;
	private Mesh dot;
	
	// New variables for player movement implemented 9/19/26
	private float playerX, playerY = 0.0f;
	// delta time implementation
	// so the dot moves at the same speed no matter how fast the loop is running
	private float speed = 1.0f; // Screen units per second
	private long lastTime; // timestamp of the previous frame, in nanoseconds
	
	
	
	// Commented out the flash functionality that was used in testing
	/*
	private long lastFlashTime = 0;
	public static final long FLASH_INTERVAL_MS = 1000;
	private Random rand = new Random();
	*/

	
	public void start() { // this starts the whole game basicly title is game for testing  cyrrentkly 
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		window.setBackGroundColor(1.0f, 1.0f, 1.0f); // this set the background in red for testing --- Update 9/19/26 made the background white for testing
		window.create(); // this createzs the window 
		// Implementation for black dot
		renderer = new Renderer();
		dot = createDot(0.05f, 32);
		dot.create();
		window.setFullscrean(false); // makes it into a full screenm
		
		
		// New implementation for delta time
		// If we keep this, keep at bottom of init
		// Start of the clock
		lastTime = System.nanoTime();
		// lastFlashTime = System.currentTimeMillis();
	}
	
	
	// Builds a dot: one center vertex plus 'segments' vertices around it.
	// This is the shape that will move with WASD
	// 
	// radius - size of the shape in screen units (the screen spans -1 to 1)
	// segments - number of edge points: more segments = smoother edge;
	//
	// NOTE: Temporary test shape, see what ya'll think :)
	private Mesh createDot(float radius, int segments)
	{
		// The screen coordinates run -1 to 1 on both axes, so on a wide window
		// a "round" shape gets stretched sideways. Scaling x by height/width
		// cancels that out. Uses the initial window size; it won't adapt if the
		// window is resized or goes full screen.
		float aspect = (float) HEIGHT / WIDTH; // Creates the aspect based off of the window size and converts to float
		
		// One vertex per edge point. plus one extra for center
		Vertex[] vertices = new Vertex[segments + 1];
		
		// Each triangle uses 3 indices (center, edge point, next edge point)
		int[] indices = new int[segments * 3];
		
		// Vertex 0 is the center. (0, 0, 0) is the middle of the screen
		vertices[0] = new Vertex(new Vector3f(0, 0, 0));
		
		// The shape is weird that the loop creates
		// default value for the below variable is 2
		int angle_value = 30; //DEFAULT:2 for a circle. 
		for(int i = 0; i < segments; i++)
		{
			// Angle of this edge point around the center. In radians
			double angle = angle_value * Math.PI * i / segments;
			
			// Point on a circle: x = cos(angle), y = sin(angle), scaled by radius
			// x also gets aspect correlation from above.
			float x = (float) (Math.cos(angle) * radius * aspect);
			float y = (float) ((Math.sin(angle) * radius));
			
			// Edge points are stored at indices 1.. segments (0 is center)
			vertices[i + 1] = new Vertex(new Vector3f(x, y, 0));
			
			// triangle 1 = center, this edge point, next edge point.
			indices[i * 3] = 0;
			indices[i * 3 + 1] = i + 1;
			
			// (i + 1) % segments wraps the last triangle back to the first edge
			// point so the shape closes. The +1 skips the center at index 0
			indices[i * 3 + 2] = (i+1) % segments + 1;
		}
		
		return new Mesh(vertices, indices);
		
	}
	
	
	public void run () { // current funtion that run the updating cunters 
		init();
		while (!window.shouldClose() && !Input.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) { // this should close mehtod funtion allows us to cloase the program otherwise your gone lol
			update();
			render();
			if(Input.isKeyDown(GLFW.GLFW_KEY_F11)) window.setFullscrean(!window.fullscrean());
			
		}
		window.destory();
		
	}
	
	private void update() {
		//System.out.print("update game!");
		window.update(); //   process input/window events (does NOT render anything bascily make sure  imput is there)
		// Implemented 9/19/26 by CJ
		// measuring frame time
		long now = System.nanoTime();
		float delta = (now - lastTime) / 1_000_000_000.0f; // seconds since the last time
		lastTime = now;
		
		// Implementation of WASD 
		// Two floating points for tracking position based on the movements
		float dx = 0, dy = 0; // Holds the direction you want to go this frame
		// UP is positive Y in OpenGL 
		
		// Updates the x and y values based on the pressed key
		if (Input.isKeyDown(GLFW.GLFW_KEY_W)) dy += 1;
		if (Input.isKeyDown(GLFW.GLFW_KEY_S)) dy -= 1;
		if (Input.isKeyDown(GLFW.GLFW_KEY_D)) dx += 1;
		if (Input.isKeyDown(GLFW.GLFW_KEY_A)) dx -= 1;
		
		// Diagonal fix: without this, W+D moves ~41% faster than W alone
		float length = (float) Math.sqrt(dx * dx + dy * dy);
		if(length > 0)
		{
			dx /= length;
			dy /= length;
		}
		
		// updates the player position
		playerX += dx * speed * delta; 
		playerY += dy * speed * delta;
		
		// Temp testing for last time, comment out
		//System.out.println(delta);
		// Values will be weird for printing delta, with 60 FPS you should roughly
		// get updates 

        // flash test - remove when done testing
		// CJ Gezley removed flash test
		
		/*
        long now = System.currentTimeMillis();
        if (now - lastFlashTime >= FLASH_INTERVAL_MS) {
            window.setBackGroundColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            lastFlashTime = now;
        }
        */

        if(Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            System.out.println( "X: " + Input.getMouseX() + ", Y:" + Input.getMouseY());
        }
    }


	private void render() {
		//System.out.print("render game!");
		// Added by CJ 9/19/26
		GL11.glColor3f(0.0f, 0.0f, 0.0f); // Black color for dot
		
		GL11.glPushMatrix(); // Save the current position state 
		GL11.glTranslatef(playerX, playerY, 0); // shift everything drawn next
		renderer.renderMesh(dot); // REnders the dot
		GL11.glPopMatrix(); // restore it so nothing else is shifted
		
		
		window.swapBuffers(); // this willl to render the frame that is just done meain it will render one frame fully before moving on
	}
	
	
	public static void main(String[] arg) {
		new Main().start();
		
	}

}
