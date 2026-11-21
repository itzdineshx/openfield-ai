package com.openfield.app.navigation

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.openfield.app.core.components.CruxLogo
import com.openfield.app.core.components.OpenFieldLogo
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueBorder
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryBackground
import com.openfield.app.core.theme.OpenFieldSecondaryText
import com.openfield.app.data.repository.OpenFieldRepository
import com.openfield.app.feature.chat.ChatScreen
import com.openfield.app.feature.document.CrossSourceQaScreen
import com.openfield.app.feature.document.DocumentDetailScreen
import com.openfield.app.feature.document.DocumentQAScreen
import com.openfield.app.feature.history.HistoryScreen
import com.openfield.app.feature.home.HomeScreen
import com.openfield.app.feature.image.ImageAnalysisScreen
import com.openfield.app.feature.learn.LearnScreen
import com.openfield.app.feature.library.KnowledgeCenterScreen
import com.openfield.app.feature.library.KnowledgePackDetailScreen
import com.openfield.app.feature.library.LibraryScreen
import com.openfield.app.feature.onboarding.OnboardingScreen
import com.openfield.app.feature.problem.ProblemSolverScreen
import com.openfield.app.feature.settings.AboutScreen
import com.openfield.app.feature.settings.ProfileMenuSheet
import com.openfield.app.feature.settings.SettingsScreen
import com.openfield.app.feature.setup.ModelSetupScreen
import com.openfield.app.feature.splash.SplashScreen
import com.openfield.app.feature.tasks.TaskAutomationScreen
import com.openfield.app.feature.tools.ToolDetailScreen
import com.openfield.app.feature.tools.ToolsScreen
import com.openfield.app.feature.tools.math.CalculatorConverterScreen
import com.openfield.app.feature.write.CreateWriteScreen
import kotlinx.coroutines.launch

