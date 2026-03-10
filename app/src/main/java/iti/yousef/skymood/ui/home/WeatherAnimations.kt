package iti.yousef.skymood.ui.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Selects and renders the appropriate weather animation based on the
 * OpenWeatherMap weather condition code.
 *
 * Condition code ranges:
 * - 2xx: Thunderstorm → Rain animation with darker tones
 * - 3xx: Drizzle → Light rain
 * - 5xx: Rain → Rain animation
 * - 6xx: Snow → Snow animation
 * - 7xx: Atmosphere (fog, mist) → Fog animation
 * - 800: Clear → Sun animation
 * - 80x: Clouds → Cloud animation
 */
@Composable
public fun WeatherAnimationBackground(weatherConditionCode: Int, isNight: Boolean) {
    when (weatherConditionCode) {
        in 200..299 -> RainAnimation(heavy = true, isNight = isNight)
        in 300..399 -> RainAnimation(heavy = false, isNight = isNight)
        in 500..599 -> RainAnimation(heavy = true, isNight = isNight)
        in 600..699 -> SnowAnimation(isNight = isNight)
        in 700..799 -> FogAnimation(isNight = isNight)
        800 -> ClearSkyAnimation(isNight = isNight)
        in 801..804 -> CloudAnimation(isNight = isNight)
        else -> ClearSkyAnimation(isNight = isNight)
    }
}


@Composable
fun RainAnimation(heavy: Boolean, isNight: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (heavy) 1200 else 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainDrop"
    )

    val drops = remember {
        val count = if (heavy) 60 else 30
        List(count) {
            Triple(
                Random.nextFloat(),              // x position ratio
                Random.nextFloat(),              // y start offset
                Random.nextFloat() * 30f + 20f   // drop length
            )
        }
    }

    val bgColors = if (isNight) {
        listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF2C3E50))
    } else {
        listOf(Color(0xFF2C3E50), Color(0xFF4A6274), Color(0xFF607D8B))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Gradient background
        drawRect(brush = Brush.verticalGradient(bgColors))

        // Rain streaks
        val dropColor = Color.White.copy(alpha = if (heavy) 0.4f else 0.25f)
        drops.forEach { (xRatio, yStart, length) ->
            val x = xRatio * size.width
            val yOffset = ((yStart + progress) % 1f) * (size.height + length)
            drawLine(
                color = dropColor,
                start = Offset(x, yOffset - length),
                end = Offset(x - 3f, yOffset),
                strokeWidth = if (heavy) 2.5f else 1.5f
            )
        }
    }
}


@Composable
fun SnowAnimation(isNight: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowfall"
    )

    val flakes = remember {
        List(40) {
            Triple(
                Random.nextFloat(),              // x ratio
                Random.nextFloat(),              // y offset
                Random.nextFloat() * 4f + 2f     // radius
            )
        }
    }

    val bgColors = if (isNight) {
        listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB))
    } else {
        listOf(Color(0xFF78909C), Color(0xFF90A4AE), Color(0xFFB0BEC5))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(bgColors))

        flakes.forEach { (xRatio, yStart, radius) ->
            val yOffset = ((yStart + progress) % 1f) * size.height
            val wobble = sin(progress * 6.28f + xRatio * 12f) * 25f
            val x = xRatio * size.width + wobble
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = radius,
                center = Offset(x, yOffset)
            )
        }
    }
}

/**
 * Animated fog effect: translucent horizontal bands that shift slowly.
 */
@Composable
fun FogAnimation(isNight: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "fog")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fogShift"
    )

    val bgColors = if (isNight) {
        listOf(Color(0xFF37474F), Color(0xFF455A64), Color(0xFF546E7A))
    } else {
        listOf(Color(0xFF78909C), Color(0xFF90A4AE), Color(0xFFB0BEC5))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(bgColors))

        // Draw multiple fog bands
        for (i in 0..5) {
            val y = size.height * (i * 0.18f + progress * 0.05f)
            val alpha = 0.08f + (i % 3) * 0.04f
            drawRect(
                color = Color.White.copy(alpha = alpha),
                topLeft = Offset(-50f + sin(progress * 3.14f + i) * 30f, y),
                size = androidx.compose.ui.geometry.Size(size.width + 100f, size.height * 0.08f)
            )
        }
    }
}

