#version 460 core

uniform float time;
uniform vec3 colorPalette[4];

in vec2 worldUV;

layout (location = 1) out vec4 fragColor;

const float DISTORT_SCALE = 8.0;
const float SIZE = 8.0;

vec3 palette(float t, vec3 a, vec3 b, vec3 c, vec3 d) {
    return a + b * cos(6.28318 * (c * t + d));
}

vec3 paletteLine(float t) {
    return palette(t, colorPalette[0], colorPalette[1], colorPalette[2], colorPalette[3]);
}

void main() {
    float offset = worldUV.x / 32.0 + worldUV.y / 32.0;

    vec3 patternGlow = vec3(0.0, 0.0, 0.0);

    for (float i = 0.0; i < 4.0; ++i) {
        float posNeg = mod(i, 2.0) * 2.0 - 1.0;
        float scale = (1.0 / SIZE) * pow(1.5, i);
        float timeScale = 0.25 * pow(0.5, i);

        vec3 currentColor = paletteLine(time * timeScale + (i / 3.0) + offset * scale);

        vec2 fractionUV = fract(worldUV * vec2(scale, scale) - vec2(
            (i + sin(time * timeScale + offset * DISTORT_SCALE + i * 0.5) * posNeg) * i * 0.2,
            (i + cos(time * timeScale + offset * DISTORT_SCALE + i * 0.5) * posNeg) * i * 0.2
        )) - vec2(0.5, 0.5);

        float centerDist = length(fractionUV);

        float circle = abs(sin(centerDist * 8.0 + (time + offset * DISTORT_SCALE) * posNeg) / 8.0);

        patternGlow += currentColor * (0.005 / circle) - 0.01;
    }

    fragColor = vec4(patternGlow, 1.0f);
}
