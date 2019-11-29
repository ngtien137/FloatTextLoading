package com.luza.floattextloading

import android.util.Log

const val prefixLog = "FTL_LOG"
fun Any.log(message:String){
    Log.e(fixLogTag(this.toString()),message)
}

private fun fixLogTag(tag:String): String {
    var logTag = prefixLog+tag
    if (logTag.length>23){
        logTag = logTag.substring(0,22)
    }
    return logTag
}