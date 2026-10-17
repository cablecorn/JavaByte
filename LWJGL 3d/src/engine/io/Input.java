package engine.io;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

/*
 * INPUT — tracks keyboard, mouse button, mouse position, and scroll state.
 *
 * GLFW does not let you ask "is this key down right now?" directly. Instead,
 * it calls a callback function whenever something happens. This class
 * creates one callback for each kind of input, and each callback records
 * the latest state in a static field. The rest of the game can then read that
 * state from anywhere with Input.isKeyDown(...), Input.getMouseX(), and so on,
 * without needing an Input object.
 *
 * Setup: Window.create() makes one Input object and registers its four
 * callbacks with GLFW. On shutdown, Window calls destroy() to free them.
 */
public class Input {
	
	/*
	 * STATE ARRAYS — one slot per key and one per mouse button. A slot is true
	 * while that key or button is held down. The arrays are sized with the +1
	 * because GLFW_KEY_LAST and GLFW_MOUSE_BUTTON_LAST are the highest valid
	 * index values, and an array of exactly that size would be one slot too
	 * short and crash the window.
	 */
	private static  boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST + 1]; // true while the key at that index is held down
	private static boolean[] buttons = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1]; // true while the mouse button at that index is held down (left, right, and so on)
	/*
	 * MOUSE STATE — the cursor position in pixels (from the top-left of the
	 * window) and the total scroll offset. Scroll is added up over time,
	 * so it keeps growing until something resets it (see resetScroll below).
	 */
	private static double mouseX, mouseY; 
	private static double scrollX , scrollY ; 
	
	
	/*
	 * CALLBACK OBJECTS — these are the four listeners that GLFW calls. They are
	 * kept in fields so destroy() can free them later.
	 */
	private  GLFWKeyCallback keyboard;
	private  GLFWCursorPosCallback mouseMove;
	private  GLFWMouseButtonCallback mouseButtons;
	private  GLFWScrollCallback mouseScroll;
	
	//===============================================================================================================
	/*
	 * CONSTRUCTOR — creates the four callbacks. Nothing is registered with GLFW
	 * yet; that happens in Window.createCallBacks(), which hands each callback
	 * to GLFW. Each callback only records state, and does no other work.
	 */
	public Input() {		
		/*
		 * KEY CALLBACK — runs when a key is pressed, repeated, or released.
		 * "action" is GLFW_PRESS, GLFW_REPEAT, or GLFW_RELEASE, so the key
		 * counts as down for anything except a release.
		 */
		keyboard = new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) { // records the state of the key that changed
				keys[key] = (action != GLFW.GLFW_RELEASE);
				
			}
			
		};
	
	
		/*
		 * CURSOR CALLBACK — runs whenever the mouse moves, and stores the
		 * new position.
		 */
		mouseMove = new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) { // records the new cursor position
				mouseX = xpos;
				mouseY = ypos;
			}
			
		};
		
		
		/*
		 * MOUSE BUTTON CALLBACK — runs when a mouse button is pressed or
		 * released, and records whether that button is down.
		 */
		mouseButtons = new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) { // records the state of the button that changed
				buttons[button] = (action != GLFW.GLFW_RELEASE);
			}
			
		};
		
		
		
		
		/*
		 * SCROLL CALLBACK — runs when the mouse wheel (or trackpad) scrolls.
		 * The offsets are added to the running totals instead of replacing them.
		 */
		mouseScroll = new GLFWScrollCallback() {
			public void invoke(long window, double offsetx, double offsety) { // adds this scroll movement to the running total
				scrollX += offsetx;
				scrollY += offsety;
				
			};
			
		};
	}
	
	//===============================================================================================================
	
	
	
	
	/*
	 * IS KEY DOWN — returns true while the given key is held. Pass a GLFW key
	 * constant, such as GLFW.GLFW_KEY_ESCAPE.
	 */
	public static boolean isKeyDown(int key) {
		return keys[key];
	}
	//===============================================================================================================
	/*
	 * IS BUTTON DOWN — returns true while the given mouse button is held.
	 * Pass a GLFW mouse button constant, such as GLFW.GLFW_MOUSE_BUTTON_LEFT.
	 */
	public static boolean isButtonDown(int button) {
		return buttons[button];
	}
	//===============================================================================================================

	/*
	 * DESTROY — frees the four callbacks. GLFW callbacks are native resources,
	 * so they have to be released by hand when the window closes.
	 */
	public  void destroy() {
		keyboard.free();
		mouseMove.free();
		mouseButtons.free();
		mouseScroll.free();
	}
	//===============================================================================================================

	/*
	 * SCROLL GETTERS — return the total horizontal and vertical scroll since
	 * the program started (or since the last reset, if one is added).
	 */
	public static double getScrollX() {
		return scrollX;
	}
	//===============================================================================================================
	
	
	public  static double getScrollY() {
		return scrollY;
		
	}
	
	
	
	//===============================================================================================================
	
	/*
	 * MOUSE POSITION GETTERS — return the cursor's last known x and y position
	 * in pixels, measured from the top-left corner of the window.
	 */
	public static double getMouseX() {
		return mouseX;
	}
	//===============================================================================================================
	
	
	public  static double getMouseY() {
		return mouseY;
		
	}
	
	
	
	
	//===============================================================================================================

	/*
	 * CALLBACK GETTERS — give Window access to each callback so it can
	 * register them with GLFW (glfwSetKeyCallback and the matching calls).
	 */
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
	
	/*
	 * RESET SCROLL (commented out, not active) — would set the scroll totals
	 * back to 0. Without a reset, getScrollX/getScrollY return the total
	 * since startup. With it, they could return only this frame's scroll if
	 * it is called once per frame after the scroll values are read. It was
	 * disabled, so for now scroll is a running total.
	 */
//	public static void resetScroll() {
//	    scrollX = 0;
//	    scrollY = 0;
//	}
//	
	
}