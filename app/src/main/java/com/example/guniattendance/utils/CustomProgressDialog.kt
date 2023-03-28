package com.example.guniattendance.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.example.guniattendance.R

@SuppressLint("InflateParams")
class CustomProgressDialog(context: Context) {

    private var dialog: CustomDialog
    private var cpTitle: TextView
    private var cpCardView: CardView
    private var progressBar: ProgressBar

    fun start(title: String = "") {
        cpTitle.text = title
        dialog.show()
    }

    fun stop() {

        dialog.dismiss()
    }

    init {
        val inflater =  LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.progress_dialog_view, null)

        cpTitle = view.findViewById(R.id.cp_title)
        cpCardView = view.findViewById(R.id.cp_cardview)
        progressBar = view.findViewById(R.id.cp_pbar)

        // Card Color
        cpCardView.setCardBackgroundColor(Color.parseColor("#00000000"))

        // Progress Bar Color
        setColorFilter(
            progressBar.indeterminateDrawable,
            ResourcesCompat.getColor(context.resources, R.color.colorPrimary, null)
        )

        // Text Color
//        cpTitle.setTextColor(Color.WHITE)

        // Custom Dialog initialization

        dialog = CustomDialog(context)
        dialog.setContentView(view)
        dialog.setCancelable(false)
//        dialog.setOnShowListener {
//            reqActivity.window.setFlags(
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
//            )
//        }
//        dialog.setOnCancelListener {
//            reqActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//        }
//        dialog.setOnDismissListener {
//            reqActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
//        }
    }

    private fun setColorFilter(drawable: Drawable, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

    @Suppress("DEPRECATION")
    class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
        init {
            // Set Semi-Transparent Color for Dialog Background
            window?.decorView?.rootView?.setBackgroundResource(R.color.dialogBackground)
            window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
                insets.consumeSystemWindowInsets()
            }
        }
    }
}