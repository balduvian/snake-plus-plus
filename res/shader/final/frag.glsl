#version 460 core

uniform sampler2D glow;
uniform sampler2D background;

uniform float time;
uniform vec3 colorPalette[4];

in vec2 uv;
in vec2 worldUV;

out vec4 fragColor;

vec3 palette(float t, vec3 a, vec3 b, vec3 c, vec3 d) {
    return a + b * cos(6.28318 * (c * t + d));
}

vec3 paletteLine(float t) {
    return palette(t, colorPalette[0], colorPalette[1], colorPalette[2], colorPalette[3]);
}

vec3 smartGlow(vec3 color, float width, float scale) {
    if (scale == 1.0) return vec3(1.0, 1.0, 1.0);

    float whiteLimit = 1.0 - (width / (1.0 + width));

    return (
        step(whiteLimit, scale) *
            mix(
                color * ((width / (1.0 - scale)) - width),
                vec3(1.0, 1.0, 1.0),
                (scale - whiteLimit) / (1.0 - whiteLimit)
            )
        ) + (
            step(scale, whiteLimit) * color * ((width / (1.0 - scale)) - width)
        );
}

void main() {
    float colorOffset = worldUV.x / 32.0 + worldUV.y / 32.0;

    vec4 glowSample = texture(glow, uv);
    vec4 backgroundColor = texture(background, uv);

    float edgeMix = glowSample.x;
    float inside = glowSample.y;

    vec4 glowColor = vec4(smartGlow(paletteLine(time * 0.25 + colorOffset), 0.1, edgeMix), 1.0);
    vec4 voidColor = vec4(0.0, 0.0, 0.0, 1.0);

    fragColor = inside * mix(backgroundColor, glowColor, edgeMix) + (1.0 - inside) * mix(voidColor, glowColor, edgeMix);
}
