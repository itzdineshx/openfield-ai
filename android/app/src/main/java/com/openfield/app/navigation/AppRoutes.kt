package com.openfield.app.navigation

object AppRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING_1 = "onboarding_1"
    const val ONBOARDING_2 = "onboarding_2"
    const val ONBOARDING_3 = "onboarding_3"
    const val PERMISSIONS = "permissions"
    const val MODEL_SETUP = "model_setup"

    // Main 4 Top-level Tabs
    const val HOME = "home"
    const val CHATS = "chats"
    const val TOOLS = "tools"
    const val LIBRARY = "library"

    // Feature Detail & Sub-screens
    const val CHAT_SESSION = "chat_session"
    const val IMAGE_ANALYSIS = "image_analysis"
    const val DOCUMENT_QA = "document_qa"
    const val CROSS_SOURCE_QA = "cross_source_qa"
    const val DOCUMENT_DETAIL = "document_detail"
    const val PROBLEM_SOLVER = "problem_solver"
    const val LEARN_EXPLAIN = "learn_explain"
    const val CREATE_WRITE = "create_write"
    const val TOOL_DETAIL = "tool_detail"
    const val CALCULATOR = "calculator"
    const val TASKS = "tasks"
    const val PACK_DETAIL = "pack_detail"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun chatSessionRoute(sessionId: String) = "$CHAT_SESSION/$sessionId"
    fun documentDetailRoute(docId: String) = "$DOCUMENT_DETAIL/$docId"
    fun toolDetailRoute(toolId: String) = "$TOOL_DETAIL/$toolId"
    fun packDetailRoute(packId: String) = "$PACK_DETAIL/$packId"
}

