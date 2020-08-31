package org.hyperledger.indy.lock.data

import android.os.Bundle
import org.hyperledger.indy.lock.logger.IWLogger

/**
 * IndyAndroid
 * Class: IWAuthenticatiorResponse
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */

class IWAuthenticatorResponse constructor(bundle: Bundle) {

    private var errorCode = 0
    private var errorMessage: String? = null
    private var key: ByteArray? = null
    init {
        errorCode = bundle.getInt("result_code")
        errorMessage = bundle.getString("result_message")
        key = bundle.getByteArray("result_key")

        IWLogger.d("IWAuthenticatorResponse errorCode :" + errorCode)
        IWLogger.d("IWAuthenticatorResponse errorMessage :" + errorMessage)
        IWLogger.d("IWAuthenticatorResponse key :" + key)
    }

    fun isSuccess(): Boolean {
        return if (errorCode == 0) true else false
    }

    fun getKey(): ByteArray? {
        return key
    }

    fun getErrorCode(): Int {
        return errorCode
    }

    fun getErrorMessage(): String? {
        return errorMessage
    }
}