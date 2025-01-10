import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.stealthshield.fakeshutdown.manager.PermissionManager
import com.example.stealthshield.fakeshutdown.manager.SharedPreferencesManager
import com.example.stealthshield.ui.screens.profile_components.FakeShutdownScreen
import com.example.stealthshield.viewmodel.FakeShutdownViewModel
import com.example.stealthshield.ui.components.BottomNavItem
import com.example.stealthshield.ui.components.BottomNavigationBar
import com.example.stealthshield.ui.screens.FindMyDevicePage
import com.example.stealthshield.ui.screens.HomePage
import com.example.stealthshield.ui.screens.SettingPage
import com.example.stealthshield.ui.screens.profile_components.*
import com.example.stealthshield.viewmodel.AuthViewModel
import com.example.stealthshield.viewmodel.MapViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier,
    authNavController: NavController,
    authViewModel: AuthViewModel,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val isInternetAvailable = checkInternetConnectivity(context)
    val mapViewModel = MapViewModel(context)

    LaunchedEffect(isInternetAvailable) {
        if (!isInternetAvailable) {
            snackbarHostState.showSnackbar("No internet connection.")
        }
    }

    // For Fake-shutdown
    val sharedPreferencesManager = SharedPreferencesManager(context)
    val permissionManager = PermissionManager()
    val fakeShutdownViewModel= FakeShutdownViewModel(LocalContext.current,sharedPreferencesManager,permissionManager)


    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {

                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth }, // Slide in from right
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth }, // Slide out to left
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth }, // Slide in from left
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(durationMillis = 300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth }, // Slide out to right
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            },
        ) {
            composable(BottomNavItem.Home.route) {
                HomePage(modifier = Modifier, authViewModel = authViewModel)
            }
            composable(BottomNavItem.FindMyDevice.route) {
                FindMyDevicePage(modifier = Modifier, mapViewModel)
            }
            composable(BottomNavItem.Profile.route) {
                SettingPage(modifier = Modifier, navController = navController, authViewModel = authViewModel)
            }




            // Profile_Components
            composable("fake_shutdown") {
                FakeShutdownScreen(context,fakeShutdownViewModel)
            }
            composable("emergency_contacts") {
                EmergencyContactsScreen()
            }
            composable("location_tracking") {
                LocationTrackingScreen()
            }
            composable("geofence_safe_zones") {
                SafeZonesScreen(mapViewModel = mapViewModel)
            }
            composable("signout") {
                SignOutScreen(navController_auth = authNavController, authViewModel = authViewModel)
            }
            composable("feature_screen") {
                FeatureScreen(navController = navController)
            }




            //Feature Tab
            composable("victim_capture") {
                VictimCaptureScreen(authViewModel=authViewModel)
            }

        }
    }
}

fun checkInternetConnectivity(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