/**
 * Animated clear sky: radial sun glow with rotating ray beams.
 * At night, shows a starfield with a moon glow.
 */
@Composable
fun ClearSkyAnimation(isNight: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "clearSky")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunRotation"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    val stars = remember {
        List(50) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (isNight) {
            // Night sky gradient
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF1A237E))
                )
            )
            // Stars
            stars.forEach { (xR, yR, r) ->
                val twinkle = (sin(rotation * 0.05f + xR * 20f) + 1f) / 2f
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f + twinkle * 0.5f),
                    radius = r,
                    center = Offset(xR * size.width, yR * size.height * 0.6f)
                )
            }
            // Moon glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE8EAF6).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.75f, size.height * 0.15f),
                    radius = 180f
                ),
                radius = 180f,
                center = Offset(size.width * 0.75f, size.height * 0.15f)
            )
            drawCircle(
                color = Color(0xFFE8EAF6),
                radius = 40f,
                center = Offset(size.width * 0.75f, size.height * 0.15f)
            )
        } else {
            // Daytime gradient
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF90CAF9))
                )
            )
            // Sun glow
            val sunCenter = Offset(size.width * 0.8f, size.height * 0.12f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF176).copy(alpha = 0.4f * pulse),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = 200f * pulse
                ),
                radius = 200f * pulse,
                center = sunCenter
            )
            drawCircle(
                color = Color(0xFFFFF176),
                radius = 50f,
                center = sunCenter
            )
            // Rotating sun rays
            val rayCount = 12
            for (i in 0 until rayCount) {
                val angle = Math.toRadians((rotation + i * 30f).toDouble())
                val innerR = 60f
                val outerR = 120f * pulse
                drawLine(
                    color = Color(0xFFFFF176).copy(alpha = 0.3f),
                    start = Offset(
                        sunCenter.x + cos(angle).toFloat() * innerR,
                        sunCenter.y + sin(angle).toFloat() * innerR
                    ),
                    end = Offset(
                        sunCenter.x + cos(angle).toFloat() * outerR,
                        sunCenter.y + sin(angle).toFloat() * outerR
                    ),
                    strokeWidth = 3f
                )
            }
        }
    }
}


@Composable
fun CloudAnimation(isNight: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val drift by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloudDrift"
    )

    val clouds = remember {
        listOf(
            CloudData(yRatio = 0.1f, sizeMultiplier = 1.2f, speedOffset = 0f),
            CloudData(yRatio = 0.25f, sizeMultiplier = 0.8f, speedOffset = 0.3f),
            CloudData(yRatio = 0.15f, sizeMultiplier = 1.0f, speedOffset = 0.6f),
            CloudData(yRatio = 0.35f, sizeMultiplier = 0.7f, speedOffset = 0.45f),
            CloudData(yRatio = 0.05f, sizeMultiplier = 0.9f, speedOffset = 0.8f)
        )
    }

    val bgColors = if (isNight) {
        listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF303F9F))
    } else {
        listOf(Color(0xFF1976D2), Color(0xFF42A5F5), Color(0xFF90CAF9))
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(brush = Brush.verticalGradient(bgColors))

        clouds.forEach { cloud ->
            val x = ((drift + cloud.speedOffset) % 1.6f - 0.3f) * size.width
            val y = cloud.yRatio * size.height
            drawCloud(
                center = Offset(x, y),
                scale = cloud.sizeMultiplier,
                color = Color.White.copy(alpha = if (isNight) 0.2f else 0.6f)
            )
        }
    }
}

private data class CloudData(
    val yRatio: Float,
    val sizeMultiplier: Float,
    val speedOffset: Float
)


private fun DrawScope.drawCloud(center: Offset, scale: Float, color: Color) {
    val baseRadius = 35f * scale
    drawCircle(color, baseRadius, Offset(center.x, center.y))
    drawCircle(color, baseRadius * 0.8f, Offset(center.x - baseRadius * 0.9f, center.y + 5f))
    drawCircle(color, baseRadius * 0.7f, Offset(center.x + baseRadius * 0.9f, center.y + 5f))
    drawCircle(color, baseRadius * 0.6f, Offset(center.x - baseRadius * 0.4f, center.y - baseRadius * 0.5f))
    drawCircle(color, baseRadius * 0.65f, Offset(center.x + baseRadius * 0.4f, center.y - baseRadius * 0.4f))
}
