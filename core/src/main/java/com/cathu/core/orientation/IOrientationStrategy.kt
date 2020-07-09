package com.cathu.core.orientation

import android.graphics.RectF
import androidx.recyclerview.widget.RecyclerView
import com.cathu.core.SmartScrollBar

/**
 *       Created by Cathu on 2020/7/6 22:19
 */
interface IOrientationStrategy {

    fun computeRecyclerViewTotalLength(recyclerView: RecyclerView): Int

    fun computeRecyclerViewCurrentLength(recyclerView: RecyclerView): Int

    fun createSlider(
        maxLength: Int,
        currentLength: Int,
        width: Int,
        height: Int,
        bindView: RecyclerView?
    ):RectF

    fun canScroll(bindView: RecyclerView?): Boolean

    companion object{

        fun createStrategy(orientation: Int):IOrientationStrategy {
            return if (orientation == SmartScrollBar.VERTICAL){
                VerticalStrategy()
            }else{
                HorizontalStrategy()
            }
        }
    }
}