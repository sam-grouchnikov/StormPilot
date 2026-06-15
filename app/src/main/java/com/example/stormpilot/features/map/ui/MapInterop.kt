package com.example.stormpilot.features.map.ui

import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import org.maplibre.android.maps.MapView as AndroidMapView

internal fun disableMapLibreFocusOverlay(root: View): Boolean {
    var foundMapView = false
    root.forEachViewInTree { view ->
        if (view is AndroidMapView) {
            foundMapView = true
            view.forEachViewInTree { mapViewChild ->
                mapViewChild.clearFocus()
                mapViewChild.isFocusable = false
                mapViewChild.isFocusableInTouchMode = false
                if (mapViewChild is ViewGroup) {
                    mapViewChild.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mapViewChild.defaultFocusHighlightEnabled = false
                }
            }
            view.foreground = ColorDrawable(android.graphics.Color.TRANSPARENT)
        }
    }
    return foundMapView
}

private fun View.forEachViewInTree(action: (View) -> Unit) {
    action(this)
    if (this is ViewGroup) {
        for (index in 0 until childCount) {
            getChildAt(index).forEachViewInTree(action)
        }
    }
}

internal fun searchSurfaceColor(isDarkMode: Boolean): Color =
    if (isDarkMode) Color(0xFF131618) else Color(0xFFFFFFFF)
