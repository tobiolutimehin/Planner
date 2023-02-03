package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import kotlinx.coroutines.launch

class TasksViewModel(private val dao: TaskManagerDao) : ViewModel() {
    val tasks: LiveData<List<ManagerWithTasks>> = dao.getTaskManagers().asLiveData()

    fun getTaskManager(id: Long): LiveData<ManagerWithTasks> = dao.getTaskManager(id).asLiveData()

    fun insertTask(tasks: List<TaskEntity>) =
        viewModelScope.launch {
            dao.insertTasks(tasks)
        }

    fun saveTaskManager(string: String, tasks: List<Task>, taskManagerType: TaskManagerType) {
        viewModelScope.launch {
            dao.insertTaskManagerWithTasks(
                TaskManagerEntity(name = string, type = taskManagerType),
                tasks
            )
        }
    }

    suspend fun insertTaskManager(taskManagerEntity: TaskManagerEntity) =
        dao.insertTaskManager(taskManagerEntity)

    suspend fun deleteTask(taskEntity: TaskEntity) = viewModelScope.launch {
        dao.deleteTask(taskEntity)
    }

    suspend fun deleteTaskManager(taskManagerEntity: TaskManagerEntity) = viewModelScope.launch {
        dao.deleteTaskManager(taskManagerEntity)
    }

    suspend fun updateTask(taskEntity: TaskEntity) = viewModelScope.launch {
        dao.updateTask(taskEntity)
    }

    suspend fun updateTaskManager(taskManagerEntity: TaskManagerEntity) = viewModelScope.launch {
        dao.updateTaskManager(taskManagerEntity)
    }
}

class TasksViewModelFactory(private val dao: TaskManagerDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TasksViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
