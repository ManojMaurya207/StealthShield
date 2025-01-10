package com.example.stealthshield.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.FindMyDevice,
        BottomNavItem.Profile
    )

    NavigationBar(
        modifier = Modifier
            .padding(bottom = 15.dp, top = 0.dp, start = 15.dp, end = 15.dp)
            .alpha(0.95f)
            .clickable{}
            .clip(RoundedCornerShape(15.dp))
        ,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val scaleIcon by animateFloatAsState(
                targetValue = if (isSelected) 1.3f else 1.1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = ""
            )
            val scaleTitle by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = ""
            )
            NavigationBarItem(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .wrapContentSize(Alignment.Center)
                ,
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title,
                        modifier = Modifier
                            .scale(scaleIcon)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        modifier = Modifier
                            .scale(scaleTitle)
                    )
                },
                selected = isSelected,
                onClick = {
                    if(currentRoute != item.route){
                            navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = false }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                alwaysShowLabel = false,
            )
        }
    }
}













//
//
//    AnimatedBottomBar(
//        selectedItem = selectedItem, // Track the selected item index
//        itemSize = items.size, // Total number of items in the bottom navigation bar
//        indicatorStyle = IndicatorStyle.FILLED,
//        containerColor = MaterialTheme.colorScheme.background,// Style the indicator to be filled when selected
//    ) {
//        items.forEachIndexed { index, navigationItem ->
//            val selected = currentRoute == navigationItem.route // Determine if the current item is selected
//            BottomBarItem(
//                selected = selected, // Pass the selection status
//                onClick = {
//                    if (!selected) { // Only navigate if the selected item is different
//                        selectedItem = index // Update the selected item index
//                        navController.popBackStack() // Pop the backstack
//                        navController.navigate(navigationItem.route) {
//                            navController.graph.startDestinationRoute?.let { route ->
//                                popUpTo(route) { saveState = true } // Pop up to the start destination
//                            }
//                            launchSingleTop = true // Avoid multiple instances of the same destination
//                            restoreState = true // Restore the state of the previous screen
//                        }
//                    }
//                },
//                imageVector = if (selected) navigationItem.selectedIcon else navigationItem.unselectedIcon, // Show selected or unselected icon
//                label = navigationItem.title, // Set the label text for the item
//                containerColor = Color.Transparent, // Transparent background for the item
//                itemStyle = ItemStyle.STYLE5,
//                // Style the item (you can customize this)
//            )
//        }
//    }
//
