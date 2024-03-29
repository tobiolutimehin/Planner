package com.planner.feature.tasks.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.planner.core.data.entity.ManagerWithTasks
import com.planner.feature.tasks.R
import com.planner.feature.tasks.databinding.TaskManagerCardItemBinding
import com.planner.feature.tasks.utils.Converters.toTitleName
import com.planner.feature.tasks.utils.Converters.toTypeName

class TaskManagerListAdapter(
    private val context: Context?,
    private val openDetail: (Long) -> Unit,
) : ListAdapter<ManagerWithTasks, TaskManagerListAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private var binding: TaskManagerCardItemBinding,
        private val context: Context?,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(manager: ManagerWithTasks, openDetail: (Long) -> Unit) {
            val toComplete = manager.tasks.filterNot { it.isDone }.size
            val type = manager.taskManager.type
            val name = manager.taskManager.name

            binding.apply {
                taskManagerStatus.text = context?.resources.let {
                    if (toComplete == 0) {
                        it?.getString(R.string.no_pending_tasks)
                    } else {
                        it?.getQuantityString(R.plurals.pending_tasks, toComplete, toComplete)
                    }
                }

                taskManagerCardTitle.text = name.ifBlank { context?.getString(type.toTitleName()) }
                taskManagerType.text = context?.getString(type.toTypeName())
                root.setOnClickListener { openDetail(manager.taskManager.managerId) }
            }
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
        holder.bind(manager = manager, openDetail = openDetail)
    }
}
