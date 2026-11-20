#version 460 core

// HUD VERTEX SHADER - same as mainVertex.glsl but with no view/projection,
// so the model matrix places the quad directly on the screen (-1 to 1).

in vec3 position;
in vec3 color;
in vec2 textureCoord;

out vec3 passColor;
out vec2 passTextureCoord;

uniform mat4 model;

void main() {
    gl_Position = model * vec4(position, 1.0);
    passColor = color;
    passTextureCoord = textureCoord;
}
