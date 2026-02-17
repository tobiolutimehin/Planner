package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.SavedContactEntity
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.data.entity.TaskManagerWithTasksAndContributors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(private val dao: TaskManagerDao) : ViewModel() {
    val tasks: LiveData<List<ManagerWithTasks>> = dao.getTaskManagers().asLiveData()

    fun getTaskManager(id: Long): LiveData<ManagerWithTasks> = dao.getTaskManager(id).asLiveData()

    fun getTaskManagerWithContributors(id: Long): LiveData<TaskManagerWithTasksAndContributors> =
        dao.getTaskManagerWithContributors(id).asLiveData()

    fun saveTaskManager(
        name: String,
        tasks: List<Task>,
        taskManagerType: TaskManagerType,
        contributors: List<SavedContactEntity>,
    ) {
        viewModelScope.launch {
            dao.insertTaskManagerWithTasks(
                TaskManagerEntity(name = name, type = taskManagerType),
                tasks,
                contributors,
            )
        }
    }

    fun deleteTaskManager(taskManagerEntity: TaskManagerEntity) =
        viewModelScope.launch {
            dao.deleteTaskManagerWithTasks(taskManagerEntity)
        }

    private fun updateTask(taskEntity: TaskEntity) =
        viewModelScope.launch {
            dao.updateTask(taskEntity)
        }

    fun updateTaskManager(
        taskManagerEntity: TaskManagerEntity,
        tasks: List<Task>,
        contributors: List<SavedContactEntity>,
    ) =
        viewModelScope.launch {
            dao.updateTaskManagerWithTasks(taskManagerEntity, tasks, contributors)
        }

    fun updateTaskManagerWithTaskEntity(tasks: List<TaskEntity>) =
        viewModelScope.launch {
            tasks.forEach { updateTask(it) }
        }
}
