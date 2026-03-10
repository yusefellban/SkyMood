package iti.yousef.skymood.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Data class representing a single onboarding page with its visual content.
 */
public data class OnboardingPage(
    val titleResId: Int,
    val descriptionResId: Int,
    val icon: ImageVector,
    val gradientColors: List<Color>
)

/**
 * Returns the list of three onboarding pages:
 * 1. Welcome overview
 * 2. Real-Time weather features
 * 3. Location permission explanation
 */
private fun getOnboardingPages() = listOf(
    OnboardingPage(
        titleResId = iti.yousef.skymood.R.string.onboarding_welcome_title,
        descriptionResId = iti.yousef.skymood.R.string.onboarding_welcome_desc,
        icon = Icons.Default.WbSunny,
        gradientColors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1), Color(0xFF01579B))
    ),
    OnboardingPage(
        titleResId = iti.yousef.skymood.R.string.onboarding_realtime_title,
        descriptionResId = iti.yousef.skymood.R.string.onboarding_realtime_desc,
        icon = Icons.Default.Cloud,
        gradientColors = listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF42A5F5))
    ),
    OnboardingPage(
        titleResId = iti.yousef.skymood.R.string.onboarding_location_title,
        descriptionResId = iti.yousef.skymood.R.string.onboarding_location_desc,
        icon = Icons.Default.LocationOn,
        gradientColors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
    )
)

/**
 * Full-screen onboarding composable with a horizontal pager,
 * page indicators, and a location permission request on the last page.
 *
 * @param onFinished Callback invoked when onboarding is completed
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = getOnboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Track whether the location permission has been granted
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher for requesting location access
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = pages[page],
                isLastPage = page == pages.size - 1,
                locationGranted = locationGranted,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onGetStarted = onFinished,
                onNext = {
                    scope.launch {
                        pagerState.animateScrollToPage(page + 1)
                    }
                }
            )
        }

        // Page indicator dots at the bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage)
                                Color.White
                            else
                                Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }
    }
}

/**
 * Content for a single onboarding page with animated floating particles,
 * icon, title, description, and action button.
 */
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isLastPage: Boolean,
    locationGranted: Boolean,
    onRequestPermission: () -> Unit,
    onGetStarted: () -> Unit,
    onNext: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(page.gradientColors)
            )
    ) {
        // Floating particle animation in the background
        FloatingParticles(particleColor = Color.White.copy(alpha = 0.15f))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 3 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glowing icon circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = stringResource(page.titleResId),
                        modifier = Modifier.size(72.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Text(
                    text = stringResource(page.titleResId),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(page.descriptionResId),
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(48.dp))

                if (isLastPage) {
                    // On the last page, show permission button or Get Started
                    if (!locationGranted) {
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = page.gradientColors[1]
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(iti.yousef.skymood.R.string.grant_permission),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Button(
                        onClick = onGetStarted,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (locationGranted) Color.White else Color.White.copy(alpha = 0.3f),
                            contentColor = if (locationGranted) page.gradientColors[1] else Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(iti.yousef.skymood.R.string.get_started),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    // "Next" button on non-last pages
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.25f),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Next",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas-based floating particles animation for visual richness.
 * Draws small circles that drift upward with a gentle sine-wave motion.
 */
@Composable
private fun FloatingParticles(particleColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleProgress"
    )

    // Pre-generate particle positions for stability
    val particles = remember {
        List(20) {
            Triple(
                Random.nextFloat(),           // x position (0..1)
                Random.nextFloat(),            // y start (0..1)
                Random.nextFloat() * 6f + 3f  // radius
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { (xRatio, yStart, radius) ->
            val x = xRatio * size.width
            val yOffset = (yStart + animProgress) % 1f
            val y = size.height * (1f - yOffset)
            val wobble = sin(animProgress * 6.28f + xRatio * 10f) * 20f
            drawCircle(
                color = particleColor,
                radius = radius,
                center = Offset(x + wobble, y)
            )
        }
    }
}
