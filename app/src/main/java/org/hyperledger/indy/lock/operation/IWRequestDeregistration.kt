package org.hyperledger.indy.lock.operation

/**
 * IndyAndroid
 * Class: IWRequestDeregistration
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
class IWRequestDeregistration  : IWRequestOperation{

    private lateinit var authenticationType: IWRequestOperation.AuthenticatorType

    override fun setAuthenticationType(type: IWRequestOperation.AuthenticatorType) {

        this.authenticationType = type
    }

    override fun getAuthenticatorType(): IWRequestOperation.AuthenticatorType {

        return this.authenticationType
    }

    override fun getRequestType(): IWRequestOperation.OperationType {

        return IWRequestOperation.OperationType.DEREGISTRATION
    }
}
