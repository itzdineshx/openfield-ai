package com.openfield.app.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val OpenFieldShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val CardShape = RoundedCornerShape(18.dp)
val ButtonShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(50.dp)
val ComposerShape = RoundedCornerShape(24.dp)
val BubbleShapeUser = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
val BubbleShapeAssistant = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)

