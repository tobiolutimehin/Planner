package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType

/**
 * The AddTaskViewModel class is responsible for managing the list of tasks and the current task management type.
 */
class AddTaskViewModel : ViewModel() {

    private var _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList

    private var _taskManagementType = MutableLiveData<TaskManagerType>()
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

    /**
     * Sets the current task management type.
     *
     * @param taskManagerType The task management type to be set.
     */
    fun setTaskManagementType(taskManagerType: TaskManagerType) {
        _taskManagementType.value = taskManagerType
    }
}
