package com.kotlin.widget.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.util.AttributeSet
import android.view.View
import com.kotlin.widget.R

/**
 * @description: 自定义View 看下9.0
 * @author: James Li
 * @create: 2019/05/30 18:05
 **/
class CustomView9 @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mPaint: Paint = Paint()    //画笔，用来绘制遮罩效果

    var frameDrawable: Drawable? = null
    var stateDrawable: Drawable? = null
    var stateMaskDrawable: Drawable? = null
    var sliderDrawable: Drawable? = null

    private var mFrameDrawable: Drawable? = null // 框架层图片
    private var mStateDrawable: Drawable? = null    // 状态图片
    private var mStateMaskDrawable: Drawable? = null    // 状态遮罩图片
    private var mSliderDrawable: Drawable? = null    // 滑块图片

    init {
        frameDrawable = context.resources.getDrawable(R.mipmap.switch_use_frame)
        stateDrawable = context.resources.getDrawable(R.mipmap.switch_use_state)
        stateMaskDrawable = context.resources.getDrawable(R.mipmap.switch_use_state_mask)
        sliderDrawable = context.resources.getDrawable(R.mipmap.switch_use_slider)

        setDrawables(frameDrawable, stateDrawable, stateMaskDrawable, sliderDrawable)
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //canvas.drawBitmap()
        val sliderBitmap = getBitmapFromDrawable(sliderDrawable)
        val frameDrawable = getBitmapFromDrawable(frameDrawable)
        val stateMaskDrawable = getBitmapFromDrawable(stateMaskDrawable)
        val sliderDrawable = getBitmapFromDrawable(sliderDrawable)
        if (sliderBitmap != null && !sliderBitmap.isRecycled) {
            canvas.drawBitmap(sliderBitmap, 0f, 0f, mPaint)
        }

        if (frameDrawable != null && !frameDrawable.isRecycled) {
            canvas.drawBitmap(frameDrawable, 0f, 0f, mPaint)
        }

        if (stateMaskDrawable != null && !stateMaskDrawable.isRecycled) {
            canvas.drawBitmap(stateMaskDrawable, 0f, 0f, mPaint)
        }

        if (sliderDrawable != null && !sliderDrawable.isRecycled) {
            canvas.drawBitmap(sliderDrawable, 0f, 0f, mPaint)
        }

        mStateMaskDrawable!!.draw(canvas)


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
        //
        //mTempMinSlideX = -1 * (stateDrawable.intrinsicWidth - frameDrawable.intrinsicWidth)  // 初始化X轴最小值
        //setSlideX(if (isChecked) mTempMinSlideX else mTempMaxSlideX)  // 根据选中状态初始化默认坐标

        requestLayout()
    }
}