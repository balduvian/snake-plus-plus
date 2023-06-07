#version 460 core

layout (location = 0) in vec3 vertices;

uniform mat4 mvp;

uniform vec2 cameraDims;
uniform vec2 offset;

out vec2 cameraUV;
out vec2 worldUV;

void main() {
	cameraUV = vertices.xy * cameraDims;
	worldUV = vertices.xy * cameraDims + offset;

	gl_Position = mvp * vec4(vertices, 1.0);
}
