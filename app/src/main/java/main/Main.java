package main;

import org.lwjgl.glfw.GLFW;

import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.Renderer;
import engine.graphics.Shader;
import engine.graphics.Vertex;
import engine.io.Input;
import engine.io.ModelLoader;
import engine.io.Window;
import engine.maths.Vector2f;
import engine.maths.Vector3f;
import engine.object.Camera;
import engine.object.GameObject;
import org.lwjgl.opengl.GL11; // New import required for the overlay

//import org.joml.Vector3f; // JOML version — uncomment this + comment out the line above to swap

// Note: Material, Vertex, and Vector2f are only used by the commented-out test
// meshes below, so they are unused while the model loader version is active.


/*
* MAIN — entry point and core game loop controller.
* Implements Runnable so the game logic can run on its own dedicated thread,
* separate from the main thread that launches it.
*
* It owns the main pieces of the engine: the Window, the Shader, the Renderer,
* the Camera, and the GameObject being drawn. The loop is: init() once, then
* update() and render() every frame until the window closes.
*/

public class Main implements Runnable{
	public Thread game; 
	public  Window window;
	public Renderer renderer;
	public Shader shader;
	public  final int WIDTH = 1280, HEIGHT = 760;
	public long lastTime = 0;
	private static final float MODEL_YAW_OFFSET = 180;

	private float acornTime = 0f;
	private static final float ACORN_BASE_Y    = 0.5f;  // resting height
	private static final float ACORN_AMPLITUDE = 0.10f; // how far it bobs up/down
	private static final float ACORN_SPEED     = 2f;    // bob speed (radians/sec)
	
	/* Added by CJ 11/19/26
	 * These fields are required for the new HUD elements
	 * Since the camera is fixed, we can put flat digit objects inside of the 3D world
	 * This uses a second shader with no camera math, so its position will be fixed
	 */
	public Shader hudShader;
	public Renderer hudRenderer;
	public Mesh[][] digitMeshes = new Mesh[10][];
	public GameObject[] tensDigits = new GameObject[10];
	public GameObject[] unitsDigits = new GameObject[10];
	public int score = 0; 
	
	/*
	 * Added by CJ 11/19/26
	 * Similar logic to the score itself, adding a flat object for text to explicitly
	 * say the number you are looking at is the score 
	 */
	
	public Mesh[] labelMeshes;
	public GameObject scoreLabel;
	
	/*
	 * Added by CJ 11/19/26
	 * These fields are for collecting the acorns
	 * The pickupradius is half of a square, so it pick up upon entering a square
	 * Collected remember which acorns are gone
	 */
	
	public boolean[] collected = new boolean[64];
	private static final float PICKUP_RADIUS = 0.9f; // Modify this to change the pickup range 
	

	//=================================================================
		/*
		 * TEST MESHES (all commented out, kept for comparison). The project went
		 * through three ways of creating what gets drawn. Only the third is active.
		 *
		 *  1. SINGLE TEXTURED QUAD (first commented block below) — two triangles
		 *     that verify the pipeline end to end: vertex positions, per-vertex
		 *     colors, texture coordinates (UVs), index order, and the texture.
		 *     UV (0,0) = top-left of the image here, because the V values were
		 *     flipped to correct for the image loader reading pixel rows
		 *     top-to-bottom. It uses the old Vertex(position, color, uv) form.
		 *     The block is cut off partway: the closing of the index array and the
		 *     Material argument are not included, so it cannot be uncommented as is.
		 *
		 *  2. CUBE (second commented block) — 6 faces of 4 vertices each (24
		 *     vertices), with 2 triangles per face (36 indices). Each face has its
		 *     own vertices so each face gets its own texture coordinates. It uses
		 *     an even older Vertex(position, uv) form with no color or normal, so
		 *     it would not compile with today's three-argument Vertex.
		 *
		 *  3. LOADED MODEL (active, further down) — the model file is read with
		 *     ModelLoader and returns an array of meshes.
		 */
	
//	public Mesh mesh = new Mesh(
//			new Vertex[] { 
//				    new Vertex(new Vector3f(-0.5f,  0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(0.0f,0.0f)), // 0: Top-Left (Green)
//				    new Vertex(new Vector3f( 0.5f,  0.5f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector2f(1.0f,0.0f)), // 1: Top-Right (Red)
//				    new Vertex(new Vector3f( 0.5f, -0.5f, 0.0f), new Vector3f(0.5f, 0.0f, 0.5f), new Vector2f(1.0f,1.0f)), // 2: Bottom-Right (Purple)
//				    new Vertex(new Vector3f(-0.5f, -0.5f, 0.0f), new Vector3f(0.0f, 0.8f, 1.0f), new Vector2f(0.0f,1.0f))  // 3: Bottom-Left (Light Blue)
//				}, new int[] {
//	        		0, 1 ,2,
//	        		0, 3, 2 // draws the second triangle from point 0 to 3 to 2
	
