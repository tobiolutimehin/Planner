package com.planner.feature.tasks.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.planner.core.data.dao.TaskManagerDao
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskEntity
import com.planner.core.data.entity.TaskManagerEntity
import com.planner.core.data.entity.TaskManagerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing tasks and task managers.
 *
 * @param dao Data Access Object for accessing the TaskManagerEntity and TaskEntity tables in the database.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(private val dao: TaskManagerDao) : ViewModel() {
    private val listStates = mutableMapOf<TaskManagerType, TaskManagerListState>()
    private var selectedTaskManagerPage: TaskManagerType? = null

    val todoTaskManagers: StateFlow<PagingData<ManagerWithTasks>> =
        Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 1, enablePlaceholders = true),
            pagingSourceFactory = { dao.getTaskManagersPaged(TaskManagerType.TODO_LIST) },
        ).flow
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PagingData.empty(),
            )

    val projectTaskManagers: StateFlow<PagingData<ManagerWithTasks>> =
        Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 1, enablePlaceholders = true),
            pagingSourceFactory = { dao.getTaskManagersPaged(TaskManagerType.PROJECT) },
        ).flow
            .cachedIn(viewModelScope)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PagingData.empty(),
            )

    /**
     * LiveData object containing a list of all task managers and their associated tasks.
     */
    val tasks: LiveData<List<ManagerWithTasks>> = dao.getTaskManagers().asLiveData()

    fun saveTaskManagerListState(
        type: TaskManagerType,
        anchorPosition: Int,
        anchorOffset: Int,
    ) {
        val existing = listStates[type] ?: TaskManagerListState()
        listStates[type] =
            existing.copy(
                anchorPosition = anchorPosition.coerceAtLeast(0),
                anchorOffset = anchorOffset,
            )
    }

    fun markTaskManagerClicked(
        type: TaskManagerType,
        managerId: Long,
        adapterPosition: Int,
        itemTopOffset: Int,
    ) {
        val existing = listStates[type] ?: TaskManagerListState()
        listStates[type] =
            existing.copy(
                clickedItemId = managerId,
                anchorPosition = adapterPosition.coerceAtLeast(0),
                clickedItemOffset = itemTopOffset,
            )
    }

    fun getTaskManagerListState(type: TaskManagerType): TaskManagerListState =
        listStates[type] ?: TaskManagerListState()

    fun saveSelectedTaskManagerPage(type: TaskManagerType) {
        selectedTaskManagerPage = type
    }

    fun getSelectedTaskManagerPage(defaultType: TaskManagerType): TaskManagerType =
        selectedTaskManagerPage ?: defaultType

    /**
     * Returns the task manager with the given ID.
     *
     * @param id ID of the task manager to retrieve.
     * @return LiveData object containing the task manager and its associated tasks.
     */
    fun getTaskManager(id: Long): LiveData<ManagerWithTasks> = dao.getTaskManager(id).asLiveData()

    /**
     * Inserts a new task manager and its associated tasks into the database.
     *
     * @param string Name of the task manager.
     * @param tasks List of tasks to associate with the task manager.
     * @param taskManagerType Type of the task manager.
     */
    fun saveTaskManager(string: String, tasks: List<Task>, taskManagerType: TaskManagerType) {
        viewModelScope.launch {
            dao.insertTaskManagerWithTasks(
                TaskManagerEntity(name = string, type = taskManagerType),
                tasks,
            )
        }
    }

    /**
     * Deletes the given task manager and its associated tasks from the database.
     *
     * @param taskManagerEntity TaskManagerEntity to delete.
     */
    fun deleteTaskManager(taskManagerEntity: TaskManagerEntity) = viewModelScope.launch {
        dao.deleteTaskManagerWithTasks(taskManagerEntity)
    }

    /**
     * Updates the given task in the database.
     *
     * @param taskEntity TaskEntity to update.
     */
    private suspend fun updateTask(taskEntity: TaskEntity) {
        dao.updateTask(taskEntity)
    }

    /**
     * Updates the given task manager and its associated tasks in the database.
     *
     * @param taskManagerEntity TaskManagerEntity to update.
     * @param tasks List of tasks to associate with the updated task manager.
     */
    fun updateTaskManager(taskManagerEntity: TaskManagerEntity, tasks: List<Task>) =
        viewModelScope.launch {
            dao.updateTaskManagerWithTasks(taskManagerEntity, tasks)
        }

    /**
     * Updates the task manager and its associated tasks in the database using the given TaskEntity objects.
     *
     * @param tasks List of TaskEntity objects representing the updated tasks.
     */
    fun updateTaskManagerWithTaskEntity(tasks: List<TaskEntity>) =
        viewModelScope.launch {
            tasks.forEach { updateTask(it) }
        }
}

data class TaskManagerListState(
    val clickedItemId: Long? = null,
    val clickedItemOffset: Int? = null,
    val anchorPosition: Int = 0,
    val anchorOffset: Int = 0,
)
