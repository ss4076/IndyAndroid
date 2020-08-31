package org.hyperledger.indy.lock.authenticator

import android.os.Bundle

/**
 * IndyAndroid
 * Class: IWAuthenticatorCallback
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
open interface IWAuthenticatorCallback {
    fun onCompleted(response: Bundle)
}