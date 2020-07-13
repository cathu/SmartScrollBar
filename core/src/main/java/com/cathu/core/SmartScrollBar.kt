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

    //  <方向>
    private var orientation: Int = 0
    private lateinit var orientationHandler:IOrientationStrategy

    //  <背景圆角 只支持 Android 5.0>  <如果corner == 0，即表示无圆角>  <单位：dp>
    private var backgroundCorner = 0

    //  <滑块圆角>
    private var sliderCorner = 0

    //  <滑块颜色>
    private var sliderColor: Int = Color.WHITE

    //  <不满足滑动时，当前的状态>  <0 ==> 消失（invisible） 1 ==> 消失（gone） 2 ==> 显示 >
    private var cantScrollState = 0

    //  <滑块显示时，当前的状态>   <0 ==> 一直显示 1==>[dismissTime] 毫秒后消失>
    private var canScrollState = 0
    //  <单位：ms>
    private var dismissTime = 0


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
        cantScrollState = typeArray.getInt(R.styleable.SmartScrollBar_smart_cant_scroll_style, 0)
        canScrollState = typeArray.getInt(R.styleable.SmartScrollBar_smart_can_scroll_style, 0)
        dismissTime = typeArray.getInt(R.styleable.SmartScrollBar_smart_dismiss_time, 1000)
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
            Log.e(TAG,"该 ScrollBar 已经绑定了一个 RecyclerView，无法重复绑定！")
            return
        }

        this.bindView = recyclerView

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                computeLength()
                setInvisibleStyle()
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
        try {
            recyclerView.adapter!!.registerAdapterDataObserver(recyclerViewDataListener)
        }catch (e:Exception){

        }
    }


    /**
     *  <计算长度，包括总长度、现在的位置长度>
     */
    private fun computeLength(){
        val totalLength = orientationHandler.computeRecyclerViewTotalLength(bindView!!)

        //if (totalLength > maxLength) {
            maxLength = totalLength
        //}
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
                setInvisibleStyle()
                postInvalidate()
            }
        }
    }


    /**
     *  <设置不可滑动时，View的显示状态>
     */
    fun setInvisibleStyle(){
        if (!orientationHandler.canScroll(bindView)){
            when(cantScrollState){
                //invisible
                0-> visibility = View.INVISIBLE
                //gone
                1-> visibility = View.GONE
            }
        }else{
            visibility = View.VISIBLE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scrollRect = orientationHandler.createSlider(maxLength,currentLength,width,height,bindView)
        canvas.drawRoundRect(
            scrollRect,
            sliderCorner.toFloat(),
            sliderCorner.toFloat(),
            paint
        )
        setVisibleStyle()
    }


    /**
     *  <设置显示状态下，View 的风格>
     */
    private fun setVisibleStyle() {
        alpha = 1f
        if (canScrollState == 1){
            animate().alpha(0f).setDuration(dismissTime.toLong()).start()
        }
    }


    companion object{
        const val VERTICAL = 0
        const val HORIZONTAL = 1
    }
}