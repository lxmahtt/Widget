package com.kotlin.widget.widget

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CompoundButton
import android.widget.Scroller
import com.kotlin.widget.R
import org.jetbrains.anko.AnkoLogger

class SwitchButtonDrawable @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : CompoundButton(context, attrs, defStyle), AnkoLogger {
    companion object {
        private const val TOUCH_MODE_IDLE = 0       //抬起
        private const val TOUCH_MODE_DOWN = 1       //按下
        private const val TOUCH_MODE_DRAGGING = 2   //拖动
    }

    private var mFrameDrawable: Drawable? = null // 框架层图片
    private var mStateDrawable: Drawable? = null    // 状态图片
    private var mStateMaskDrawable: Drawable? = null    // 状态遮罩图片
    private var mSliderDrawable: Drawable? = null    // 滑块图片
    //滑块默认是在左边，默认的CompoundButton是checked
    private var mSwitchToLeft: Boolean = true

    private var mButtonLeft: Int = 0  // 按钮在画布上的X坐标
    private var mButtonTop: Int = 0  // 按钮在画布上的Y坐标
    private var mTempSlideX = 0 // X 轴当前坐标，用于动态绘制图片显示坐标，实现滑动效果
    private var mTempMinSlideX = 0  // X 轴最小坐标，用于防止往左边滑动时超出范围
    private val mTempMaxSlideX = 0  // X 轴最大坐标，用于防止往右边滑动时超出范围
    private var mTempTotalSlideDistance: Int = 0   //滑动距离，用于记录每次滑动的距离，在滑动结束后根据距离判断是否切换状态或者回滚
    private var mDuration = 200 //动画持续时间
    private var mTouchMode: Int = 0 //触摸模式，用来在处理滑动事件的时候区分操作
    private val mTouchSlop: Int
    private var mTouchX: Float = 0.toFloat()   //记录上次触摸坐标，用于计算滑动距离
    private var mMinChangeDistanceScale = 0.2f   //有效距离比例，例如按钮宽度为 100，比例为 0.3，那么只有当滑动距离大于等于 (100*0.3) 才会切换状态，否则就回滚
    private val mPaint: Paint = Paint()    //画笔，用来绘制遮罩效果
    private val mButtonRectF: RectF   //按钮的位置
    private val mSwitchScroller: SwitchScroller?  //切换滚动器，用于实现平滑滚动效果

