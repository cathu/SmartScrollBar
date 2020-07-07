package com.cathu.core.orientation

import android.graphics.RectF
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

        val scrollWidthRatio = if (maxLength == 0) {
            1f
        } else {
            bindViewHeight / maxLength
        }
        return RectF(0f, top, width.toFloat(), top + scrollWidthRatio * height)
    }
}