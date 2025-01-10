package com.example.stealthshield.ui.screens.beforeLogin

import com.example.stealthshield.R

data class OnBoardingItem(
    val title: String,
    val text: String,
    val Image: Int,
) {
    companion object {

        fun get() = listOf(
            OnBoardingItem("R.string.title1", "R.string.text1", R.drawable.profile_picture),
            OnBoardingItem("R.string.title2", "R.string.text2", R.drawable.profile_picture),
            OnBoardingItem("R.string.title3", "R.string.text3", R.drawable.profile_picture)
        )
    }
}