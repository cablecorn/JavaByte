//#version 460 core

/*
 * FRAGMENT SHADER — runs once per pixel ("fragment") covered by the mesh.
 * Determines the final color of that pixel by sampling the bound texture
 * at the interpolated texture coordinate passed in from the vertex shader.
 */

//in vec3 passColor;          // per-fragment interpolated color (currently unused below)
//in vec2 passTextureCoord;   // per-fragment interpolated texture coordinate (u, v)
//out vec4 outColor;          // final pixel color output
//uniform sampler2D tex;      // the bound texture, set from Java via Renderer/Material


//void main() {
	// Sample the texture at this fragment's UV coordinate and use it
	// directly as the output color.
//	outColor = texture(tex, passTextureCoord);


//}





// multi textuire shader below
#version 460 core

/*
 * FRAGMENT SHADER — runs once per pixel ("fragment") covered by the mesh.
 * Determines the final color of that pixel by sampling the bound texture
 * at the interpolated texture coordinate passed in from the vertex shader.
 */

in vec3 passColor;          // per-fragment interpolated color (currently unused below)
in vec2 passTextureCoord;   // per-fragment interpolated texture coordinate (u, v)
out vec4 outColor;          // final pixel color output
uniform sampler2D tex;      // the bound texture, set from Java via Renderer/Material



void main() {
	vec4 color = texture(tex, passTextureCoord);
	if (color.a < 0.5) discard;
	outColor = color;
}