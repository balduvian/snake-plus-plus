#version 330 core

uniform sampler2D sampler;

uniform vec4 color1 = vec4(0, 1, 1, 1);
uniform vec4 color2 = vec4(0, 0, 1, 1);
uniform float time = 0, freq = 1;
uniform bool rgb = true, loop = false, textured = false;

in vec2 iTexCoords;
in float percent;

out vec4 color;

vec4 hsvToRgb(vec4 hsva);

void main() {
	vec4 tcolor;
	if(textured)
		tcolor = texture(sampler, iTexCoords);
	if(!textured || tcolor.xyz == vec3(1, 1, 1)) {
		float finalPercent = mod((percent + time) * freq, 1);
		if(loop) {
			if(finalPercent > 0.5)
				finalPercent = 1 - finalPercent;
			finalPercent *= 2;		
		}
		vec4 interpolated = color1 + (color2 - color1) * finalPercent;
		if(rgb)
			color = interpolated;
		else
			color = hsvToRgb(interpolated);
		if(textured)
			color.w = texture(sampler, iTexCoords).w;
	}
	else if(tcolor.w != 0)
		color = tcolor;
	else
		discard;
}

vec4 hsvToRgb(vec4 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return vec4(c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y), c.w);
}