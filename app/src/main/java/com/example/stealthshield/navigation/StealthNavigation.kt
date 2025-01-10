package com.example.stealthshield.navigation

import MainScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stealthshield.ui.screens.beforeLogin.LoginPage
import com.example.stealthshield.ui.screens.beforeLogin.SignUpPage
import com.example.stealthshield.viewmodel.AuthState
import com.example.stealthshield.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StealthNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val navController = rememberNavController()
    // Determine the start destination based on the auth state
    val startDestination = if (authViewModel.authState.value == AuthState.Authenticated) "mainscreen" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginPage(modifier, navController, authViewModel, snackbarHostState)
        }
        composable("signup") {
            SignUpPage(modifier, navController, authViewModel, snackbarHostState)
        }
        composable("mainscreen") {
            MainScreen(modifier, navController, authViewModel)
        }
    }

    // Observe the authentication state and update SharedPreferences and navigation
//    LaunchedEffect(authViewModel.authState) {
//        authViewModel.authState.observeForever { authState ->
//            when (authState) {
//                is AuthState.Authenticated -> {
//                    navController.navigate("mainscreen") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                }
//                is AuthState.Unauthenticated -> {
//                    navController.navigate("login") {
//                        popUpTo("mainscreen") { inclusive = true }
//                    }
//                }
//                else -> Unit
//            }
//        }
//    }
}
