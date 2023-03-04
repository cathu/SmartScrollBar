package com.cathu.core

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.toRect
import androidx.recyclerview.widget.RecyclerView
import com.cathu.core.orientation.IOrientationStrategy
import java.lang.Exception
import kotlin.math.roundToInt

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
    private var orientation: Int = VERTICAL
    private lateinit var orientationHandler: IOrientationStrategy

    //  <背景圆角 只支持 Android 5.0>  <如果corner == 0，即表示无圆角>  <单位：dp>
    private var backgroundCorner = 0

    //  <滑块圆角>
    private var sliderCorner = 0

    //  <滑块颜色>
    private var sliderColor: Int = Color.WHITE

    //  <滑块风格>  <0 ==> 倍数缩放 1==>固定大小 >
    private var sliderStyle = 0

    //  <滑块长度>  <0.0 - 1.0 为百分比，1 往后就是长度 (单位：dp) >
    private var sliderLength = 0f

    //  <不满足滑动时，当前的状态>  <0 ==> 消失（invisible） 1 ==> 消失（gone） 2 ==> 显示 >
    private var cantScrollState = 0

    //  <滑块显示时，当前的状态>   <0 ==> 一直显示 1==>[dismissTime] 毫秒后消失>
    private var canScrollState = 0

    //  <单位：ms>
    private var dismissTime = 0

    //  <是否支持拖拽>
    private var enableDrag = false

    //  <自定义背景 rect>
    private val customBgRectF = RectF()

    //  <自定义背景圆角>
    private var customBgCorner = 0f

    //  <背景 paint>
    private val customBgPaint by lazy { Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    } }

    //  <ScrollingView 滚动的类型>
    private var scrollType = SCROLL_TYPE_SMOOTH

    private val gestureDetector by lazy { GestureDetector(context, gestureListener).apply {
        setIsLongpressEnabled(false)
        this::class.java.getDeclaredField("mTouchSlopSquare").apply { isAccessible = true }.set(this,0)
    } }

    private val sliderRegion = Region()


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
        sliderStyle = typeArray.getInt(R.styleable.SmartScrollBar_smart_slider_style, 0)
        sliderLength = try {
            typeArray.getFraction(R.styleable.SmartScrollBar_smart_slider_length, 1, 1, 0f)
        } catch (e: Exception) {
            typeArray.getDimension(R.styleable.SmartScrollBar_smart_slider_length, 0f)
        }
        enableDrag = typeArray.getBoolean(R.styleable.SmartScrollBar_smart_enable_drag,false)

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

        if (orientation == VERTICAL) {
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
        if (backgroundCorner > Math.min(width, height) / 2) {
            backgroundCorner = Math.min(width, height) / 2
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
            Log.e(TAG, "该 ScrollBar 已经绑定了一个 RecyclerView，无法重复绑定！")
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
        } catch (e: Exception) {

        }
    }


    /**
     *  <计算长度，包括总长度、现在的位置长度>
     */
    private fun computeLength() {
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
            bindView?.postDelayed({
                maxLength = 0
                computeLength()
                setInvisibleStyle()
                postInvalidate()
            }, 200)
        }
    }


    /**
     *  <设置不可滑动时，View的显示状态>
     */
    private fun setInvisibleStyle() {
        if (!orientationHandler.canScroll(bindView)) {
            when (cantScrollState) {
                //invisible
                0 -> visibility = View.INVISIBLE
                //gone
                1 -> visibility = View.GONE
            }
        } else {
            visibility = View.VISIBLE
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //  1. 绘制自定义背景
        if (!customBgRectF.isEmpty){
            canvas.drawRoundRect(customBgRectF,customBgCorner,customBgCorner,customBgPaint)
        }

        //  2. 绘制 slider
        val scrollRect = if (sliderStyle == 1 && sliderLength != 0f) {
            orientationHandler.createFixedSlider(sliderLength, width, height, bindView)
        } else {
            orientationHandler.createSlider(maxLength, currentLength, width, height, bindView)
        }

        canvas.drawRoundRect(
            scrollRect,
            sliderCorner.toFloat(),
            sliderCorner.toFloat(),
            paint
        )
        sliderRegion.set(scrollRect.toRect())

        //  3. 设置 slider 是否消失
        setVisibleStyle()
    }


    /**
     *  <设置自定义背景>
     */
    fun setCustomBackground(holderWH: (Int,Int) -> RectF, corner: Float, @ColorInt color: Int) {
        post {
            this.customBgRectF.set(holderWH.invoke(width,height))
            this.customBgCorner = corner
            customBgPaint.color = color
        }
    }


    /**
     *  <设置 ScrollingView 滚动的类型>
     *  @see [SCROLL_TYPE_SMOOTH] [SCROLL_TYPE_ITEM]
     *  @suggestion 如果项目比较少建议使用[SCROLL_TYPE_SMOOTH]，如果项目比较多建议使用[SCROLL_TYPE_ITEM]。
     *  因为[SCROLL_TYPE_SMOOTH]会更流畅，但是如果项目过多（例如有10000个项目），就会造成卡顿，卡顿的原因是
     *  [RecyclerView.scrollBy]耗时较长，而如果项目较小且使用[SCROLL_TYPE_ITEM]，那么滑动的时候由于
     *  [LayoutManager.scrollToPosition] 的机制问题，前几项不会有反应。
     */
    fun setScrollingType(type: Int){
        if (type != SCROLL_TYPE_SMOOTH && type != SCROLL_TYPE_ITEM){
            throw IllegalArgumentException("Please use [SmartScrollBar.SCROLL_TYPE_ITEM] or [SmartScrollBar.SCROLL_TYPE_SMOOTH]")
        }
        this.scrollType = type
    }

    /**
     *  <设置显示状态下，View 的风格>
     */
    private fun setVisibleStyle() {
        alpha = 1f
        if (canScrollState == 1) {
            animate().alpha(0f).setDuration(dismissTime.toLong()).start()
        }
    }


    /**
     *  <触摸事件>
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }


    //  <ScrollingView 整体的比率(即当前可见项占 ScrollingView 的比率)>
    private var firstRatio: Float? = null
    //  <ScrollingView offset的比率>
    private var lastOffsetRatio = 0f
    private var isTouched: Boolean? = null

    /**
     *  <手势监听>
     */
    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {


        override fun onDown(e: MotionEvent): Boolean {
            if (e.action == MotionEvent.ACTION_DOWN) {
                firstRatio = null
                lastOffsetRatio = 0f
                isTouched = null
            }
            return enableDrag
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            //Log.e(TAG,"onScroll: e1:(${e1.x.toInt()},${e1.y.toInt()}),e2:(${e2.x.toInt()},${e2.y.toInt()}),action:${e1.action},${e2.action}")
            if (isTouched == null) {
                isTouched = sliderRegion.contains(e1.x.toInt(), e1.y.toInt())
            }

            if (isTouched == true) {
                //Log.e(TAG,"触摸到Slider")
                bindView?.let {
                    val barLength: Int
                    val offsetRatio:Float
                    if (orientation == VERTICAL) {
                        barLength = height - sliderRegion.bounds.height()
                        if (barLength == 0){
                            return true
                        }
                        if (firstRatio == null) {
                            firstRatio = sliderRegion.bounds.top.toFloat() / barLength
                        }
                        offsetRatio = (e2.y - e1.y) / barLength
                    } else {
                        barLength = width - sliderRegion.bounds.width()
                        if (barLength == 0){
                            return true
                        }
                        if (firstRatio == null) {
                            firstRatio = sliderRegion.bounds.left.toFloat() / barLength
                        }
                        offsetRatio = (e2.x - e1.x) / barLength
                    }
                    
                    if (scrollType == SCROLL_TYPE_SMOOTH){
                        scrollRecyclerViewByDistance(offsetRatio)
                    }else{
                        var ratio = (firstRatio ?: 0f) + offsetRatio
                        if (ratio <= 0) {
                            ratio = 0f
                        }
                        if (ratio >= 1f) {
                            ratio = 1f
                        }
                        scrollRecyclerViewByPosition(ratio)
                    }
                }
                return true
            }
            return false
        }
    }


    /**
     *  <滚动 RecyclerView：滚动距离>
     */
    private fun scrollRecyclerViewByDistance(offsetRatio: Float) {
        bindView?.adapter?:return
        bindView?.layoutManager?:return
        if (orientation == VERTICAL){
            bindView!!.scrollBy(0, ((orientationHandler.computeRecyclerViewTotalLength(bindView!!) - bindView!!.height) * (offsetRatio - lastOffsetRatio)).roundToInt())
        }else{
            bindView!!.scrollBy(((orientationHandler.computeRecyclerViewTotalLength(bindView!!) - bindView!!.width) * (offsetRatio - lastOffsetRatio)).roundToInt(),0)
        }
        lastOffsetRatio = offsetRatio
    }

    /**
     *  <滚动 RecyclerView：以项为单位>
     */
    fun scrollRecyclerViewByPosition(ratio: Float) {
        bindView?.adapter?:return
        bindView?.layoutManager?:return
        val position = bindView?.adapter!!.itemCount - 1
        val transRatio = if (ratio < 0){
            0f
        }else if (ratio >= 1){
            1f
        }else{
            ratio
        }

        bindView?.layoutManager!!.scrollToPosition(((position * transRatio).toInt()))
    }


    companion object {
        const val VERTICAL = LinearLayout.VERTICAL
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        //  <顺滑滚动>
        const val SCROLL_TYPE_SMOOTH = 1
        //  <以项滚动>
        const val SCROLL_TYPE_ITEM = 2
    }
}