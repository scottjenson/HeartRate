package org.jenson.heartrate

import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text

private val ZoneBlue   = Color(0xFF00E5FF)
private val ZoneGreen  = Color(0xFF64DD17)
private val ZoneYellow = Color(0xFFFFD600)
private val ZoneRed    = Color(0xFFFF1744)

@Composable
fun HeartRateScreen(
    heartRate: Int?,
    availability: String?,
    isAmbient: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val textPaint = remember(density) {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 120.sp.toPx() }
            val googleSans = Typeface.create("google-sans-flex", Typeface.NORMAL).let { tf ->
                if (tf === Typeface.DEFAULT) Typeface.create("google-sans", Typeface.NORMAL) else tf
            }
            typeface = Typeface.create(googleSans, 500, false)
            setFontVariationSettings("'ROND' 100, 'opsz' 96, 'wght' 650")
        }
    }

    val targetColor = when {
        heartRate == null || heartRate < 95 -> Color.White
        heartRate < 105                     -> ZoneBlue
        heartRate < 120                     -> ZoneGreen
        heartRate < 140                     -> ZoneYellow
        else                                -> ZoneRed
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(500),
        label = "heartRateZoneColor",
    )

    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .drawBehind {
                        textPaint.color = animatedColor.toArgb()
                        val x = size.width / 2f
                        val y = size.height / 2f - (textPaint.ascent() + textPaint.descent()) / 2f
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                heartRate?.toString() ?: "--", x, y, textPaint
                            )
                        }
                    }
            )

            if (!isAmbient && !availability.isNullOrEmpty() && availability != "AVAILABLE") {
                Text(
                    text = availability,
                    style = TextStyle(fontSize = 12.sp),
                    color = Color.Gray,
                )
            }
        }
    }
}
