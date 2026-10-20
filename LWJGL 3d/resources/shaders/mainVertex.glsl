#version 460 core

/*
 * VERTEX SHADER — runs once per vertex. Passes each vertex's clip-space
 * position to the GPU's rasterizer, and forwards the color/texture
 * coordinate attributes through to the fragment shader (where they get
 * interpolated across each triangle's surface).
 */

in vec3 position;        // per-vertex position (x, y, z), from attribute location 0
in vec3 color;            // per-vertex color (r, g, b), from attribute location 1
in vec2 textureCoord;     // per-vertex texture coordinate (u, v), from attribute location 2

out vec3 passColor;           // forwarded to the fragment shader
out vec2 passTextureCoord;    // forwarded to the fragment shader




uniform mat4 model;

uniform mat4 projection;

uniform mat4 view;

void main() {
    // Promote the 3-component position into the 4-component clip-space
    // output OpenGL requires; 1.0 is the standard w component for a
    // regular (non-homogeneous-weighted) vertex.
    
    gl_Position = projection * view *  model * vec4(position, 1.0);

    // Map position [-1, 1] to color [0, 1] for smooth visual gradients
    //vec3 normalizedPos = position * 0.1 + vec3(0.1);

    // Pass the color and texture coordinate through unchanged so the
    // fragment shader can use the interpolated values.
    
    passColor = color;
    passTextureCoord = textureCoord;

}