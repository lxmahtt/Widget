package com.kotlin.widget

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.kotlin.widget.ui.CanvasActivity
import com.kotlin.widget.utils.UtilTools
import com.kotlin.widget.widget.TagGroup
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity(), View.OnClickListener, AnkoLogger {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSwitchButtonDrawable.setOnCheckedChangeListener { _, mChecked ->
            if (mChecked) {
                toast("报备")
            }
        }
        mSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

            }
        }
        mBtnCanvas.setOnClickListener(this)

        val tags = arrayOf("测试", "测试", "测试", "测试", "测试", "测试", "测试", "测试")
        tag_group.setTags(*tags)

        tag_group.setOnTagClickListener { tag ->
            val index = tags.indexOf(tag)

        }

        tag_group.setOnTagChangeListener(object : TagGroup.OnTagChangeListener {
            override fun onAppend(tagGroup: TagGroup?, tag: String?) {

            }

            override fun onDelete(tagGroup: TagGroup?, tag: String?) {
            }

        })

        etv.initWidth(UtilTools.getScreenWidth(this))
        
        etv.maxLines = 1
        val content =
            "中国共产党是中国工人阶级的先锋队，同时是中国人民和中华民族的同时是中国人民和中华民族的先锋队同时是中国人民和中华民族的先锋队同时是中国人民和中华民族的先锋队先锋队，是中国特色社会主义事业的领导核心，代表中国先进生产力的发展要求，代表中国先进文化的前进方向，代表中国最广大人民的根本利益。党的最高理想和最终目标是实现共产主义。"
        etv.setCloseText(content)
        println("+++++++++++++++${etv.lineCount}")

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mBtnCanvas -> {
                startActivity<CanvasActivity>()
            }
        }
    }
}
