package com.basset.core.presentation.modifier

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ContainerShapeDefaults {
    @Composable
    fun shapeForIndex(
        index: Int,
        size: Int,
        forceDefault: Boolean = false,
    ): RoundedCornerShape {
        val internalShape by remember(index, size, forceDefault) {
            derivedStateOf {
                when {
                    index == -1 || size == 1 || forceDefault -> defaultShape
                    index == 0 && size > 1 -> topShape
                    index == size - 1 -> bottomShape
                    else -> centerShape
                }
            }
        }
        val topStart by animateDpAsState(
            internalShape.topStart.dp
        )
        val topEnd by animateDpAsState(
            internalShape.topEnd.dp
        )
        val bottomStart by animateDpAsState(
            internalShape.bottomStart.dp
        )
        val bottomEnd by animateDpAsState(
            internalShape.bottomEnd.dp
        )
        return RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomStart = bottomStart,
            bottomEnd = bottomEnd
        )
    }

    val topShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = 6.dp,
        bottomEnd = 6.dp
    )

    val centerShape = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 6.dp,
        bottomStart = 6.dp,
        bottomEnd = 6.dp
    )

    val bottomShape = RoundedCornerShape(
        topStart = 6.dp,
        topEnd = 6.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )

    val defaultShape = RoundedCornerShape(16.dp)

    private val CornerSize.dp: Dp
        @Composable
        get() = with(LocalDensity.current) { toPx(Size.Unspecified, this).toDp() }
}