    init {
        mSwitchScroller = SwitchScroller(getContext(), AccelerateDecelerateInterpolator())
        mButtonRectF = RectF()

        var frameDrawable: Drawable? = null
        var stateDrawable: Drawable? = null
        var stateMaskDrawable: Drawable? = null
        var sliderDrawable: Drawable? = null
        if (attrs != null) {
            val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SwitchButtonDrawable)
            if (typedArray != null) {
                frameDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_frameDrawable)
                stateDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_stateDrawable)
                stateMaskDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_stateMaskDrawable)
                sliderDrawable = typedArray.getDrawable(R.styleable.SwitchButtonDrawable_switch_sliderDrawable)
                mSwitchToLeft = typedArray.getBoolean(R.styleable.SwitchButtonDrawable_switch_sliderToLeft, true)
                typedArray.recycle()
            }
        }
        if (frameDrawable == null || stateDrawable == null || stateMaskDrawable == null || sliderDrawable == null) {
            frameDrawable = context.resources.getDrawable(R.mipmap.switch_use_frame)
            stateDrawable = context.resources.getDrawable(R.mipmap.switch_use_state)
            stateMaskDrawable = context.resources.getDrawable(R.mipmap.switch_use_state_mask)
            sliderDrawable = context.resources.getDrawable(R.mipmap.switch_use_slider)
        }
        setDrawables(frameDrawable, stateDrawable, stateMaskDrawable, sliderDrawable)

        val config = ViewConfiguration.get(getContext())
        mTouchSlop = config.scaledTouchSlop
        isChecked = mSwitchToLeft
        isClickable = true //设置允许点击，当用户点击在按钮其它区域的时候就会切换状态
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 计算宽度
        var measureWidth: Int
        //view根据xml中layout_width和layout_height测量出对应的宽度和高度值，
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        measureWidth = when (widthSpecMode) {
            MeasureSpec.AT_MOST   // 如果 widthSize 是当前视图可使用的最大宽度
            -> mFrameDrawable!!.intrinsicWidth
            MeasureSpec.EXACTLY   // 如果 widthSize 是当前视图可使用的绝对宽度
            -> widthSpecSize
            MeasureSpec.UNSPECIFIED   // 如果 widthSize 对当前视图宽度的计算没有任何参考意义
            -> widthSpecSize
            else -> mFrameDrawable!!.intrinsicWidth
        }

        // 计算高度
        var measureHeight: Int
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        measureHeight = when (heightSpecMode) {
            MeasureSpec.AT_MOST   // 如果 heightSize 是当前视图可使用的最大宽度
            -> mFrameDrawable!!.intrinsicHeight
            MeasureSpec.EXACTLY//如果heightSize是当前视图可使用的绝对宽度
            -> heightSpecSize
            MeasureSpec.UNSPECIFIED   // 如果 heightSize 对当前视图宽度的计算没有任何参考意义
            -> heightSpecSize
            else -> mFrameDrawable!!.intrinsicHeight
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (measureWidth < measuredWidth) {
            measureWidth = measuredWidth
        }

        if (measureHeight < measuredHeight) {
            measureHeight = measureHeight
        }

        setMeasuredDimension(measureWidth, measureHeight)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 保存图层并全体偏移，让 paddingTop 和 paddingLeft 生效
        canvas.save()
        canvas.translate(mButtonLeft.toFloat(), mButtonTop.toFloat())

        // 绘制状态层
        if (mStateDrawable != null && mStateMaskDrawable != null) {
            val stateBitmap = getBitmapFromDrawable(mStateDrawable)
            if (mStateMaskDrawable != null && stateBitmap != null && !stateBitmap.isRecycled) {
                // 保存并创建一个新的透明层，如果不这样做的话，画出来的背景会是黑的
                val layerId = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), mPaint, Canvas.ALL_SAVE_FLAG)

                // 绘制遮罩层
                mStateMaskDrawable!!.draw(canvas)

                // 绘制状态图片按并应用遮罩效果
                mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)//遮罩类型  Alpha混合:新创建的图层和就图层重合后，只显示重合部分

                //画显示的图片
                //mStateDrawable!!.setBounds(mTempSlideX, 0, mTempSlideX + mStateDrawable!!.intrinsicWidth, mStateDrawable!!.intrinsicHeight)
                //mStateDrawable!!.draw(canvas)

                canvas.drawBitmap(stateBitmap, mTempSlideX.toFloat(), 0f, mPaint)

                //将画笔去除Xfermode
                mPaint.xfermode = null
                // 融合图层
                canvas.restoreToCount(layerId)
            }
        }

        // 绘制框架层
        if (mFrameDrawable != null) {
            mFrameDrawable!!.draw(canvas)
        }

        // 绘制滑块层
        if (mSliderDrawable != null) {
            val sliderBitmap = getBitmapFromDrawable(mSliderDrawable)
            if (sliderBitmap != null && !sliderBitmap.isRecycled) {
                canvas.drawBitmap(sliderBitmap, mTempSlideX.toFloat(), 0f, mPaint)

                //mSliderDrawable!!.setBounds(mTempSlideX, 0, mTempSlideX + mSliderDrawable!!.intrinsicWidth, mSliderDrawable!!.intrinsicHeight)
                //mSliderDrawable!!.draw(canvas)
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
                if (isEnabled && mButtonRectF.contains(event.x, event.y)) {
                    mTouchMode = TOUCH_MODE_DOWN
                    mTempTotalSlideDistance = 0 // 清空总滑动距离
                    mTouchX = event.x  // 记录X轴坐标
                    isClickable = false    // 当用户触摸在按钮位置的时候禁用点击效果，这样做的目的是为了不让背景有按下效果
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (mTouchMode) {
                    TOUCH_MODE_IDLE -> {
                    }
                    TOUCH_MODE_DOWN -> {
                        val x = event.x
                        if (Math.abs(x - mTouchX) > mTouchSlop) {
                            mTouchMode = TOUCH_MODE_DRAGGING
                            // 禁值父View拦截触摸事件
                            // 如果不加这段代码的话，当被 ScrollView 包括的时候，你会发现，当你在此按钮上按下，
                            // 紧接着滑动的时候 ScrollView 会跟着滑动，然后按钮的事件就丢失了，这会造成很难完成滑动操作
                            // 这样一来用户会抓狂的，加上这句话呢 ScrollView 就不会滚动了
                            if (parent != null) {
                                parent.requestDisallowInterceptTouchEvent(true)
                            }
                            mTouchX = x
                            return true
                        }
                    }
                    TOUCH_MODE_DRAGGING -> {
                        val newTouchX = event.x
                        mTempTotalSlideDistance += setSlideX(mTempSlideX + (newTouchX - mTouchX).toInt())    // 更新X轴坐标并记录总滑动距离
                        mTouchX = newTouchX
                        invalidate()
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                isClickable = true

                // 结尾滑动操作
                if (mTouchMode == TOUCH_MODE_DRAGGING) { // 这是滑动操作
                    mTouchMode = TOUCH_MODE_IDLE
                    // 如果滑动距离大于等于最小切换距离就切换状态，否则回滚
                    if (Math.abs(mTempTotalSlideDistance) >= Math.abs(mFrameDrawable!!.intrinsicWidth * mMinChangeDistanceScale)) {
                        toggle()   //切换状态
                    } else {
                        mSwitchScroller!!.startScroll(isChecked)
                    }
                } else if (mTouchMode == TOUCH_MODE_DOWN) { // 这是按在按钮上的单击操作
                    mTouchMode = TOUCH_MODE_IDLE
                    toggle()
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_OUTSIDE -> {
                isClickable = true
                if (mTouchMode == TOUCH_MODE_DRAGGING) {
                    mTouchMode = TOUCH_MODE_IDLE
                    mSwitchScroller!!.startScroll(isChecked) //回滚
                } else {
                    mTouchMode = TOUCH_MODE_IDLE
                }
            }
        }

        super.onTouchEvent(event)
        return isEnabled
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val drawableState = drawableState
        if (mFrameDrawable != null) mFrameDrawable!!.state = drawableState  // 更新框架图片的状态
        if (mStateDrawable != null) mStateDrawable!!.state = drawableState // 更新状态图片的状态
        if (mStateMaskDrawable != null) mStateMaskDrawable!!.state = drawableState // 更新状态遮罩图片的状态
        if (mSliderDrawable != null) mSliderDrawable!!.state = drawableState // 更新滑块图片的状态
        invalidate()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mFrameDrawable || who === mStateDrawable || who === mStateMaskDrawable || who === mSliderDrawable
    }

    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun jumpDrawablesToCurrentState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.jumpDrawablesToCurrentState()
            if (mFrameDrawable != null) mFrameDrawable!!.jumpToCurrentState()
            if (mStateDrawable != null) mStateDrawable!!.jumpToCurrentState()
            if (mStateMaskDrawable != null) mStateMaskDrawable!!.jumpToCurrentState()
            if (mSliderDrawable != null) mSliderDrawable!!.jumpToCurrentState()
        }
    }

    override fun setChecked(checked: Boolean) {
        //如果图层的checked和现在的checked不同
        val changed = checked != isChecked
        super.setChecked(checked)
        //如果不同，滑动
        if (changed) {
            if (width > 0 && mSwitchScroller != null) {   // 如果已经绘制完成
                mSwitchScroller.startScroll(checked)
            } else {
                setSlideX(if (isChecked) mTempMinSlideX else mTempMaxSlideX)  // 直接修改X轴坐标，因为尚未绘制完成的时候，动画执行效果不理想，所以直接修改坐标，而不执行动画
            }
        }
    }

    /**
     * 设置图片
     *
     * @param frameDrawable       框架图片
     * @param stateDrawable     状态图片
     * @param stateMaskDrawable 状态遮罩图片
     * @param sliderDrawable    滑块图片
     */
    private fun setDrawables(frameDrawable: Drawable?, stateDrawable: Drawable?, stateMaskDrawable: Drawable?, sliderDrawable: Drawable?) {
        if (frameDrawable == null || stateDrawable == null || stateMaskDrawable == null || sliderDrawable == null) {
            throw IllegalArgumentException("ALL NULL")
        }

        mFrameDrawable = frameDrawable
        mStateDrawable = stateDrawable
        mStateMaskDrawable = stateMaskDrawable
        mSliderDrawable = sliderDrawable

        mFrameDrawable!!.setBounds(0, 0, mFrameDrawable!!.intrinsicWidth, mFrameDrawable!!.intrinsicHeight)
        mFrameDrawable!!.callback = this
        mStateDrawable!!.setBounds(0, 0, mStateDrawable!!.intrinsicWidth, mStateDrawable!!.intrinsicHeight)
        mStateDrawable!!.callback = this
        mStateMaskDrawable!!.setBounds(0, 0, mStateMaskDrawable!!.intrinsicWidth, mStateMaskDrawable!!.intrinsicHeight)
        mStateMaskDrawable!!.callback = this
        mSliderDrawable!!.setBounds(0, 0, mSliderDrawable!!.intrinsicWidth, mSliderDrawable!!.intrinsicHeight)
        mSliderDrawable!!.callback = this

        mTempMinSlideX = -1 * (stateDrawable.intrinsicWidth - frameDrawable.intrinsicWidth)  // 初始化X轴最小值
        setSlideX(if (isChecked) mTempMinSlideX else mTempMaxSlideX)  // 根据选中状态初始化默认坐标

        requestLayout()
    }

    /**
     * 设置X轴坐标
     * @param newSlideX 新的 X 轴坐标
     * @return Xz轴坐标增加的值，例如 newSlideX 等于 100，旧的X轴坐标为 49，那么返回值就是 51
     */
    private fun setSlideX(newSlideX: Int): Int {
        var newSlideX = newSlideX
        //防止滑动超出范围
        if (newSlideX < mTempMinSlideX) newSlideX = mTempMinSlideX
        if (newSlideX > mTempMaxSlideX) newSlideX = mTempMaxSlideX
        //计算本次距离增量
        val addDistance = newSlideX - mTempSlideX
        mTempSlideX = newSlideX
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
    private inner class SwitchScroller internal constructor(context: Context, interpolator: android.view.animation.Interpolator) : Runnable {
        private val scroller: Scroller = Scroller(context, interpolator)

        /**
         * 开始滚动
         * @param checked 是否选中
         */
        internal fun startScroll(checked: Boolean) {
            scroller.startScroll(mTempSlideX, 0, (if (checked) mTempMinSlideX else mTempMaxSlideX) - mTempSlideX, 0, mDuration)
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

