package com.kotlin.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.View
import org.jetbrains.anko.px2sp

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
    1.首先，我们调用了canvas.drawARGB(255, 139, 197, 186)方法将整个Canvas都绘制成一个颜色，在执行完这句代码后，
    canvas上所有像素的颜色值的ARGB颜色都是(255,139,197,186)，由于像素的alpha分量是255而不是0，所以此时所有像素都不透明。

    2.当我们执行了canvas.drawCircle(r, r, r, paint)之后，Android会在所画圆的位置用黄颜色的画笔绘制一个黄色的圆形，
    此时整个圆形内部所有的像素颜色值的ARGB颜色都是0xFFFFCC44，然后用这些黄色的像素替换掉Canvas中对应的同一位置中颜色值
    ARGB为(255,139,197,186)的像素，这样就将黄色圆形绘制到Canvas上了。

    3.当我们执行了canvas.drawRect(r, r, r * 2.7f, r * 2.7f, paint)之后，Android会在所画矩形的位置用蓝色的画笔
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
     * 没有新建图层
    1.首先，我们调用了canvas.drawARGB(255, 139, 197, 186)方法将整个Canvas都绘制成一个颜色，此时所有像素都不透明。

    2.然后我们通过调用canvas.drawCircle(r, r, r, paint)绘制了一个黄色的圆形到Canvas上面。

    3.然后我们执行代码paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR))，将画笔的PorterDuff模式设置为CLEAR。

    4.然后调用canvas.drawRect(r, r, r * 2.7f, r * 2.7f, paint)方法绘制蓝色的矩形，但是最终界面上出现了一个黑色的矩形。

    5.在绘制完成后，我们调用paint.setXfermode(null)将画笔去除Xfermode。
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

        //使用Clear作为PorterDuffXfermode绘制蓝色的矩形
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mPaint.color = Color.BLUE
        canvas.drawRect(r, r, r * 2.7f, r * 2.7f, mPaint)
        //去除Xfermode
        mPaint.xfermode = null
    }*/

    /**
     *1.首先，我们调用了canvas.drawARGB(255, 139, 197, 186)方法将整个Canvas都绘制成一个颜色，此时所有像素都不透明。
     *2.画圆和画矩形，都在新的图层之间
     *
     * Canvas默认一个layer，平时的drawXXX()都是把东西绘制在默认的layer上
     * saveLayer()会重新创建一个layer，新生成的layer，ARGB默认(0,0,0,0)完全透明，该方法，返回一个layerId，可以
     * 通过restoreToCount(layerId)进行绘制融合。
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画背景
        canvas.drawARGB(255, 139, 197, 186)
        val canvasWidth = width.toFloat()
        val r = canvasWidth / 10.0f

        //CLEAR
        val saveLayerClear = canvas.saveLayer(0.0f, 0.0f, canvasWidth, canvasWidth, null)
        //在新图层上画
        //画黄色圆
        mPaint.color = Color.YELLOW
        canvas.drawCircle(r, r, r, mPaint)
        //使用Clear作为PorterDuffXfermode绘制蓝色的矩形
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mPaint.color = Color.BLUE
        canvas.drawRect(r, r, r * 2.5f, r * 2.5f, mPaint)
        mPaint.color = Color.BLACK
        mPaint.textSize = px2sp(180)
        canvas.drawText("CLEAR", r * 0.5f, r / 2, mPaint)
        //去除Xfermode
        mPaint.xfermode = null


        //画黄色圆
        mPaint.color = Color.YELLOW
        canvas.drawCircle(r * 3.5f, r, r, mPaint)
        //使用Clear作为PorterDuffXfermode绘制蓝色的矩形
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        mPaint.color = Color.BLUE
        canvas.drawRect(r * 3.5f, r, r * 5f, r * 2.5f, mPaint)
        mPaint.color = Color.BLACK
        mPaint.textSize = px2sp(180)
        canvas.drawText("SRC", r * 3f, r / 2, mPaint)
        //去除Xfermode
        mPaint.xfermode = null

        //画黄色圆
        mPaint.color = Color.YELLOW
        canvas.drawCircle(r * 6.0f, r, r, mPaint)
        mPaint.color = Color.BLACK
        mPaint.textSize = px2sp(180)
        canvas.drawText("DST_OVER", r * 5.5f, r / 2, mPaint)
        //使用Clear作为PorterDuffXfermode绘制蓝色的矩形
        mPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
        mPaint.color = Color.BLUE
        canvas.drawRect(r * 6.0f, r, r * 7.5f, r * 2.5f, mPaint)
        //去除Xfermode
        mPaint.xfermode = null

        //DST
        //DST_IN
        //SRC_OUT
        
        //融合图层
        canvas.restoreToCount(saveLayerClear)
    }

}