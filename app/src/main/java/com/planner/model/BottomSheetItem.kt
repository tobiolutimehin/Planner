package com.planner.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Data class representing an item in the bottom sheet list.
 *
 * @param imgSrc The resource ID for the item's icon.
 * @param title The resource ID for the item's title.
 * @param subtitle The resource ID for the item's subtitle.
 * @param uri The URI string to navigate to when the item is clicked.
 */
data class BottomSheetItem(
    @DrawableRes val imgSrc: Int,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val uri: String,
)
