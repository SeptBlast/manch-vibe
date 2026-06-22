package com.solace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.solace.ui.auth.AuthUiState
import com.solace.ui.auth.AuthViewModel
import com.solace.ui.home.HomeViewModel
import com.solace.ui.onboarding.OnboardingAnswers
import com.solace.ui.onboarding.OnboardingScreen
import com.solace.ui.profile.ProfileCardCreationScreen
import com.solace.ui.screens.HomeScreen
import com.solace.ui.screens.LoginScreen
import com.solace.ui.theme.SolaceTheme
import com.solace.ui.theme.SolaceThemeVariant
import dagger.hilt.android.AndroidEntryPoint

private object Route {
    const val LOGIN = "login"
    const val ONBOARDING = "onboarding"
    const val PROFILE_CARD = "profile_card"
    const val HOME = "home"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authVM: AuthViewModel = hiltViewModel()
            val homeVM: HomeViewModel = hiltViewModel()
            val authState by authVM.uiState.collectAsState()
            val homeState by homeVM.uiState.collectAsState()

            val variant = when (homeState.uiConfig.themeVariant.name) {
                "DARK" -> SolaceThemeVariant.DARK
                else   -> SolaceThemeVariant.DEFAULT
            }

            SolaceTheme(variant = variant) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootNavigation(
                        authState = authState,
                        authVM = authVM,
                        homeVM = homeVM,
                        activity = this,
                    )
                }
            }
        }
    }
}

@Composable
private fun RootNavigation(
    authState: AuthUiState,
    authVM: AuthViewModel,
    homeVM: HomeViewModel,
    activity: android.app.Activity,
) {
    val nav = rememberNavController()
    val homeState by homeVM.uiState.collectAsState()

    // Answers carried from onboarding → profile card creation
    var pendingAnswers by remember { mutableStateOf<OnboardingAnswers?>(null) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {
                nav.navigate(Route.ONBOARDING) {
                    popUpTo(Route.LOGIN) { inclusive = true }
                }
            }
            is AuthUiState.Idle -> nav.navigate(Route.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
            else -> Unit
        }
    }

    NavHost(navController = nav, startDestination = Route.LOGIN) {

        composable(Route.LOGIN) {
            LoginScreen(
                config = homeState.uiConfig.loginScreen,
                onEmailLogin  = authVM::signInWithEmail,
                onGoogleLogin = { authVM.signInWithGoogle(activity) },
                onAppleLogin  = { authVM.signInWithApple(activity) },
            )
        }

        composable(Route.ONBOARDING) {
            val uid = (authState as? AuthUiState.Authenticated)?.user?.uid ?: return@composable
            OnboardingScreen(
                uid = uid,
                onComplete = { answers ->
                    pendingAnswers = answers
                    nav.navigate(Route.PROFILE_CARD) {
                        popUpTo(Route.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.PROFILE_CARD) {
            val uid = (authState as? AuthUiState.Authenticated)?.user?.uid ?: return@composable
            val answers = pendingAnswers ?: OnboardingAnswers()
            ProfileCardCreationScreen(
                uid = uid,
                answers = answers,
                onComplete = {
                    pendingAnswers = null
                    nav.navigate(Route.HOME) {
                        popUpTo(Route.PROFILE_CARD) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.HOME) {
            val uid = (authState as? AuthUiState.Authenticated)?.user?.uid ?: ""
            HomeScreen(
                config = homeState.uiConfig.homeScreen,
                profiles = homeState.profiles,
                currentUid = uid,
            )
        }
    }
}
