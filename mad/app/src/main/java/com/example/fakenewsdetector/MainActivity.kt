package com.example.fakenewsdetector

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fakenewsdetector.data.AppSettings
import com.example.fakenewsdetector.data.SettingsStore
import com.example.fakenewsdetector.domain.CheckResult
import com.example.fakenewsdetector.ui.CheckScreen
import com.example.fakenewsdetector.ui.HistoryScreen
import com.example.fakenewsdetector.ui.OnboardingScreen
import com.example.fakenewsdetector.ui.ResultScreen
import com.example.fakenewsdetector.ui.SettingsScreen
import com.example.fakenewsdetector.ui.SystemBarIcons
import com.example.fakenewsdetector.ui.openFactCheck
import com.example.fakenewsdetector.ui.sharePlainText
import com.example.fakenewsdetector.ui.theme.FakeNewsDetectorTheme
import com.example.fakenewsdetector.ui.theme.isDark
import com.example.fakenewsdetector.viewmodel.CheckViewModel
import com.example.fakenewsdetector.viewmodel.HistoryViewModel
import com.example.fakenewsdetector.viewmodel.SettingsViewModel
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private var pendingShare by mutableStateOf<SharedPost?>(null)
    private var pendingResultId by mutableStateOf<Long?>(null)

    override fun attachBaseContext(newBase: Context) {
        val language = SettingsStore(newBase).settings.value.language
        super.attachBaseContext(SettingsStore.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        setContent {
            val container = (application as FakeNewsApp).container
            val settingsViewModel: SettingsViewModel =
                viewModel(factory = SettingsViewModel.factory(container))
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            FakeNewsDetectorTheme(mode = settings.themeMode) {
                SystemBarIcons(window = window, dark = settings.themeMode.isDark())
                AppRoot(
                    container = container,
                    settings = settings,
                    settingsViewModel = settingsViewModel,
                    share = pendingShare,
                    resultId = pendingResultId,
                    onConsumeShare = { pendingShare = null },
                    onConsumeResultId = { pendingResultId = null },
                    onRestartForLanguage = { recreate() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_SEND) {
            val text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
            if (!text.isNullOrBlank()) {
                pendingShare = SharedPost(
                    text = text,
                    sourceApp = intent.getStringExtra(ShareReceiverActivity.EXTRA_SOURCE_APP)
                )
            }
        }
        intent.getLongExtra(EXTRA_RESULT_ID, -1L).takeIf { it > 0L }?.let { pendingResultId = it }
    }

    companion object {
        /** Kept for compatibility with the original share hand-off. */
        const val EXTRA_SHARED_TEXT = "shared_text"

        /** Opens the stored result with this id (used by the quick-check sheet). */
        const val EXTRA_RESULT_ID = "result_id"
    }
}

private data class SharedPost(val text: String, val sourceApp: String?)

private sealed interface MainRoute {
    data object Check : MainRoute
    data object History : MainRoute
    data object Settings : MainRoute
    data class Result(val result: CheckResult, val origin: MainRoute) : MainRoute
}

@Composable
private fun AppRoot(
    container: AppContainer,
    settings: AppSettings,
    settingsViewModel: SettingsViewModel,
    share: SharedPost?,
    resultId: Long?,
    onConsumeShare: () -> Unit,
    onConsumeResultId: () -> Unit,
    onRestartForLanguage: () -> Unit
) {
    val checkViewModel: CheckViewModel = viewModel(factory = CheckViewModel.factory(container))
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
    val context = LocalContext.current

    var route by remember { mutableStateOf<MainRoute>(MainRoute.Check) }
    var showOnboarding by remember { mutableStateOf(!settings.onboardingSeen) }

    val rechecked by historyViewModel.rechecked.collectAsStateWithLifecycle()
    LaunchedEffect(rechecked) {
        rechecked?.let {
            route = MainRoute.Result(it, MainRoute.History)
            historyViewModel.consumeRechecked()
        }
    }

    LaunchedEffect(share) {
        val incoming = share ?: return@LaunchedEffect
        route = MainRoute.Check
        checkViewModel.analyze(incoming.text, incoming.sourceApp)
        onConsumeShare()
    }

    LaunchedEffect(resultId) {
        val id = resultId ?: return@LaunchedEffect
        container.historyRepository.find(id)?.let { route = MainRoute.Result(it, MainRoute.History) }
        onConsumeResultId()
    }

    BackHandler(enabled = route != MainRoute.Check) {
        route = when (val current = route) {
            is MainRoute.Result -> current.origin
            else -> MainRoute.Check
        }
    }

    if (showOnboarding) {
        BackHandler { showOnboarding = false; settingsViewModel.setOnboardingSeen(true) }
        OnboardingScreen(onDone = {
            settingsViewModel.setOnboardingSeen(true)
            showOnboarding = false
        })
        return
    }

    when (val current = route) {
        is MainRoute.Result -> {
            ResultScreen(
                result = current.result,
                onBack = { route = current.origin },
                onShareCorrection = {
                    sharePlainText(
                        context,
                        container.correctionFactory.build(current.result),
                        context.getString(R.string.share_chooser_title)
                    )
                },
                onSecondaryAction = {
                    if (current.origin == MainRoute.History) {
                        historyViewModel.recheck(current.result)
                    } else {
                        checkViewModel.clear()
                        route = MainRoute.Check
                    }
                },
                secondaryLabel = stringResource(
                    if (current.origin == MainRoute.History) {
                        R.string.action_recheck
                    } else {
                        R.string.action_check_another
                    }
                ),
                onOpenSource = { openFactCheck(context, it.url) }
            )
        }

        MainRoute.Settings -> {
            SettingsScreen(
                settings = settings,
                onBack = { route = MainRoute.Check },
                onThemeModeChange = settingsViewModel::setThemeMode,
                onLanguageChange = { language ->
                    if (language != settings.language) {
                        settingsViewModel.setLanguage(language)
                        onRestartForLanguage()
                    }
                },
                onClearHistory = { historyViewModel.clearAll() }
            )
        }

        else -> {
            val selectedTab = if (route == MainRoute.History) 1 else 0
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 0.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { route = MainRoute.Check },
                            icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_check)) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { route = MainRoute.History },
                            icon = { Icon(Icons.Filled.History, contentDescription = null) },
                            label = { Text(stringResource(R.string.nav_history)) }
                        )
                    }
                }
            ) { innerPadding ->
                if (selectedTab == 1) {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onOpenResult = { route = MainRoute.Result(it, MainRoute.History) },
                        onRechecked = { route = MainRoute.Result(it, MainRoute.History) },
                        modifier = Modifier.padding(innerPadding)
                    )
                } else {
                    CheckScreen(
                        viewModel = checkViewModel,
                        onOpenSettings = { route = MainRoute.Settings },
                        onOpenResult = { route = MainRoute.Result(it, MainRoute.Check) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
