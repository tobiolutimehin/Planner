package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType

/**
 * The AddTaskViewModel class is responsible for managing the list of tasks and the current task management type.
 */
class AddTaskViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private companion object {
        const val TASK_MANAGER_TYPE_KEY = "task_manager_type"
    }

    private var _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList

    private val _taskManagementType = savedStateHandle.getLiveData<TaskManagerType>(TASK_MANAGER_TYPE_KEY)
    val taskManagerType: LiveData<TaskManagerType> = _taskManagementType

    /**
     * Adds a task to the task list.
     *
     * @param task The task to be added.
     */
    fun addTask(task: Task) {
        val updatedTaskList = _taskList.value?.toMutableList() ?: mutableListOf()
        updatedTaskList.add(task)
        _taskList.value = updatedTaskList
    }

    /**
     * Adds a list of tasks to the task list.
     *
     * @param tasks The list of tasks to be added.
     */
    fun addTasks(tasks: List<Task>) {
        val updatedTaskList = _taskList.value?.toMutableList() ?: mutableListOf()
        updatedTaskList.addAll(tasks)
        _taskList.value = updatedTaskList
    }

    fun resetTasks(tasks: List<Task>) {
        _taskList.value = tasks
    }

    /**
     * Removes a task from the task list.
     *
     * @param task The task to be removed.
     */
    fun removeTask(task: Task) {
        val updatedTaskList = _taskList.value?.toMutableList() ?: mutableListOf()
        updatedTaskList.remove(task)
        _taskList.value = updatedTaskList
    }

    fun initializeTaskManagementType(taskManagerType: TaskManagerType) {
        if (_taskManagementType.value == null) {
            setTaskManagementType(taskManagerType)
        }
    }

    /**
     * Sets the current task management type.
     *
     * @param taskManagerType The task management type to be set.
     */
    fun setTaskManagementType(taskManagerType: TaskManagerType) {
        savedStateHandle[TASK_MANAGER_TYPE_KEY] = taskManagerType
    }
}
