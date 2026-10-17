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
	
	
	
	
	//code below is for cube testing 
	
	
//	public Mesh mesh = new Mesh(new Vertex[] {
//			//Back face
//			new Vertex(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector2f(0.0f, 0.0f)),
//			new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector2f(0.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector2f(1.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector2f(1.0f, 0.0f)),
//			
//			//Front face
//			new Vertex(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector2f(0.0f, 0.0f)),
//			new Vertex(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector2f(0.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector2f(1.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector2f(1.0f, 0.0f)),
//			
//			//Right face
//			new Vertex(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector2f(0.0f, 0.0f)),
//			new Vertex(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector2f(0.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector2f(1.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector2f(1.0f, 0.0f)),
//			
//			//Left face
//			new Vertex(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector2f(0.0f, 0.0f)),
//			new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector2f(0.0f, 1.0f)),
//			new Vertex(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector2f(1.0f, 1.0f)),
//			new Vertex(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector2f(1.0f, 0.0f)),
//			
//			//Top face
//			new Vertex(new Vector3f(-0.5f,  0.5f,  0.5f), new Vector2f(0.0f, 0.0f)),
//			new Vertex(new Vector3f(-0.5f,  0.5f, -0.5f), new Vector2f(0.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f,  0.5f, -0.5f), new Vector2f(1.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f,  0.5f,  0.5f), new Vector2f(1.0f, 0.0f)),
//			
//			//Bottom face
//			new Vertex(new Vector3f(-0.5f, -0.5f,  0.5f), new Vector2f(0.0f, 0.0f)),
//			new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector2f(0.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f, -0.5f, -0.5f), new Vector2f(1.0f, 1.0f)),
//			new Vertex(new Vector3f( 0.5f, -0.5f,  0.5f), new Vector2f(1.0f, 0.0f)),
//	}, new int[] {
//			//Back face
//			0, 1, 3,	
//			3, 1, 2,	
//			
//			//Front face
//			4, 5, 7,
//			7, 5, 6,
//			
//			//Right face
//			8, 9, 11,
//			11, 9, 10,
//			
//			//Left face
//			12, 13, 15,
//			15, 13, 14,
//			
//			//Top face
//			16, 17, 19,
//			19, 17, 18,
//			
//			//Bottom face
//			20, 21, 23,
//			23, 21, 22
//	        }, new Material("/textures/gamemenu.jpg")
//	    );
	
	
	
	
	
	
	
	
	
	
	
	
	
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
//	public Mesh mesh = ModelLoader.loadModel("resources/models/dragon.obj", "/textures/gamemenu.jpg");
	public Mesh[] meshes = ModelLoader.loadModel("resources/models/model.gltf", "/textures/gamemenu.jpg");
	
//	public GameObject[] objects = new GameObject[500];    // only for testing multi render
	
//	public GameObject object = new GameObject(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), mesh);  // for single texture
	
	public GameObject object = new GameObject(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), meshes);
	
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

		window.setBackGroundColor(1.0f, 0, 0); // sets the background to red for testing 
		window.create(); // creates the window and the OpenGL context
//		mesh.create();  //creates the mesh   // use this for single
		
		for (Mesh m : meshes) m.create();
		shader.create();

		
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
		//System.out.print("update game!");
		window.update(); // polls input/window events and clears the screen (does NOT draw anything)
		//camera.update(); // first person 
		camera.update(object); // third person
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
	
		renderer.renderMesh(object, camera);
		
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
		shader.destroy();

	}
	
	/*
	 * MAIN — program entry point. Creates the Main object and starts the
	 * game thread via start().
	 */
	
	public static void main(String[] arg) {
		new Main().start();
		
	}

}