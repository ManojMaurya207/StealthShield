package com.example.stealthshield.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import com.example.stealthshield.R

import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun myLableText(modifier: Modifier = Modifier, value: String) {
    Text(
        text = value,
        modifier = modifier
            .fillMaxWidth(),
        style = TextStyle(
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center
    )
}


@Composable
fun myHeadingText(modifier: Modifier = Modifier, value: String) {
    Text(
        text = value,
        modifier = modifier
            .fillMaxWidth(),
        style = TextStyle(
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal
        ),
        textAlign = TextAlign.Center
    )
}

@Composable
fun AnimatedImageView(
    rawResId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = Int.MAX_VALUE // Corrected parameter name
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))

    LottieAnimation(
        composition = composition,
        modifier = modifier.animateContentSize(),
        iterations = iterations // Use the passed iterations parameter
    )
}