	/*
	 * LOADED MODEL (active) — the scene setup. Compare it with the commented-out
	 * lines to see what changed:
	 *   OLD: "mesh" was ONE Mesh (a hand-made quad/cube, or one loaded mesh from
	 *        an .obj file), and the GameObject took that single mesh.
	 *   NEW: "meshes" is an ARRAY of Meshes loaded from model.gltf, and the
	 *        GameObject takes the whole array. The second argument of loadModel
	 *        is a fallback texture used for any mesh whose material has none.
	 *
	 * The three Vector3f arguments to GameObject are its position, rotation (in
	 * degrees), and scale. The camera starts at (0, 0, 1) looking along its
	 * default direction, with no rotation.
	 *
	 * Note the model path is a plain file path ("resources/models/...") that
	 * Assimp opens from disk, while the shaders and textures use classpath
	 * paths that start with "/".
	 *
	 * The commented-out "objects" array below was a stress test that drew 500
	 * copies of one mesh at random positions to check multi-object rendering.
	 */

	public Mesh[] meshes = ModelLoader.loadModel("src/main/resources/models/stage/board.obj", "/textures/checker.png");
	
	// CJ Added 11/7/26 -> new Mesh object to load the new sphere object named "dot.obj"
	public Mesh[] acornMeshes = ModelLoader.loadModel("src/main/resources/models/Acorns/scene.gltf", "/textures/scrats_acorn_prop_mat_baseColor.jpeg");
	public Mesh[] playerMesh = ModelLoader.loadModel("src/main/resources/models/player/scene.gltf", "/textures/material_0_baseColor.png");

