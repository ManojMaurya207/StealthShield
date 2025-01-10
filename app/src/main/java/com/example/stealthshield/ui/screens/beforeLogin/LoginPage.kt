package com.example.stealthshield.ui.screens.beforeLogin

import android.app.Activity.RESULT_OK
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.stealthshield.R
import com.example.stealthshield.ui.components.AnimatedImageView
import com.example.stealthshield.ui.components.myHeadingText
import com.example.stealthshield.ui.components.myLableText
import com.example.stealthshield.viewmodel.AuthState
import com.example.stealthshield.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val authState by authViewModel.authState.observeAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                coroutineScope.launch {
                    authViewModel.signInWithIntent(result.data!!)
                }
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Google Sign-in cancelled", duration = SnackbarDuration.Long)
                }
                authViewModel.setAuthState(AuthState.Unauthenticated)
            }
        },

    )

    // Observing auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                navController.navigate("mainscreen") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
            else -> Unit
        }
    }

    // UI Layout
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        myLableText(modifier = Modifier, "Hey There!")
        Spacer(modifier = Modifier.height(5.dp))
        myHeadingText(modifier = Modifier, "Welcome Back!")

        AnimatedImageView(rawResId = R.raw.login, modifier = Modifier.size(250.dp), iterations = 1)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = errorMessage.isNotEmpty() && email.isEmpty(),
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Email, contentDescription = "Email Icon")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Lock, contentDescription = "Password Icon")
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            isError = errorMessage.isNotEmpty() && password.isEmpty()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (authState == AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(50.dp),
                onClick = {
                    errorMessage = ""
                    authViewModel.loginUser(email, password)
                },
                enabled = authState != AuthState.Loading
            ) {
                Text(text = "Login", fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }


        TextButton(onClick = { navController.navigate("signup") },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp)
        ) {
            Text(text = "Don't have an account? Signup", )
        }

        Divider()

        Spacer(modifier = Modifier.height(10.dp))

        GoogleSignInIcon(
            onclicked = {
                coroutineScope.launch {
                    val signInIntentSender = authViewModel.signIn()
                    if (signInIntentSender != null) {
                        launcher.launch(
                            IntentSenderRequest.Builder(signInIntentSender).build()
                        )
                    } else {
                        snackbarHostState.showSnackbar("Google Sign-in Failed", duration = SnackbarDuration.Long)
                    }
                }
            }
        )
    }
}

@Composable
fun GoogleSignInIcon(modifier: Modifier = Modifier, onclicked: () -> Unit) {
    IconButton(onClick = { onclicked() }) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Login",
                modifier = Modifier.size(40.dp)
            )
        }
    }
    Text(text = "Continue with Google", fontSize = 11.sp)
}
