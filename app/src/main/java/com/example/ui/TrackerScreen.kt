package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ExpensesTabContent
import com.example.ui.components.TasksTabContent
import com.example.ui.components.TrackerHeader

@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    BackHandler(enabled = uiState.selectedTab == TrackerTab.EXPENSES) {
        viewModel.selectTab(TrackerTab.TASKS)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            when (event) {
                is TrackerEvent.TaskDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.task_deleted_snackbar),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreTask(event.task)
                    }
                }
                is TrackerEvent.ExpenseDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(R.string.expense_deleted_snackbar),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreExpense(event.expense)
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                TrackerHeader(
                    selectedTab = uiState.selectedTab,
                    taskCount = uiState.tasks.size,
                    expenseCount = uiState.expenses.size,
                    onSelectTab = viewModel::selectTab
                )

                AnimatedContent(
                    targetState = uiState.selectedTab,
                    transitionSpec = {
                        val direction = if (targetState == TrackerTab.EXPENSES) 1 else -1
                        (slideInHorizontally(animationSpec = tween(220)) { it / 6 * direction } +
                            fadeIn(animationSpec = tween(220))) togetherWith
                            (slideOutHorizontally(animationSpec = tween(180)) { -it / 6 * direction } +
                                fadeOut(animationSpec = tween(180)))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "tracker_tab_content"
                ) { tab ->
                    when (tab) {
                        TrackerTab.TASKS -> {
                            TasksTabContent(
                                taskInput = uiState.taskInput,
                                tasks = uiState.tasks,
                                completedCount = uiState.completedTaskCount,
                                onTaskInputChanged = viewModel::onTaskInputChanged,
                                onAddTask = viewModel::addTask,
                                onToggleTask = viewModel::toggleTask,
                                onDeleteTask = viewModel::deleteTask
                            )
                        }
                        TrackerTab.EXPENSES -> {
                            ExpensesTabContent(
                                expenses = uiState.expenses,
                                totalExpensePaise = uiState.totalExpensePaise,
                                expenseNameInput = uiState.expenseNameInput,
                                expenseAmountInput = uiState.expenseAmountInput,
                                showNameError = uiState.showExpenseNameError,
                                showAmountError = uiState.showExpenseAmountError,
                                onExpenseNameChanged = viewModel::onExpenseNameChanged,
                                onExpenseAmountChanged = viewModel::onExpenseAmountChanged,
                                onAddExpense = viewModel::addExpense,
                                onDeleteExpense = viewModel::deleteExpense
                            )
                        }
                    }
                }
            }
        }
    }
}
