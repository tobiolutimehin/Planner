package com.planner.feature.tasks.utils

import android.widget.Button
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import com.google.android.material.button.MaterialButton
import com.planner.core.data.entity.Task
import com.planner.core.data.entity.TaskManagerType
import com.planner.core.ui.R

object BindingAdapters {
    @BindingAdapter("app:enabledWithList")
    @JvmStatic
    fun setEnabledWithList(button: Button, taskList: LiveData<List<Task>>?) {
        button.isEnabled = taskList?.value?.isNotEmpty() ?: false
    }

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
