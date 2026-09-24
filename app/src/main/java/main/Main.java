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

    private float playerX, playerY = 0.0f;
    private float speed = 1.0f;
    private long lastTime;
	
	public void start() {
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
        float [] vertices = {
        0.0f, 0.5f, 0.0f,
        1.0f, 0.0f, 0.0f,
       -0.5f, -0.5f, 0.0f,
        0.0f, 1.0f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.0f, 0.0f, 1.0f
        };
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		window.setBackGroundColor(1.0f, 1.0f, 1.0f);
		window.create();
		window.setFullscrean(false);

        lastTime = System.nanoTime();

        int [] indices = {0, 1, 2};
        testMesh = new Mesh(vertices, indices);
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
	
	public void run () {
		init();
		while (!window.shouldClose() && !Input.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
			update();
			render();
			if(Input.isKeyDown(GLFW.GLFW_KEY_F11)) window.setFullscrean(!window.fullscrean());
		}
		window.destory();
	}
	
	private void update() {
		window.update();
        long now = System.nanoTime();
        float delta = (now - lastTime) / 1_000_000_000.0f;
        lastTime = now;

        float dx = 0, dy = 0;

        if (Input.isKeyDown(GLFW.GLFW_KEY_W)) dy += 1;
        if (Input.isKeyDown(GLFW.GLFW_KEY_S)) dy -= 1;
		if (Input.isKeyDown(GLFW.GLFW_KEY_D)) dx += 1;
		if (Input.isKeyDown(GLFW.GLFW_KEY_A)) dx -= 1;

		float length = (float) Math.sqrt(dx * dx + dy * dy);
		if(length > 0) {
			dx /= length;
			dy /= length;
		}

		playerX += dx * speed * delta;
		playerY += dy * speed * delta;

		if(Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			System.out.println( "X: " + Input.getScrollX() + ", Y:" + Input.getScrollY());
		}
	}
	
	private void render() {
        testProgram.Use();
        testProgram.SetMovement(playerX, playerY);
        testMesh.Draw();
		window.swapBuffers();
	}
	
	public static void main(String[] arg) {
		new Main().start();
	}
}
