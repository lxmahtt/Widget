package com.kotlin.widget.widget

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import com.kotlin.widget.R
import com.kotlin.widget.utils.UtilTools


/**
 * @description: 展开收起
 * @author: James Li
 * @create: 2019/10/07 15:13
 **/
class ExpandTextView : TextView {
    private var originText: String? = null// 原始内容文本
    private var initWidth = 0// TextView可展示宽度
    private var mMaxLines = 3// TextView最大行数
    private var SPAN_CLOSE: SpannableString? = null// 收起的文案(颜色处理)
    private var SPAN_EXPAND: SpannableString? = null// 展开的文案(颜色处理)
    private val TEXT_EXPAND = "　展开"
    private var TEXT_CLOSE = ""

    constructor(context: Context) : super(context) {
        initCloseEnd()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initCloseEnd()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initCloseEnd()
    }

    /**
     * 设置TextView可显示的最大行数
     * @param maxLines 最大行数
     */
    override fun setMaxLines(maxLines: Int) {
        this.mMaxLines = maxLines
        super.setMaxLines(maxLines)
    }

    /**
     * 初始化TextView的可展示宽度
     * @param width
     */
    fun initWidth(width: Int) {
        initWidth = width
    }

    /**
     * 收起的文案(颜色处理)初始化
     */
    private fun initCloseEnd() {
        val content = TEXT_EXPAND
        SPAN_CLOSE = SpannableString(content)
        val span = ButtonSpan(context, OnClickListener {
            super@ExpandTextView.setMaxLines(Integer.MAX_VALUE)
            setExpandText(originText)
        }, R.color.color_5a85fc)
        SPAN_CLOSE!!.setSpan(span, 0, content.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    /**
     * 展开的文案(颜色处理)初始化
     */
    private fun initExpandEnd() {
        val content = TEXT_CLOSE
        SPAN_EXPAND = SpannableString(content)
        val span = ButtonSpan(context, OnClickListener {
            super@ExpandTextView.setMaxLines(mMaxLines)
            setCloseText(originText)
        }, R.color.color_5a85fc)
        SPAN_EXPAND!!.setSpan(span, 0, content.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }

    fun setCloseText(text: CharSequence?) {

        if (SPAN_CLOSE == null) {
            initCloseEnd()
        }
        var appendShowAll = false// true 不需要展开收起功能， false 需要展开收起功能
        originText = text!!.toString()

        // SDK >= 16 可以直接从xml属性获取最大行数
        var maxLines = 0
        maxLines = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getMaxLines()
        } else {
            mMaxLines
        }
        var workingText = StringBuilder(originText!!).toString()
        if (maxLines != -1) {
            val layout = createWorkingLayout(workingText)
            if (layout.lineCount > maxLines) {
                //获取一行显示字符个数，然后截取字符串数
                workingText = originText!!.substring(0, layout.getLineEnd(maxLines - 1))
                    .trim { it <= ' ' }// 收起状态原始文本截取展示的部分
                val showText = originText!!.substring(
                    0,
                    layout.getLineEnd(maxLines - 1)
                ).trim { it <= ' ' } + "..." + SPAN_CLOSE
                var layout2 = createWorkingLayout(showText)
                // 对workingText进行-1截取，直到展示行数==最大行数，并且添加 SPAN_CLOSE 后刚好占满最后一行
                while (layout2.lineCount > maxLines) {
                    val lastSpace = workingText.length - 1
                    if (lastSpace == -1) {
                        break
                    }
                    workingText = workingText.substring(0, lastSpace)
                    layout2 = createWorkingLayout("$workingText...$SPAN_CLOSE")
                }
                appendShowAll = true
                workingText = "$workingText..."
            }
        }

        setText(workingText)
        if (appendShowAll) {
            // 必须使用append，不能在上面使用+连接，否则spannable会无效
            append(SPAN_CLOSE)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun setExpandText(text: String?) {

        val lineEnd = createWorkingLayout(text).getLineEnd(0) - 3
        var textSpan = ""
        for (index in 0..lineEnd) {
            textSpan = "${textSpan}\u3000"
        }
        TEXT_CLOSE = "\n${textSpan}折叠"
        if (SPAN_EXPAND == null) {
            initExpandEnd()
        }
        setText(originText)
        append(SPAN_EXPAND)
        movementMethod = LinkMovementMethod.getInstance()
    }

    //返回textview的显示区域的layout，该textview的layout并不会显示出来，只是用其宽度来比较要显示的文字是否过长
    private fun createWorkingLayout(workingText: String?): Layout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            StaticLayout(
                workingText, paint, initWidth - paddingLeft - paddingRight,
                Layout.Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineSpacingExtra, false
            )
        } else {
            StaticLayout(
                workingText, paint, initWidth - paddingLeft - paddingRight,
                Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
            )
        }
    }

    inner class ButtonSpan @JvmOverloads constructor(
        private val context: Context,
        private var onClickListener: OnClickListener?,
        private val colorId: Int = R.color.white
    ) : ClickableSpan() {

        override fun updateDrawState(ds: TextPaint) {
            ds.color = ContextCompat.getColor(context, colorId)
            ds.textSize = UtilTools.dip2px(context, 14f).toFloat()
            ds.isUnderlineText = false
        }

        override fun onClick(widget: View) {
            if (onClickListener != null) {
                onClickListener!!.onClick(widget)
            }
        }
    }
}