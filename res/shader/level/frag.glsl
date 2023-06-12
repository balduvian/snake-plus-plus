#version 460 core

layout (location = 0) uniform sampler2D level;

uniform float time;
uniform vec2 levelSize;
uniform vec3 colorPalette[4];

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
const float POWERUP_LENGTH = 1.0 / 255.0;
const float POWERUP_SPEED = 2.0 / 255.0;
const float POWERUP_LENGTH_DOWN = 3.0 / 255.0;
const float POWERUP_SPEED_DOWN = 4.0 / 255.0;

/* */
vec4 accessGrid(vec2 gridSpace) {
    return texture(level, floor(gridSpace) / levelSize);
}

float edgeValue(vec2 gridSpace, vec2 offset, float dist) {
    float adjacent = abs(accessGrid(gridSpace).y - accessGrid(gridSpace + offset).y);

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

/* */
float isValue(float value, float supposedToBe) {
    return step(supposedToBe - 0.0001, value) * step(value, supposedToBe + 0.0001);
}

vec2 project(vec2 vec, vec2 onto) {
    return onto * (dot(vec, onto) / ((onto.x * onto.x) + (onto.y * onto.y)));
}

float distanceToLine(vec4 line, vec2 pos) {
    float lineDistance = abs((line.z - line.x) * (line.y - pos.y) - (line.x - pos.x) * (line.w - line.y)) /
    sqrt(pow(line.z - line.x, 2.0) + pow(line.w - line.y, 2.0));

    vec2 lineVector = line.zw - line.xy;
    vec2 pointVector = project(pos - line.xy, lineVector);

    float inT = step(0.0, dot(lineVector, pointVector)) * step(length(pointVector), length(lineVector));

    return inT * lineDistance + (1.0 - inT) * min(distance(pos, line.xy), distance(pos, line.zw));
}

float lineValueAware(vec2 point, vec4 line, vec2 offset, float powerup) {
    float hasPowerup = isValue(accessGrid(floor(point + offset)).x, powerup);

    vec2 lineOffset = floor(point + offset) + vec2(0.5, 0.5);

    float d = distanceToLine(line + lineOffset.xyxy, point);

    return hasPowerup * max(1.0 - 2.0 * d, 0.0);
}

float ringValue(vec2 point, vec2 center) {
    return max(1.0 - 2.0 * abs(distance(point, center) - 0.3), 0.0);
}

float ringValueAware(vec2 point, vec2 offset, float powerup) {
    float hasPowerup = isValue(accessGrid(floor(point + offset)).x, powerup);

    return hasPowerup * ringValue(point, floor(point + offset) + vec2(0.5, 0.5));
}

/* powerups */

float closeToLengthPowerup(vec2 point) {
    float value = max(
        lineValueAware(point, vec4(0.0, -0.15, 0.0, 0.15), vec2(0.0, 0.0), POWERUP_LENGTH),
        lineValueAware(point, vec4(-0.15, 0.0, 0.15, 0.0), vec2(0.0, 0.0), POWERUP_LENGTH)
    );

    value = max(value, ringValueAware(point, vec2(0.0, 0.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(-1.0, 0.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(-1.0, 1.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(0.0, 1.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(1.0, 1.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(1.0, 0.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(1.0, -1.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(0.0, -1.0), POWERUP_LENGTH));
    value = max(value, ringValueAware(point, vec2(-1.0, -1.0), POWERUP_LENGTH));

    return value;
}

float closeToSpeedPowerup(vec2 point) {
    float value = max(
        lineValueAware(point, vec4(0.0, -0.15, 0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED),
        lineValueAware(point, vec4(0.075, 0.075, 0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED)
    );
    value = max(value, lineValueAware(point, vec4(-0.075, 0.075, 0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED));

    value = max(value, ringValueAware(point, vec2(0.0, 0.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(-1.0, 0.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(-1.0, 1.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(0.0, 1.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(1.0, 1.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(1.0, 0.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(1.0, -1.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(0.0, -1.0), POWERUP_SPEED));
    value = max(value, ringValueAware(point, vec2(-1.0, -1.0), POWERUP_SPEED));

    return value;
}

float closeToLengthDownPowerup(vec2 point) {
    float value = lineValueAware(point, vec4(-0.15, 0.0, 0.15, 0.0), vec2(0.0, 0.0), POWERUP_LENGTH_DOWN);

    value = max(value, ringValueAware(point, vec2(0.0, 0.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(-1.0, 0.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(-1.0, 1.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(0.0, 1.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(1.0, 1.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(1.0, 0.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(1.0, -1.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(0.0, -1.0), POWERUP_LENGTH_DOWN));
    value = max(value, ringValueAware(point, vec2(-1.0, -1.0), POWERUP_LENGTH_DOWN));

    return value;
}

float closeToSpeedDownPowerup(vec2 point) {
    float value = max(
    lineValueAware(point, vec4(0.0, -0.15, 0.0, 0.15), vec2(0.0, 0.0), POWERUP_SPEED_DOWN),
    lineValueAware(point, vec4(0.075, -0.075, 0.0, -0.15), vec2(0.0, 0.0), POWERUP_SPEED_DOWN)
    );
    value = max(value, lineValueAware(point, vec4(-0.075, -0.075, 0.0, -0.15), vec2(0.0, 0.0), POWERUP_SPEED_DOWN));

    value = max(value, ringValueAware(point, vec2(0.0, 0.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(-1.0, 0.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(-1.0, 1.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(0.0, 1.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(1.0, 1.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(1.0, 0.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(1.0, -1.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(0.0, -1.0), POWERUP_SPEED_DOWN));
    value = max(value, ringValueAware(point, vec2(-1.0, -1.0), POWERUP_SPEED_DOWN));

    return value;
}

/* */

vec4 getFinalColor() {
    float colorOffset = worldUV.x / 32.0 + worldUV.y / 32.0;

    /* inside outside check */
    float inside = accessGrid(floor(worldUV)).y;

    /* edge check */
    float edgeMix = gridDistance(worldUV);

    float ring = 0.0;
    for (float i = 0.0; i < 3.0; ++i) {
        float posNeg = mod(i, 2.0) * 2.0 - 1.0;
        float scale = 0.25 * pow(1.5, i);

        vec2 fractionUV = fract(worldUV * vec2(scale, scale) - vec2(
        (i + sin(time + colorOffset * DISTORT_SCALE + i * 0.5) * posNeg) * i * 0.2,
        (i + cos(time + colorOffset * DISTORT_SCALE + i * 0.5) * posNeg) * i * 0.2
        )) - vec2(0.5, 0.5);

        float centerDist = length(fractionUV);
        ring += (0.005 / abs(sin(centerDist * 8.0 + (time + colorOffset * DISTORT_SCALE) * posNeg) / 8.0)) - (0.005 / 8.0);
    }

    /* powerup check */
    float lengthPowerup = closeToLengthPowerup(worldUV);
    float speedPowerup = closeToSpeedPowerup(worldUV);
    float lengthDownPowerup = closeToLengthDownPowerup(worldUV);
    float speedDownPowerup = closeToSpeedDownPowerup(worldUV);
    float powerupMix = max(max(max(lengthPowerup, speedPowerup), lengthDownPowerup), speedDownPowerup);

    edgeMix = max(edgeMix, powerupMix);

    float edgeHighlight = 0.1 / (1.0 - edgeMix);

    vec4 insideColor = vec4(paletteLine(time * 0.25 + colorOffset) * ring, 1.0);

    vec4 glowColor = vec4(paletteLine(time * 0.25 + colorOffset), 1.0) * edgeHighlight;
    vec4 backgroundColor = vec4(0.0, 0.0, 0.0, 1.0);

    return inside * mix(insideColor, glowColor, edgeMix) + (1.0 - inside) * mix(backgroundColor, glowColor, edgeMix);
}

/* -------------------------------------------------- */

void main() {
    fragColor = getFinalColor();
}