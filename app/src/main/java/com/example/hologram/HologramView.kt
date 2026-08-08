package com.example.hologram

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.*
import kotlin.random.Random

private class HologramParticle(
    var x: Float,
    var y: Float,
    var z: Float, // Depth: 0.2 to 2.2
    var vx: Float,
    var vy: Float,
    var size: Float,
    var alpha: Float
)

// 3D Point for Polyhedral Core Wireframe
private data class Point3D(val x: Float, val y: Float, val z: Float)

@Composable
fun HologramCoreView(
    state: HologramCoreState,
    audioAmplitude: Float = 0f, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    var parallaxX by remember { mutableStateOf(0f) }
    var parallaxY by remember { mutableStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "hologram")

    // Rotation phase for 3D core & orbital rings
    val rotationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    HologramCoreState.THINKING -> 2500
                    HologramCoreState.SPEAKING -> 4000
                    HologramCoreState.ACTION -> 3000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Secondary counter-rotation phase
    val counterRotationPhase by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_rotation"
    )

    // Core energy breathing / pulse
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    HologramCoreState.LISTENING -> 500
                    HologramCoreState.SPEAKING -> 350
                    else -> 1800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Bloom light scatter oscillation phase
    val bloomPhase by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bloom_phase"
    )

    // Particles field
    val particles = remember {
        List(48) {
            HologramParticle(
                x = Random.nextFloat() * 600f - 300f,
                y = Random.nextFloat() * 600f - 300f,
                z = Random.nextFloat() * 1.8f + 0.2f,
                vx = (Random.nextFloat() - 0.5f) * 1.8f,
                vy = (Random.nextFloat() - 0.5f) * 1.8f,
                size = Random.nextFloat() * 4.5f + 1.5f,
                alpha = Random.nextFloat() * 0.7f + 0.2f
            )
        }
    }

    // 3D Icosahedron Base Vertices for Core
    val baseIcosahedron = remember {
        val phi = (1f + sqrt(5f)) / 2f
        val raw = listOf(
            Point3D(-1f, phi, 0f), Point3D(1f, phi, 0f), Point3D(-1f, -phi, 0f), Point3D(1f, -phi, 0f),
            Point3D(0f, -1f, phi), Point3D(0f, 1f, phi), Point3D(0f, -1f, -phi), Point3D(0f, 1f, -phi),
            Point3D(phi, 0f, -1f), Point3D(phi, 0f, 1f), Point3D(-phi, 0f, -1f), Point3D(-phi, 0f, 1f)
        )
        // Normalize vertices to unit sphere
        raw.map { p ->
            val len = sqrt(p.x * p.x + p.y * p.y + p.z * p.z)
            Point3D(p.x / len, p.y / len, p.z / len)
        }
    }

    // Icosahedron Edges (30 pairs of vertex indices)
    val icosahedronEdges = remember {
        val edges = mutableListOf<Pair<Int, Int>>()
        val threshold = 1.1f // Unit distance threshold between adjacent vertices in normalized icosahedron
        for (i in baseIcosahedron.indices) {
            for (j in i + 1 until baseIcosahedron.size) {
                val dx = baseIcosahedron[i].x - baseIcosahedron[j].x
                val dy = baseIcosahedron[i].y - baseIcosahedron[j].y
                val dz = baseIcosahedron[i].z - baseIcosahedron[j].z
                val dist = sqrt(dx * dx + dy * dy + dz * dz)
                if (dist < threshold) {
                    edges.add(Pair(i, j))
                }
            }
        }
        edges
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(360.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        parallaxX = (parallaxX + dragAmount.x * 0.25f).coerceIn(-50f, 50f)
                        parallaxY = (parallaxY + dragAmount.y * 0.25f).coerceIn(-50f, 50f)
                    },
                    onDragEnd = {
                        parallaxX = 0f
                        parallaxY = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f + parallaxX, size.height / 2f + parallaxY)
            val baseRadius = min(size.width, size.height) * 0.24f
            val dynamicAmp = if (audioAmplitude > 0.05f) audioAmplitude else 0.12f

            // =========================================================================
            // LAYER 0: POST-PROCESSING MULTI-PASS BLOOM & LIGHT SCATTERING LAYER
            // =========================================================================

            // 0a. Deep Atmospheric Background Diffusion Halo
            val outerGlowRadius = baseRadius * 3.0f * bloomPhase
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        state.primaryColor.copy(alpha = 0.45f),
                        state.secondaryColor.copy(alpha = 0.22f),
                        state.primaryColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = outerGlowRadius
                ),
                radius = outerGlowRadius,
                center = center,
                blendMode = BlendMode.Screen
            )

            // 0b. Secondary Reactive Audio Bloom Core
            val reactiveBloomRadius = baseRadius * (1.8f + dynamicAmp * 0.9f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.50f),
                        state.primaryColor.copy(alpha = 0.65f),
                        state.secondaryColor.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = reactiveBloomRadius
                ),
                radius = reactiveBloomRadius,
                center = center,
                blendMode = BlendMode.Plus
            )

            // 0c. Volumetric Radial Light Rays (Light Scattering / Optical Sunburst Bloom)
            val numLightRays = 16
            rotate(rotationPhase * 0.3f, center) {
                for (i in 0 until numLightRays) {
                    val angleRad = (i.toFloat() / numLightRays) * 2 * PI
                    val rayLength = baseRadius * (2.2f + sin(angleRad * 3 + rotationPhase * 0.05).toFloat() * 0.5f + dynamicAmp * 0.8f)
                    val rayWidthRad = 0.08f + (dynamicAmp * 0.04f)

                    val rayPath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(
                            center.x + (rayLength * cos(angleRad - rayWidthRad)).toFloat(),
                            center.y + (rayLength * sin(angleRad - rayWidthRad)).toFloat()
                        )
                        lineTo(
                            center.x + (rayLength * cos(angleRad + rayWidthRad)).toFloat(),
                            center.y + (rayLength * sin(angleRad + rayWidthRad)).toFloat()
                        )
                        close()
                    }

                    drawPath(
                        path = rayPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                state.primaryColor.copy(alpha = 0.35f + dynamicAmp * 0.2f),
                                state.secondaryColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = rayLength
                        ),
                        blendMode = BlendMode.Screen
                    )
                }
            }

            // 0d. Chromatic Aberration & Refraction Halos (Offset Cyan/Magenta Halo Dispersion)
            val aberrationOffset = 4.dp.toPx()
            drawCircle(
                color = Color(0xFF00FFFF).copy(alpha = 0.25f),
                radius = baseRadius * 1.35f * pulseScale,
                center = Offset(center.x - aberrationOffset, center.y - aberrationOffset),
                style = Stroke(width = 3.dp.toPx()),
                blendMode = BlendMode.Screen
            )
            drawCircle(
                color = Color(0xFFFF00FF).copy(alpha = 0.22f),
                radius = baseRadius * 1.35f * pulseScale,
                center = Offset(center.x + aberrationOffset, center.y + aberrationOffset),
                style = Stroke(width = 3.dp.toPx()),
                blendMode = BlendMode.Screen
            )

            // =========================================================================
            // LAYER 1: 3D DEPTH PARTICLE CONSTELLATION FIELD
            // =========================================================================
            particles.forEach { p ->
                if (state == HologramCoreState.LISTENING) {
                    // Pull inward
                    val dx = center.x - (center.x + p.x)
                    val dy = center.y - (center.y + p.y)
                    p.x += dx * 0.04f
                    p.y += dy * 0.04f
                    if (abs(p.x) < 12f && abs(p.y) < 12f) {
                        p.x = Random.nextFloat() * 540f - 270f
                        p.y = Random.nextFloat() * 540f - 270f
                    }
                } else {
                    p.x += p.vx
                    p.y += p.vy
                    if (abs(p.x) > 280f) p.vx *= -1f
                    if (abs(p.y) > 280f) p.vy *= -1f
                }

                val pOffset = Offset(center.x + p.x * p.z, center.y + p.y * p.z)
                val effectiveRadius = p.size * p.z

                // Particle bloom glow
                drawCircle(
                    color = state.primaryColor.copy(alpha = (p.alpha * p.z * 0.8f).coerceIn(0.1f, 0.95f)),
                    radius = effectiveRadius,
                    center = pOffset
                )
            }

            // Neural constellation connection threads
            for (i in particles.indices) {
                for (j in i + 1 until min(particles.size, i + 5)) {
                    val p1 = particles[i]
                    val p2 = particles[j]
                    val o1 = Offset(center.x + p1.x * p1.z, center.y + p1.y * p1.z)
                    val o2 = Offset(center.x + p2.x * p2.z, center.y + p2.y * p2.z)
                    val dist = (o1 - o2).getDistance()

                    if (dist < 110f) {
                        val lineAlpha = (1f - dist / 110f) * 0.28f
                        drawLine(
                            color = state.primaryColor.copy(alpha = lineAlpha),
                            start = o1,
                            end = o2,
                            strokeWidth = 1.2f
                        )
                    }
                }
            }

            // =========================================================================
            // LAYER 2: OUTER & MIDDLE TELEMETRY ORBITAL RINGS
            // =========================================================================

            // Outer Dashed Ring 1 (Clockwise)
            rotate(rotationPhase, center) {
                drawCircle(
                    color = state.primaryColor.copy(alpha = 0.65f),
                    radius = baseRadius * 1.6f,
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(35f, 18f, 12f, 18f), 0f)
                    )
                )

                // Precision Telemetry Ticks
                for (angle in 0 until 360 step 20) {
                    val rad = Math.toRadians(angle.toDouble())
                    val innerR = baseRadius * 1.6f
                    val outerR = baseRadius * 1.7f
                    val start = Offset(
                        center.x + (innerR * cos(rad)).toFloat(),
                        center.y + (innerR * sin(rad)).toFloat()
                    )
                    val end = Offset(
                        center.x + (outerR * cos(rad)).toFloat(),
                        center.y + (outerR * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = if (angle % 60 == 0) state.secondaryColor else state.primaryColor.copy(alpha = 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = if (angle % 60 == 0) 2.5f else 1.2f
                    )
                }
            }

            // Middle Dashed Ring 2 (Counter-Clockwise)
            rotate(counterRotationPhase * 0.8f, center) {
                drawCircle(
                    color = state.secondaryColor.copy(alpha = 0.55f),
                    radius = baseRadius * 1.35f,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(70f, 25f, 20f, 25f), 0f)
                    )
                )
            }

            // =========================================================================
            // LAYER 3: DYNAMIC AUDIO WAVEFORM RING
            // =========================================================================
            val waveSegments = 40
            val waveRadiusBase = baseRadius * 1.18f
            val wavePath = Path()

            for (i in 0..waveSegments) {
                val angle = (i.toFloat() / waveSegments) * 2 * PI
                val waveNoise = sin(angle * 7 + rotationPhase * 0.12) * 14f * dynamicAmp
                val r = waveRadiusBase + waveNoise
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()

                if (i == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
            }
            wavePath.close()

            drawPath(
                path = wavePath,
                color = state.primaryColor.copy(alpha = 0.85f),
                style = Stroke(width = 2.dp.toPx())
            )

            // =========================================================================
            // LAYER 4: 3D ROTATING ICOSAHEDRON CORE WIREFRAME
            // =========================================================================
            val radX = Math.toRadians((rotationPhase * 0.75f + parallaxY * 0.8f).toDouble())
            val radY = Math.toRadians((rotationPhase * 1.10f + parallaxX * 0.8f).toDouble())
            val radZ = Math.toRadians((rotationPhase * 0.40f).toDouble())

            val coreScale = baseRadius * 0.72f * pulseScale * (1f + dynamicAmp * 0.2f)

            // Rotate and project 3D vertices
            val transformedVertices = baseIcosahedron.map { v ->
                // Rotate around X
                val y1 = v.y * cos(radX) - v.z * sin(radX)
                val z1 = v.y * sin(radX) + v.z * cos(radX)

                // Rotate around Y
                val x2 = v.x * cos(radY) + z1 * sin(radY)
                val z2 = -v.x * sin(radY) + z1 * cos(radY)

                // Rotate around Z
                val x3 = x2 * cos(radZ) - y1 * sin(radZ)
                val y3 = x2 * sin(radZ) + y1 * cos(radZ)

                // Perspective projection factor
                val fov = 3.5f
                val perspective = fov / (fov + z2.toFloat())
                val projX = center.x + (x3.toFloat() * perspective * coreScale)
                val projY = center.y + (y3.toFloat() * perspective * coreScale)

                Triple(projX, projY, z2.toFloat()) // Stores Projected X, Y, and Z depth
            }

            // Draw 3D Wireframe Edges (Sorted by depth for realistic volumetric rendering)
            val sortedEdges = icosahedronEdges.map { (i, j) ->
                val v1 = transformedVertices[i]
                val v2 = transformedVertices[j]
                val avgZ = (v1.third + v2.third) / 2f
                Triple(v1, v2, avgZ)
            }.sortedBy { it.third } // Back faces first, front faces last

            sortedEdges.forEach { (v1, v2, avgZ) ->
                // Normalize depth (-1.0 to +1.0) to alpha (0.2 to 0.95)
                val depthAlpha = ((avgZ + 1f) / 2f).coerceIn(0.2f, 0.95f)
                val strokeW = (1.0f + depthAlpha * 2.2f).dp.toPx()

                drawLine(
                    color = if (avgZ > 0.2f) Color.White.copy(alpha = depthAlpha) else state.primaryColor.copy(alpha = depthAlpha * 0.85f),
                    start = Offset(v1.first, v1.second),
                    end = Offset(v2.first, v2.second),
                    strokeWidth = strokeW
                )
            }

            // Draw Glowing Nodes at Vertices
            transformedVertices.forEach { (vx, vy, vz) ->
                val nodeAlpha = ((vz + 1f) / 2f).coerceIn(0.3f, 1.0f)
                val nodeRadius = (3.dp.toPx() + nodeAlpha * 3.5.dp.toPx())

                // Inner core node highlight
                drawCircle(
                    color = Color.White.copy(alpha = nodeAlpha),
                    radius = nodeRadius,
                    center = Offset(vx, vy)
                )

                // Outer node glow bloom
                drawCircle(
                    color = state.primaryColor.copy(alpha = nodeAlpha * 0.6f),
                    radius = nodeRadius * 2.2f,
                    center = Offset(vx, vy)
                )
            }

            // =========================================================================
            // LAYER 5: INTENSE CENTRAL PLASMA CORE HOTSPOT
            // =========================================================================
            val plasmaRadius = baseRadius * 0.45f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        Color.White.copy(alpha = 0.9f),
                        state.primaryColor.copy(alpha = 0.8f),
                        state.secondaryColor.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = plasmaRadius
                ),
                radius = plasmaRadius,
                center = center
            )

            // Dynamic Cross Starburst Lens Flare at Core Center
            val starburstLen = baseRadius * (0.8f + dynamicAmp * 0.5f)
            rotate(rotationPhase * 0.5f, center) {
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(center.x - starburstLen, center.y),
                    end = Offset(center.x + starburstLen, center.y),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(center.x, center.y - starburstLen),
                    end = Offset(center.x, center.y + starburstLen),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // =========================================================================
            // LAYER 6: CINEMATIC SCANLINES & FRAME VIGNETTE OVERLAY
            // =========================================================================
            var scanY = 0f
            while (scanY < size.height) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.10f),
                    start = Offset(0f, scanY),
                    end = Offset(size.width, scanY),
                    strokeWidth = 1f
                )
                scanY += 4.dp.toPx()
            }

            // Subtle outer vignette frame
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.25f)
                    ),
                    center = center,
                    radius = max(size.width, size.height) * 0.7f
                )
            )
        }
    }
}

