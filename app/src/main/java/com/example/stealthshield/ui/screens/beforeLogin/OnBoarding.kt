import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.stealthshield.navigation.StealthNavigation
import com.example.stealthshield.ui.theme.StealthShieldTheme
import com.example.stealthshield.viewmodel.AuthViewModel
import com.google.android.libraries.places.api.Places

class OnBoarding : ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel : AuthViewModel by viewModels()
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyALyqWnzpO1dxyp014sY9-WuysvsOo2f7g")
        }
        setContent {
            StealthShieldTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                }
            }
        }
    }
}