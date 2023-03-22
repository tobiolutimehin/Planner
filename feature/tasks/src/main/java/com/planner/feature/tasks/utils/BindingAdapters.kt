package com.planner.feature.tasks.utils

import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.google.android.material.button.MaterialButton
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.ui.R

/**
 * Utility object containing binding adapters for use in XML layouts with data binding.
 */
object BindingAdapters {

    /**
     * Sets the enabled state of a button based on the contents of a LiveData list of tasks.
     *
     * @param button the button to set the enabled state of
     * @param taskList the LiveData list of tasks to check for contents
     */
    @BindingAdapter("app:enabledWithList")
    @JvmStatic
    fun setEnabledWithList(button: Button, taskList: LiveData<List<Task>>?) {
        button.isEnabled = taskList?.value?.isNotEmpty() ?: false
    }

    /**
     * Sets the icon of a MaterialButton to indicate the currently selected task manager type.
     *
     * @param button the button to set the icon of
     * @param managerType the task manager type to compare against
     * @param presentManagerType the currently selected task manager type
     */
    @BindingAdapter(value = ["managerType", "presentManagerType"])
    @JvmStatic
    fun setToggleIcon(
        button: MaterialButton,
        managerType: TaskManagerType,
        presentManagerType: TaskManagerType,
    ) {
        if (presentManagerType == managerType) {
            button.setIconResource(R.drawable.ic_check)
        } else {
            button.icon = null
        }
    }
}
