package com.example.mapsetup.managers

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.example.mapsetup.R

class PopupManager(private val context: Context, private val rootView: View) {

    private lateinit var popupWindow: PopupWindow
    private lateinit var popupTextView: TextView

    init {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_window, null)
        popupTextView = popupView.findViewById(R.id.popup_text)
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isFocusable = true
            isOutsideTouchable = true
            setBackgroundDrawable(null)
        }
    }

    fun showPopupWindow() {
//        popupTextView.text = text
        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }

    // Other popup-related methods...
}