	public GameObject[] acorns = new GameObject[64];
	public GameObject stage = new GameObject(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), meshes);
	public GameObject player = new GameObject(new Vector3f(0, 1, 0), new Vector3f(0, 0, 0), new Vector3f(1.5f, 1.5f, 1.5f), playerMesh);
	public Camera camera = new Camera(new Vector3f(0, 0, 1),new Vector3f(0, 0, 0));
	
	
	
	//==========================================================

		/*
		 * START — spins up the game loop on its own thread.
		 * Called once, externally, to kick off the program (see main() below).
		 */
	
	public void start() { // this starts the whole game, and the thread is named "game" for debugging
		game = new Thread(this, "game");
		game.start();
	}
	
	/*
	 * INIT — one-time setup that runs before the game loop starts.
	 * Creates the window, builds the shader, creates the renderer, sets the
	 * background clear color, and uploads every mesh and the shader program
	 * to the GPU.
	 *
	 * Order matters here: the shader object is made before the renderer (which
	 * needs a valid shader reference and the window for its projection matrix),
	 * and the GPU work (every mesh's create() and shader.create()) only happens
	 * after window.create(), because it needs the window's OpenGL context.
	 *
	 * Changes from the old version: the renderer now also receives the window,
	 * and the single mesh.create() call became a loop over the meshes array.
	 */
	
	public void init() {
		System.out.print("Ilitlizeing game!");
		window = new Window(WIDTH, HEIGHT, "GAME");
		
		shader = new Shader(
			    "/shaders/mainVertex.glsl",
			    "/shaders/mainFragment.glsl"
			);
		renderer = new Renderer(window, shader);

		window.setBackGroundColor(0, 0, 0); // sets background 
		window.create(); // creates the window and the OpenGL context
//		mesh.create();  //creates the mesh   // use this for single
		
		for (Mesh m : meshes) m.create();
		for(Mesh m : playerMesh) m.create();
		shader.create();

		lastTime = System.nanoTime();
		
		// Implemented by CJ 11/19/26 -> Initializing hud element
		/*
		 * Screen positions run from -1 (left) to 1 (right) and from -1 (bottom) to 1 (top).
		 * So -0.85f, 0.88f is near the top-left. 
		 * The tens digit sits at -0.85f and the ones digit at -0.77f. Keep the scale at (1, 1, 1)
		 * Modify the digit locations to your discretion :)
		 */
		hudShader = new Shader("/shaders/hudVertex.glsl", "/shaders/mainFragment.glsl");
		hudRenderer = new Renderer(window, hudShader);
		hudShader.create();
		
		/*
		 * Same logic here for the label that says "score"
		 * create the label 
		 * Also to mention, the values for the locations of the vertex are just from messing around
		 */
		
		float hudX = -0.95f;
		labelMeshes = ModelLoader.loadModel("src/main/resources/models/hud_label.obj", "/textures/score_label.png");
		for (Mesh m : labelMeshes) m.create();
		scoreLabel = new GameObject(new Vector3f(hudX + 0.186f, 0.88f, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), labelMeshes);
		for(int d = 0; d < 10; d++) {
			digitMeshes[d] = ModelLoader.loadModel("src/main/resources/models/hud_quad.obj", "/textures/digit_" + d + ".png");
			for(Mesh m : digitMeshes[d]) m.create();
			tensDigits[d]  = new GameObject(new Vector3f(hudX + 0.434f, 0.88f, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), digitMeshes[d]);
		    unitsDigits[d] = new GameObject(new Vector3f(hudX + 0.514f, 0.88f, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), digitMeshes[d]);
		}
		

		// Variable to make the acorns smaller and a variable to adjust square size
		float squareSize = 2.0f;
		float acornScale = 0.45f;
		// CJ Added 11/7/26 -> Initialization of the acorn, note MUST be after shader.create()
		for(Mesh m : acornMeshes) m.create();
		int n = 0;
		// hardocded values for loop index, the size of the board is 8x8 but can be updated inside of board.obj
		for (int row = 0; row < 8; row++)
		{
			for ( int col = 0; col < 8; col++)
			{
				// The math here works on hopes and dreams,. this took so long to figure out but the acorns are now centered on my grid
				acorns[n++] = new GameObject(new Vector3f((col - 3.5f) * squareSize, 0.5f, (row - 3.5f) * squareSize), new Vector3f(0, 0, -45), new Vector3f(acornScale, acornScale , acornScale), acornMeshes);			}
		}


		//multi objects
//		objects[0] = object;
//		for (int i = 0; i < objects.length; i++) {
//			objects[i] = new GameObject(new Vector3f((float) (Math.random() * 50 - 25), (float) (Math.random() * 50 - 25), (float) (Math.random() * 50 - 25)), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), mesh);
//		}
	}
	
	
	/*
	 * RUN — the actual game loop (called automatically when the thread starts).
	 * Runs init() once, then repeatedly updates and renders each frame until
	 * the window is closed or Escape is pressed. Each frame it also checks:
	 *   F11        — toggles fullscreen
	 *   left click — locks and hides the mouse cursor, so the first-person
	 *                camera can turn with the mouse without the cursor leaving
	 *                the window
	 * When the loop ends, it closes the window.
	 */
	
	public void run () { // the main loop: runs until the window closes or Escape is pressed
		init();
		while (!window.shouldClose() && !Input.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) { // lets the player close the program with Escape
			update();
			render();
			if(Input.isKeyDown(GLFW.GLFW_KEY_F11)) window.setFullscrean(!window.fullscrean());
			if(Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) window.mouseState(true);
		}
		window.destory();
		
	}
	
	/*
	 * UPDATE — per-frame logic (non-rendering).
	 * Polls window/input events, then moves the camera. The first-person
	 * camera update is active (WASD to move, mouse to look). The third-person
	 * version, which orbits around the object, is commented out, so swap those
	 * two lines to change modes. It also logs the mouse position to the console
	 * while the left button is held (test/debug only).
	 */
	
	private void update() {
		long now = System.nanoTime();
		float deltaTime = (now - lastTime) / 1_000_000_000f;
		lastTime = now;
		float speed = 5f;

		int forward = 0;
		int sideways = 0;

		if (Input.isKeyDown(GLFW.GLFW_KEY_W)) {
			forward++;
		}

		if (Input.isKeyDown(GLFW.GLFW_KEY_S)) {
			forward--;
		}

		if (Input.isKeyDown(GLFW.GLFW_KEY_D)) {
			sideways++;
		}

		if (Input.isKeyDown(GLFW.GLFW_KEY_A)) {
			sideways--;
		}

		float moveX = camera.walkUp().getX() * forward + camera.walkRight().getX() * sideways;
		float moveY = 0;
		float moveZ = camera.walkUp().getZ() * forward + camera.walkRight().getZ() * sideways;

		float length = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
		if (length > 0) {
    		moveX /= length;
    		moveZ /= length;

    		// face the direction of travel (0° = facing +Z)
    		float yaw = (float) Math.toDegrees(Math.atan2(-moveX, moveZ)) + MODEL_YAW_OFFSET;
			player.getRotation().setY(yaw);
		}

		moveX = moveX * speed * deltaTime;
		moveZ = moveZ * speed * deltaTime;

		player.move(moveX, moveY, moveZ);
		
		for (int i = 0; i < acorns.length; i++) {
		    if (collected[i]) continue;
		    float dx = player.getPosition().getX() - acorns[i].getPosition().getX();
		    float dz = player.getPosition().getZ() - acorns[i].getPosition().getZ();
		    if (dx * dx + dz * dz < 4.0f) System.out.println("acorn " + i + "  dx=" + dx + "  dz=" + dz);
		    if (dx * dx + dz * dz < PICKUP_RADIUS * PICKUP_RADIUS) {
		        collected[i] = true;
		        score++;
		    }
		}

		// Code snippet to give acorns floating feeling
		acornTime += deltaTime;
		for (int i = 0; i < acorns.length; i++) {
    		acorns[i].bounce(ACORN_BASE_Y + ACORN_AMPLITUDE * (float) Math.sin(acornTime * ACORN_SPEED));
		}

		//System.out.print("update game!");
		window.update(); // polls input/window events and clears the screen (does NOT draw anything)

		camera.update(player); // third person
		if(Input.isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
			System.out.println( "X: " + Input.getMouseX() + ", Y:" + Input.getMouseY()); // test print: shows the mouse position while the left button is held

		}
		
	}
	
	/*
	 * RENDER — draws the current frame and presents it to the screen.
	 * Hands the GameObject and the camera to the Renderer, which draws every
	 * mesh in the object, then swaps the front/back buffers so the fully drawn
	 * frame becomes visible (this prevents flicker and tearing).
	 *
	 * The commented-out loop was for the 500-object stress test: it drew every
	 * object in the "objects" array instead of just the one.
	 */
	
	private void render() {
		
		
		//for multi render BELOW
//		for (int i = 0; i < objects.length; i++) {
//			renderer.renderMesh(objects[i], camera);
//		}
	
		renderer.renderMesh(stage, camera);
		renderer.renderMesh(player, camera);

		// CJ Added 11/7/26 -> render for the sphere 
		// for (GameObject acorn : acorns) renderer.renderMesh(acorn, camera);
		// Commented out above line to change the way acorns are drawn
		for(int i = 0; i < acorns.length; i++) {
			if(!collected[i]) renderer.renderMesh(acorns[i], camera);
		}
		
		// CJ Added 11/119/26 -> render for the HUD
		// Keep this before window.swapBuffers() if you are going to modify at all
		// Clearing the depth buffer first makes the score draw over the stage
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		hudRenderer.renderMesh(scoreLabel, camera);
		if(score >= 10) hudRenderer.renderMesh(tensDigits[(score / 10) % 10], camera);
		hudRenderer.renderMesh(unitsDigits[score % 10], camera);
		
		//System.out.print("render game!");
		window.swapBuffers(); // shows the finished frame and starts the next one
	}
	
	
	/*
	 * CLOSE — cleanup/shutdown logic.
	 * Releases the window, every mesh (and its texture), and the shader's GPU
	 * resources, so no GPU memory is leaked at shutdown.
	 *
	 * The old version only freed one mesh. The new version loops over the
	 * meshes array and frees each of them.
	 */
	public void close() {

		window.destory();
		for (Mesh m : meshes) m.destroy();
		for (Mesh m : playerMesh) m.destroy();
		// CJ Added 11/726 -> Destroy function for the acornMesh
		for ( Mesh m : acornMeshes) m.destroy();
		shader.destroy();
		
		// CJ Added 11/19/26 -> Destroy the hud Mesh 
		for(int d = 0; d < 10; d++) for (Mesh m : digitMeshes[d]) m.destroy();
		hudShader.destroy();
		for(Mesh m : labelMeshes) m.destroy();
	}
	
	/*
	 * MAIN — program entry point. Creates the Main object and starts the
	 * game thread via start().
	 */
	
	public static void main(String[] arg) {
		new Main().start();
		
	}

}