package com.planner.feature.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.Task
import com.planner.feature.tasks.databinding.CreateTasksItemBinding

class TasksRecyclerViewAdapter(val removeTask: (Task) -> Unit) :
    ListAdapter<Task, TasksRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private var binding: CreateTasksItemBinding,
        val removeTask: (Task) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                task.contributor?.let {
                    contributorName.isVisible = true
                    contributorName.text = task.contributor
                }
                taskListItem.text = task.description
                button.setOnClickListener {
                    removeTask(task)
                }
                taskListItem.paintFlags = task.strikeThrough
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem.hashCode() == newItem.hashCode()

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CreateTasksItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            removeTask,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }
}
