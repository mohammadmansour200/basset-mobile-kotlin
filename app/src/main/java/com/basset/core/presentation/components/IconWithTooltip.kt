package com.basset.core.presentation.components

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IconWithTooltip(
    icon: ImageVector? = null,
    content: @Composable (() -> Unit)? = null,
    text: String,
    @SuppressLint("ModifierParameter") tooltipBoxModifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    val tooltipState = rememberTooltipState(isPersistent = false)
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(
            spacingBetweenTooltipAndAnchor = 10.dp
        ),
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.inverseSurface,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    color = MaterialTheme.colorScheme.background,
                    text = text,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        state = tooltipState,
        modifier = tooltipBoxModifier
    )
    {
        if (icon != null)
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = iconModifier
            )
        if (content != null) content()
    }
}