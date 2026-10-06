package main;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;

import engine.io.Input;
import engine.io.Window;
import engine.render.ShaderProgram;
import engine.render.Mesh;
import engine.maths.Vector3f;
import engine.graphics.Vertex;

public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public  final int WIDTH = 1280, HEIGHT = 760;
    public Mesh player;
	public ShaderProgram testProgram;

	private float playerX, playerY = 0.0f;
	private float speed = 1.0f;
	private long lastTime;
	
	public void start() {
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		window.setBackGroundColor(1.0f, 1.0f, 1.0f);
		window.create();
		window.setFullscrean(false);

        lastTime = System.nanoTime();

        player = createDot(0.6f, 32);
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

	private Mesh createDot(float radius, int segments) {
		float aspect = (float) HEIGHT / WIDTH;
		// raw vertex data
		Vertex [] vertices = new Vertex [segments + 1];
		// important indices
		int [] indices = new int [segments * 3];

		// set origin point for circle
		vertices[0] = new Vertex(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));

		for (int i = 0; i < segments; i++) {
			// Angle of this edge point around the center. In radians
			double angle = 2 * Math.PI * i / segments;
			
			// Point on a circle: x = cos(angle), y = sin(angle), scaled by radius
			// x also gets aspect correlation from above.
			float x = (float) (Math.cos(angle) * radius * aspect);
			float y = (float) ((Math.sin(angle) * radius));
			
			// Edge points are stored at indices 1.. segments (0 is center)
			vertices[i + 1] = new Vertex(new Vector3f(x, y, 0.0f), new Vector3f(x, y, 0.0f));
			
			// triangle 1 = center, this edge point, next edge point.
			indices[i * 3] = 0;
			indices[i * 3 + 1] = i + 1;
			
			// (i + 1) % segments wraps the last triangle back to the first edge
			// point so the shape closes. The +1 skips the center at index 0
			indices[i * 3 + 2] = (i+1) % segments + 1;
		}

		return new Mesh(vertices, indices);
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
        player.Draw();
		window.swapBuffers();
	}
	
	public static void main(String[] arg) {
		new Main().start();
	}
}
