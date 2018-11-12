package com.wanggang.rxjavademo.util

import android.util.Log

object LogUtil {

    var isLog = true   //如果不想打印设置为false,不可打印为false

    var LOGV = true
    var LOGD = true
    var LOGI = true
    var LOGW = true
    var LOGE = true

    private val TAG = "Logger"

    fun v(mess: String?,tag: String = TAG) {
        if (isLog && LOGV) {
            if (mess != null && mess.length > 2000) {
                var i = 0
                while (i < mess.length) {
                    if (i + 2000 < mess.length)
                        Log.v(tag + i, mess.substring(i, i + 2000))
                    else
                        Log.v(tag + i, mess.substring(i, mess.length))
                    i += 2000
                }
            } else if (mess != null) {
                Log.v(tag, mess)
            } else {
                Log.v(tag, "null")
            }
        }
    }

    fun d(mess: String?,tag: String = TAG) {
        if (isLog && LOGD) {
            if (mess != null && mess.length > 2000) {
                var i = 0
                while (i < mess.length) {
                    if (i + 2000 < mess.length)
                        Log.d(tag + i, mess.substring(i, i + 2000))
                    else
                        Log.d(tag + i, mess.substring(i, mess.length))
                    i += 2000
                }
            } else if (mess != null) {
                Log.d(tag, mess)
            } else {
                Log.d(tag, "null")
            }
        }
    }

    fun i(mess: String?,tag: String = TAG) {
        if (isLog && LOGI) {
            if (mess != null && mess.length > 2000) {
                var i = 0
                while (i < mess.length) {
                    if (i + 2000 < mess.length)
                        Log.i(tag + i, mess.substring(i, i + 2000))
                    else
                        Log.i(tag + i, mess.substring(i, mess.length))
                    i += 2000
                }
            } else if (mess != null) {
                Log.i(tag, mess)
            } else {
                Log.i(tag, "null")
            }
        }
    }

    fun w(mess: String?,tag: String = TAG) {
        if (isLog && LOGW) {
            if (mess != null && mess.length > 2000) {
                var i = 0
                while (i < mess.length) {
                    if (i + 2000 < mess.length)
                        Log.w(tag + i, mess.substring(i, i + 2000))
                    else
                        Log.w(tag + i, mess.substring(i, mess.length))
                    i += 2000
                }
            } else if (mess != null) {
                Log.w(tag, mess)
            } else {
                Log.w(tag, "null")
            }
        }
    }

    fun e(mess: String?,tag: String = TAG) {
        if (isLog && LOGE) {
            if (mess != null && mess.length > 2000) {
                var i = 0
                while (i < mess.length) {
                    if (i + 2000 < mess.length)
                        Log.e(tag + i, mess.substring(i, i + 2000)+"\n")
                    else
                        Log.e(tag + i, mess.substring(i, mess.length)+"\n")
                    i += 2000
                }
            } else if (mess != null) {
                Log.e(tag, mess)
            } else {
                Log.e(tag, "null")
            }
        }
    }

}
