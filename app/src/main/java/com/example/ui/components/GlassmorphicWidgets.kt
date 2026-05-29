package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    backgroundColor: Color = Color(0xFF161618).copy(alpha = 0.75f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(cornerRadius),
        content = content
    )
}

@Composable
fun PremiumGlowingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glowColor: Color = Color(0xFF60A5FA), // High-fidelity Slate Sky Blue accent
    icon: @Composable (() -> Unit)? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .drawBehind {
                // Outer subtle clean minimalist accent bloom glow
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(glowColor.copy(alpha = 0.15f), Color.Transparent),
                        radius = size.width * 0.8f
                    ),
                    alpha = 0.6f
                )
            }
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.2f), glowColor.copy(alpha = 0.5f))
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF09090C),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun MediaCategoryPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)) // Clean minimalist slate blue-gradient
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF111113), Color(0xFF09090A))
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF60A5FA).copy(alpha = 0.4f) else Color(0xFF1E293B).copy(alpha = 0.5f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .widthIn(min = 64.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else Color(0xFF94A3B8)
        )
    }
}
