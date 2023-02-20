package com.planner.feature.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.TaskEntity
import com.planner.feature.tasks.databinding.TaskChecklistItemBinding

class ManagerDetailRecyclerViewAdapter(private val onCheckChangeListener: (Boolean, TaskEntity) -> Unit) :
    ListAdapter<TaskEntity, ManagerDetailRecyclerViewAdapter.DetailViewHolder>(DiffCallback) {

    class DetailViewHolder(private var binding: TaskChecklistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskEntity, onCheckChangeListener: (Boolean, TaskEntity) -> Unit) {
            binding.apply {
                checkbox.isChecked = task.isDone
                taskText.text = task.description
                taskText.paintFlags = task.strikeThrough

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChangeListener(isChecked, task)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean =
            oldItem.taskId == newItem.taskId

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        return DetailViewHolder(
            TaskChecklistItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val manager = getItem(position)
        holder.bind(manager, onCheckChangeListener)
    }
}
