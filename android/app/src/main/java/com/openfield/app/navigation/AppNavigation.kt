package com.openfield.app.navigation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.openfield.app.feature.splash.SplashScreen
import com.openfield.app.feature.tools.ToolDetailScreen
import com.openfield.app.feature.tools.ToolsScreen
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

    var showProfileSheet by remember { mutableStateOf(false) }

    val isTopLevelRoute = currentRoute in listOf(
        AppRoutes.HOME,
        AppRoutes.CHATS,
        AppRoutes.TOOLS,
        AppRoutes.LIBRARY
    )

    Scaffold(
        bottomBar = {
            if (isTopLevelRoute) {
                OpenFieldBottomBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(AppRoutes.HOME) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
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

                // 2. Onboarding (Ask Anything, Understand & Solve, Your Knowledge)
                composable(AppRoutes.ONBOARDING_1) {
                    OnboardingScreen(
                        onFinishOnboarding = {
                            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                                .edit()
                                .putBoolean(KEY_ONBOARDING_DONE, true)
                                .apply()
                            navController.navigate(AppRoutes.HOME) {
                                popUpTo(AppRoutes.ONBOARDING_1) { inclusive = true }
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
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 6. Image Analysis
                composable(AppRoutes.IMAGE_ANALYSIS) {
                    ImageAnalysisScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 7. Document Q&A
                composable(AppRoutes.DOCUMENT_QA) {
                    DocumentQAScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToDocumentDetail = { docId ->
                            navController.navigate(AppRoutes.documentDetailRoute(docId))
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
                        }
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

