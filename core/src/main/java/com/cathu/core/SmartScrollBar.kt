package com.cathu.core

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.cathu.core.orientation.IOrientationStrategy
import com.cathu.core.util.applyDimension
import java.lang.Exception

/**
 * Created by Zifeng.Hu on 2020/6/28
 * @Description: 滚动条
 */
class SmartScrollBar : View {

    private val TAG = "==>scrollBar"

    //  <画笔>
    private val paint: Paint

    //  <默认最低宽高（水平状态下）>    <如果是竖直状态会反过来>
    private var minWidth = 100
    private var minHeight = 30

    //  <绑定的滚动 View>
    private var bindView: RecyclerView? = null

    //  <滚动最大长度>
    private var maxLength = 0
    private var currentLength = 0


    //  <滑块长度>
    private var scrollLength = 30f

    //  <方向>
    private var orientation: Int = 0
    private lateinit var orientationHandler:IOrientationStrategy

    //  <背景圆角 只支持 Android 5.0>  <如果corner == 0，即表示无圆角>  <单位：dp>
    private var backgroundCorner = 0

    //  <滑块圆角>
    private var sliderCorner = 0

    //  <滑块颜色>
    private var sliderColor: Int = Color.WHITE

    //  <不满足滑动时，当前的状态> TODO 缺实现
    private var noSliderState = 0


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }

    init {
        paint = Paint()
        paint.style = Paint.Style.FILL
    }

    /**
     *  <初始化 XML 自定义属性>
     */
    private fun initAttrs(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.SmartScrollBar)
        backgroundCorner =
            typeArray.getDimension(R.styleable.SmartScrollBar_smart_background_corner, 0f).toInt()
        sliderCorner =
            typeArray.getDimension(R.styleable.SmartScrollBar_smart_slider_corner, 0f).toInt()
        sliderColor = typeArray.getColor(R.styleable.SmartScrollBar_smart_slider_color, Color.WHITE)
        noSliderState = typeArray.getInt(R.styleable.SmartScrollBar_smart_noSliderState, 0)
        orientation = typeArray.getInt(R.styleable.SmartScrollBar_smart_orientation, VERTICAL)
        orientationHandler = IOrientationStrategy.createStrategy(orientation)

        paint.color = sliderColor
        typeArray.recycle()
    }

    /**
     *  <设置最低宽高>
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        if (orientation == VERTICAL){
            minWidth += minHeight
            minHeight = minWidth - minHeight
            minWidth -= minHeight
        }

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(minWidth, minHeight)
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(minWidth, heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, minHeight)
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    /**
     *  <设置圆角>
     */
    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        if (backgroundCorner == 0) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(
                        0,
                        0,
                        width,
                        height,
                        backgroundCorner.toFloat()
                            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, context)
                    )
                }
            }
        }
    }


    /**
     *  <绑定滚动的 View>
     */
    fun bindScrollView(recyclerView: RecyclerView) {
        //  <绑定数据监听>
        bindDataChangedListener(recyclerView)

        if (this.bindView != null) {
            throw IllegalStateException("该 ScrollBar 已经绑定了一个 RecyclerView，无法重复绑定！")
        }

        this.bindView = recyclerView

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                computeLength()
                postInvalidate()
            }
        })
    }


    /**
     *  <绑定 RecyclerView 数据改变监听>
     */
    private fun bindDataChangedListener(recyclerView: RecyclerView) {
        if (recyclerView.adapter == null) {
            throw IllegalStateException("请先绑定 recyclerView 的 Adapter")
        }
        recyclerView.adapter!!.registerAdapterDataObserver(recyclerViewDataListener)
    }


    /**
     *  <计算长度，包括总长度、现在的位置长度>
     */
    private fun computeLength(){
        val totalLength = orientationHandler.computeRecyclerViewTotalLength(bindView!!)

        if (totalLength > maxLength) {
            maxLength = totalLength
        }
        currentLength = orientationHandler.computeRecyclerViewCurrentLength(bindView!!)
    }


    /**
     *  <RecyclerView 数据监听>
     */
    private val recyclerViewDataListener = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            bindView?.let {
                maxLength = 0
                computeLength()
                postInvalidate()
            }
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scrollRect = orientationHandler.createSlider(maxLength,currentLength,width,height,bindView)
        canvas.drawRoundRect(
            scrollRect,
            backgroundCorner.toFloat(),
            backgroundCorner.toFloat(),
            paint
        )
    }


    companion object{
        const val VERTICAL = 0
        const val HORIZONTAL = 1
    }
}