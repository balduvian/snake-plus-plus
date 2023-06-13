#version 450 core

layout (location = 0) uniform sampler2D level;

uniform float time;
uniform vec2 levelSize;
uniform vec3 colorPalette[4];
uniform bool wrap;

in vec2 worldUV;

out vec4 fragColor;

/* -------------------------------------------------- */

vec3 palette(float t, vec3 a, vec3 b, vec3 c, vec3 d) {
    return a + b * cos(6.28318 * (c * t + d));
}

vec3 paletteLine(float t) {
    return palette(t, colorPalette[0], colorPalette[1], colorPalette[2], colorPalette[3]);
}

float fadeDim(float a, float border, float min, float max) {
    float left = 1.00 - pow(1.0 / border, 2.0) * pow(a - (min + border), 2.0);
    float right = 1.00 - pow(1.0 / border, 2.0) * pow(a - (max - border), 2.0);

    return (step(a, border + min) * left) +
    (step(border + min, a) * step(a, max - border) * 1.0) +
    (step(max - border, a) * right);
}

const float DISTORT_SCALE = 8.0;

const int POWERUP_LENGTH = 1;
const int POWERUP_SPEED = 2;
const int POWERUP_LENGTH_DOWN = 3;
const int POWERUP_SPEED_DOWN = 4;

/* */

ivec4 accessGrid(vec2 gridSpace) {
   if (wrap) {
       return ivec4(texelFetch(level, ivec2(mod(floor(gridSpace), levelSize)), 0) * vec4(255.0, 255.0, 255.0, 255.0));
   } else {
       vec2 bounds = step(vec2(0.0, 0.0), floor(gridSpace)) * step(floor(gridSpace), levelSize - vec2(1.0, 1.0));
       float inside = bounds.x * bounds.y;

       return ivec4(
           (
               inside * texelFetch(level, ivec2(floor(gridSpace)), 0) +
               (1.0 - inside) * vec4(0.0, 1.0, 0.0, 1.0)
           ) * vec4(255.0, 255.0, 255.0, 255.0)
       );
   }
}

float edgeValue(vec2 gridSpace, vec2 offset, float dist) {
    float adjacent = abs(accessGrid(gridSpace).y / 255.0 - accessGrid(gridSpace + offset).y / 255.0);

    float close = 1.0 - 2.0 * dist;
    return mix(0.0, close, adjacent);
}

float gridDistance(vec2 point) {
    vec2 gridSpace = floor(point);

    float vLeft = edgeValue(gridSpace, vec2(-1.0, 0.0), point.x - floor(point.x));
    float vRight = edgeValue(gridSpace, vec2(1.0, 0.0), floor(point.x + 1.0) - point.x);
    float vDown = edgeValue(gridSpace, vec2(0.0, -1.0), point.y - floor(point.y));
    float vUp = edgeValue(gridSpace, vec2(0.0, 1.0), floor(point.y + 1.0) - point.y);

    float vLeftDown = edgeValue(gridSpace, vec2(-1.0, -1.0), distance(point, floor(point)));
    float vRightDown = edgeValue(gridSpace, vec2(1.0, -1.0), distance(point, floor(point + vec2(1.0, 0.0))));
    float vLeftUp = edgeValue(gridSpace, vec2(-1.0, 1.0), distance(point, floor(point + vec2(0.0, 1.0))));
    float vRightUp = edgeValue(gridSpace, vec2(1.0, 1.0), distance(point, floor(point + vec2(1.0, 1.0))));

    float value = max(vLeft, vRight);
    value = max(value, vLeftDown);
    value = max(value, vRightDown);
    value = max(value, vLeftUp);
    value = max(value, vRightUp);
    value = max(value, vUp);
    value = max(value, vDown);
    return value;
}
/* */

/* powerup primitives */

float isValue(int value, int supposedToBe) {
    return value == supposedToBe ? 1.0 : 0.0;
}

float distanceToLine(vec2 point, vec2 start, vec2 end) {
    vec2 pa = point - start, ba = end - start;
    float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
    return length(pa - ba * h);
}

float distanceToRing(vec2 point, vec2 center, float radius) {
    return abs(distance(point, center) - radius);
}

float distanceToLineAware(vec2 point, vec2 start, vec2 end, vec2 offset, int powerup) {
    float hasPowerup = isValue(accessGrid(point + offset).x, powerup);

    vec2 lineOffset = floor(point + offset) + vec2(0.5, 0.5);

    return (1.0 - hasPowerup) * 800.0 + distanceToLine(point, start + lineOffset, end + lineOffset);
}

float distanceToRingAware(vec2 point, vec2 offset, int powerup) {
    float hasPowerup = isValue(accessGrid(point + offset).x, powerup);

    return (1.0 - hasPowerup) * 800.0 + distanceToRing(point, floor(point + offset) + vec2(0.5, 0.5), 0.3);
}

/* powerups */

