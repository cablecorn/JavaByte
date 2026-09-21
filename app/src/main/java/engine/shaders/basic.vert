#version 330 core

in vec3 aPosition;
in vec3 vColor;

out vec3 fragColor;

void main() {
     gl_Position = vec4(aPosition, 1.0);
     fragColor = vColor;
}
