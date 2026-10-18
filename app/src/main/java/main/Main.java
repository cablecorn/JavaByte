package main;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;

import engine.io.Input;
import engine.io.Window;
import engine.render.ShaderProgram;
import engine.render.Mesh;
import engine.maths.Matrix4f;
import engine.maths.Vector3f;
import engine.graphics.Vertex;
import engine.object.IsoCamera;

public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public  final int WIDTH = 1280, HEIGHT = 760;
	public ShaderProgram testProgram;

	// meshes: one cube shape, built in a few colours (tile, tile, tile under the mouse, player)
	public Mesh player;
	public Mesh tileA, tileB, tileHover;

	public IsoCamera camera = new IsoCamera();

	// the ground is a GRID_SIZE x GRID_SIZE board; tile (col, row) is centred on (col + 0.5, row + 0.5)
	private final int GRID_SIZE = 16;
	private final float TILE_HEIGHT = 0.2f;

	private float playerX, playerZ; // position on the ground (x/z plane). Used to be playerX, playerY in 2D
	private float speed = 4.0f;      // tiles per second
	private long lastTime;
	private double lastScroll = 0;

	private int hoverCol = -1, hoverRow = -1; // tile under the mouse, -1 when none

	public void start() {
		game = new Thread(this, "game");
		game.start();
	}
	
	public void init() {
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		window.setBackGroundColor(0.10f, 0.11f, 0.14f);
		window.create();
		window.setFullscrean(false);

        lastTime = System.nanoTime();

        // meshes need the window (OpenGL context) to exist first, so they are created after window.create()
        tileA = createCube(new Vector3f(0.30f, 0.55f, 0.35f));
        tileB = createCube(new Vector3f(0.36f, 0.64f, 0.41f));
        tileHover = createCube(new Vector3f(0.90f, 0.80f, 0.35f));
        player = createCube(new Vector3f(0.85f, 0.35f, 0.30f));

        playerX = GRID_SIZE / 2.0f;
        playerZ = GRID_SIZE / 2.0f;

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

	// Builds a cube in the same style as createDot: raw vertices, then the indices that join them
	// into triangles. The cube is 1 wide and 1 deep (x and z from -0.5 to 0.5) and 1 tall (y from 0
	// to 1), so scaling y makes it grow UP from the ground. There is no lighting yet, so each face is
	// darkened by a fixed amount (the "shade" column) to make the cube read as 3D. The three faces
	// the isometric camera can see are the top, the +z face and the +x face.
	private Mesh createCube(Vector3f color) {
		//                 four corners of the face (x, y, z)                                          shade
		float [][] faces = {
			{ -0.5f, 1,-0.5f,  -0.5f, 1, 0.5f,   0.5f, 1, 0.5f,   0.5f, 1,-0.5f,   1.00f },  // top    (+y)
			{ -0.5f, 0, 0.5f,   0.5f, 0, 0.5f,   0.5f, 1, 0.5f,  -0.5f, 1, 0.5f,   0.80f },  // front  (+z)
			{  0.5f, 0, 0.5f,   0.5f, 0,-0.5f,   0.5f, 1,-0.5f,   0.5f, 1, 0.5f,   0.60f },  // right  (+x)
			{  0.5f, 0,-0.5f,  -0.5f, 0,-0.5f,  -0.5f, 1,-0.5f,   0.5f, 1,-0.5f,   0.45f },  // back   (-z)
			{ -0.5f, 0,-0.5f,  -0.5f, 0, 0.5f,  -0.5f, 1, 0.5f,  -0.5f, 1,-0.5f,   0.40f },  // left   (-x)
			{ -0.5f, 0,-0.5f,   0.5f, 0,-0.5f,   0.5f, 0, 0.5f,  -0.5f, 0, 0.5f,   0.30f }   // bottom (-y)
		};

		// raw vertex data: 4 corners per face
		Vertex [] vertices = new Vertex [faces.length * 4];
		// important indices: 2 triangles (6 indices) per face
		int [] indices = new int [faces.length * 6];

		for (int f = 0; f < faces.length; f++) {
			float shade = faces[f][12];
			for (int c = 0; c < 4; c++) {
				Vector3f position = new Vector3f(faces[f][c * 3], faces[f][c * 3 + 1], faces[f][c * 3 + 2]);
				Vector3f shaded = new Vector3f(color.getX() * shade, color.getY() * shade, color.getZ() * shade);
				vertices[f * 4 + c] = new Vertex(position, shaded);
			}

			// two triangles per face: corners 0-1-2 and 0-2-3
			int v = f * 4;
			int i = f * 6;
			indices[i] = v;
			indices[i + 1] = v + 1;
			indices[i + 2] = v + 2;
			indices[i + 3] = v;
			indices[i + 4] = v + 2;
			indices[i + 5] = v + 3;
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

		// W/S/A/D are relative to the SCREEN, not the world axes: "forward" is up the screen and
		// "right" is right on the screen, which are diagonals in world space for an iso camera.
		Vector3f forward = camera.getForward();
		Vector3f right = camera.getRight();
		playerX += (right.getX() * dx + forward.getX() * dy) * speed * delta;
		playerZ += (right.getZ() * dx + forward.getZ() * dy) * speed * delta;

		// keep the player on the board
		playerX = Math.max(0.3f, Math.min(GRID_SIZE - 0.3f, playerX));
		playerZ = Math.max(0.3f, Math.min(GRID_SIZE - 0.3f, playerZ));

		// the camera follows the player, and the scroll wheel zooms (getScrollY keeps adding up,
		// so we zoom by how much it changed since last frame)
		camera.setTarget(playerX, TILE_HEIGHT, playerZ);
		double scroll = Input.getScrollY();
		camera.zoom(scroll - lastScroll);
		lastScroll = scroll;

		// which tile is under the mouse? Ask the camera where the mouse ray hits the tile tops.
		hoverCol = -1;
		hoverRow = -1;
		Vector3f hit = camera.pickGround(Input.getMouseX(), Input.getMouseY(), window.getWidth(), window.getHeight(), TILE_HEIGHT);
		if (hit != null) {
			int col = (int) Math.floor(hit.getX());
			int row = (int) Math.floor(hit.getZ());
			if (col >= 0 && col < GRID_SIZE && row >= 0 && row < GRID_SIZE) {
				hoverCol = col;
				hoverRow = row;
			}
		}
	}
	
	private void render() {
		float aspect = (float) window.getWidth() / window.getHeight();
        testProgram.Use();
        testProgram.SetMatrix("projection", camera.getProjection(aspect));
        testProgram.SetMatrix("view", camera.getView());

        Vector3f noRotation = new Vector3f(0, 0, 0);

        // draw the board: a flat, checkerboard cube for each tile (the tiny gap, 0.98, shows the grid lines)
        for (int col = 0; col < GRID_SIZE; col++) {
            for (int row = 0; row < GRID_SIZE; row++) {
                Mesh tile = ((col + row) % 2 == 0) ? tileA : tileB;
                if (col == hoverCol && row == hoverRow) tile = tileHover;

                testProgram.SetMatrix("model", Matrix4f.transform(
                        new Vector3f(col + 0.5f, 0, row + 0.5f),
                        noRotation,
                        new Vector3f(0.98f, TILE_HEIGHT, 0.98f)));
                tile.Draw();
            }
        }

        // draw the player standing on top of the tiles
        testProgram.SetMatrix("model", Matrix4f.transform(
                new Vector3f(playerX, TILE_HEIGHT, playerZ),
                noRotation,
                new Vector3f(0.6f, 0.9f, 0.6f)));
        player.Draw();

		window.swapBuffers();
	}
	
	public static void main(String[] arg) {
		new Main().start();
	}
}
