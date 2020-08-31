package org.hyperledger.indy.lock.operation

import org.hyperledger.indy.lock.operation.IWRequestOperation.AuthenticatorType

/**
 * IndyAndroid
 * Class: IWRequestRegistration
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
class IWRequestRegistration : IWRequestOperation {

    private lateinit var authenticationType: AuthenticatorType

    override fun setAuthenticationType(type: AuthenticatorType) {

        this.authenticationType = type
    }

    override fun getAuthenticatorType(): AuthenticatorType {

        return this.authenticationType
    }

    override fun getRequestType(): IWRequestOperation.OperationType {

        return IWRequestOperation.OperationType.REGISTRATION
    }
}