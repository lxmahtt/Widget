package com.kotlin.widget

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.kotlin.widget.ui.CanvasActivity
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

        tag_group.setOnTagChangeListener(object :TagGroup.OnTagChangeListener{
            override fun onAppend(tagGroup: TagGroup?, tag: String?) {

            }

            override fun onDelete(tagGroup: TagGroup?, tag: String?) {
            }

        })

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.mBtnCanvas -> {
                startActivity<CanvasActivity>()
            }
        }
    }
}
