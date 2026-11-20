package com.openfield.app.navigation

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
                repository = repository
            )
        }
    ) {
        Scaffold { innerPadding ->
            Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination
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
    repository: OpenFieldRepository = OpenFieldRepository.getInstance()
) {
    val sessions by repository.sessions.collectAsState()
    var searchFilter by remember { mutableStateOf("") }
    val filteredSessions = remember(sessions, searchFilter) {
        if (searchFilter.isBlank()) sessions.take(8)
        else sessions.filter { it.title.contains(searchFilter, ignoreCase = true) }.take(20)
    }

    ModalDrawerSheet(
        modifier = Modifier.width(310.dp),
        drawerContainerColor = OpenFieldBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                OpenFieldLogo(size = 32.dp, showText = true, textFontSize = 18)
            }
            Text(
                text = "Offline AI Companion • Gemma 4",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldSecondaryText,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            // + New Conversation Button
            Surface(
                onClick = onNewChat,
                shape = RoundedCornerShape(12.dp),
                color = OpenFieldPrimaryBlueLight,
                border = BorderStroke(1.dp, OpenFieldPrimaryBlueBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = OpenFieldPrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "New Conversation",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = OpenFieldPrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Filter
            OutlinedTextField(
                value = searchFilter,
                onValueChange = { searchFilter = it },
                placeholder = { Text("Search chats…", fontSize = 12.5.sp, color = OpenFieldSecondaryText) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OpenFieldSecondaryText, modifier = Modifier.size(16.dp)) },
                trailingIcon = if (searchFilter.isNotBlank()) {
                    {
                        IconButton(onClick = { searchFilter = "" }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = OpenFieldSecondaryText, modifier = Modifier.size(14.dp))
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OpenFieldPrimaryBlue,
                    unfocusedBorderColor = OpenFieldCardBorder,
                    focusedContainerColor = OpenFieldSecondaryBackground,
                    unfocusedContainerColor = OpenFieldSecondaryBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Chats section
            if (filteredSessions.isNotEmpty()) {
                Text(
                    text = if (searchFilter.isBlank()) "RECENT CHATS" else "MATCHING CHATS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = OpenFieldSecondaryText,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 10.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )

                filteredSessions.forEach { session ->
                    val isSelected = currentRoute == AppRoutes.chatSessionRoute(session.id)
                    Surface(
                        onClick = { onNavigateToChat(session.id) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) OpenFieldSecondaryBackground else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = if (isSelected) OpenFieldPrimaryBlue else OpenFieldSecondaryText.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = session.title.ifBlank { "Untitled chat" },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSelected) OpenFieldNavyText else OpenFieldNavyText.copy(alpha = 0.85f),
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { repository.deleteSession(session.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete chat",
                                    tint = OpenFieldSecondaryText.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider(
                color = OpenFieldCardBorder,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            // Workspaces & Tools
            Text(
                text = "WORKSPACES",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OpenFieldSecondaryText,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 10.5.sp
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            NavigationDrawerItem(
                label = { Text("All Chats") },
                selected = currentRoute == AppRoutes.CHATS,
                onClick = { onNavigateToRoute(AppRoutes.CHATS) },
                icon = { Icon(Icons.Default.ChatBubble, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Knowledge Library") },
                selected = currentRoute == AppRoutes.LIBRARY,
                onClick = { onNavigateToRoute(AppRoutes.LIBRARY) },
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Create & Write Studio") },
                selected = currentRoute == AppRoutes.CREATE_WRITE,
                onClick = { onNavigateToRoute(AppRoutes.CREATE_WRITE) },
                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Workflows & Tasks") },
                selected = currentRoute == AppRoutes.TASKS,
                onClick = { onNavigateToRoute(AppRoutes.TASKS) },
                icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAddCheck, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("AI Tools") },
                selected = currentRoute == AppRoutes.TOOLS,
                onClick = { onNavigateToRoute(AppRoutes.TOOLS) },
                icon = { Icon(Icons.Default.Build, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
            NavigationDrawerItem(
                label = { Text("Settings") },
                selected = currentRoute == AppRoutes.SETTINGS,
                onClick = { onNavigateToRoute(AppRoutes.SETTINGS) },
                icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = OpenFieldSecondaryBackground,
                border = BorderStroke(1.dp, OpenFieldCardBorder),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = OpenFieldSecondaryText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "100% Offline • Private on Device",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = OpenFieldSecondaryText,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

