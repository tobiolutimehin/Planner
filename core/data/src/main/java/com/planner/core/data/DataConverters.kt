package com.planner.core.data

import androidx.room.TypeConverter
import com.planner.core.data.entity.TaskManagerType

/**
 * Class containing Room type converters for converting between database types and Kotlin types.
 */
class DataConverters {
    /**
     * Map containing [TaskManagerType] values, mapped by their [TaskManagerType.name] property.
     */
    private val taskManagerTypeMap: Map<String, TaskManagerType> = TaskManagerType.values()
        .associateBy { it.name }

    /**
     * Converts a [String] representation of a [TaskManagerType] to a [TaskManagerType] enum.
     *
     * @param value The [String] value to convert.
     * @return The [TaskManagerType] enum value corresponding to the given [String], or `null` if the
     * given [String] does not correspond to a valid [TaskManagerType] value.
     */
    @TypeConverter
    fun taskManagerTypeStringToEnum(value: String) = taskManagerTypeMap[value]

    /**
     * Converts a [TaskManagerType] enum value to its [String] representation.
     *
     * @param value The [TaskManagerType] enum value to convert.
     * @return The [String] representation of the given [TaskManagerType] enum value.
     */
    @TypeConverter
    fun taskManagerTypeEnumToString(value: TaskManagerType) = value.name
}
