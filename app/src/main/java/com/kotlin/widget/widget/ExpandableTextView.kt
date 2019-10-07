package com.kotlin.widget.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView
import com.kotlin.widget.R

/**
 * Created by Boyce
 * on 2019/5/29
 * 可展开折叠的TextView
 */
class ExpandableTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var expandText = "查看更多"
    private var collapseText = "收起全部"
    private var maxExpandLines = 5    // 允许显示最大行数
    private var durationTime = 0          // 动画执行时间
    private lateinit var tvSource: TextView   // 显示文本
    private lateinit var tvExpand: TextView   // 折叠按钮文本
    private var lastHeight = 0           //剩余点击按钮的高度
    private var isCollapsed = true       //默认处于收起状态
    private var collapsedHeight = 0      //收起时候的整体高度
    private var realTextViewHeight = 0   //文本框真实高度
    private var isAnimate = false        //是否正在执行动画
    private var isChange = false         //是否发生过文字变动

    private var listener: OnExpandStateChangeListener? = null
    private var expandDrawable: Drawable? = null
    private var collapseDrawable: Drawable? = null

    fun setOnExpandStateChangeListener(listener: OnExpandStateChangeListener) {
        this.listener = listener
    }

    init {
        orientation = VERTICAL
        attrs?.let { it ->
            val typeArray = context.obtainStyledAttributes(it, R.styleable.ExpandableTextView)
            maxExpandLines = typeArray.getInteger(R.styleable.ExpandableTextView_maxExpandLines, 5)
            durationTime = typeArray.getInteger(R.styleable.ExpandableTextView_duration, 0)
            val expandStr = typeArray.getString(R.styleable.ExpandableTextView_expandText)
            if (expandStr != null) expandText = expandStr
            val collapseStr = typeArray.getString(R.styleable.ExpandableTextView_collapseText)
            if (collapseStr != null) collapseText = collapseStr

            typeArray.getDrawable(R.styleable.ExpandableTextView_expandImage)?.let {
                expandDrawable = it
            }

            typeArray.getDrawable(R.styleable.ExpandableTextView_collapseImage)?.let {
                collapseDrawable = it
            }
            typeArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        tvSource = findViewById(R.id.id_source_textview)
        tvExpand = findViewById(R.id.id_expand_textview)
        tvExpand.setOnClickListener {
            val animation: ExpandCollapseAnimation

            isCollapsed = isCollapsed.not()
            if (isCollapsed) {
                tvExpand.text = expandText
                tvExpand.setCompoundDrawablesWithIntrinsicBounds(null, null, collapseDrawable, null)
                listener?.onExpandStateChanged(false, collapsedHeight)
                animation = ExpandCollapseAnimation(height, collapsedHeight)
            } else {
                tvExpand.text = collapseText
                tvExpand.setCompoundDrawablesWithIntrinsicBounds(null, null, expandDrawable, null)
                listener?.onExpandStateChanged(
                    true,
                    realTextViewHeight + lastHeight - collapsedHeight
                )
                animation = ExpandCollapseAnimation(height, realTextViewHeight + lastHeight)
            }

            animation.fillAfter = true
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    isAnimate = true
                }

                override fun onAnimationEnd(animation: Animation) {
                    clearAnimation()
                    isAnimate = false
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            clearAnimation()
            startAnimation(animation)
            //不带动画的处理方式
            //                isChange=true;
            //                requestLayout();
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        //执行动画的过程中屏蔽事件
        return isAnimate
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //如果隐藏控件或者textview的值没有发生改变，那么不进行测量
        if (visibility == View.GONE || !isChange) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        isChange = false

        //初始化默认状态，即正常显示文本
        tvExpand.visibility = View.GONE
        tvSource.maxLines = Integer.MAX_VALUE
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        //如果本身没有达到收起展开的限定要求，则不进行处理
        if (tvSource.lineCount <= maxExpandLines) {
            return
        }

        //初始化高度赋值，为后续动画事件准备数据
        realTextViewHeight = getRealTextViewHeight(tvSource)

        //如果处于收缩状态，则设置最多显示行数
        if (isCollapsed) {
            tvSource.maxLines = maxExpandLines
        }
        tvExpand.visibility = View.VISIBLE
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (isCollapsed) {
            tvSource.post {
                lastHeight = height - tvSource.height
                collapsedHeight = measuredHeight
            }
        }
    }

    /**
     * 获取textview的真实高度
     * @param textView
     * @return
     */
    private fun getRealTextViewHeight(textView: TextView): Int {
        //getLineTop返回值是一个根据行数而形成等差序列，如果参数为行数，则值即为文本的高度
        val textHeight = textView.layout.getLineTop(textView.lineCount)
        return textHeight + textView.compoundPaddingBottom + textView.compoundPaddingTop
    }

    fun setText(text: String) {
        isChange = true
        tvSource.text = text
    }

    fun setText(text: String, isCollapsed: Boolean) {
        this.isCollapsed = isCollapsed
        if (isCollapsed) {
            tvExpand.text = expandText
        } else {
            tvExpand.text = collapseText
        }
        clearAnimation()
        setText(text)
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    inner class ExpandCollapseAnimation(private val startValue: Int, private val endValue: Int) :
        Animation() {
        init {
            duration = durationTime.toLong()
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
            super.applyTransformation(interpolatedTime, t)
            val height = (endValue - startValue) * interpolatedTime + startValue
            tvSource.maxHeight = height.toInt() - lastHeight
            this@ExpandableTextView.layoutParams.height = height.toInt()
            this@ExpandableTextView.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    interface OnExpandStateChangeListener {
        fun onExpandStateChanged(isExpanded: Boolean, height: Int)
    }
}