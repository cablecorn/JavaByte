package engine.io;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import engine.maths.*;

//import org.joml.Vector3f; // JOML version — uncomment this + comment out the import engine.maths.*; import above to swap



public class Window {
	
	private int width, height; // var need for how window size display
	private String title;
	private long window;
	public static  int frames; // fps counter
	public static long time; // time variable 
	
	public Input input;
	private Vector3f background = new Vector3f(0, 0, 0);
	
	private GLFWWindowSizeCallback sizeCallback;
	
	private boolean Resized;
	
	private boolean fullscrean;
	
	
	private int[] windowPosX = new int [1],  windowPosY = new int [1];
	
	
	
	//---------------------------------------------------------------------------------------------
	
	
	
	
	public Window(int width, int height, String title) { // public method funtion for window 
		this.width = width; //the "this" just tell the languge that use the class itslef correct
		this.height = height;
		this.title = title;
	}
	//================================================================
	public void create() {
		if(!GLFW.glfwInit()) { //this return true or false if it ran
			System.err.println("ERROR: GLFW WASNT INITLAIZED");
			return;
		}
		
		input = new Input();
		
		window = GLFW.glfwCreateWindow(width, height, title, fullscrean ? GLFW.glfwGetPrimaryMonitor() : 0 , 0);
		
		if(window == 0) { //this return true or false if it ran
			System.err.println("ERROR: window is not created ");
			return;
		}
		
		
		GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		windowPosX[0] = (videoMode.width() - width) / 2;
		windowPosY[0] = (videoMode.height() - height) / 2;
		GLFW.glfwSetWindowPos(window, windowPosX[0], windowPosY[0]); // calcualte the windwo
		
		GLFW.glfwMakeContextCurrent(window);
		
		GL.createCapabilities();  // lets LWJGL bind Java calls to this context's OpenGL functions
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		createCallBacks();
		
		GLFW.glfwShowWindow(window);
		
		GLFW.glfwSwapInterval(1); // sets vsyc to true
		time = System.currentTimeMillis(); // get time using systme funtion 
		
	}
	//================================================================
	
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
		GLFW.glfwPollEvents(); // this gets request call form the window 
		
		
		
		frames++;
		if(System.currentTimeMillis() > time + 1000) {
			GLFW.glfwSetWindowTitle(window, title + "| FPS: " + frames);
			time = System.currentTimeMillis(); // cause of the swapbuffer funtion in window this will run either 60fps or 120fps
			frames = 0;
		}
	}
	//================================================================
	public void swapBuffers() {
		GLFW.glfwSwapBuffers(window); // this funtion allwos the render funtion fisnh render before it goes onto another frame of image 
	}
	//================================================================
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(window);
	}
	//================================================================
	public void destory() {
		input.destroy();
		GLFW.glfwWindowShouldClose(window);
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}
	//================================================================
	
	public void setBackGroundColor(float r, float g, float b) {
		background.set(r, g, b);
	}

	//================================================================
	
	
	

	
	
	
	public void setFullscrean(boolean fullscrean) {
	    this.fullscrean = fullscrean;
	    Resized = true;

	    if (fullscrean) {
	        GLFW.glfwGetWindowPos(window, windowPosX, windowPosY); // save windowed position before switching
	        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
	        GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0,
	                vidMode.width(), vidMode.height(), vidMode.refreshRate());
	    } else {
	        GLFW.glfwSetWindowMonitor(window, 0, windowPosX[0], windowPosY[0],
	                width, height, GLFW.GLFW_DONT_CARE);
	    }
	}

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


	
	
	
	
	
	
	
	
	
}
