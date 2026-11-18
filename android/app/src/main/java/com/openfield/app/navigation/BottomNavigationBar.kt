package com.openfield.app.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openfield.app.core.theme.OpenFieldBackground
import com.openfield.app.core.theme.OpenFieldCardBorder
import com.openfield.app.core.theme.OpenFieldNavyText
import com.openfield.app.core.theme.OpenFieldPrimaryBlue
import com.openfield.app.core.theme.OpenFieldPrimaryBlueLight
import com.openfield.app.core.theme.OpenFieldSecondaryText

data class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    NavItem(
        route = AppRoutes.HOME,
        title = "Home",
        selectedIcon = Icons.Default.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    NavItem(
        route = AppRoutes.CHATS,
        title = "Chats",
        selectedIcon = Icons.Default.ChatBubble,
        unselectedIcon = Icons.Default.ChatBubbleOutline
    ),
    NavItem(
        route = AppRoutes.TOOLS,
        title = "Tools",
        selectedIcon = Icons.Default.GridView,
        unselectedIcon = Icons.Default.GridView
    ),
    NavItem(
        route = AppRoutes.LIBRARY,
        title = "Library",
        selectedIcon = Icons.AutoMirrored.Filled.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Filled.MenuBook
    )
)

@Composable
fun OpenFieldBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = OpenFieldBackground,
        shadowElevation = 8.dp,
        border = BorderStroke(0.5.dp, OpenFieldCardBorder)
    ) {
        NavigationBar(
            containerColor = OpenFieldBackground,
            modifier = Modifier.height(68.dp),
            tonalElevation = 0.dp
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            onNavigateToRoute(item.route)
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OpenFieldPrimaryBlue,
                        selectedTextColor = OpenFieldPrimaryBlue,
                        unselectedIconColor = OpenFieldSecondaryText,
                        unselectedTextColor = OpenFieldSecondaryText,
                        indicatorColor = OpenFieldPrimaryBlueLight
                    )
                )
            }
        }
    }
}

