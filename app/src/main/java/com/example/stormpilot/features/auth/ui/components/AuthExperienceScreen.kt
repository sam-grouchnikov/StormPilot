package com.example.stormpilot.features.auth.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stormpilot.R
import com.example.stormpilot.features.auth.ui.WelcomeActions
import com.example.stormpilot.ui.theme.StormPilotTheme
import com.example.stormpilot.ui.theme.extendedColors

private const val HeaderMoveMillis = 460
private const val FormSlideMillis = 340

private enum class AuthMode(val title: String, val subtitle: String) {
    Welcome("Stormpilot", "A more intelligent way to track storms"),
    SignIn("Welcome Back", "Continue with a more intelligent way to track storms"),
    SignUp("Welcome to StormPilot", "Start with a more intelligent way to track storms"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthExperienceScreen(onLogin: () -> Unit) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.Welcome) }
    val transition = updateTransition(mode, label = "Auth mode")
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    StormPilotTheme(
        darkTheme = true,
        dynamicColor = false,
        opaqueNavigationBar = false,
        useSurfaceContainerNavigationBar = true,
        syncAppDarkMode = false,
    ) {
        val colors = MaterialTheme.extendedColors
        val density = LocalDensity.current
        val welcomeOffset = with(density) { 180.toDp() }
        val welcomeButtonPadding = with(density) { 58.toDp() }
        val keyboardVisible = WindowInsets.isImeVisible

        BackHandler(enabled = mode != AuthMode.Welcome && keyboardVisible) {
            focusManager.clearFocus()
            keyboard?.hide()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.auth.backgroundEnd,
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .authBackgroundGradient(colors.auth)
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
            ) {
                val welcomeTop = (maxHeight / 2 - welcomeOffset - 80.dp).coerceAtLeast(0.dp)
                val authTop = minOf(welcomeTop, maxHeight * 0.11f, 90.dp)
                val headerTop by transition.animateDp(
                    transitionSpec = { tween(HeaderMoveMillis) },
                    label = "Header position",
                ) { if (it == AuthMode.Welcome) welcomeTop else authTop }
                val authLogoSize = if (maxHeight < 440.dp) 54.dp else 76.dp

                Column(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.height(headerTop))
                    AuthHeader(transition, authLogoSize)
                    Spacer(modifier = Modifier.height(34.dp))

                    // Only the form viewport resizes for the keyboard.
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .imePadding(),
                    ) {
                        val formHeight = maxHeight
                        transition.AnimatedContent(
                            modifier = Modifier.fillMaxSize().clipToBounds(),
                            transitionSpec = {
                                if (initialState == AuthMode.Welcome) {
                                    val enter = fadeIn(tween(260, delayMillis = 130)) +
                                        slideInVertically(tween(HeaderMoveMillis)) { it / 12 }
                                    enter.togetherWith(fadeOut(tween(150))).using(null)
                                } else {
                                    slideInHorizontally(tween(FormSlideMillis)) { -it }
                                        .togetherWith(
                                            slideOutHorizontally(tween(FormSlideMillis)) { it },
                                        )
                                        .using(null)
                                }
                            },
                        ) { panelMode ->
                            if (panelMode != AuthMode.Welcome) {
                                AuthForm(
                                    isSignUp = panelMode == AuthMode.SignUp,
                                    minHeight = formHeight,
                                    onPrimaryAction = {
                                        if (mode == panelMode) onLogin()
                                    },
                                    onSwitchMode = {
                                        if (mode == panelMode) {
                                            focusManager.clearFocus()
                                            keyboard?.hide()
                                            mode = if (panelMode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                transition.AnimatedVisibility(
                    visible = { it == AuthMode.Welcome },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 28.dp)
                        .padding(bottom = welcomeButtonPadding),
                    enter = fadeIn(),
                    exit = fadeOut(tween(180)) + slideOutVertically(tween(220)) { it / 5 },
                ) {
                    WelcomeActions(
                        onSignInClick = { if (mode == AuthMode.Welcome) mode = AuthMode.SignIn },
                        onSignUpClick = { if (mode == AuthMode.Welcome) mode = AuthMode.SignUp },
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthHeader(transition: Transition<AuthMode>, authLogoSize: Dp) {
    val logoSize by transition.animateDp(
        transitionSpec = { tween(HeaderMoveMillis) },
        label = "Logo size",
    ) { if (it == AuthMode.Welcome) 90.dp else authLogoSize }
    val textGap by transition.animateDp(
        transitionSpec = { tween(HeaderMoveMillis) },
        label = "Logo to text spacing",
    ) { if (it == AuthMode.Welcome) 4.dp else 10.dp }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "StormPilot logo",
            modifier = Modifier.size(logoSize),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(textGap))

        transition.AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                fadeIn(tween(280, delayMillis = 60))
                    .togetherWith(fadeOut(tween(200)))
                    .using(SizeTransform(clip = false) { _, _ -> tween(HeaderMoveMillis) })
            },
        ) { copyMode ->
            if (copyMode == AuthMode.Welcome) {
                AuthHeaderCopy(copyMode)
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    // Reserve space for either copy so wrapped text cannot move the form.
                    val otherMode = if (copyMode == AuthMode.SignIn) AuthMode.SignUp else AuthMode.SignIn
                    AuthHeaderCopy(
                        mode = otherMode,
                        modifier = Modifier.alpha(0f).clearAndSetSemantics {},
                    )
                    AuthHeaderCopy(copyMode)
                }
            }
        }
    }
}

@Composable
private fun AuthHeaderCopy(mode: AuthMode, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.extendedColors
    val isWelcome = mode == AuthMode.Welcome
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = mode.title,
            textAlign = TextAlign.Center,
            style = if (isWelcome) {
                MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 10.sp,
                    fontSize = 44.sp,
                )
            } else {
                MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 23.sp,
                )
            },
            color = colors.auth.primaryContent,
        )

        if (isWelcome) Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = mode.subtitle,
            textAlign = TextAlign.Center,
            style = if (isWelcome) {
                MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 25.sp)
            } else {
                MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal)
            },
            color = if (isWelcome) colors.auth.supportingContent else colors.theme.tonalOutlineVariant,
        )
    }
}

@Composable
private fun AuthForm(
    isSignUp: Boolean,
    minHeight: Dp,
    onPrimaryAction: () -> Unit,
    onSwitchMode: () -> Unit,
) {
    val colors = MaterialTheme.extendedColors.auth
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .heightIn(min = minHeight)
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExpressiveInputField(
            value = email,
            onValueChange = { email = it },
            icon = Icons.Outlined.Email,
            label = "Email",
            placeholder = "Enter your email",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        Spacer(modifier = Modifier.height(22.dp))

        ExpressiveInputField(
            value = password,
            onValueChange = { password = it },
            icon = Icons.Outlined.Lock,
            label = "Password",
            placeholder = if (isSignUp) "Create a password" else "Enter your password",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryButtonContainer,
                contentColor = colors.backgroundStart,
            ),
        ) {
            Text(
                text = if (isSignUp) "Create account" else "Log in",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthOptions(
            switchPrompt = if (isSignUp) "Already have an account?" else "No account yet?",
            switchActionLabel = if (isSignUp) "Sign in" else "Sign up",
            onSwitchMode = onSwitchMode,
        )

        if (isSignUp) {
            Spacer(modifier = Modifier.height(24.dp))
            Spacer(modifier = Modifier.weight(1f))
            SignUpLegalText()
        }
    }
}
