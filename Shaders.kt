package com.liquid.launcher.glass

/**
 * AGSL shaders (Android 13+).
 *
 * The wallpaper is procedural water (gradient + caustics + touch ripples).
 * Because it is a pure function of position, every glass element re-evaluates
 * the very same function at a refracted position. That gives real lens-like
 * refraction, rim lighting and a little colour dispersion without having to
 * capture the screen behind each element.
 */
object Shaders {

    private const val COMMON = """
uniform float2 uRes;
uniform float uTime;
uniform float3 uRip;

float3 waterBg(float2 p) {
    float2 uv = p / uRes;
    float aspect = uRes.x / uRes.y;
    float2 q = float2(uv.x * aspect, uv.y);

    float3 top = float3(0.03, 0.36, 0.86);
    float3 mid = float3(0.28, 0.30, 0.92);
    float3 bot = float3(0.82, 0.30, 0.74);
    float3 col = mix(top, mid, smoothstep(0.0, 0.55, uv.y));
    col = mix(col, bot, smoothstep(0.45, 1.0, uv.y));

    float t = uTime * 0.35;
    float2 w = q * 3.2;
    float c = 0.0;
    for (int i = 0; i < 3; i++) {
        float fi = float(i) + 1.0;
        w += float2(sin(w.y * 1.2 + t * fi), cos(w.x * 1.1 - t * fi)) * 0.45;
        c += 0.5 + 0.5 * sin(w.x * 1.7 + w.y * 1.3);
    }
    c = c / 3.0;
    col += float3(pow(c, 6.0)) * 0.35;

    float age = uRip.z;
    if (age >= 0.0) {
        float r = length(p - uRip.xy);
        float fade = 1.0 - age / 3.0;
        float d = r - age * 520.0;
        col += float3(exp(-d * d / 900.0) * fade * 0.28);
        col += float3(sin(r * 0.08 - age * 12.0) * exp(-r * 0.004) * fade * 0.05);
    }
    return clamp(col, 0.0, 1.0);
}
"""

    /** Full-screen animated water wallpaper. */
    const val WALLPAPER = COMMON + """
half4 main(float2 fc) {
    return half4(half3(waterBg(fc)), 1.0);
}
"""

    /** Liquid-glass surface: rounded rect with refraction, dispersion and lighting. */
    const val GLASS = COMMON + """
uniform float2 uSize;
uniform float2 uOrigin;
uniform float uRadius;
uniform float uGlass;

float sdRR(float2 p, float2 b, float r) {
    float2 q = abs(p) - b + float2(r);
    return min(max(q.x, q.y), 0.0) + length(max(q, float2(0.0))) - r;
}

half4 main(float2 fc) {
    float2 hs = uSize * 0.5;
    float2 p = fc - hs;
    float rad = min(uRadius, min(hs.x, hs.y));
    float d = sdRR(p, hs, rad);
    if (d > 1.0) return half4(0.0);

    float2 n = float2(
        sdRR(p + float2(1.0, 0.0), hs, rad) - sdRR(p - float2(1.0, 0.0), hs, rad),
        sdRR(p + float2(0.0, 1.0), hs, rad) - sdRR(p - float2(0.0, 1.0), hs, rad));
    n = n / max(length(n), 0.0001);

    float depth = clamp(-d / max(rad, 1.0), 0.0, 1.0);
    float bend = pow(1.0 - depth, 2.2);

    float2 s0 = fc + uOrigin - n * bend * rad * 0.9 * uGlass;
    float3 cR = waterBg(s0 + n * 4.0 * bend);
    float3 cG = waterBg(s0);
    float3 cB = waterBg(s0 - n * 4.0 * bend);
    float3 col = float3(cR.r, cG.g, cB.b);

    col = mix(col, float3(1.0), 0.14) * 1.05;

    float v = fc.y / uSize.y;
    col += float3(0.16 * (1.0 - v) * (1.0 - v));

    float2 L = normalize(float2(-0.6, -0.8));
    float facing = dot(n, L);
    float rim = pow(1.0 - depth, 5.0);
    col += float3(rim * (0.35 + 0.9 * max(facing, 0.0)) + rim * 0.3 * max(-facing, 0.0));

    float inner = smoothstep(0.0, 0.35, 1.0 - depth) * max(-facing, 0.0) * 0.10;
    col -= float3(inner);

    float spec = 1.0 - smoothstep(0.0, 0.25, length(fc / uSize - float2(0.28, 0.20)));
    col += float3(spec * 0.22);

    float a = clamp(0.5 - d, 0.0, 1.0);
    col = clamp(col, 0.0, 1.0);
    return half4(half3(col * a), half(a));
}
"""
}
