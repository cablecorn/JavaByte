package main;

import org.lwjgl.glfw.GLFW;

import engine.io.Input;
import engine.io.Window;

public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public  final int WIDTH = 1280, HEIGHT = 760;

	
	public void start() { // this starts the whole game basicly title is game for testing  cyrrentkly 
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		window.setBackGroundColor(1.0f, 0, 0); // this set the background in red for testing 
		window.create(); // this createzs the window 
		window.setFullscrean(false); // makes it into a full screenm

				
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
		if(Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			System.out.println( "X: " + Input.getScrollX() + ", Y:" + Input.getScrollY()); // testing scroll track when we left click it will show value  // testing if its tracks
			
		}
		
	}
	
	private void render() {
		//System.out.print("render game!");
		window.swapBuffers(); // this willl to render the frame that is just done meain it will render one frame fully before moving on
	}
	
	
	public static void main(String[] arg) {
		new Main().start();
		
	}

}
