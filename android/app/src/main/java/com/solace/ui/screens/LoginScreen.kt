package com.solace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.solace.ui.components.OutlinedAuthButton
import com.solace.ui.components.PrimaryButton
import com.solace.ui.config.AuthProvider
import com.solace.ui.config.LoginScreenConfig
import com.solace.ui.theme.*

@Composable
fun LoginScreen(
    config: LoginScreenConfig = LoginScreenConfig(),
    onEmailLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onEmailSignUp: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleLogin: () -> Unit = {},
    onAppleLogin: () -> Unit = {},
) {
    if (!config.visible) return

    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val showEmail = AuthProvider.EMAIL in config.providers
    val showGoogle = AuthProvider.GOOGLE in config.providers
    val showApple = AuthProvider.APPLE in config.providers

    val passwordsMatch = !isSignUp || password == confirmPassword
    val formValid = email.isNotBlank() && password.isNotBlank() &&
        (!isSignUp || (confirmPassword.isNotBlank() && passwordsMatch))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SolaceSpacing.lg, vertical = SolaceSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(SolaceSpacing.xxl))

            // Brand wordmark
            Text(
                text = "Solace",
                style = MaterialTheme.typography.displayLarge,
                color = SolaceTeal,
            )

            Spacer(Modifier.height(SolaceSpacing.sm))

            // Tagline — driven by config
            Text(
                text = config.tagline,
                style = MaterialTheme.typography.bodyLarge,
                color = TextOnDark.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(SolaceSpacing.xxl))

            // ── Email / password form ─────────────────────────────────────
            if (showEmail) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = TextOnDark.copy(alpha = 0.6f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    colors = loginTextFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(SolaceSpacing.sm))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = TextOnDark.copy(alpha = 0.6f)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (isSignUp) ImeAction.Next else ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (!isSignUp && formValid) onEmailLogin(email, password)
                        },
                    ),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                if (passwordVisible) "Hide" else "Show",
                                color = SolaceTeal,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    },
                    colors = loginTextFieldColors(),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Confirm password — sign-up only
                if (isSignUp) {
                    Spacer(Modifier.height(SolaceSpacing.sm))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm password", color = TextOnDark.copy(alpha = 0.6f)) },
                        singleLine = true,
                        isError = confirmPassword.isNotBlank() && !passwordsMatch,
                        supportingText = {
                            if (confirmPassword.isNotBlank() && !passwordsMatch) {
                                Text("Passwords don't match", color = Error)
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (formValid) onEmailSignUp(email, password)
                            },
                        ),
                        trailingIcon = {
                            TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Text(
                                    if (confirmPasswordVisible) "Hide" else "Show",
                                    color = SolaceTeal,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        },
                        colors = loginTextFieldColors(),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(SolaceSpacing.md))

                PrimaryButton(
                    label = if (isSignUp) "Create account" else config.emailCtaLabel,
                    enabled = formValid,
                    onClick = {
                        keyboardController?.hide()
                        if (isSignUp) onEmailSignUp(email, password)
                        else onEmailLogin(email, password)
                    },
                )

                Spacer(Modifier.height(SolaceSpacing.sm))

                TextButton(onClick = {
                    isSignUp = !isSignUp
                    confirmPassword = ""
                }) {
                    Text(
                        if (isSignUp) "Already have an account? Sign in"
                        else "Don't have an account? Sign up",
                        color = SolaceTeal,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // ── Divider ───────────────────────────────────────────────────
            if (showEmail && (showGoogle || showApple)) {
                Spacer(Modifier.height(SolaceSpacing.md))
                OrDivider()
                Spacer(Modifier.height(SolaceSpacing.md))
            }

            // ── OAuth buttons ─────────────────────────────────────────────
            if (showGoogle) {
                OutlinedAuthButton(
                    label = config.googleCtaLabel,
                    onClick = onGoogleLogin,
                )
                Spacer(Modifier.height(SolaceSpacing.sm))
            }

            if (showApple) {
                OutlinedAuthButton(
                    label = config.appleCtaLabel,
                    onClick = onAppleLogin,
                )
            }

            Spacer(Modifier.height(SolaceSpacing.lg))
        }
    }
}

@Composable
private fun OrDivider() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Divider)
        Text(
            text = "  or  ",
            style = MaterialTheme.typography.bodySmall,
            color = TextOnDark.copy(alpha = 0.5f),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Divider)
    }
}

@Composable
private fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SolaceTeal,
    unfocusedBorderColor = Divider,
    focusedLabelColor = SolaceTeal,
    cursorColor = SolaceTeal,
    focusedTextColor = TextOnDark,
    unfocusedTextColor = TextOnDark,
)

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SolaceTheme {
        LoginScreen(
            config = LoginScreenConfig(
                providers = listOf(AuthProvider.EMAIL, AuthProvider.GOOGLE, AuthProvider.APPLE),
            ),
        )
    }
}

@Preview(showBackground = true, name = "Email only")
@Composable
private fun LoginScreenEmailOnlyPreview() {
    SolaceTheme {
        LoginScreen(config = LoginScreenConfig(providers = listOf(AuthProvider.EMAIL)))
    }
}
