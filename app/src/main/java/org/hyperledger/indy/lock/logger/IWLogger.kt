package org.hyperledger.indy.lock.logger

import android.util.Log
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

/**
 * IndyAndroid
 * Class: IWLogger
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
object IWLogger {

    private var sEnabled = false
    private const val TAG_DEFAULT = "[IWLogger]"
    private var sTag = TAG_DEFAULT
    private const val PRINT_LOG_MAX_LENGTH = 1024

    private fun addPrefix(tag: String, msg: String): String {
        return "[$tag] $msg"
    }

    private fun addStackTraceInfo(msg: String): String {
        val stackTraceElement = Thread.currentThread().stackTrace[4]
        val stringBuilder = StringBuilder()
        stringBuilder.append("[")
        stringBuilder.append(stackTraceElement.fileName)
        stringBuilder.append(" -> ")
        stringBuilder.append(stackTraceElement.methodName)
        stringBuilder.append(" -> #")
        stringBuilder.append(stackTraceElement.lineNumber)
        stringBuilder.append("] ")
        stringBuilder.append(msg)
        return stringBuilder.toString()
    }

    fun setEnabled(enabled: Boolean) {
        sEnabled = enabled
    }

    fun setTag(tag: String) {
        sTag = "[$tag]"
    }

    fun w(msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(msg)
            Log.w(sTag, dbgMsg)
        }
    }

    fun w(tag: String, msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.w(sTag, dbgMsg)
        }
    }

    fun w(tag: String, msg: String, encoding: String?) {
        if (sEnabled) {
            var dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            try {
                dbgMsg = String(dbgMsg.toByteArray(charset(encoding!!)), StandardCharsets.UTF_8)
                Log.w(sTag, dbgMsg)
            } catch (e: UnsupportedEncodingException) {
                // TODO Auto-generated catch block
            }
        }
    }

    fun w(tag: String, msg: String, tr: Throwable?) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.w(sTag, dbgMsg, tr)
        }
    }

    fun e(msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(msg)
            Log.e(sTag, dbgMsg)
        }
    }

    fun e(tag: String, msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.e(sTag, dbgMsg)
        }
    }

    fun e(tag: String, msg: String, e: Exception) {
        if (sEnabled) {
            e.printStackTrace()
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.e(sTag, dbgMsg)
        }
    }

    fun e(tag: String, msg: String, encoding: String?) {
        if (sEnabled) {
            var dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            try {
                dbgMsg = String(dbgMsg.toByteArray(charset(encoding!!)), StandardCharsets.UTF_8)
                Log.e(sTag, dbgMsg)
            } catch (e1: UnsupportedEncodingException) {
                // TODO Auto-generated catch block
            }
        }
    }

    fun d(msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(msg)
            var index = 0
            val logLength = dbgMsg.length
            do {
                index += if (logLength - index > PRINT_LOG_MAX_LENGTH) {
                    val endIndex = index + PRINT_LOG_MAX_LENGTH
                    Log.d(sTag, dbgMsg.substring(index, endIndex))
                    PRINT_LOG_MAX_LENGTH
                } else {
                    Log.d(sTag, dbgMsg.substring(index, logLength))
                    logLength - index
                }
            } while (index < logLength)
        }
    }

    fun d(tag: String, msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.d(sTag, dbgMsg)
        }
    }

    fun d(tag: String, msg: String, encoding: String?) {
        if (sEnabled) {
            var dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            try {
                dbgMsg = String(dbgMsg.toByteArray(charset(encoding!!)), StandardCharsets.UTF_8)
                Log.d(sTag, dbgMsg)
            } catch (e: UnsupportedEncodingException) {
                // TODO Auto-generated catch block
            }
        }
    }

    fun i(msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(msg)
            Log.i(sTag, dbgMsg)
        }
    }

    fun i(tag: String, msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.i(sTag, dbgMsg)
        }
    }

    fun i(tag: String, msg: String, e: Exception) {
        if (sEnabled) {
            e.printStackTrace()
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.i(sTag, dbgMsg)
        }
    }

    fun i(tag: String, msg: String, encoding: String?) {
        if (sEnabled) {
            var dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            try {
                dbgMsg = String(dbgMsg.toByteArray(charset(encoding!!)), StandardCharsets.UTF_8)
                Log.i(sTag, dbgMsg)
            } catch (e: UnsupportedEncodingException) {
                // TODO Auto-generated catch block
            }
        }
    }

    fun v(msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(msg)
            Log.v(sTag, dbgMsg)
        }
    }

    fun v(tag: String, msg: String) {
        if (sEnabled) {
            val dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            Log.v(sTag, dbgMsg)
        }
    }

    fun v(tag: String, msg: String, encoding: String?) {
        if (sEnabled) {
            var dbgMsg = addStackTraceInfo(addPrefix(tag, msg))
            try {
                dbgMsg = String(dbgMsg.toByteArray(charset(encoding!!)), StandardCharsets.UTF_8)
                Log.v(sTag, dbgMsg)
            } catch (e: UnsupportedEncodingException) {
                // TODO Auto-generated catch block
            }
        }
    }

    fun isLoggable(): Boolean {
        return sEnabled
    }

    fun isLoggable(tag: String?, level: Int): Boolean {
        return sEnabled && Log.isLoggable(sTag, level)
    }
}