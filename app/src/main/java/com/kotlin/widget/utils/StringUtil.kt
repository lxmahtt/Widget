package com.kotlin.widget.utils

class StringUtil {
    companion object {
        val MI_PUSH_APP_ID: String
            get() = "2882303761517839196"
        val MI_PUSH_APP_KEY: String
            get() = "5111783919196"
        val SECRETKEY: String
            get() = "7Y69329150U2C12P5538b5F51fcb18Z2"
        val USER_INFO: String
            get() = "USER_INFO"
        val SKIP_UPDATE: String
            get() = "SKIP_UPDATE"
        const val LOGIN_PHONE: String = "LOGIN_PHONE"

        const val PAGE_SIZE: String = "30"


        /**
         * 不规则数据处理
         * @param string
         * @return
         */
        fun replaceStrs(string: String, replace: String): String {
            var string = string
            val array = arrayOf("_", "/", "#", ".", " ", "-")
            for (s1 in array) {
                if (string.contains(s1)) {
                    string = string.replace(s1, replace)
                }
            }

            return string
        }

        const val IS_POST: String = "IS_POST"
        const val POST_BODY: String = "POST_BODY"
        const val PROJECT_ID: String = "PROJECT_ID"
        const val START_DATE: String = "START_DATE"
        const val END_DATE: String = "END_DATE"
        const val TITLE_INFO: String = "TITLE_INFO"
        const val DOWN_LOAD_URL: String = "DOWN_LOAD_URL"
        const val WEB_URL: String = "WEB_URL"
        const val BUNDLE_1: String = "BUNDLE_1"
        const val BUNDLE_2: String = "BUNDLE_2"
        const val BUNDLE_3: String = "BUNDLE_3"
        const val BUNDLE_4: String = "BUNDLE_4"
        const val BUNDLE_5: String = "BUNDLE_5"
        const val LOC_LAT: String = "LOC_LAT"
        const val LOC_LONG: String = "LOC_LONG"
        const val SEARCH_TYPE: String = "SEARCH_TYPE"
        const val STORE_ID: String = "STORE_ID"
        const val TYPE: String = "TYPE" // 0项目专员,1区域经理
        const val IS_FIRST: String = "IS_FIRST"
        const val IS_FIRST_STORE: String = "IS_FIRST_STORE"
        const val HISTORY_SEARCH: String = "HISTORY_SEARCH"//项目
        const val HISTORY_SEARCH_STORE: String = "HISTORY_SEARCH_STORE"//根据关键字搜索门店
        const val HISTORY_SEARCH_BROKER: String = "HISTORY_SEARCH_BROKER"//经纪人
        const val HISTORY_SEARCH_COMPANY: String = "HISTORY_SEARCH_COMPANY" //机构
        const val HISTORY_STORE: String = "HISTORY_STORE" //门店
        const val HOUSE_INFO: String = "HOUSE_INFO"
        const val ROLE_CHANGE: String = "ROLE_CHANGE"
    }

}