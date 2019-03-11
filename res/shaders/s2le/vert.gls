#version 330 core

uniform mat4 mvp;

layout (location = 0) in vec3 vertices;
layout (location = 1) in vec2 texCoords;

out vec2 iTexCoords;
out float percent;

void main() {
	iTexCoords = texCoords;
	vec4 position = mvp * vec4(vertices, 1);
	percent = (position.x + 1) / 4 - (position.y - 1) / 4;
	gl_Position = position;
}