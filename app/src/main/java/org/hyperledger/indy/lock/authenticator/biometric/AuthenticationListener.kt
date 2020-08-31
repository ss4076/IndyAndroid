package org.hyperledger.indy.lock.authenticator.biometric

import android.os.Bundle

/**
 * IndyAndroid
 * Class: AuthenticationListener
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
open interface AuthenticationListener {
    fun success(errorCode:Int, message: String, key: ByteArray?)
    fun failure(errorCode: Int, message: String)
}