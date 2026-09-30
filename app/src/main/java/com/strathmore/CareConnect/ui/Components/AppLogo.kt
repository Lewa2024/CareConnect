package com.strathmore.CareConnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The CareConnect wordmark: a filled circular badge with a heart glyph, plus the app name.
 * size = "large" for the Login splash, "compact" for top bars on every other screen.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val badgeSize = if (large) 72.dp else 32.dp
    val iconSize = if (large) 36.dp else 18.dp
    val textSize = if (large) 26.sp else 18.sp

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (large) 10.dp else 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            "CareConnect",
            fontSize = textSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}