package com.kotlin.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * @description: Canvas了解
 * @author: James Li
 * @create: 2019/05/22 09:53
 **/
class AboutCanvas @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0) : View(context, attrs, defStyle) {

    private val mPaint: Paint = Paint()

    /**
    首先，我们调用了canvas.drawARGB(255, 139, 197, 186)方法将整个Canvas都绘制成一个颜色，在执行完这句代码后，
    canvas上所有像素的颜色值的ARGB颜色都是(255,139,197,186)，由于像素的alpha分量是255而不是0，所以此时所有像素都不透明。

    当我们执行了canvas.drawCircle(r, r, r, paint)之后，Android会在所画圆的位置用黄颜色的画笔绘制一个黄色的圆形，
    此时整个圆形内部所有的像素颜色值的ARGB颜色都是0xFFFFCC44，然后用这些黄色的像素替换掉Canvas中对应的同一位置中颜色值
    ARGB为(255,139,197,186)的像素，这样就将黄色圆形绘制到Canvas上了。

    当我们执行了canvas.drawRect(r, r, r * 2.7f, r * 2.7f, paint)之后，Android会在所画矩形的位置用蓝色的画笔
    绘制一个蓝色的矩形，此时整个矩形内部所有的像素颜色值的ARGB颜色都是0xFF66AAFF，然后用这些蓝色的像素替换掉Canvas中
    对应的同一位置中的像素，这样黄色的圆中的右下角部分的像素与其他一些背景色像素就被蓝色像素替换了，这样就将蓝色矩形绘制到
    Canvas上了。
     */
    /*@SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画背景
        canvas.drawARGB(255, 139, 197, 186)
        val canvasWidth = canvas.width
        val r = canvasWidth / 3.0f

        mPaint.color = Color.YELLOW
        //画画圆
        canvas.drawCircle(r, r, r, mPaint)

        //换矩形
        mPaint.color = Color.BLUE
        canvas.drawRect(r, r, r * 2.7f, r * 2.7f, mPaint)

    }*/


    /**
    首先，我们调用了canvas.drawARGB(255, 139, 197, 186)方法将整个Canvas都绘制成一个颜色，此时所有像素都不透明。

    然后我们通过调用canvas.drawCircle(r, r, r, paint)绘制了一个黄色的圆形到Canvas上面。

    然后我们执行代码paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR))，将画笔的PorterDuff模式设置为CLEAR。

    然后调用canvas.drawRect(r, r, r * 2.7f, r * 2.7f, paint)方法绘制蓝色的矩形，但是最终界面上出现了一个白色的矩形。

    在绘制完成后，我们调用paint.setXfermode(null)将画笔去除Xfermode。
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画背景
        canvas.drawARGB(255, 139, 197, 186)
        val canvasWidth = canvas.width
        val r = canvasWidth / 3.0f

        mPaint.color = Color.YELLOW
        //画画圆
        canvas.drawCircle(r, r, r, mPaint)

        //使用Clear作为PorterDuffXfermode绘制蓝色的矩形
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mPaint.color = Color.BLUE
        canvas.drawRect(r, r, r * 2.7f, r * 2.7f, mPaint)
        //去除Xfermode
        mPaint.xfermode = null
    }

}