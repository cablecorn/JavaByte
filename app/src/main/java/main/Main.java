package main;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;

import engine.io.Input;
import engine.io.Window;
import engine.render.ShaderProgram;
import engine.render.Mesh;

public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public  final int WIDTH = 1280, HEIGHT = 760;
    public Mesh testMesh;
    public ShaderProgram testProgram;
	
	public void start() { // this starts the whole game basicly title is game for testing  cyrrentkly 
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
        float [] vertices = {
        0.0f, 0.5f, 0.0f,  // top corner
        1.0f, 0.0f, 0.0f,  // red fragment
       -0.5f, -0.5f, 0.0f,  // bottom left corner
        0.0f, 1.0f, 0.0f,   // green fragment
        0.5f, -0.5f, 0.0f,  // bottom right corner
        0.0f, 0.0f, 1.0f   // blue fragment
        };
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		window.setBackGroundColor(1.0f, 0, 0); // this set the background in red for testing 
		window.create(); // this createzs the window 
		window.setFullscrean(false); // makes it into a full screenm

        testMesh = new Mesh(vertices);
        try {
        testProgram = new ShaderProgram(
                    "src/main/java/engine/shaders/basic.vert",
                    "src/main/java/engine/shaders/basic.frag");
        }
        catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("Something wrong happened with the Shader constructor");
            System.exit(1);
        }
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
        testProgram.Use();
        testMesh.Draw();
		//System.out.print("render game!");
		window.swapBuffers(); // this willl to render the frame that is just done meain it will render one frame fully before moving on
	}
	
	
	public static void main(String[] arg) {
		new Main().start();
		
	}

}
