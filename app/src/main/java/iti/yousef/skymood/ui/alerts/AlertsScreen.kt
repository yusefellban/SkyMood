package iti.yousef.skymood.ui.alerts

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import iti.yousef.skymood.data.local.AlertEntity
import iti.yousef.skymood.data.local.AlertType
import java.text.SimpleDateFormat
import java.util.*

val SkyBlue = Color(0xFF1A73E8)
val DeepNavy = Color(0xFF0D1B2A)
val CardBackground = Color(0xFF1E2D3D)
val AccentPurple = Color(0xFF7C4DFF)
val AccentOrange = Color(0xFFFF6B35)
val AlertGreen = Color(0xFF4CAF50)
val AlertRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlertsViewModel = viewModel()
) {
    val alerts by viewModel.alerts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(DeepNavy, Color(0xFF1A2640), Color(0xFF0D1B2A))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground)
    ) {
        // Decorative floating orbs
        FloatingOrbs()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AlertsHeader(onNavigateBack = onNavigateBack)

            // Stats bar
            AnimatedAlertStats(alerts = alerts)

            // Content
            if (alerts.isEmpty()) {
                EmptyAlertsPlaceholder(onAddClick = { showAddDialog = true })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(alerts, key = { it.id }) { alert ->
                        AlertCard(
                            alert = alert,
                            onToggle = { viewModel.toggleAlert(alert) },
                            onDelete = { viewModel.deleteAlert(alert) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            PulsatingFab(onClick = { showAddDialog = true })
        }
    }

    if (showAddDialog) {
        AddAlertDialog(
            context = context,
            onDismiss = { showAddDialog = false },
            onConfirm = { label, from, to, type ->
                viewModel.addAlert(label, from, to, type)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FloatingOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 30f, label = "orb_y",
        animationSpec = infiniteRepeatable(tween(3000, easing = EaseInOutSine), RepeatMode.Reverse)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset((-40).dp, (20 + offsetY).dp)
                .size(160.dp)
                .background(SkyBlue.copy(alpha = 0.08f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(40.dp, (80 - offsetY).dp)
                .size(200.dp)
                .background(AccentPurple.copy(alpha = 0.06f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-30).dp, (-60 + offsetY).dp)
                .size(140.dp)
                .background(AccentOrange.copy(alpha = 0.07f), CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertsHeader(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Weather Alerts",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Stay ahead of the weather",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun AnimatedAlertStats(alerts: List<AlertEntity>) {
    val active = alerts.count { it.isActive }
    val total = alerts.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(icon = Icons.Default.Notifications, label = "Total", value = "$total", color = SkyBlue)
        StatChip(icon = Icons.Default.CheckCircle, label = "Active", value = "$active", color = AlertGreen)
        StatChip(icon = Icons.Default.Warning, label = "Paused", value = "${total - active}", color = AccentOrange)
    }
}

@Composable
private fun RowScope.StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground.copy(alpha = 0.8f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun EmptyAlertsPlaceholder(onAddClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f, label = "scale",
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        Brush.radialGradient(listOf(SkyBlue.copy(0.3f), Color.Transparent)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "No Alerts Set",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your first weather alert\nto get notified in advance",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Alert", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: AlertEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val typeColor = if (alert.alertType == AlertType.ALARM) AccentOrange else SkyBlue
    val statusColor = if (alert.isActive) AlertGreen else Color.Gray

    val animatedAlpha by animateFloatAsState(
        targetValue = if (alert.isActive) 1f else 0.55f,
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(typeColor.copy(0.15f), Color.Transparent)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type icon
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(typeColor.copy(0.2f), CircleShape)
                            .border(1.5.dp, typeColor.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alert.alertType == AlertType.ALARM) Icons.Default.Alarm else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alert.label,
                            color = Color.White.copy(alpha = animatedAlpha),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (alert.isActive) "● ACTIVE" else "○ PAUSED",
                                color = statusColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    // Toggle switch
                    Switch(
                        checked = alert.isActive,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AlertGreen,
                            checkedTrackColor = AlertGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.2f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                // Time range row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${dateFormat.format(Date(alert.fromTime))}  →  ${dateFormat.format(Date(alert.toTime))}",
                        color = Color.White.copy(alpha = animatedAlpha * 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete alert",
                            tint = AlertRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulsatingFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f, label = "pulse",
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse)
    )

    Box(contentAlignment = Alignment.Center) {
        // Pulse ring
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(pulseScale)
                .background(SkyBlue.copy(alpha = 0.25f), CircleShape)
        )
        FloatingActionButton(
            onClick = onClick,
            containerColor = SkyBlue,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Alert", tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAlertDialog(
    context: Context,
    onDismiss: () -> Unit,
    onConfirm: (String, Long, Long, AlertType) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var fromTime by remember { mutableStateOf(System.currentTimeMillis() + 60_000) }
    var toTime by remember { mutableStateOf(System.currentTimeMillis() + 3_600_000) }
    var selectedType by remember { mutableStateOf(AlertType.NOTIFICATION) }
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    fun pickDateTime(initial: Long, onResult: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = initial }
        DatePickerDialog(
            context,
            { _, y, m, d ->
                TimePickerDialog(context, { _, h, min ->
                    cal.set(y, m, d, h, min)
                    onResult(cal.timeInMillis)
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A2640), Color(0xFF0D1B2A)))
                )
                .border(1.dp, SkyBlue.copy(0.3f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Dialog title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SkyBlue.copy(0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAlarm, null, tint = SkyBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("New Alert", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Alert label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Alert Label", color = Color.White.copy(alpha = 0.5f)) },
                    placeholder = { Text("e.g. Morning Rain", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SkyBlue,
                        unfocusedBorderColor = Color.White.copy(0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = SkyBlue
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Time selectors
                Text("Duration Window", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimeButton(
                        label = "From",
                        time = dateFormat.format(Date(fromTime)),
                        color = SkyBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { pickDateTime(fromTime) { fromTime = it } }
                    )
                    TimeButton(
                        label = "To",
                        time = dateFormat.format(Date(toTime)),
                        color = AccentPurple,
                        modifier = Modifier.weight(1f),
                        onClick = { pickDateTime(toTime) { toTime = it } }
                    )
                }

                // Alert type selector
                Text("Alert Type", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AlertType.entries.forEach { type ->
                        val isSelected = selectedType == type
                        val chipColor = if (type == AlertType.ALARM) AccentOrange else SkyBlue
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) chipColor.copy(0.25f) else CardBackground)
                                .border(
                                    1.5.dp,
                                    if (isSelected) chipColor else Color.White.copy(0.15f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedType = type }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (type == AlertType.ALARM) Icons.Default.Alarm else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (isSelected) chipColor else Color.White.copy(0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = if (isSelected) chipColor else Color.White.copy(0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color.White.copy(0.2f), Color.White.copy(0.2f)))
                        )
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (label.isNotBlank() && toTime > fromTime) {
                                onConfirm(label, fromTime, toTime, selectedType)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                        enabled = label.isNotBlank() && toTime > fromTime
                    ) {
                        Text("Save Alert", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeButton(
    label: String,
    time: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.12f))
            .border(1.dp, color.copy(0.4f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = color.copy(0.7f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = time, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
