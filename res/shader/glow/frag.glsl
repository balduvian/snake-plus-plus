#version 460 core

layout (location = 0) uniform sampler2D level;

uniform float time;
uniform vec2 levelSize;
uniform bool wrap;

in vec2 worldUV;

layout (location = 0) out vec4 fragColor;

/* -------------------------------------------------- */

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

void main() {
    float colorOffset = worldUV.x / 32.0 + worldUV.y / 32.0;

    /* inside outside check */
    float inside = accessGrid(worldUV).y / 255.0;

    /* edge check */
    float edgeMix = gridDistance(worldUV);

    /* powerup check */
    float distanceToPowerup = min(distanceToLengthPowerup(worldUV), distanceToSpeedPowerup(worldUV));
    distanceToPowerup = min(distanceToPowerup, distanceToLengthDownPowerup(worldUV));
    distanceToPowerup = min(distanceToPowerup, distanceToSpeedDownPowerup(worldUV));

    float powerupMix = max(1.0 - 2.0 * distanceToPowerup, 0.0);

    edgeMix = max(edgeMix, powerupMix);

    fragColor = vec4(edgeMix, inside, 0.0, 1.0);
}
