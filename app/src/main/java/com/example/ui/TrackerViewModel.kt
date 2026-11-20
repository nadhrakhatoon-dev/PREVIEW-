package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ExpenseEntity
import com.example.data.TaskEntity
import com.example.data.TrackerRepository
import com.example.util.parseRupeesToPaise
import com.example.util.sanitizeAmountInput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TrackerTab {
    TASKS,
    EXPENSES
}

sealed interface TrackerEvent {
    data class TaskDeleted(val task: TaskEntity) : TrackerEvent
    data class ExpenseDeleted(val expense: ExpenseEntity) : TrackerEvent
}

data class TrackerUiState(
    val selectedTab: TrackerTab = TrackerTab.TASKS,
    val tasks: List<TaskEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val taskInput: String = "",
    val expenseNameInput: String = "",
    val expenseAmountInput: String = "",
    val showExpenseNameError: Boolean = false,
    val showExpenseAmountError: Boolean = false
) {
    val completedTaskCount: Int
        get() = tasks.count { it.isCompleted }

    val totalExpensePaise: Long
        get() = expenses.sumOf { it.amountPaise }
}

private data class FormInputState(
    val selectedTab: TrackerTab = TrackerTab.TASKS,
    val taskInput: String = "",
    val expenseNameInput: String = "",
    val expenseAmountInput: String = "",
    val showExpenseNameError: Boolean = false,
    val showExpenseAmountError: Boolean = false
)

class TrackerViewModel(
    private val repository: TrackerRepository
) : ViewModel() {

    private val formState = MutableStateFlow(FormInputState())

    private val _events = MutableSharedFlow<TrackerEvent>()
    val events: SharedFlow<TrackerEvent> = _events.asSharedFlow()

    val uiState: StateFlow<TrackerUiState> = combine(
        repository.tasks,
        repository.expenses,
        formState
    ) { tasks, expenses, form ->
        TrackerUiState(
            selectedTab = form.selectedTab,
            tasks = tasks,
            expenses = expenses,
            taskInput = form.taskInput,
            expenseNameInput = form.expenseNameInput,
            expenseAmountInput = form.expenseAmountInput,
            showExpenseNameError = form.showExpenseNameError,
            showExpenseAmountError = form.showExpenseAmountError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrackerUiState()
    )

    fun selectTab(tab: TrackerTab) {
        formState.update { it.copy(selectedTab = tab) }
    }

    fun onTaskInputChanged(value: String) {
        formState.update { it.copy(taskInput = value) }
    }

    fun addTask() {
        val title = formState.value.taskInput.trim()
        if (title.isEmpty()) return
        formState.update { it.copy(taskInput = "") }
        viewModelScope.launch {
            repository.addTask(title)
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task.id)
            _events.emit(TrackerEvent.TaskDeleted(task))
        }
    }

    fun restoreTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.restoreTask(task)
        }
    }

    fun onExpenseNameChanged(value: String) {
        formState.update {
            it.copy(
                expenseNameInput = value,
                showExpenseNameError = false
            )
        }
    }

    fun onExpenseAmountChanged(value: String) {
        val sanitized = sanitizeAmountInput(value)
        formState.update {
            it.copy(
                expenseAmountInput = sanitized,
                showExpenseAmountError = false
            )
        }
    }

    fun addExpense() {
        val current = formState.value
        val trimmedName = current.expenseNameInput.trim()
        val parsedPaise = parseRupeesToPaise(current.expenseAmountInput)

        val nameInvalid = trimmedName.isEmpty()
        val amountInvalid = parsedPaise == null

        if (nameInvalid || amountInvalid) {
            formState.update {
                it.copy(
                    showExpenseNameError = nameInvalid,
                    showExpenseAmountError = amountInvalid
                )
            }
            return
        }

        formState.update {
            it.copy(
                expenseNameInput = "",
                expenseAmountInput = "",
                showExpenseNameError = false,
                showExpenseAmountError = false
            )
        }

        viewModelScope.launch {
            repository.addExpense(trimmedName, parsedPaise)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense.id)
            _events.emit(TrackerEvent.ExpenseDeleted(expense))
        }
    }

    fun restoreExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.restoreExpense(expense)
        }
    }
}

class TrackerViewModelFactory(
    private val repository: TrackerRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackerViewModel::class.java)) {
            return TrackerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
