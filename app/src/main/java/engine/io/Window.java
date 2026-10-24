package engine.io;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import engine.maths.Matrix4f;
import engine.maths.Vector3f;

//import org.joml.Vector3f; // JOML version — uncomment this + comment out the import engine.maths.Vector3f; import above to swap
// (The old note said to comment out "import engine.maths.*;". The import is now written
// for the specific classes, so the line to swap is the Vector3f one.)


/*
 * WINDOW — creates and manages the game window and its OpenGL context.
 *
 * Responsibilities:
 *  - Opens the GLFW window, centers it, and sets up OpenGL (depth testing
 *    and blending for transparency).
 *  - Registers the input callbacks from the Input class.
 *  - Clears the screen each frame, handles resizing, and shows the FPS in
 *    the title bar.
 *  - Swaps the front and back buffers so each finished frame appears.
 *  - Toggles fullscreen, locks or unlocks the mouse cursor, and holds the
 *    projection matrix that the Renderer uses.
 *
 * Usage order: new Window(...) -> setBackGroundColor(...) -> create() ->
 * each frame: update(), draw things, swapBuffers() -> destory() on shutdown.
 */
public class Window {
	
	private int width, height; // the window size in pixels
	private String title;
	private long window; // the GLFW window handle (0 means no window)
	public static  int frames; // frames drawn since the last FPS update
	public static long time; // time of the last FPS update, in milliseconds
	
	public Input input;
	private Vector3f background = new Vector3f(0, 0, 0); // the clear color (r, g, b)
	
	private GLFWWindowSizeCallback sizeCallback;
	
	private boolean Resized; // set to true by the size callback, then handled in update()
	
	private boolean fullscrean; // true while the window is fullscreen
	
	
	private int[] windowPosX = new int [1],  windowPosY = new int [1]; // saved window position, restored when leaving fullscreen
	
	private Matrix4f projection; // turns 3D camera space into 2D screen space (see the constructor)
	
	
	//---------------------------------------------------------------------------------------------
	
	
	
	
	/*
	 * CONSTRUCTOR — stores the window size and title, and builds the
	 * projection matrix. No window is opened here; that happens in create().
	 *
	 * The projection matrix is a perspective projection: 70 degree field of
	 * view, the width/height aspect ratio, a near plane at 0.1, and a far
	 * plane at 1000. Things closer than 0.1 or farther than 1000 are not
	 * drawn. It is built once here, from the starting window size.
	 */
	public Window(int width, int height, String title) { // sets up the window settings
		this.width = width; // "this" means the field of this object, not the parameter
		this.height = height;
		this.title = title;
		projection = Matrix4f.projection(70.0f, (float) width/ (float) height, 0.1f, 1000.0f);
	}
	//================================================================
	/*
	 * CREATE — opens the window and prepares OpenGL.
	 *
	 * Steps:
	 *  1. Start GLFW. If it fails, print an error and stop.
	 *  2. Create the Input object that will track keys and the mouse.
	 *  3. Create the window (on the primary monitor if fullscreen is on).
	 *     If that fails, print an error and stop.
	 *  4. Center the window on screen using the monitor's size.
	 *  5. Make this window's OpenGL context the current one, then call
	 *     GL.createCapabilities() so LWJGL can find the OpenGL functions.
	 *  6. Turn on depth testing (nearer surfaces hide farther ones) and
	 *     blending (needed for the see-through glass). The blend function
	 *     makes a pixel's alpha control how much it covers what is behind it.
	 *  7. Register the input callbacks, show the window, and turn on vsync
	 *     (a swap interval of 1 matches the frame rate to the monitor).
	 *
	 * Note: step 5 and the depth-test line appear twice in the code below.
	 * The second copy does the same thing again and also adds the blending
	 * lines, so the first copy is redundant.
	 */
	public void create() {
		if(!GLFW.glfwInit()) { // returns true if GLFW started, false if it failed
			System.err.println("ERROR: GLFW WASNT INITLAIZED");
			return;
		}
		
		input = new Input();
		
		window = GLFW.glfwCreateWindow(width, height, title, fullscrean ? GLFW.glfwGetPrimaryMonitor() : 0 , 0);
		
		if(window == 0) { // a handle of 0 means the window was not created
			System.err.println("ERROR: window is not created ");
			return;
		}
		
		
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		windowPosX[0] = (videoMode.width() - width) / 2;
		windowPosY[0] = (videoMode.height() - height) / 2;
		GLFW.glfwSetWindowPos(window, windowPosX[0], windowPosY[0]); // places the window in the center of the screen
		
		GLFW.glfwMakeContextCurrent(window);
		
		GL.createCapabilities();  // lets LWJGL bind Java calls to this context's OpenGL functions
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		
		
		GL.createCapabilities();  // lets LWJGL bind Java calls to this context's OpenGL functions
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		
		
		createCallBacks();
		
		GLFW.glfwShowWindow(window);
		
		GLFW.glfwSwapInterval(1); // turns on vsync
		time = System.currentTimeMillis(); // start time for the FPS counter
		
	}
	//================================================================
	
