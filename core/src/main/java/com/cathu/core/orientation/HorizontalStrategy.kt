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

        val scrollWidthRatio = if (maxLength == 0) {
            1f
        } else {
            bindViewWidth / maxLength
        }
        return RectF(left, 0f, left + scrollWidthRatio * width, height.toFloat())
    }
}