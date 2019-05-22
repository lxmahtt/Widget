package com.kotlin.widget.ext

import android.view.View

/**
 * @description: 扩展
 * @author: James Li
 * @create: 2019/05/22 11:13
 **/

fun View.onClick(method: () -> Unit): View {
    setOnClickListener { method() }
    return this
}