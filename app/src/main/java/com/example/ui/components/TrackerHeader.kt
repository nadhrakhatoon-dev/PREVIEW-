package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.TrackerTab
import com.example.ui.theme.MonoBadgeTextStyle
import com.example.util.formatTodayDate

@Composable
fun TrackerHeader(
    selectedTab: TrackerTab,
    taskCount: Int,
    expenseCount: Int,
    onSelectTab: (TrackerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLabel = remember { formatTodayDate() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedTab == TrackerTab.TASKS) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                )
                Text(
                    text = todayLabel.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.header_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("header_title")
            )
        }

        SegmentedTabBar(
            selectedTab = selectedTab,
            taskCount = taskCount,
            expenseCount = expenseCount,
            onSelectTab = onSelectTab
        )
    }
}

@Composable
private fun SegmentedTabBar(
    selectedTab: TrackerTab,
    taskCount: Int,
    expenseCount: Int,
    onSelectTab: (TrackerTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TrackerTabPill(
                label = stringResource(R.string.tab_tasks),
                count = taskCount,
                icon = Icons.Rounded.TaskAlt,
                isSelected = selectedTab == TrackerTab.TASKS,
                activeAccent = MaterialTheme.colorScheme.primary,
                activeBadgeContainer = MaterialTheme.colorScheme.primaryContainer,
                activeBadgeContent = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onSelectTab(TrackerTab.TASKS) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_tasks")
            )

            TrackerTabPill(
                label = stringResource(R.string.tab_expenses),
                count = expenseCount,
                icon = Icons.Rounded.AccountBalanceWallet,
                isSelected = selectedTab == TrackerTab.EXPENSES,
                activeAccent = MaterialTheme.colorScheme.secondary,
                activeBadgeContainer = MaterialTheme.colorScheme.secondaryContainer,
                activeBadgeContent = MaterialTheme.colorScheme.onSecondaryContainer,
                onClick = { onSelectTab(TrackerTab.EXPENSES) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_expenses")
            )
        }
    }
}

@Composable
private fun TrackerTabPill(
    label: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    activeAccent: Color,
    activeBadgeContainer: Color,
    activeBadgeContent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.surfaceContainerLowest
        } else {
            Color.Transparent
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_fg"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) activeAccent else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_icon"
    )

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .semantics { selected = isSelected }
            .clickable(role = Role.Tab, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) activeBadgeContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        style = MonoBadgeTextStyle,
                        color = if (isSelected) activeBadgeContent
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
