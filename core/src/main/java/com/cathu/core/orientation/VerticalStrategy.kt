package com.cathu.core.orientation

import android.graphics.RectF
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

/**
 *       Created by Cathu on 2020/7/6 22:21
 */
class VerticalStrategy:IOrientationStrategy {

    override fun computeRecyclerViewTotalLength(recyclerView: RecyclerView): Int {
        return recyclerView.computeVerticalScrollRange()
    }

    override fun computeRecyclerViewCurrentLength(recyclerView: RecyclerView): Int {
        return recyclerView.computeVerticalScrollOffset()
    }

    override fun computeRecyclerViewExtent(recyclerView: RecyclerView): Int {
        return recyclerView.computeVerticalScrollExtent()
    }

    override fun createSlider(
        maxLength: Int,
        currentLength: Int,
        width: Int,
        height: Int,
        bindView: RecyclerView?
    ): RectF {
        val top = if (maxLength == 0) {
            0f
        } else {
            height * (currentLength.toFloat() / maxLength)
        }

        val bindViewHeight = try {
            bindView!!.height.toFloat()
        } catch (e: Exception) {
            0f
        }

        var scrollWidthRatio = if (maxLength == 0) {
            1f
        } else {
            bindViewHeight / maxLength
        }
        if (scrollWidthRatio>=1f){
            scrollWidthRatio = 1f
        }
        return RectF(0f, top, width.toFloat(), top + scrollWidthRatio * height)
    }

    override fun createFixedSlider(
        sliderLength: Float,
        width: Int,
        height: Int,
        bindView: RecyclerView?
    ): RectF {

        val sliderHeight = if (sliderLength<=1.0){
            height * sliderLength
        }else{
            sliderLength
        }

        val top = if (bindView == null || computeRecyclerViewTotalLength(bindView) == 0) {
            0f
        } else {
            (computeRecyclerViewCurrentLength(bindView).toFloat() / (computeRecyclerViewTotalLength(bindView) - computeRecyclerViewExtent(bindView))) * (height - sliderHeight)
        }
        return RectF(0f,top,width.toFloat(),top+sliderHeight)
    }

    override fun canScroll(bindView: RecyclerView?): Boolean {
        return (bindView?.canScrollVertically(1) == true) or (bindView?.canScrollVertically(-1) == true)
    }
}