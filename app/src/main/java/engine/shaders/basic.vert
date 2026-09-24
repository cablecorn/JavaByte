#version 330 core

in vec3 aPosition;
in vec3 vColor;
uniform vec2 offset;

out vec3 fragColor;

void main() {
     vec3 finalPosition = vec3(offset.x + aPosition.x, offset.y + aPosition.y, aPosition.z);
     gl_Position = vec4(finalPosition, 1.0);
     fragColor = vColor;
}
