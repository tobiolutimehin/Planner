package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType

class AddTaskViewModel : ViewModel() {

    private var _taskList = MutableLiveData<List<Task>>()
    val taskList: LiveData<List<Task>> = _taskList

    private var _taskManagementType = MutableLiveData<TaskManagerType>()
    val taskManagerType: LiveData<TaskManagerType> = _taskManagementType

    fun addTask(task: Task) {
        val updatedTaskList = _taskList.value?.toMutableList() ?: mutableListOf()
        updatedTaskList.add(task)
        _taskList.value = updatedTaskList
    }

    fun addTasks(tasks: List<Task>) {
        val updatedTaskList = _taskList.value?.toMutableList() ?: mutableListOf()
        updatedTaskList.addAll(tasks)
        _taskList.value = updatedTaskList
    }

    fun removeTask(task: Task) {
        val updatedTaskList = _taskList.value?.toMutableList() ?: mutableListOf()
        updatedTaskList.remove(task)
        _taskList.value = updatedTaskList
    }

    fun setTaskManagementType(taskManagerType: TaskManagerType) {
        _taskManagementType.value = taskManagerType
    }
}
