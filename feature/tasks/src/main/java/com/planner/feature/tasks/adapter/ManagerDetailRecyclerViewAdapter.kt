package com.planner.feature.tasks.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.TaskEntity
import com.planner.feature.tasks.databinding.TaskChecklistItemBinding

class ManagerDetailRecyclerViewAdapter :
    ListAdapter<TaskEntity, ManagerDetailRecyclerViewAdapter.DetailViewHolder>(DiffCallback) {

    class DetailViewHolder(private var binding: TaskChecklistItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: TaskEntity) {
            binding.apply {
                checkbox.isChecked = task.isDone
                taskText.text = task.description
                taskText.paintFlags = shouldHaveStrikeThrough(task.isDone)

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    taskText.paintFlags = shouldHaveStrikeThrough(isChecked)
                }
            }
        }

        private fun shouldHaveStrikeThrough(isDone: Boolean) =
            if (isDone) {
                Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                0
            }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(
            oldItem: TaskEntity,
            newItem: TaskEntity,
        ): Boolean =
            oldItem.taskId == newItem.taskId

        override fun areContentsTheSame(
            oldItem: TaskEntity,
            newItem: TaskEntity,
        ): Boolean =
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
        holder.bind(manager)
    }
}
