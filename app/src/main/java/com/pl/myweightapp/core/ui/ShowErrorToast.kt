package com.pl.myweightapp.core.ui

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

fun showErrorToast(context: Context, message: String) {
    val toast = Toast(context)

    // Tworzymy prosty layout LinearLayout
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(24, 16, 24, 16)
        //TODO - setBackgroundResource(R.color.background_dark) // lub custom bg
        gravity = Gravity.CENTER_VERTICAL
    }

    // Ikona błędu
    val imageView = ImageView(context).apply {
        //TODO - setImageResource(R.drawable.ic_dialog_alert)
        setColorFilter(Color.RED)
    }

    // Tekst
    val textView = TextView(context).apply {
        text = message
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        setPadding(16, 0, 0, 0)
    }

    layout.addView(imageView)
    layout.addView(textView)

    @Suppress("DEPRECATION")
    toast.view = layout
    toast.duration = Toast.LENGTH_LONG
    toast.show()
}

fun showToast(context: Context, message: String) {
    Toast.makeText(
        context,
        message,
        Toast.LENGTH_LONG
    ).show()
}
