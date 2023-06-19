#version 460 core

uniform float time;
uniform vec3 colorPalette[4];

in vec2 worldUV;

layout (location = 1) out vec4 fragColor;

void main() {
    fragColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
}
