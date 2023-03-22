package com.planner.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.planner.databinding.FragmentPlanListDialogListDialogItemBinding
import com.planner.feature.tasks.R
import com.planner.model.BottomSheetItem

/**
 * Adapter for the bottom sheet list in the PlanListDialogFragment.
 *
 * @param onClick Lambda function to handle item click events.
 * @property options List of options to display in the bottom sheet.
 */
class PlanAdapter(private val onClick: (Uri) -> Unit) :
    RecyclerView.Adapter<PlanAdapter.ViewHolder>() {

    private val options = listOf(
        BottomSheetItem(
            imgSrc = R.drawable.bottomsheet_plane,
            title = com.planner.R.string.plan_a_trip,
            subtitle = com.planner.R.string.start_planning,
            uri = "android-app://com.planner.trips/addTripFragment",
        ),
        BottomSheetItem(
            imgSrc = R.drawable.bottomsheet_folder,
            title = com.planner.R.string.start_new_project,
            subtitle = com.planner.R.string.start_new_project_subtitle,
            uri = "android-app://com.planner.tasks/addTaskManagerFragment",
        ),
    )

    /**
     * View holder for a bottom sheet item.
     *
     * @param binding View binding for the item layout.
     * @param context The context used to retrieve string resources.
     * @param clickOption Lambda function to handle item click events.
     */
    class ViewHolder(
        private var binding: FragmentPlanListDialogListDialogItemBinding,
        private val context: Context?,
        private val clickOption: (Uri) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds a bottom sheet item to the view holder.
         *
         * @param option The bottom sheet item to bind.
         */
        fun bind(option: BottomSheetItem) {
            binding.apply {
                optionImage.setImageResource(option.imgSrc)
                optionTitle.text = context?.getString(option.title)
                optionSubtitle.text = context?.getString(option.subtitle)
                binding.root.setOnClickListener { clickOption(option.uri.toUri()) }
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
            onClick,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = options[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = options.size
}
