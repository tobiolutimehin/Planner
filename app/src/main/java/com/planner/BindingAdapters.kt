package com.planner

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.ManagerWithTasks

object BindingAdapters {

    @BindingAdapter("app:recyclerViewVisibility")
    @JvmStatic
    fun setRecyclerViewVisibility(view: RecyclerView, tasks: LiveData<List<ManagerWithTasks>>) {
        view.visibility = if (isTasksDone(tasks)) View.GONE else View.VISIBLE
    }

    @BindingAdapter("app:isEmptyPage")
    @JvmStatic
    fun isEmptyPage(view: View, tasks: LiveData<List<ManagerWithTasks>>) {
        view.visibility = if (isTasksDone(tasks)) View.VISIBLE else View.GONE
    }

    private fun isTasksDone(tasks: LiveData<List<ManagerWithTasks>>): Boolean {
        return tasks.value?.all { managerWithTasks ->
            managerWithTasks.tasks.all { task ->
                task.isDone
            }
        } ?: false
    }
}