private const val PREFS_NAME = "openfield_prefs"
private const val KEY_ONBOARDING_DONE = "onboarding_completed"

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = remember {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_ONBOARDING_DONE, false)) AppRoutes.HOME else AppRoutes.SPLASH
    }

    val targetRoute by NavigationDispatcher.targetRoute.collectAsState()
    LaunchedEffect(targetRoute) {
        targetRoute?.let { route ->
            navController.navigate(route) {
                launchSingleTop = true
            }
            NavigationDispatcher.clear()
        }
    }

    var showProfileSheet by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val repository = remember { OpenFieldRepository.getInstance() }

    val isTopLevelRoute = currentRoute in listOf(
        AppRoutes.HOME,
        AppRoutes.CHATS,
        AppRoutes.TOOLS,
        AppRoutes.LIBRARY
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevelRoute,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(route) {
                            popUpTo(AppRoutes.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onNavigateToChat = { sessionId ->
                    scope.launch {
                        drawerState.close()
                        navController.navigate(AppRoutes.chatSessionRoute(sessionId))
                    }
                },
                onNewChat = {
                    scope.launch {
                        drawerState.close()
                        val newId = repository.createNewSessionNow("New Chat")
                        navController.navigate(AppRoutes.chatSessionRoute(newId))
                    }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } },
                repository = repository
            )
        }
    ) {
        Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
            Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Splash Screen
                composable(AppRoutes.SPLASH) {
                    SplashScreen(
                        onGetStarted = {
                            navController.navigate(AppRoutes.ONBOARDING_1) {
                                popUpTo(AppRoutes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                // 2. Onboarding (Ask Anything, Understand & Solve, Your Knowledge, Permissions)
                composable(AppRoutes.ONBOARDING_1) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            navController.navigate(AppRoutes.MODEL_SETUP) {
                                popUpTo(AppRoutes.ONBOARDING_1) { inclusive = true }
                            }
                        }
                    )
                }

                // Model Setup & Download Screen (Post-onboarding first-run setup)
                composable(AppRoutes.MODEL_SETUP) {
                    ModelSetupScreen(
                        onComplete = {
                            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_ONBOARDING_DONE, true)
                                .apply()
                            navController.navigate(AppRoutes.HOME) {
                                popUpTo(AppRoutes.MODEL_SETUP) { inclusive = true }
                            }
                        },
                        onSkipToHome = {
                            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_ONBOARDING_DONE, true)
                                .apply()
                            navController.navigate(AppRoutes.HOME) {
                                popUpTo(AppRoutes.MODEL_SETUP) { inclusive = true }
                            }
                        }
                    )
                }

                // 3. Home Dashboard
                composable(AppRoutes.HOME) {
                    HomeScreen(
                        onNavigateToChat = { sessionId ->
                            navController.navigate(AppRoutes.chatSessionRoute(sessionId))
                        },
                        onNavigateToImageAnalysis = {
                            navController.navigate(AppRoutes.IMAGE_ANALYSIS)
                        },
                        onNavigateToDocumentQA = {
                            navController.navigate(AppRoutes.DOCUMENT_QA)
                        },
                        onNavigateToProblemSolver = {
                            navController.navigate(AppRoutes.PROBLEM_SOLVER)
                        },
                        onNavigateToLearnExplain = {
                            navController.navigate(AppRoutes.LEARN_EXPLAIN)
                        },
                        onNavigateToCreateWrite = {
                            navController.navigate(AppRoutes.CREATE_WRITE)
                        },
                        onOpenProfileMenu = {
                            showProfileSheet = true
                        },
                        onNavigateToTasks = {
                            navController.navigate(AppRoutes.TASKS)
                        },
                        onNavigateToModelSetup = {
                            navController.navigate(AppRoutes.MODEL_SETUP)
                        },
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }

                // 4. Chat History
                composable(AppRoutes.CHATS) {
                    HistoryScreen(
                        onNavigateToChat = { sessionId ->
                            navController.navigate(AppRoutes.chatSessionRoute(sessionId))
                        }
                    )
                }

                // 5. AI Chat Session
                composable(
                    route = "${AppRoutes.CHAT_SESSION}/{sessionId}",
                    arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
                    ChatScreen(
                        sessionId = sessionId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToStudio = { navController.navigate(AppRoutes.CREATE_WRITE) },
                        onNavigateToTasks = { navController.navigate(AppRoutes.TASKS) }
                    )
                }

                // 6. Image Analysis
                composable(AppRoutes.IMAGE_ANALYSIS) {
                    ImageAnalysisScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProblemSolver = {
                            navController.navigate(AppRoutes.PROBLEM_SOLVER)
                        }
                    )
                }

                // 7. Document Q&A
                composable(AppRoutes.DOCUMENT_QA) {
                    DocumentQAScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDocumentDetail = { docId ->
                            navController.navigate(AppRoutes.documentDetailRoute(docId))
                        },
                        onNavigateToCreateWrite = {
                            navController.navigate(AppRoutes.CREATE_WRITE)
                        }
                    )
                }

                // 8. Document Detail
                composable(
                    route = "${AppRoutes.DOCUMENT_DETAIL}/{docId}",
                    arguments = listOf(navArgument("docId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val docId = backStackEntry.arguments?.getString("docId").orEmpty()
                    DocumentDetailScreen(
                        docId = docId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToQA = {
                            navController.navigate(AppRoutes.DOCUMENT_QA)
                        }
                    )
                }

                // 9. Solve a Problem
                composable(AppRoutes.PROBLEM_SOLVER) {
                    ProblemSolverScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 10. Learn & Explain
                composable(AppRoutes.LEARN_EXPLAIN) {
                    LearnScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 11. AI Tools List
                composable(AppRoutes.TOOLS) {
                    ToolsScreen(
                        onNavigateToToolDetail = { toolId ->
                            navController.navigate(AppRoutes.toolDetailRoute(toolId))
                        },
                        onNavigateToImageAnalysis = {
                            navController.navigate(AppRoutes.IMAGE_ANALYSIS)
                        },
                        onNavigateToDocumentQA = {
                            navController.navigate(AppRoutes.DOCUMENT_QA)
                        },
                        onNavigateToCreateWrite = {
                            navController.navigate(AppRoutes.CREATE_WRITE)
                        },
                        onNavigateToCalculator = {
                            navController.navigate(AppRoutes.CALCULATOR)
                        },
                        onNavigateToTasks = {
                            navController.navigate(AppRoutes.TASKS)
                        }
                    )
                }

                // 11b. Offline Calculator & Units (Deterministic)
                composable(AppRoutes.CALCULATOR) {
                    CalculatorConverterScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 11c. Personal Task Automation (Gate 6)
                composable(AppRoutes.TASKS) {
                    TaskAutomationScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 12. Create & Write Dedicated Studio
                composable(AppRoutes.CREATE_WRITE) {
                    CreateWriteScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 12. AI Tool Detail
                composable(
                    route = "${AppRoutes.TOOL_DETAIL}/{toolId}",
                    arguments = listOf(navArgument("toolId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val toolId = backStackEntry.arguments?.getString("toolId") ?: "summarize"
                    ToolDetailScreen(
                        toolId = toolId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 13. Knowledge Center (Phase 3 Unified Personal Knowledge)
                composable(AppRoutes.LIBRARY) {
                    val repo = remember { OpenFieldRepository.getInstance() }
                    val coroutineScope = rememberCoroutineScope()
                    KnowledgeCenterScreen(
                        repository = repo,
                        onNavigateToPackDetail = { packId ->
                            navController.navigate(AppRoutes.packDetailRoute(packId))
                        },
                        onNavigateToDocumentDetail = { docId ->
                            navController.navigate(AppRoutes.documentDetailRoute(docId))
                        },
                        onNavigateToCrossSourceQa = { sourceIds, collectionId ->
                            coroutineScope.launch {
                                val sources = sourceIds.mapNotNull { repo.knowledgeSourceRegistry.getSourceById(it) }
                                val coll = collectionId?.let { cid -> repo.collections.value.find { it.id == cid } }
                                repo.crossSourceQaController.setScope(sources, coll)
                                navController.navigate(AppRoutes.CROSS_SOURCE_QA)
                            }
                        },
                        onOpenDrawerOrMenu = {
                            showProfileSheet = true
                        }
                    )
                }

                // 13b. Cross-Source Multi-Document Q&A
                composable(AppRoutes.CROSS_SOURCE_QA) {
                    CrossSourceQaScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 14. Knowledge Pack Detail
                composable(
                    route = "${AppRoutes.PACK_DETAIL}/{packId}",
                    arguments = listOf(navArgument("packId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val packId = backStackEntry.arguments?.getString("packId").orEmpty()
                    KnowledgePackDetailScreen(
                        packId = packId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 15. Settings
                composable(AppRoutes.SETTINGS) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAbout = {
                            navController.navigate(AppRoutes.ABOUT)
                        }
                    )
                }

                // 16. About OpenField
                composable(AppRoutes.ABOUT) {
                    AboutScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
    }

    // Profile Menu Bottom Sheet
    ProfileMenuSheet(
        show = showProfileSheet,
        onDismiss = { showProfileSheet = false },
        onNavigateToSettings = {
            navController.navigate(AppRoutes.SETTINGS)
        },
        onNavigateToAbout = {
            navController.navigate(AppRoutes.ABOUT)
        }
    )
}

@Composable
private fun AppDrawerContent(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onCloseDrawer: () -> Unit,
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val focusManager = LocalFocusManager.current
    val sessions by repository.sessions.collectAsState()
    var searchFilter by remember { mutableStateOf("") }

    val exploreItems = remember {
        listOf(
            DrawerFeatureItem("All AI Tools", AppRoutes.TOOLS, Icons.Default.Build),
            DrawerFeatureItem("Library", AppRoutes.LIBRARY, Icons.AutoMirrored.Filled.MenuBook),
            DrawerFeatureItem("Tasks", AppRoutes.TASKS, Icons.AutoMirrored.Filled.PlaylistAddCheck)
        )
    }

    val workspaceItems = remember {
        listOf(
            DrawerFeatureItem("Ask AI", AppRoutes.HOME, Icons.Default.AutoAwesome),
            DrawerFeatureItem("Analyze Image", AppRoutes.IMAGE_ANALYSIS, Icons.Default.Image),
            DrawerFeatureItem("Ask Document", AppRoutes.DOCUMENT_QA, Icons.Default.Description),
            DrawerFeatureItem("Solve a Problem", AppRoutes.PROBLEM_SOLVER, Icons.Default.Build),
            DrawerFeatureItem("Learn & Explain", AppRoutes.LEARN_EXPLAIN, Icons.Default.Lightbulb),
            DrawerFeatureItem("Create & Write", AppRoutes.CREATE_WRITE, Icons.Default.Edit)
        )
    }

    val allSearchableItems = remember { exploreItems + workspaceItems }

    val filteredFeatures = remember(searchFilter, allSearchableItems) {
        if (searchFilter.isBlank()) emptyList()
        else allSearchableItems.filter {
            it.title.contains(searchFilter, ignoreCase = true)
        }
    }

    val filteredSessions = remember(searchFilter, sessions) {
        if (searchFilter.isBlank()) sessions
        else sessions.filter { it.title.contains(searchFilter, ignoreCase = true) }
    }

    ModalDrawerSheet(
        modifier = Modifier.width(304.dp),
        drawerContainerColor = OpenFieldBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- PINNED HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 14.dp, end = 10.dp, bottom = 8.dp)
            ) {
                // Brand row + Close button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CruxLogo(size = 30.dp, showText = true, textFontSize = 17)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onCloseDrawer()
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close menu",
                            tint = OpenFieldSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // New conversation CTA
                Surface(
                    onClick = {
                        focusManager.clearFocus()
                        onNewChat()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = OpenFieldPrimaryBlueLight,
                    border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = OpenFieldPrimaryBlue,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "New conversation",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = OpenFieldPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(7.dp))

                // Search pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = OpenFieldSecondaryBackground,
                    border = BorderStroke(0.5.dp, OpenFieldCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchFilter.isEmpty()) {
                                Text(
                                    text = "Search…",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = OpenFieldSecondaryText.copy(alpha = 0.55f),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            BasicTextField(
                                value = searchFilter,
                                onValueChange = { searchFilter = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = OpenFieldNavyText,
                                    fontSize = 12.sp
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (searchFilter.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    searchFilter = ""
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = OpenFieldSecondaryText.copy(alpha = 0.6f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = OpenFieldCardBorder.copy(alpha = 0.5f))

            // --- SCROLLABLE BODY ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                if (searchFilter.isNotBlank()) {
                    if (filteredFeatures.isNotEmpty()) {
                        DrawerSectionHeader("TOOLS & MODES")
                        filteredFeatures.forEach { item ->
                            DrawerNavItem(
                                label = item.title,
                                icon = item.icon,
                                selected = currentRoute == item.route,
                                onClick = {
                                    focusManager.clearFocus()
                                    onNavigateToRoute(item.route)
                                }
                            )
                        }
                    }
                    if (filteredSessions.isNotEmpty()) {
                        DrawerSectionHeader("CHATS")
                        filteredSessions.forEach { session ->
                            DrawerChatRow(
                                title = session.title,
                                isSelected = currentRoute == AppRoutes.chatSessionRoute(session.id),
                                onClick = {
                                    focusManager.clearFocus()
                                    onNavigateToChat(session.id)
                                },
                                onDelete = { repository.deleteSession(session.id) }
                            )
                        }
                    }
                    if (filteredFeatures.isEmpty() && filteredSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results for \"$searchFilter\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                } else {
                    // EXPLORE section — AI Tools, Library, Tasks
                    DrawerSectionHeader("EXPLORE")
                    exploreItems.forEach { item ->
                        DrawerNavItem(
                            label = item.title,
                            icon = item.icon,
                            selected = currentRoute == item.route,
                            onClick = {
                                focusManager.clearFocus()
                                onNavigateToRoute(item.route)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        color = OpenFieldCardBorder.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // WORKSPACE section — AI interaction modes
                    DrawerSectionHeader("WORKSPACE")
                    workspaceItems.forEach { item ->
                        DrawerNavItem(
                            label = item.title,
                            icon = item.icon,
                            selected = currentRoute == item.route,
                            onClick = {
                                focusManager.clearFocus()
                                onNavigateToRoute(item.route)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(
                        color = OpenFieldCardBorder.copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // RECENT CHATS section
                    DrawerSectionHeader("RECENT CHATS")
                    if (sessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "No conversations yet. Tap + to start.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = OpenFieldSecondaryText.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else {
                        sessions.forEach { session ->
                            DrawerChatRow(
                                title = session.title,
                                isSelected = currentRoute == AppRoutes.chatSessionRoute(session.id),
                                onClick = {
                                    focusManager.clearFocus()
                                    onNavigateToChat(session.id)
                                },
                                onDelete = { repository.deleteSession(session.id) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- PINNED FOOTER ---
            HorizontalDivider(color = OpenFieldCardBorder.copy(alpha = 0.5f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                DrawerNavItem(
                    label = "Settings",
                    icon = Icons.Default.Settings,
                    selected = currentRoute == AppRoutes.SETTINGS,
                    onClick = {
                        focusManager.clearFocus()
                        onNavigateToRoute(AppRoutes.SETTINGS)
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Private & On-Device",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OpenFieldSecondaryText.copy(alpha = 0.6f),
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }
    }
}

private data class DrawerFeatureItem(
    val title: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tag: String? = null
)

@Composable
private fun DrawerNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    trailingBadge: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) OpenFieldPrimaryBlueLight else Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) OpenFieldPrimaryBlue else OpenFieldNavyText.copy(alpha = 0.72f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (selected) OpenFieldPrimaryBlue else OpenFieldNavyText,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 13.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (trailingBadge != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (selected) OpenFieldPrimaryBlue.copy(alpha = 0.15f) else OpenFieldSecondaryBackground
                ) {
                    Text(
                        text = trailingBadge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (selected) OpenFieldPrimaryBlue else OpenFieldSecondaryText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerChatRow(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) OpenFieldSecondaryBackground else Color.Transparent,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubble,
                contentDescription = null,
                tint = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryText.copy(alpha = 0.65f),
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title.ifBlank { "Untitled chat" },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isSelected) OpenFieldNavyText else OpenFieldNavyText.copy(alpha = 0.85f),
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 12.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete chat",
                    tint = OpenFieldSecondaryText.copy(alpha = 0.45f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = OpenFieldSecondaryText,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            fontSize = 10.sp
        ),
        modifier = modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 2.dp)
    )
}