float distanceToLengthPowerup(vec2 point) {
    float d = min(
        distanceToLineAware(point, vec2(0.0, -0.15), vec2(0.0, 0.15), vec2(0.0, 0.0), POWERUP_LENGTH),
        distanceToLineAware(point, vec2(-0.15, 0.0), vec2(0.15, 0.0), vec2(0.0, 0.0), POWERUP_LENGTH)
    );

    d = min(d, distanceToRingAware(point, vec2(0.0, 0.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 0.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 1.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(0.0, 1.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(1.0, 1.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(1.0, 0.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(1.0, -1.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(0.0, -1.0), POWERUP_LENGTH));
    d = min(d, distanceToRingAware(point, vec2(-1.0, -1.0), POWERUP_LENGTH));

    return d;
}

float distanceToSpeedPowerup(vec2 point) {
    float d = min(
        distanceToLineAware(point, vec2(0.0, -0.15), vec2(0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED),
        distanceToLineAware(point, vec2(0.075, 0.075), vec2(0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED)
    );
    d = min(d, distanceToLineAware(point, vec2(-0.075, 0.075), vec2(0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED));

    d = min(d, distanceToRingAware(point, vec2(0.0, 0.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 0.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 1.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(0.0, 1.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(1.0, 1.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(1.0, 0.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(1.0, -1.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(0.0, -1.0), POWERUP_SPEED));
    d = min(d, distanceToRingAware(point, vec2(-1.0, -1.0), POWERUP_SPEED));

    return d;
}

float distanceToLengthDownPowerup(vec2 point) {
    float d = distanceToLineAware(point, vec2(-0.15, 0.0), vec2(0.15, 0.0), vec2(0.0, 0.0), POWERUP_LENGTH_DOWN);

    d = min(d, distanceToRingAware(point, vec2(0.0, 0.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 0.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 1.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(0.0, 1.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(1.0, 1.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(1.0, 0.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(1.0, -1.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(0.0, -1.0), POWERUP_LENGTH_DOWN));
    d = min(d, distanceToRingAware(point, vec2(-1.0, -1.0), POWERUP_LENGTH_DOWN));

    return d;
}

float distanceToSpeedDownPowerup(vec2 point) {
    float d = min(
        distanceToLineAware(point, vec2(0.0, -0.15), vec2(0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED_DOWN),
        distanceToLineAware(point, vec2(0.075, -0.075), vec2(0.0, -0.15), vec2(0.0, 0.0), POWERUP_SPEED_DOWN)
    );
    d = min(d, distanceToLineAware(point, vec2(-0.075, -0.075), vec2(0.0, -0.15), vec2(0.0, 0.0), POWERUP_SPEED_DOWN));

    d = min(d, distanceToRingAware(point, vec2(0.0, 0.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 0.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(-1.0, 1.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(0.0, 1.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(1.0, 1.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(1.0, 0.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(1.0, -1.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(0.0, -1.0), POWERUP_SPEED_DOWN));
    d = min(d, distanceToRingAware(point, vec2(-1.0, -1.0), POWERUP_SPEED_DOWN));

    return d;
}

/* */

vec4 getFinalColor() {
    float colorOffset = worldUV.x / 32.0 + worldUV.y / 32.0;

    /* inside outside check */
    float inside = accessGrid(worldUV).y / 255.0;

    /* edge check */
    float edgeMix = gridDistance(worldUV);

    float patternMix = 0.0;
    for (float i = 0.0; i < 3.0; ++i) {
        float posNeg = mod(i, 2.0) * 2.0 - 1.0;
        float scale = 0.25 * pow(1.5, i);

        vec2 fractionUV = fract(worldUV * vec2(scale, scale) - vec2(
            (i + sin(time + colorOffset * DISTORT_SCALE + i * 0.5) * posNeg) * i * 0.2,
            (i + cos(time + colorOffset * DISTORT_SCALE + i * 0.5) * posNeg) * i * 0.2
        )) - vec2(0.5, 0.5);

        float centerDist = length(fractionUV);
        patternMix += (0.005 / abs(sin(centerDist * 8.0 + (time + colorOffset * DISTORT_SCALE) * posNeg) / 8.0)) - (0.005 / 8.0);
    }

    /* powerup check */
    float distanceToPowerup = min(distanceToLengthPowerup(worldUV), distanceToSpeedPowerup(worldUV));
    distanceToPowerup = min(distanceToPowerup, distanceToLengthDownPowerup(worldUV));
    distanceToPowerup = min(distanceToPowerup, distanceToSpeedDownPowerup(worldUV));

    float powerupMix = max(1.0 - 2.0 * distanceToPowerup, 0.0);

    edgeMix = max(edgeMix, powerupMix);

    float edgeHighlight = 0.1 / (1.0 - edgeMix);

    vec4 insideColor = vec4(paletteLine(time * 0.25 + colorOffset) * patternMix, 1.0);

    vec4 glowColor = vec4(paletteLine(time * 0.25 + colorOffset), 1.0) * edgeHighlight;
    vec4 backgroundColor = vec4(0.0, 0.0, 0.0, 1.0);

    return inside * mix(insideColor, glowColor, edgeMix) + (1.0 - inside) * mix(backgroundColor, glowColor, edgeMix);
}

/* -------------------------------------------------- */

void main() {
    fragColor = getFinalColor();
}