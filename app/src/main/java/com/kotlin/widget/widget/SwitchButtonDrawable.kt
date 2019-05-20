package com.kotlin.widget.widget

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CompoundButton
import android.widget.Scroller
import com.kotlin.widget.R

class SwitchButtonDrawable @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    CompoundButton(context, attrs, defStyle) {

    companion object {
        private const val TOUCH_MODE_IDLE = 0
        private const val TOUCH_MODE_DOWN = 1
        private const val TOUCH_MODE_DRAGGING = 2
    }

    private var withTextInterval: Int = 0   // 文字和按钮之间的间距
    private var frameDrawable: Drawable? = null // 框架层图片
    private var stateDrawable: Drawable? = null    // 状态图片
    private var stateMaskDrawable: Drawable? = null    // 状态遮罩图片
    private var sliderDrawable: Drawable? = null    // 滑块图片

    private var buttonLeft: Int = 0  // 按钮在画布上的X坐标
    private var buttonTop: Int = 0  // 按钮在画布上的Y坐标
    private var tempSlideX = 0 // X 轴当前坐标，用于动态绘制图片显示坐标，实现滑动效果
    private var tempMinSlideX = 0  // X 轴最小坐标，用于防止往左边滑动时超出范围
    private val tempMaxSlideX = 0  // X 轴最大坐标，用于防止往右边滑动时超出范围
    private var tempTotalSlideDistance: Int = 0   //滑动距离，用于记录每次滑动的距离，在滑动结束后根据距离判断是否切换状态或者回滚
    private var duration = 200 //动画持续时间
    private var touchMode: Int = 0 //触摸模式，用来在处理滑动事件的时候区分操作
    private val touchSlop: Int
    private var touchX: Float = 0.toFloat()   //记录上次触摸坐标，用于计算滑动距离
    private var minChangeDistanceScale = 0.2f   //有效距离比例，例如按钮宽度为 100，比例为 0.3，那么只有当滑动距离大于等于 (100*0.3) 才会切换状态，否则就回滚
    private val paint: Paint    //画笔，用来绘制遮罩效果
    private val buttonRectF: RectF   //按钮的位置
    private val switchScroller: SwitchScroller?  //切换滚动器，用于实现平滑滚动效果
    private val porterDuffMaskType: PorterDuffXfermode//遮罩类型

