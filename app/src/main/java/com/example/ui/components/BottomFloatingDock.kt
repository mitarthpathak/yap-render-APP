package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.InfoModalType

enum class DockTab {
    HOME,
    HELP,
    ACCOUNT
}

@Composable
fun BottomFloatingDock(
    currentTab: DockTab,
    onTabSelected: (DockTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // macOS / Telegram / WhatsApp inspired floating dock pill
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xF2FFFFFF), // Subtle translucent white
            shadowElevation = 14.dp,
            tonalElevation = 6.dp,
            modifier = Modifier
                .border(1.2.dp, Color(0xFFE5E0D4), RoundedCornerShape(32.dp))
                .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color(0x24000000))
                .testTag("floating_dock")
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(
                    label = "Home",
                    iconFilled = Icons.Filled.Home,
                    iconOutlined = Icons.Outlined.Home,
                    isSelected = currentTab == DockTab.HOME,
                    onClick = { onTabSelected(DockTab.HOME) },
                    testTag = "dock_tab_home"
                )

                DockItem(
                    label = "Help & Theory",
                    iconFilled = Icons.Filled.MenuBook,
                    iconOutlined = Icons.Outlined.MenuBook,
                    isSelected = currentTab == DockTab.HELP,
                    onClick = { onTabSelected(DockTab.HELP) },
                    testTag = "dock_tab_help"
                )

                DockItem(
                    label = "Your Account",
                    iconFilled = Icons.Filled.AccountCircle,
                    iconOutlined = Icons.Outlined.AccountCircle,
                    isSelected = currentTab == DockTab.ACCOUNT,
                    onClick = { onTabSelected(DockTab.ACCOUNT) },
                    testTag = "dock_tab_account"
                )
            }
        }
    }
}

@Composable
private fun DockItem(
    label: String,
    iconFilled: ImageVector,
    iconOutlined: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) YapHeadingBlack else Color.Transparent,
        animationSpec = spring(),
        label = "dock_item_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else YapSubtextGray,
        animationSpec = spring(),
        label = "dock_item_content"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = YapHeadingBlack),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(
            imageVector = if (isSelected) iconFilled else iconOutlined,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )

        AnimatedVisibility(visible = isSelected) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}
