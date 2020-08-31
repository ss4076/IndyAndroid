package org.hyperledger.indy.lock.handler

import org.hyperledger.indy.lock.data.IWAuthenticatorResponse

/**
 * IndyAndroid
 * Class: IWRequestHandlerCallback
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
open interface IWRequestHandlerCallback {
    fun onCompleted(response: IWAuthenticatorResponse);
}