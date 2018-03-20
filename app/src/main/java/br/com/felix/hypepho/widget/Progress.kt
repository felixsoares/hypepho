package br.com.felix.hypepho.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import br.com.felix.hypepho.R


/**
 * Created by Felix on 01/03/2018.
 */

class Progress : LinearLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        gravity = Gravity.CENTER

        layoutParams = LinearLayout.LayoutParams(
                convertDpToPixel(50f, context),
                convertDpToPixel(50f, context)
        )

        val imageView = ImageView(context)
        imageView.layoutParams = LinearLayout.LayoutParams(
                convertDpToPixel(50f, context),
                convertDpToPixel(50f, context)
        )

        imageView.setBackgroundResource(R.drawable.animate)

        val spinner = imageView.background as AnimationDrawable
        spinner.start()

        addView(imageView)
    }

    fun show() {
        this.animate()
                .alphaBy(1.0f)
                .setDuration(350)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        visibility = View.VISIBLE
                    }
                })
    }

    fun hide() {
        this.animate()
                .alpha(0.0f)
                .setDuration(350)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        visibility = View.GONE
                    }
                })
    }

    private fun convertDpToPixel(dp: Float, context: Context): Int {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }
}