    init {
        gravity = Gravity.CENTER_VERTICAL
        paint = Paint()
        paint.color = Color.RED
        porterDuffMaskType = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        switchScroller = SwitchScroller(getContext(), AccelerateDecelerateInterpolator())
        buttonRectF = RectF()
        withTextInterval = (16 * context.resources.displayMetrics.density + 0.5).toInt()

        var frameDrawable: Drawable? = null
        var stateDrawable: Drawable? = null
        var stateMaskDrawable: Drawable? = null
        var sliderDrawable: Drawable? = null
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SwitchButtonDrawable)
            if (typedArray != null) {
                withTextInterval =
                    typedArray.getDimension(
                        R.styleable.SwitchButtonDrawable_switch_withTextInterval,
                        withTextInterval.toFloat()
                    )
                        .toInt()
                frameDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_frameDrawable)
                stateDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_stateDrawable)
                stateMaskDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_stateMaskDrawable)
                sliderDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_sliderDrawable)
                typedArray.recycle()
            }
        }
        if (frameDrawable == null || stateDrawable == null || stateMaskDrawable == null || sliderDrawable == null) {
            frameDrawable = context.resources.getDrawable(R.mipmap.switch_frame)
            stateDrawable = context.resources.getDrawable(R.drawable.selector_switch_state)
            stateMaskDrawable = context.resources.getDrawable(R.mipmap.switch_state_mask)
            sliderDrawable = context.resources.getDrawable(R.drawable.selector_switch_slider)
        }
        setDrawables(frameDrawable, stateDrawable, stateMaskDrawable, sliderDrawable)

        val config = ViewConfiguration.get(getContext())
        touchSlop = config.scaledTouchSlop
        isChecked = isChecked
        isClickable = true //设置允许点击，当用户点击在按钮其它区域的时候就会切换状态
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 计算宽度
        var measureWidth: Int
        measureWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST   // 如果 widthSize 是当前视图可使用的最大宽度
            -> compoundPaddingLeft + compoundPaddingRight
            MeasureSpec.EXACTLY   // 如果 widthSize 是当前视图可使用的绝对宽度
            -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.UNSPECIFIED   // 如果 widthSize 对当前视图宽度的计算没有任何参考意义
            -> compoundPaddingLeft + compoundPaddingRight
            else -> compoundPaddingLeft + compoundPaddingRight
        }

        // 计算高度
        var measureHeight: Int
        measureHeight = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.AT_MOST   // 如果 heightSize 是当前视图可使用的最大宽度
            -> (if (frameDrawable != null) frameDrawable!!.intrinsicHeight else 0) + compoundPaddingTop + compoundPaddingBottom
            MeasureSpec.EXACTLY//如果heightSize是当前视图可使用的绝对宽度
            -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.UNSPECIFIED   // 如果 heightSize 对当前视图宽度的计算没有任何参考意义
            -> (if (frameDrawable != null) frameDrawable!!.intrinsicHeight else 0) + compoundPaddingTop + compoundPaddingBottom
            else -> (if (frameDrawable != null) frameDrawable!!.intrinsicHeight else 0) + compoundPaddingTop + compoundPaddingBottom
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (measureWidth < measuredWidth) {
            measureWidth = measuredWidth
        }

        if (measureHeight < measuredHeight) {
            measureHeight = measuredHeight
        }

        setMeasuredDimension(measureWidth, measureHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val drawables = compoundDrawables
        var drawableRightWidth = 0
        var drawableTopHeight = 0
        var drawableBottomHeight = 0
        if (drawables.size > 1 && drawables[1] != null) {
            drawableTopHeight = drawables[1].intrinsicHeight + compoundDrawablePadding
        }
        if (drawables.size > 2 && drawables[2] != null) {
            drawableRightWidth = drawables[2].intrinsicWidth + compoundDrawablePadding
        }
        if (drawables.size > 3 && drawables[3] != null) {
            drawableBottomHeight = drawables[3].intrinsicHeight + compoundDrawablePadding
        }

        buttonLeft =
            width - (if (frameDrawable != null) frameDrawable!!.intrinsicWidth else 0) - paddingRight - drawableRightWidth
        buttonTop =
            (height - (if (frameDrawable != null) frameDrawable!!.intrinsicHeight else 0) + drawableTopHeight - drawableBottomHeight) / 2
        val buttonRight = buttonLeft + if (frameDrawable != null) frameDrawable!!.intrinsicWidth else 0
        val buttonBottom = buttonTop + if (frameDrawable != null) frameDrawable!!.intrinsicHeight else 0
        buttonRectF.set(buttonLeft.toFloat(), buttonTop.toFloat(), buttonRight.toFloat(), buttonBottom.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 保存图层并全体偏移，让 paddingTop 和 paddingLeft 生效
        canvas.save()
        canvas.translate(buttonLeft.toFloat(), buttonTop.toFloat())

        // 绘制状态层
        if (stateDrawable != null && stateMaskDrawable != null) {
            val stateBitmap = getBitmapFromDrawable(stateDrawable)
            if (stateMaskDrawable != null && stateBitmap != null && !stateBitmap.isRecycled) {
                // 保存并创建一个新的透明层，如果不这样做的话，画出来的背景会是黑的
                val src = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), paint, Canvas.ALL_SAVE_FLAG)
                // 绘制遮罩层
                stateMaskDrawable!!.draw(canvas)
                // 绘制状态图片按并应用遮罩效果
                paint.xfermode = porterDuffMaskType
                canvas.drawBitmap(stateBitmap, tempSlideX.toFloat(), 0f, paint)
                paint.xfermode = null
                // 融合图层
                canvas.restoreToCount(src)
            }
        }

        // 绘制框架层
        if (frameDrawable != null) {
            frameDrawable!!.draw(canvas)
        }

        // 绘制滑块层
        if (sliderDrawable != null) {
            val sliderBitmap = getBitmapFromDrawable(sliderDrawable)
            if (sliderBitmap != null && !sliderBitmap.isRecycled) {
                canvas.drawBitmap(sliderBitmap, tempSlideX.toFloat(), 0f, paint)
            }
        }

        // 融合图层
        canvas.restore()
    }

    @SuppressLint("ClickableViewAccessibility", "ObsoleteSdkInt")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            event.actionMasked
        } else {
            event.action and MotionEvent.ACTION_MASK
        }) {
            MotionEvent.ACTION_DOWN -> {
                // 如果按钮当前可用并且按下位置正好在按钮之内
                if (isEnabled && buttonRectF.contains(event.x, event.y)) {
                    touchMode = TOUCH_MODE_DOWN
                    tempTotalSlideDistance = 0 // 清空总滑动距离
                    touchX = event.x  // 记录X轴坐标
                    isClickable = false    // 当用户触摸在按钮位置的时候禁用点击效果，这样做的目的是为了不让背景有按下效果
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    TOUCH_MODE_IDLE -> {
                    }
                    TOUCH_MODE_DOWN -> {
                        val x = event.x
                        if (Math.abs(x - touchX) > touchSlop) {
                            touchMode = TOUCH_MODE_DRAGGING
                            // 禁值父View拦截触摸事件
                            // 如果不加这段代码的话，当被 ScrollView 包括的时候，你会发现，当你在此按钮上按下，
                            // 紧接着滑动的时候 ScrollView 会跟着滑动，然后按钮的事件就丢失了，这会造成很难完成滑动操作
                            // 这样一来用户会抓狂的，加上这句话呢 ScrollView 就不会滚动了
                            if (parent != null) {
                                parent.requestDisallowInterceptTouchEvent(true)
                            }
                            touchX = x
                            return true
                        }
                    }
                    TOUCH_MODE_DRAGGING -> {
                        val newTouchX = event.x
                        tempTotalSlideDistance += setSlideX(tempSlideX + (newTouchX - touchX).toInt())    // 更新X轴坐标并记录总滑动距离
                        touchX = newTouchX
                        invalidate()
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                isClickable = true

                // 结尾滑动操作
                if (touchMode == TOUCH_MODE_DRAGGING) { // 这是滑动操作
                    touchMode = TOUCH_MODE_IDLE
                    // 如果滑动距离大于等于最小切换距离就切换状态，否则回滚
                    if (Math.abs(tempTotalSlideDistance) >= Math.abs(frameDrawable!!.intrinsicWidth * minChangeDistanceScale)) {
                        toggle()   //切换状态
                    } else {
                        switchScroller!!.startScroll(isChecked)
                    }
                } else if (touchMode == TOUCH_MODE_DOWN) { // 这是按在按钮上的单击操作
                    touchMode = TOUCH_MODE_IDLE
                    toggle()
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                isClickable = true
                if (touchMode == TOUCH_MODE_DRAGGING) {
                    touchMode = TOUCH_MODE_IDLE
                    switchScroller!!.startScroll(isChecked) //回滚
                } else {
                    touchMode = TOUCH_MODE_IDLE
                }
            }
        }

        super.onTouchEvent(event)
        return isEnabled
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val drawableState = drawableState
        if (frameDrawable != null) frameDrawable!!.state = drawableState  // 更新框架图片的状态
        if (stateDrawable != null) stateDrawable!!.state = drawableState // 更新状态图片的状态
        if (stateMaskDrawable != null) stateMaskDrawable!!.state = drawableState // 更新状态遮罩图片的状态
        if (sliderDrawable != null) sliderDrawable!!.state = drawableState // 更新滑块图片的状态
        invalidate()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === frameDrawable || who === stateDrawable || who === stateMaskDrawable || who === sliderDrawable
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun jumpDrawablesToCurrentState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.jumpDrawablesToCurrentState()
            if (frameDrawable != null) frameDrawable!!.jumpToCurrentState()
            if (stateDrawable != null) stateDrawable!!.jumpToCurrentState()
            if (stateMaskDrawable != null) stateMaskDrawable!!.jumpToCurrentState()
            if (sliderDrawable != null) sliderDrawable!!.jumpToCurrentState()
        }
    }

    override fun setChecked(checked: Boolean) {
        val changed = checked != isChecked
        super.setChecked(checked)
        if (changed) {
            if (width > 0 && switchScroller != null) {   // 如果已经绘制完成
                switchScroller.startScroll(checked)
            } else {
                setSlideX(if (isChecked) tempMinSlideX else tempMaxSlideX)  // 直接修改X轴坐标，因为尚未绘制完成的时候，动画执行效果不理想，所以直接修改坐标，而不执行动画
            }
        }
    }

    override fun getCompoundPaddingRight(): Int {
        // 重写此方法实现让文本提前换行，避免当文本过长时被按钮给盖住
        var padding = super.getCompoundPaddingRight() + if (frameDrawable != null) frameDrawable!!.intrinsicWidth else 0
        if (!TextUtils.isEmpty(text)) {
            padding += withTextInterval
        }
        return padding
    }

    /**
     * 设置图片
     *
     * @param frameBitmap       框架图片
     * @param stateDrawable     状态图片
     * @param stateMaskDrawable 状态遮罩图片
     * @param sliderDrawable    滑块图片
     */
    private fun setDrawables(
        frameBitmap: Drawable?,
        stateDrawable: Drawable?,
        stateMaskDrawable: Drawable?,
        sliderDrawable: Drawable?
    ) {
        if (frameBitmap == null || stateDrawable == null || stateMaskDrawable == null || sliderDrawable == null) {
            throw IllegalArgumentException("ALL NULL")
        }

        this.frameDrawable = frameBitmap
        this.stateDrawable = stateDrawable
        this.stateMaskDrawable = stateMaskDrawable
        this.sliderDrawable = sliderDrawable

        this.frameDrawable!!.setBounds(0, 0, this.frameDrawable!!.intrinsicWidth, this.frameDrawable!!.intrinsicHeight)
        this.frameDrawable!!.callback = this
        this.stateDrawable!!.setBounds(0, 0, this.stateDrawable!!.intrinsicWidth, this.stateDrawable!!.intrinsicHeight)
        this.stateDrawable!!.callback = this
        this.stateMaskDrawable!!.setBounds(
            0,
            0,
            this.stateMaskDrawable!!.intrinsicWidth,
            this.stateMaskDrawable!!.intrinsicHeight
        )
        this.stateMaskDrawable!!.callback = this
        this.sliderDrawable!!.setBounds(
            0,
            0,
            this.sliderDrawable!!.intrinsicWidth,
            this.sliderDrawable!!.intrinsicHeight
        )
        this.sliderDrawable!!.callback = this

        this.tempMinSlideX = -1 * (stateDrawable.intrinsicWidth - frameBitmap.intrinsicWidth)  // 初始化X轴最小值
        setSlideX(if (isChecked) tempMinSlideX else tempMaxSlideX)  // 根据选中状态初始化默认坐标

        requestLayout()
    }

    /**
     * 设置图片
     *
     * @param frameDrawableResId     框架图片 ID
     * @param stateDrawableResId     状态图片 ID
     * @param stateMaskDrawableResId 状态遮罩图片 ID
     * @param sliderDrawableResId    滑块图片 ID
     */
    fun setDrawableResIds(
        frameDrawableResId: Int,
        stateDrawableResId: Int,
        stateMaskDrawableResId: Int,
        sliderDrawableResId: Int
    ) {
        if (resources != null) {
            setDrawables(
                resources.getDrawable(frameDrawableResId),
                resources.getDrawable(stateDrawableResId),
                resources.getDrawable(stateMaskDrawableResId),
                resources.getDrawable(sliderDrawableResId)
            )
        }
    }

    /**
     * 设置动画持续时间
     *
     * @param duration 动画持续时间
     */
    fun setDuration(duration: Int) {
        this.duration = duration
    }

    /**
     * 设置有效距离比例
     *
     * @param minChangeDistanceScale 有效距离比例，例如按钮宽度为 100，比例为 0.3，那么只有当滑动距离大于等于 (100*0.3) 才会切换状态，否则就回滚
     */
    fun setMinChangeDistanceScale(minChangeDistanceScale: Float) {
        this.minChangeDistanceScale = minChangeDistanceScale
    }

    /**
     * 设置按钮和文本之间的间距
     *
     * @param withTextInterval 按钮和文本之间的间距，当有文本的时候此参数才能派上用场
     */
    fun setWithTextInterval(withTextInterval: Int) {
        this.withTextInterval = withTextInterval
        requestLayout()
    }

    /**
     * 设置X轴坐标
     *
     * @param newSlideX 新的 X 轴坐标
     * @return Xz轴坐标增加的值，例如 newSlideX 等于 100，旧的X轴坐标为 49，那么返回值就是 51
     */
    private fun setSlideX(newSlideX: Int): Int {
        var newSlideX = newSlideX
        //防止滑动超出范围
        if (newSlideX < tempMinSlideX) newSlideX = tempMinSlideX
        if (newSlideX > tempMaxSlideX) newSlideX = tempMaxSlideX
        //计算本次距离增量
        val addDistance = newSlideX - tempSlideX
        this.tempSlideX = newSlideX
        return addDistance
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }

        return when (drawable) {
            is DrawableContainer -> getBitmapFromDrawable(drawable.current)
            is BitmapDrawable -> drawable.bitmap
            else -> null
        }
    }

    /**
     * 切换滚动器，用于实现滚动动画
     */
    private inner class SwitchScroller internal constructor(
        context: Context,
        interpolator: android.view.animation.Interpolator
    ) : Runnable {
        private val scroller: Scroller = Scroller(context, interpolator)

        /**
         * 开始滚动
         *
         * @param checked 是否选中
         */
        internal fun startScroll(checked: Boolean) {
            scroller.startScroll(
                tempSlideX,
                0,
                (if (checked) tempMinSlideX else tempMaxSlideX) - tempSlideX,
                0,
                duration
            )
            post(this)
        }

        override fun run() {
            if (scroller.computeScrollOffset()) {
                setSlideX(scroller.currX)
                invalidate()
                post(this)
            }
        }
    }
}
