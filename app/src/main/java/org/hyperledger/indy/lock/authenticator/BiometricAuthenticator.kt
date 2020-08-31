package org.hyperledger.indy.lock.authenticator

import android.content.Context
import android.os.Build
import android.os.Bundle
import org.hyperledger.indy.lock.authenticator.biometric.AuthenticationListener
import org.hyperledger.indy.lock.authenticator.biometric.BiometricPromptCallback
import org.hyperledger.indy.lock.logger.IWLogger

/**
 * IndyAndroid
 * Class: BiometricAuthenticator
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */

class BiometricAuthenticator(context: Context) : IWAuthenticator {

    private lateinit var authenticatorCallback: IWAuthenticatorCallback

    private val context: Context

    override fun registration(callback: IWAuthenticatorCallback) {
        IWLogger.d("BiometricAuthenticator registration")
        this.authenticatorCallback = callback


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            val bpc = BiometricPromptCallback(context, object : AuthenticationListener {

                override fun success(errorCode:Int, message: String, key: ByteArray?) {
                    IWLogger.d("failed resultCode :$errorCode, message: $message, key: $key")
                    val bundle = Bundle()
                    bundle.putInt("result_code", errorCode)
                    bundle.putString("result_message", message)
                    bundle.putByteArray("result_key", key)
                    authenticatorCallback.onCompleted(bundle)
                }

                override fun failure(errorCode: Int, message: String) {
                    IWLogger.d("failed resultCode :$errorCode, message: $message")
                    val bundle = Bundle()
                    bundle.putInt("result_code", errorCode)
                    bundle.putString("result_message", message)
                    authenticatorCallback.onCompleted(bundle)
                }
            })
            bpc.registration()
        }
    }

    override fun authentication(callback: IWAuthenticatorCallback) {

        this.authenticatorCallback = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            val bpc = BiometricPromptCallback(context, object : AuthenticationListener {

                override fun success(errorCode:Int, message: String, key: ByteArray?) {
                    IWLogger.d("failed resultCode :$errorCode, message: $message, key: $key")
                    val bundle = Bundle()
                    bundle.putInt("result_code", errorCode)
                    bundle.putString("result_message", message)
                    bundle.putByteArray("result_key", key)
                    authenticatorCallback.onCompleted(bundle)
                }

                override fun failure(errorCode: Int, message: String) {
                    IWLogger.d("failed resultCode :$errorCode, message: $message")
                    val bundle = Bundle()
                    bundle.putInt("result_code", errorCode)
                    bundle.putString("result_message", message)
                    authenticatorCallback.onCompleted(bundle)
                }
            })
            bpc.authentication()
        }
    }

    override fun deregistration(callback: IWAuthenticatorCallback) {

        this.authenticatorCallback = callback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

            val bpc = BiometricPromptCallback(context, object : AuthenticationListener {

                override fun success(errorCode:Int, message: String, key: ByteArray?) {
                    IWLogger.d("failed resultCode :$errorCode, message: $message, key: $key")
                    val bundle = Bundle()
                    bundle.putInt("result_code", errorCode)
                    bundle.putString("result_message", message)
                    bundle.putByteArray("result_key", key)
                    authenticatorCallback.onCompleted(bundle)
                }

                override fun failure(errorCode: Int, message: String) {
                    IWLogger.d("failed resultCode :$errorCode, message: $message")
                    val bundle = Bundle()
                    bundle.putInt("result_code", errorCode)
                    bundle.putString("result_message", message)
                    authenticatorCallback.onCompleted(bundle)
                }
            })
            bpc.deregistration()
        }
    }

    init {
        this.context = context
    }
}