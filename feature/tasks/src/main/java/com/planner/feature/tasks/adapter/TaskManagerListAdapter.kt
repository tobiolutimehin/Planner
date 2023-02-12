package com.planner.feature.tasks.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.core.data.entity.TaskManagerType
import com.planner.feature.tasks.R
import com.planner.feature.tasks.databinding.TaskManagerCardItemBinding

class TaskManagerListAdapter(private val context: Context) :
    ListAdapter<ManagerWithTasks, TaskManagerListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private var binding: TaskManagerCardItemBinding,
        private val context: Context,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(manager: ManagerWithTasks) {
            val toComplete = manager.task.filterNot { it.isDone }.size
            binding.apply {
                taskManagerCardTitle.text =
                    getTitleName(manager.taskManager.name, manager.taskManager.type)
                taskManagerStatus.text = context.resources.getQuantityString(
                    R.plurals.pending_tasks,
                    toComplete,
                    toComplete,
                )
                taskManagerType.text = manager.taskManager.type.toTypeName(context)
            }
        }

        private fun getTitleName(name: String, type: TaskManagerType) =
            name.ifBlank { type.toTitleName() }

        private fun TaskManagerType.toTitleName(): String {
            return context.getString(
                when (this@toTitleName) {
                    TaskManagerType.TODO_LIST -> R.string.personal_todo_list
                    TaskManagerType.PROJECT -> R.string.group_project
                },
            )
        }

        private fun TaskManagerType.toTypeName(context: Context): CharSequence {
            return context.getString(
                when (this@toTypeName) {
                    TaskManagerType.TODO_LIST -> R.string.to_do_list
                    TaskManagerType.PROJECT -> R.string.group_project
                },
            )
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ManagerWithTasks>() {
        override fun areItemsTheSame(
            oldItem: ManagerWithTasks,
            newItem: ManagerWithTasks,
        ): Boolean =
            oldItem.taskManager.managerId == newItem.taskManager.managerId

        override fun areContentsTheSame(
            oldItem: ManagerWithTasks,
            newItem: ManagerWithTasks,
        ): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            TaskManagerCardItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            context = context,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val manager = getItem(position)
        holder.bind(manager)
    }
}
