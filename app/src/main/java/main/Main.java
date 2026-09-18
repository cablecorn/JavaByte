package main;

import org.lwjgl.glfw.GLFW;

import engine.graphics.Mesh;
import engine.graphics.Renderer;
import engine.graphics.Vertex;
import engine.io.Input;
import engine.io.Window;
import engine.maths.Vector3f;

//import org.joml.Vector3f; // JOML version — uncomment this + comment out the line above to swap


public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public Renderer renderer;
	public  final int WIDTH = 1280, HEIGHT = 760;

	//=================================================================
	//the code below i belive is part of render and we can swap with joml
	public Mesh mesh = new Mesh(
	        new Vertex[] { 
	        		new Vertex(new Vector3f(-0.5f,  0.5f, 0.0f)), // 0: Top-left
	        		new Vertex(new Vector3f( 0.5f,  0.5f, 0.0f)), // 1: Bottom-left
	                new Vertex(new Vector3f( 0.5f, -0.5f, 0.0f)), // 2: Bottom-right
	                new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f))  // 3: Top-right
	        }, 
	        new int[] {
	            
	        		0, 1 ,2,
	        		0, 3, 2 // mean draaw from point 0 to 3
	        		
	        		
	        }
	    );
	
	
	//==========================================================
	
	public void start() { // this starts the whole game basicly title is game for testing  cyrrentkly 
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		renderer = new Renderer();
		
		window.setBackGroundColor(1.0f, 0, 0); // this set the background in red for testing 
		window.create(); // this createzs the window 
		mesh.create();  //creates the meash

				
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
		window.update(); // process input/window events (does NOT render anything bascily make sure  imput is there)
		if(Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			System.out.println( "X: " + Input.getMouseX() + ", Y:" + Input.getMouseY()); // testing scroll track when we left click it will show value  // testing if its tracks
			
		}
		
	}
	
	private void render() {
		
		renderer.renderMesh(mesh);
		
		//System.out.print("render game!");
		window.swapBuffers(); // this willl to render the frame that is just done meain it will render one frame fully before moving on
	}
	
	
	public static void main(String[] arg) {
		new Main().start();
		
	}

}
