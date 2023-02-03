package com.planner.core.data

import androidx.room.TypeConverter
import com.planner.core.data.entity.TaskManagerType

class DataConverters {
    private val taskManagerTypeMap: Map<String, TaskManagerType> = TaskManagerType.values()
        .associateBy { it.name }

    @TypeConverter
    fun taskManagerTypeStringToEnum(value: String) = taskManagerTypeMap[value]

    @TypeConverter
    fun taskManagerTypeEnumToString(value: TaskManagerType) = value.name
}
