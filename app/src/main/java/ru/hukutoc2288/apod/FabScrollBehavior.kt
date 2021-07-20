package ru.hukutoc2288.apod

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout

fun getToolbarHeight(context: Context): Int {
    val styledAttributes = context.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize))
    val toolbarHeight = styledAttributes.getDimension(0, 0f).toInt()
    styledAttributes.recycle()
    return toolbarHeight
}

class FabScrollBehavior(context: Context, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<View>(context, attrs) {
    private val toolbarHeight: Int = getToolbarHeight(context)
    override fun layoutDependsOn(parent: CoordinatorLayout, fab: View, dependency: View): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, fab: View, dependency: View): Boolean {
        if (dependency is AppBarLayout) {
            val lp = fab.layoutParams as CoordinatorLayout.LayoutParams
            val fabBottomMargin = lp.bottomMargin
            val distanceToScroll = fab.height + fabBottomMargin
            val ratio = dependency.getY() / toolbarHeight.toFloat()
            fab.translationY = -distanceToScroll * ratio
        }
        return true
    }

}