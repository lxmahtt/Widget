package com.kotlin.widget.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.kotlin.widget.R
import com.kotlin.widget.ext.onClick
import kotlinx.android.synthetic.main.layout_header_bar.view.*

/**
 * @program: KotlinMall
 * @description: 自定义HeadBar
 * @author: James Li
 * @create: 2019-04-02 20:15
 **/
class HeaderBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isShowBack = true
    private var titleText: String? = null
    private var rightText: String? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderBar)
        isShowBack = typedArray.getBoolean(R.styleable.HeaderBar_isShowBack, true)
        titleText = typedArray.getString(R.styleable.HeaderBar_titleText)
        rightText = typedArray.getString(R.styleable.HeaderBar_rightText)

        //初始化布局
        initView()

        //回收掉
        typedArray.recycle()
    }

    private fun initView() {
        //加载布局
        View.inflate(context, R.layout.layout_header_bar, this)

        //开始定义空间
        mLeftIv.visibility = if (isShowBack) View.VISIBLE else View.GONE
        //先判断titleText是否胃口，如果不为空，作为参数it传给方法内部
        titleText?.let { mTitleTv.text = it }
        rightText?.let {
            mRightTv.text = it
            mRightTv.visibility = View.VISIBLE
        }


        mLeftIv.onClick {
            if (context is Activity) {
                (context as Activity).finish()
            }
        }
    }

    fun getRightView(): TextView {
        return mRightTv
    }


}