package engine.io;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

public class Input {
	
	private static  boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST + 1]; // arrays of keys pressed and use bulit funtion of glfw //adding 1 cuase we get crash of windwo sense the array size is incorrect 
	private static boolean[] buttons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1]; // tracks teh button on mosue like right click //adding 1 cuase we get crash of windwo sense the array size is incorrect 
	private static double mouseX, mouseY; 
	private static double scrollX , scrollY ; 
	
	
	private  GLFWKeyCallback keyboard;
	private  GLFWCursorPosCallback mouseMove;
	private  GLFWMouseButtonCallback mouseButtons;
	private  GLFWScrollCallback mouseScroll;
	
	//===============================================================================================================
	public Input() {		
		keyboard = new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) { // funtion that trakes keyinputs 
				// TODO Auto-generated method stub
				keys[key] = (action != GLFW.GLFW_RELEASE);
				
			}
			
		};
	
	
		mouseMove = new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) { // funtion that trakes keyinputs 
				// TODO Auto-generated method stub
				mouseX = xpos;
				mouseY = ypos;
			}
			
		};
		
		
		mouseButtons = new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) { // funtion that trakes keyinputs 
				// TODO Auto-generated method stub
				buttons[button] = (action != GLFW.GLFW_RELEASE);
			}
			
		};
		
		
		
		
		mouseScroll = new GLFWScrollCallback() {
			public void invoke(long window, double offsetx, double offsety) {
				// TODO Auto-generated method stub
				scrollX += offsetx;
				scrollY += offsety;
				
			};
			
		};
	}
	
	//===============================================================================================================
	
	
	
	
	
	public static boolean isKeyDown(int key) {
		return keys[key];
	}
	//===============================================================================================================
	public static boolean isButtonDown(int button) {
		return buttons[button];
	}
	//===============================================================================================================

	
	public  void destroy() {
		keyboard.free();
		mouseMove.free();
		mouseButtons.free();
		mouseScroll.free();
	}
	//===============================================================================================================

	public static double getScrollX() {
		return scrollX;
	}
	//===============================================================================================================
	
	
	public  static double getScrollY() {
		return scrollY;
		
	}
	
	
	
	//===============================================================================================================
	
	public static double getMouseX() {
		return mouseX;
	}
	//===============================================================================================================
	
	
	public  static double getMouseY() {
		return mouseY;
		
	}
	
	
	
	
	//===============================================================================================================

	public  GLFWKeyCallback getKeyboardCallback() {
		return keyboard;
	}

	//===============================================================================================================
	
	public  GLFWCursorPosCallback getMouseMoveCallback() {
		return mouseMove;
	}
	
	//===============================================================================================================
	
	public  GLFWMouseButtonCallback getMouseButtons() {
		return mouseButtons;
	}
	
	//===============================================================================================================
	public  GLFWScrollCallback getMouseScrollCallback() {
		return mouseScroll;
	}
	
//	public static void resetScroll() {
//	    scrollX = 0;
//	    scrollY = 0;
//	}
//	
	
}
