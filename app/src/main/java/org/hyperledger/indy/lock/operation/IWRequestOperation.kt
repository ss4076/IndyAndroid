package org.hyperledger.indy.lock.operation

/**
 * IndyAndroid
 * Class: IWRequestOperation
 * Created by djpark on 2020/08/28.
 *
 * Description:
 */
interface IWRequestOperation {
    enum class OperationType(val value: Int) {
        REGISTRATION(0x01), AUTHENTICATION(0x02), DEREGISTRATION(0x03);

    }

    enum class AuthenticatorType(val value: Int) {
        BIOMETRIC(0x01), PIN(0x02);

    }

    fun setAuthenticationType(type: AuthenticatorType)
    fun getAuthenticatorType(): AuthenticatorType
    fun getRequestType(): OperationType
}
/*
enum OperationType {
        REGISTRATION            (0x01),
        AUTHENTICATION          (0x02),
        DEREGISTRATION          (0x03);

        private int value;

        OperationType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    enum AuthenticatorType {
        BIOMETRIC            (0x01),
        PIN                  (0x02);

        private int value;

        AuthenticatorType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    void setAuthenticationType(AuthenticatorType type);
    AuthenticatorType getAuthenticatorType();
    OperationType getRequestType();
* */