	/*
	 * CREATE CALLBACKS — registers the listeners GLFW calls for events.
	 * It builds a resize callback (which records the new size and flags that
	 * a resize happened) and then hands GLFW the keyboard, cursor, mouse
	 * button, scroll, and resize callbacks.
	 */
	private void createCallBacks() {
	    sizeCallback = new GLFWWindowSizeCallback() {
	        public void invoke(long window, int w, int h) {
	            width = w;
	            height = h;
	            Resized= true;
	        }
	    };  
	    
	    GLFW.glfwSetKeyCallback(window, input.getKeyboardCallback());
	    GLFW.glfwSetCursorPosCallback(window, input.getMouseMoveCallback());
	    GLFW.glfwSetMouseButtonCallback(window, input.getMouseButtons());
	    GLFW.glfwSetScrollCallback(window, input.getMouseScrollCallback());
	    GLFW.glfwSetWindowSizeCallback(window, sizeCallback);

	}  
	
	//===================================================
	
	/*
	 * UPDATE — runs once per frame, at the start of the frame.
	 *
	 * Steps:
	 *  1. If the window was resized, update the OpenGL viewport (the area of
	 *     the window that gets drawn into) to the new size.
	 *  2. Set the clear color from the background field and clear the color
	 *     and depth buffers, which wipes the previous frame.
	 *  3. Poll GLFW so waiting events (keys, mouse, resize) are delivered
	 *     to the callbacks.
	 *  4. Count frames, and once a second show the FPS count in the title.
	 *
	 * The commented-out Input.resetScroll() line would clear the scroll total
	 * once per frame, so scroll would only report this frame's movement.
	 */
	public void update() {
		
//		 Input.resetScroll();   // clear it AFTER polling, so this frame's scroll value survives to be read
		
		if(Resized) {
			GL11.glViewport(0, 0, width, height);
			Resized = false ;
		}
		
		GL11.glClearColor( background.getX(), background.getY(), background.getZ(), 1.0f);
		
		//=======================================================================================================================================================================================
		// JOML version (JOML uses public fields, not getters) — uncomment once swapped:
		// GL11.glClearColor( background.x, background.y, background.z, 1.0f);
		
		//=======================================================================================================================================================================================

		
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
		GLFW.glfwPollEvents(); // delivers waiting window and input events to the callbacks
		
		
		
		frames++;
		if(System.currentTimeMillis() > time + 1000) {
			GLFW.glfwSetWindowTitle(window, title + "| FPS: " + frames);
			time = System.currentTimeMillis(); // vsync from swapBuffers limits this to the monitor's refresh rate (60 or 120 fps, for example)
			frames = 0;
		}
	}
	//================================================================
	/*
	 * SWAP BUFFERS — shows the finished frame. The game draws into a hidden
	 * back buffer, and this swaps it with the visible front buffer, so the
	 * player never sees a half-drawn frame.
	 */
	public void swapBuffers() {
		GLFW.glfwSwapBuffers(window);
	}
	//================================================================
	/*
	 * SHOULD CLOSE — returns true once the user has asked to close the window
	 * (for example by clicking the X button). The game loop checks this.
	 */
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(window);
	}
	//================================================================
	/*
	 * DESTORY — shuts the window down: frees the input callbacks, destroys
	 * the GLFW window, and terminates GLFW. The method name is spelled
	 * "destory" in the code, so the rest of the project calls it by that name.
	 */
	public void destory() {
		input.destroy();
		GLFW.glfwWindowShouldClose(window);
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}
	//================================================================
	
	/*
	 * SET BACKGROUND COLOR — sets the color the screen is cleared to each
	 * frame. Each value runs from 0.0 to 1.0, for example (1, 0, 0) is red.
	 */
	public void setBackGroundColor(float r, float g, float b) {
		background.set(r, g, b);
	}

	//================================================================
	
	
	

	
	
	/*
	 * SET FULLSCREEN — switches between fullscreen and windowed mode.
	 *
	 * Going fullscreen: saves the current window position, then moves the
	 * window onto the primary monitor at the monitor's full size and refresh
	 * rate. Going back: restores the saved position and the window size.
	 * It also flags a resize so the viewport is updated on the next update().
	 */
	public void setFullscrean(boolean fullscrean) {
	    this.fullscrean = fullscrean;
	    Resized = true;

	    if (fullscrean) {
	        GLFW.glfwGetWindowPos(window, windowPosX, windowPosY); // save the windowed position before switching
	        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
	        GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0,
	                vidMode.width(), vidMode.height(), vidMode.refreshRate());
	    } else {
	        GLFW.glfwSetWindowMonitor(window, 0, windowPosX[0], windowPosY[0],
	                width, height, GLFW.GLFW_DONT_CARE);
	    }
	}

	
	
	/*
	 * MOUSE STATE — locks or unlocks the mouse cursor. When locked, the
	 * cursor is hidden and held in place, which is what a camera that turns
	 * with the mouse needs. When unlocked, the cursor works normally.
	 */
	public void mouseState(boolean lock) {
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, lock ? GLFW.GLFW_CURSOR_DISABLED :  GLFW.GLFW_CURSOR_NORMAL );
	}
	
	
	/*
	 * GETTERS — return the window's width, height, title, GLFW handle,
	 * fullscreen state, and projection matrix. The Renderer uses
	 * getProjectionMatrix() every frame.
	 */
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public String getTitle() {
		return title;
	}

	public long getWindow() {
		return window;
	}

	public boolean fullscrean() {
	    return fullscrean;
	}
	public Matrix4f getProjectionMatrix() {
		return projection;
	}


	
	
	
	
	
	
	
	
	
	
}