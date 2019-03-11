#version 330 core

uniform vec4 iColor;

uniform sampler2D sampler;

in vec2 texCoords;

out vec4 color;

void main() {
	color = iColor * texture(sampler, texCoords);
	if(color.a == 0)
		discard;
}