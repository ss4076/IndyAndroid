package org.hyperledger.indy.lock.manager

import org.hyperledger.indy.lock.data.IWAuthenticatorResponse

/**
 * IndyAndroid
 * Class: ResultCallback
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
interface IWResultCallback {
    fun onCompleted(response: IWAuthenticatorResponse)
}