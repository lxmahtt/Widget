package com.kotlin.widget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.View
import android.widget.RemoteViews.RemoteView
import com.kotlin.widget.R
import java.util.*

/**
 * 有时针、分针、秒针的显示时钟
 * 换图，表盘，时针，分针，秒针图片
 */
class Clock @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0) : View(context, attrs, defStyle) {

    //当前时间
    private var mCalendar: Calendar? = null

    //表盘，时分秒的背景图
    private val mHourHand: Drawable
    private val mMinuteHand: Drawable
    private val mSecondHand: Drawable
    private val mDial: Drawable

    //表盘宽高
    private val mDialWidth: Int
    private val mDialHeight: Int

    //是否被加载?
    private var mAttached: Boolean = false

    //被加载后，通知重绘
    private val mHandler = Handler()

    //时分秒初始化
    private var mMinutes: Float = 0.toFloat()
    private var mHour: Float = 0.toFloat()
    private var mSecond: Float = 0.toFloat()

    //view变化后，要缩放
    private var mChanged: Boolean = false

    //时间变化后，通知界面改动
    private var tickHandler: Handler? = null

    //时间变化的线程1秒改一次
    private val tickRunnable = object : Runnable {
        override fun run() {
            onTimeChanged()
            postInvalidate()
            tickHandler!!.postDelayed(this, 1000)
        }
    }

    //时区改变后的通知
    private val mIntentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //时区改变
            if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
                val tz = intent.getStringExtra("time-zone")
                //根据时区创建新的时间
                mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz))
            }
            //改变时间
            onTimeChanged()
            //界面重绘
            invalidate()
        }
    }

    init {
        mCalendar = Calendar.getInstance()

        mDial = context.resources.getDrawable(R.mipmap.clock_plate)
        mHourHand = context.resources.getDrawable(R.mipmap.clock_hour)
        mMinuteHand = context.resources.getDrawable(R.mipmap.clock_minute)
        mSecondHand = context.resources.getDrawable(R.mipmap.clock_second)

        mDialWidth = mDial.intrinsicWidth
        mDialHeight = mDial.intrinsicHeight

        prepareRefresh()
    }

    private fun prepareRefresh() {
        tickHandler = Handler()
        tickHandler!!.post(tickRunnable)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!mAttached) {
            mAttached = true
            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_TIME_TICK)
            filter.addAction(Intent.ACTION_TIME_CHANGED)
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
            context.registerReceiver(mIntentReceiver, filter, null,
                mHandler)
        }
        mCalendar = Calendar.getInstance()
        onTimeChanged()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mAttached) {
            context.unregisterReceiver(mIntentReceiver)
            mAttached = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthResult = 0
        //view根据xml中layout_width和layout_height测量出对应的宽度和高度值，
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        when (widthSpecMode) {
            MeasureSpec.UNSPECIFIED -> widthResult = widthSpecSize
            MeasureSpec.AT_MOST//wrap_content时候
            -> widthResult = mDialWidth
            MeasureSpec.EXACTLY ->
                //当xml布局中是准确的值，比如200dp是，判断一下当前view的宽度和准确值,取两个中大的，这样的好处是子View至少保持原来大小
                widthResult = widthSpecSize
        }

        var heightResult = 0
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        when (heightSpecMode) {
            MeasureSpec.UNSPECIFIED -> heightResult = heightSpecSize
            MeasureSpec.AT_MOST//wrap_content时候
            -> heightResult = mDialHeight
            MeasureSpec.EXACTLY ->
                //当xml布局中是准确的值，比如200dp是，判断一下当前view的宽度和准确值,取两个中大的，这样的好处是子View至少保持原来大小
//                heightResult = Math.max(getContentHeight(),heightSpecSize)
                heightResult = heightSpecSize

        }

        setMeasuredDimension(widthResult, heightResult)
    }

    private fun getContentHeight(): Int {
        return mDialWidth + paddingLeft + paddingRight
    }

    private fun getContentWidth(): Int {
        return mDialHeight + paddingTop + paddingBottom
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mChanged = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val changed = mChanged
        if (changed) {
            mChanged = false
        }

        //自定义控件的宽高
        val availableWidth = right - left
        val availableHeight = bottom - top

        //获取一半后，放图片
        val x = availableWidth / 2
        val y = availableHeight / 2

        //默认是不缩放的
        var scaled = false

        //如果View的宽高比表盘小，对画布进行缩放
        if (availableWidth != mDialWidth || availableHeight != mDialHeight) {
            scaled = true
            //获取画布缩放比例
            val scale = Math.min(availableWidth.toFloat() / mDialWidth.toFloat(),
                availableHeight.toFloat() / mDialHeight.toFloat())
            canvas.save()
            canvas.scale(scale, scale, x.toFloat(), y.toFloat())
        }

        //画表盘
        if (changed) {
            //将表盘绘制在：View的宽高减去图片的宽高
            mDial.setBounds(x - mDialWidth / 2, y - mDialHeight / 2, x + mDialWidth / 2, y + mDialHeight / 2)
        }
        mDial.draw(canvas)
        canvas.save()

        var w: Int
        var h: Int
        //画时针
        canvas.rotate(mHour / 12.0f * 360.0f, x.toFloat(), y.toFloat())
        if (changed) {
            w = mHourHand.intrinsicWidth
            h = mHourHand.intrinsicHeight
            mHourHand.setBounds(x - w / 2, y - h / 2, x + w / 2, y + h / 2)
        }
        mHourHand.draw(canvas)
        canvas.restore()

        //画分针
        canvas.save()
        canvas.rotate(mMinutes / 60.0f * 360.0f, x.toFloat(), y.toFloat())
        if (changed) {
            w = mMinuteHand.intrinsicWidth
            h = mMinuteHand.intrinsicHeight
            mMinuteHand.setBounds(x - w / 2, y - h / 2, x + w / 2, y + h / 2)
        }
        mMinuteHand.draw(canvas)
        canvas.restore()

        //画秒针
        canvas.save()
        canvas.rotate(mSecond / 60.0f * 360.0f, x.toFloat(), y.toFloat())
        val secondHand = mSecondHand
        if (changed) {
            w = secondHand.intrinsicWidth
            h = secondHand.intrinsicHeight
            secondHand.setBounds(x - w / 2, y - h / 2, x + w / 2, y + h / 2)
        }
        secondHand.draw(canvas)
        canvas.restore()
        if (scaled) {
            canvas.restore()
        }
    }

    private fun onTimeChanged() {
        mCalendar = Calendar.getInstance()
        val hour = mCalendar!!.get(Calendar.HOUR)
        val minute = mCalendar!!.get(Calendar.MINUTE)
        val second = mCalendar!!.get(Calendar.SECOND)

        mMinutes = minute + second / 60.0f
        mHour = hour + mMinutes / 60.0f
        mSecond = second.toFloat()
        mChanged = true
        updateContentDescription()
    }

    private fun updateContentDescription() {
        val flags = DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_24HOUR
        val contentDescription = DateUtils.formatDateTime(context,
            System.currentTimeMillis(), flags)
        setContentDescription(contentDescription)
    }
}