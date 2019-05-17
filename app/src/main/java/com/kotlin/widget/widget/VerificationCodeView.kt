package com.kotlin.widget.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.kotlin.widget.R

class VerificationCodeView(private val mContext: Context, attrs: AttributeSet) : LinearLayout(mContext, attrs),
    TextWatcher, View.OnKeyListener, View.OnFocusChangeListener {
    private var endTime: Long = 0
    var onCodeFinishListener: OnCodeFinishListener? = null

    /**
     * 输入框数量
     */
    private var mEtNumber: Int = 0

    /**
     * 输入框类型
     */
    private var mEtInputType: VCInputType? = null

    /**
     * 输入框的宽度
     */
    private var mEtWidth: Int = 0

    /**
     * 文字颜色
     */
    private var mEtTextColor: Int = 0

    /**
     * 文字大小
     */
    private var mEtTextSize: Float = 0.toFloat()

    /**
     * 输入框背景
     */
    private var mEtTextBg: Int = 0

    private var mCursorDrawable: Int = 0

    enum class VCInputType {
        NUMBER,
        NUMBER_PASSWORD,
        TEXT,
        TEXT_PASSWORD
    }

    init {
        @SuppressLint("Recycle", "CustomViewStyleable")
        val typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.vericationCodeView)
        mEtNumber = typedArray.getInteger(R.styleable.vericationCodeView_vcv_et_number, 4)
        val inputType = typedArray.getInt(R.styleable.vericationCodeView_vcv_et_inputType, VCInputType.NUMBER.ordinal)
        mEtInputType = VCInputType.values()[inputType]
        mEtWidth = typedArray.getDimensionPixelSize(R.styleable.vericationCodeView_vcv_et_width, 120)
        mEtTextColor = typedArray.getColor(R.styleable.vericationCodeView_vcv_et_text_color, Color.BLACK)
        mEtTextSize = typedArray.getDimensionPixelSize(R.styleable.vericationCodeView_vcv_et_text_size, 16).toFloat()
        mEtTextBg = typedArray.getResourceId(R.styleable.vericationCodeView_vcv_et_bg, 0)
        mCursorDrawable = typedArray.getResourceId(R.styleable.vericationCodeView_vcv_et_cursor, R.drawable.et_verification_cursor)

        //释放资源
        typedArray.recycle()
        initView()
    }

    @SuppressLint("ResourceAsColor")
    private fun initView() {
        for (i in 0 until mEtNumber) {
            val editText = EditText(mContext)
            initEditText(editText, i)
            addView(editText)
            if (i == 0) { //设置第一个editText获取焦点
                editText.isFocusable = true
            }
        }
    }

    private fun initEditText(editText: EditText, i: Int) {
        val childHPadding = 14
        val childVPadding = 14

        val layoutParams = LayoutParams(mEtWidth, mEtWidth)
        layoutParams.bottomMargin = childVPadding
        layoutParams.topMargin = childVPadding
        layoutParams.leftMargin = childHPadding
        layoutParams.rightMargin = childHPadding
        layoutParams.gravity = Gravity.CENTER
        editText.layoutParams = layoutParams
        editText.gravity = Gravity.CENTER
        editText.id = i
        editText.isCursorVisible = true
        editText.maxEms = 1
        editText.setTextColor(mEtTextColor)
        editText.textSize = mEtTextSize
        editText.maxLines = 1
        editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))
        when (mEtInputType) {
            VCInputType.NUMBER -> editText.inputType = InputType.TYPE_CLASS_NUMBER
            VCInputType.NUMBER_PASSWORD -> editText.inputType =
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
            VCInputType.TEXT -> editText.inputType = InputType.TYPE_CLASS_TEXT
            VCInputType.TEXT_PASSWORD -> editText.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            else -> editText.inputType = InputType.TYPE_CLASS_NUMBER
        }
        editText.setPadding(0, 0, 0, 0)
        editText.setOnKeyListener(this)
        editText.setBackgroundResource(mEtTextBg)

        //修改光标的颜色（反射）
        try {
            val f = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            f.isAccessible = true
            f.set(editText, mCursorDrawable)
        } catch (ignored: Exception) {
        }

        editText.addTextChangedListener(this)
        editText.onFocusChangeListener = this
        editText.setOnKeyListener(this)
    }


    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable) {
        //输入完成后，转换光标
        focus()
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            backFocus()
        }
        return false
    }

    override fun setEnabled(enabled: Boolean) {
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.isEnabled = enabled
        }
    }

    /**
     * 获取焦点
     */
    private fun focus() {
        //TextView数量
        val count = childCount
        var editText: EditText
        //利用for循环找出还最前面那个还没被输入字符的EditText，并把焦点移交给它。
        for (i in 0 until count) {
            editText = getChildAt(i) as EditText
            if (editText.text.isEmpty()) {
                editText.isCursorVisible = true
                editText.requestFocus()
                break
            } else {
                editText.isCursorVisible = false
            }
        }
        val stringBuffer = StringBuffer()
        for (i in 0 until mEtNumber) {
            editText = getChildAt(i) as EditText
            stringBuffer.append(editText.text)
        }
        var codeText = stringBuffer.toString()
        if (codeText.length == mEtNumber) {
            if (onCodeFinishListener != null) {
                onCodeFinishListener!!.onComplete(codeText)
            }
        } else {
            if (onCodeFinishListener != null) {
                onCodeFinishListener!!.onChange()
            }
        }
    }

    private fun backFocus() {
        //博主手机不好，经常点一次却触发两次`onKey`事件，就设置了一个防止多点击，间隔100毫秒。
        val startTime = System.currentTimeMillis()
        var editText: EditText
        //循环检测有字符的`editText`，把其置空，并获取焦点。
        for (i in mEtNumber - 1 downTo 0) {
            editText = getChildAt(i) as EditText
            if (editText.text.isNotEmpty() && startTime - endTime > 100) {
                editText.setText("")
                editText.isCursorVisible = true
                editText.requestFocus()
                endTime = startTime
                return
            }
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            focus()
        }
    }

    interface OnCodeFinishListener {
        fun onComplete(content: String)
        fun onChange()
    }
}

