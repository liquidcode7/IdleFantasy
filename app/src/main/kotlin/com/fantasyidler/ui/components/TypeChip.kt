package com.fantasyidler.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Small pill showing an elemental type name.
 * Returns without rendering anything for null or "neutral" types.
 */
@Composable
fun TypeChip(type: String?, modifier: Modifier = Modifier) {
    if (type == null || type == "neutral") return
    val label = type.replaceFirstChar { it.uppercase() }
    Surface(
        shape    = RoundedCornerShape(4.dp),
        color    = MaterialTheme.colorScheme.secondaryContainer,
        modifier = modifier,
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}
