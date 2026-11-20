package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.TrackerRepository
import com.example.ui.TrackerScreen
import com.example.ui.TrackerViewModel
import com.example.ui.TrackerViewModelFactory
import com.example.ui.theme.MyDailyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = TrackerRepository(
            taskDao = database.taskDao(),
            expenseDao = database.expenseDao()
        )
        val viewModelFactory = TrackerViewModelFactory(repository)

        setContent {
            MyDailyTrackerTheme {
                val trackerViewModel: TrackerViewModel = viewModel(factory = viewModelFactory)
                TrackerScreen(viewModel = trackerViewModel)
            }
        }
    }
}
