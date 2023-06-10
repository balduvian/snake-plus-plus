#version 330 core

uniform vec4 color = vec4(0, 0.6, 0.9, 1);

out vec4 fragColor;

void main() {
	fragColor = color;
}
