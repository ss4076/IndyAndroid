package org.hyperledger.indy.lock.handler

import android.content.Context
import android.os.Bundle
import org.hyperledger.indy.lock.authenticator.BiometricAuthenticator
import org.hyperledger.indy.lock.authenticator.IWAuthenticator
import org.hyperledger.indy.lock.authenticator.IWAuthenticatorCallback
import org.hyperledger.indy.lock.authenticator.PinAuthenticator
import org.hyperledger.indy.lock.data.IWAuthenticatorResponse
import org.hyperledger.indy.lock.logger.IWLogger
import org.hyperledger.indy.lock.operation.IWRequestOperation
import org.hyperledger.indy.lock.operation.IWRequestOperation.OperationType.*
import org.hyperledger.indy.lock.operation.IWRequestOperation.AuthenticatorType.*

/**
 * IndyAndroid
 * Class: IWRequestHandle
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
class IWRequestHandler constructor(var context: Context) {

    private var mContext: Context

    lateinit var handlerCallback: IWRequestHandlerCallback

    init {
        this.mContext = context
    }

    fun handler(request: IWRequestOperation, callback: IWRequestHandlerCallback) {
        IWLogger.d("IWRequestHandler handler")
        this.handlerCallback = callback;

        if (!this.isValidRequest(request)) {
            IWLogger.d("IWRequestHandler isValidRequest request: ${request}")
            var bundle = Bundle()
            bundle.putInt("result_code", -1);
            bundle.putString("result_message", "request validation check fail")
            this.handlerCallback.onCompleted(IWAuthenticatorResponse(bundle))
            return
        }

        IWLogger.d("IWRequestHandler request type ${request.getRequestType()}")
        IWLogger.d("IWRequestHandler authenticator type ${request.getAuthenticatorType()}")

        when(request.getRequestType()) {
            REGISTRATION-> this.registration(request)
            AUTHENTICATION-> this.authentication(request)
            DEREGISTRATION-> this.deregistration(request)
            else-> println("Error")
        }
    }

    private fun deregistration(request: IWRequestOperation) {

        this.getAuthenticator(request).deregistration(object: IWAuthenticatorCallback {
            override fun onCompleted(bundle: Bundle) {
                IWLogger.d("IWRequestHandler deregistration ${bundle}")
                handlerCallback.onCompleted(IWAuthenticatorResponse(bundle))
            }
        })
    }

    private fun authentication(request: IWRequestOperation) {

        this.getAuthenticator(request).authentication(object: IWAuthenticatorCallback {
            override fun onCompleted(bundle: Bundle) {
                IWLogger.d("IWRequestHandler authentication ${bundle}")
                handlerCallback.onCompleted(IWAuthenticatorResponse(bundle))
            }
        })
    }

    private fun registration(request: IWRequestOperation) {

        this.getAuthenticator(request).registration(object: IWAuthenticatorCallback {
            override fun onCompleted(bundle: Bundle) {
                IWLogger.d("IWRequestHandler registration ${bundle}")
                handlerCallback.onCompleted(IWAuthenticatorResponse(bundle))
            }
        })
    }

    private fun getAuthenticator(request:IWRequestOperation): IWAuthenticator {

        if (request.getAuthenticatorType().value == PIN.value) {
            return PinAuthenticator(mContext)
        } else {
            return BiometricAuthenticator(mContext)
        }
    }

    private fun isValidRequest(request: IWRequestOperation) : Boolean {

        if (request == null) {
            return false
        }
        // TODO 코틀린 문법으로 더 좋은 방법이 있을 듯.
        if (request.getRequestType().value < REGISTRATION.value
                || request.getRequestType().value > DEREGISTRATION.value)
            return false;

        if (request.getAuthenticatorType().value < BIOMETRIC.value
                || request.getAuthenticatorType().value > PIN.value)
            return false;

        return true
    }
}