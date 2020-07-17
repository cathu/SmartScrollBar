package com.cathu.core.orientation

import android.graphics.RectF
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception

/**
 *       Created by Cathu on 2020/7/6 22:21
 */
class HorizontalStrategy:IOrientationStrategy {

    override fun computeRecyclerViewTotalLength(recyclerView: RecyclerView): Int {
        return recyclerView.computeHorizontalScrollRange()
    }

    override fun computeRecyclerViewCurrentLength(recyclerView: RecyclerView): Int {
        return recyclerView.computeHorizontalScrollOffset()
    }

    override fun computeRecyclerViewExtent(recyclerView: RecyclerView): Int {
        return recyclerView.computeHorizontalScrollExtent()
    }

    override fun createSlider(
        maxLength: Int,
        currentLength: Int,
        width: Int,
        height: Int,
        bindView: RecyclerView?
    ): RectF {
        val left = if (maxLength == 0) {
            0f
        } else {
            width * (currentLength.toFloat() / maxLength)
        }

        val bindViewWidth = try {
            bindView!!.width.toFloat()
        } catch (e: Exception) {
            0f
        }

        var scrollWidthRatio = if (maxLength == 0) {
            1f
        } else {
            bindViewWidth / maxLength
        }
        if (scrollWidthRatio>=1f){
            scrollWidthRatio = 1f
        }
        return RectF(left, 0f, left + scrollWidthRatio * width, height.toFloat())
    }

    override fun createFixedSlider(
        sliderLength: Float,
        width: Int,
        height: Int,
        bindView: RecyclerView?
    ): RectF {
        val sliderWidth = if (sliderLength<=1.0){
            width * sliderLength
        }else{
            sliderLength
        }

        val left = if (bindView == null || computeRecyclerViewTotalLength(bindView) == 0) {
            0f
        } else {
            (computeRecyclerViewCurrentLength(bindView).toFloat() / (computeRecyclerViewTotalLength(bindView) - computeRecyclerViewExtent(bindView))) * (width - sliderWidth)
        }


        return RectF(left,0f,left + sliderWidth,height.toFloat())
    }

    override fun canScroll(bindView: RecyclerView?): Boolean {
        return (bindView?.canScrollHorizontally(1) == true) or (bindView?.canScrollHorizontally(-1) == true)
    }
}