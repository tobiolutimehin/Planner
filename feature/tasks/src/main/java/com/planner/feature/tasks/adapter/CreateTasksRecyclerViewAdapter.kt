package com.planner.feature.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.strikeThrough
import com.planner.feature.tasks.databinding.CreateTaskItemBinding

class CreateTasksRecyclerViewAdapter(val removeTask: (Task) -> Unit) :
    ListAdapter<Task, CreateTasksRecyclerViewAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private var binding: CreateTaskItemBinding,
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
                taskListItem.paintFlags = task.isDone.strikeThrough()
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
            CreateTaskItemBinding.inflate(
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
