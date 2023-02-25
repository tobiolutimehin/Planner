package com.planner.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.planner.databinding.FragmentPlanListDialogListDialogItemBinding
import com.planner.feature.tasks.R

class PlanAdapter : RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    private val options = listOf(
        BottomSheetItem(
            imgSrc = R.drawable.bottomsheet_plane,
            title = com.planner.R.string.plan_a_trip,
            subtitle = com.planner.R.string.start_planning,
        ),
        BottomSheetItem(
            imgSrc = R.drawable.bottomsheet_folder,
            title = com.planner.R.string.start_new_project,
            subtitle = com.planner.R.string.start_new_project_subtitle,
        ),
    )

    class ViewHolder(
        private var binding: FragmentPlanListDialogListDialogItemBinding,
        private val context: Context?,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(option: BottomSheetItem) {
            binding.apply {
                optionImage.setImageResource(option.imgSrc)
                optionTitle.text = context?.getString(option.title)
                optionSubtitle.text = context?.getString(option.subtitle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentPlanListDialogListDialogItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
            parent.context,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = options[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = options.size
}

data class BottomSheetItem(
    @DrawableRes val imgSrc: Int,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
)
