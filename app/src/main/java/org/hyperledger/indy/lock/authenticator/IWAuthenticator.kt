package org.hyperledger.indy.lock.authenticator

/**
 * IndyAndroid
 * Class: IWAuthenticator
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
open interface IWAuthenticator {

    fun registration(callback: IWAuthenticatorCallback)
    fun authentication(callback: IWAuthenticatorCallback)
    fun deregistration(callback: IWAuthenticatorCallback)
}