package com.example.data

import kotlinx.coroutines.flow.Flow

class TrackerRepository(
    private val taskDao: TaskDao,
    private val expenseDao: ExpenseDao
) {
    val tasks: Flow<List<TaskEntity>> = taskDao.observeTasks()
    val expenses: Flow<List<ExpenseEntity>> = expenseDao.observeExpenses()

    suspend fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isNotEmpty()) {
            taskDao.insertTask(TaskEntity(title = trimmed))
        }
    }

    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun toggleTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun addExpense(name: String, amountPaise: Long) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && amountPaise > 0L) {
            expenseDao.insertExpense(
                ExpenseEntity(
                    name = trimmed,
                    amountPaise = amountPaise
                )
            )
        }
    }

    suspend fun restoreExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }
}
