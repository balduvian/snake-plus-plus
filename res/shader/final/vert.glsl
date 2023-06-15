#version 460 core

layout (location = 0) in vec3 vertex;
layout (location = 1) in vec2 texCoord;

uniform mat4 mvp;
uniform mat4 model;

out vec2 uv;
out vec2 worldUV;

void main() {
	worldUV = (model * vec4(vertex, 1.0)).xy;
	gl_Position = mvp * vec4(vertex, 1.0);
	uv = texCoord;
}
