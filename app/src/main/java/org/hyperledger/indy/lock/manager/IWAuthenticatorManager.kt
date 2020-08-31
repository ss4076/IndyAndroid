package org.hyperledger.indy.lock.manager

import android.content.Context
import org.hyperledger.indy.lock.data.IWAuthenticatorResponse
import org.hyperledger.indy.lock.handler.IWRequestHandler
import org.hyperledger.indy.lock.handler.IWRequestHandlerCallback
import org.hyperledger.indy.lock.logger.IWLogger
import org.hyperledger.indy.lock.operation.IWRequestAuthentication
import org.hyperledger.indy.lock.operation.IWRequestDeregistration
import org.hyperledger.indy.lock.operation.IWRequestOperation
import org.hyperledger.indy.lock.operation.IWRequestOperation.AuthenticatorType
import org.hyperledger.indy.lock.operation.IWRequestRegistration

/**
 * IndyAndroid
 * Class: IWAuthenticatorManager (SingleTon)
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
object IWAuthenticatorManager {

    fun registration(context: Context, type: AuthenticatorType, resultCallback: IWResultCallback) {

        val reg = IWRequestRegistration() as IWRequestOperation
        reg.setAuthenticationType(type)
        IWLogger.d("IWAuthenticatorManager reg")
        IWRequestHandler(context).handler(reg, object : IWRequestHandlerCallback {
            override fun onCompleted(response: IWAuthenticatorResponse) {
                resultCallback.onCompleted(response)
            }
        })
    }

    fun authentication(context: Context, type: AuthenticatorType, resultCallback: IWResultCallback) {

        val auth = IWRequestAuthentication()
        auth.setAuthenticationType(type)
        IWRequestHandler(context).handler(auth, object : IWRequestHandlerCallback {
            override fun onCompleted(response: IWAuthenticatorResponse) {
                resultCallback.onCompleted(response)
            }
        })
    }

    fun deregistration(context: Context, type: AuthenticatorType, resultCallback: IWResultCallback) {

        val dereg = IWRequestDeregistration()
        dereg.setAuthenticationType(type)
        IWRequestHandler(context).handler(dereg, object : IWRequestHandlerCallback {
            override fun onCompleted(response: IWAuthenticatorResponse) {
                resultCallback.onCompleted(response)
            }
        })
    }
}