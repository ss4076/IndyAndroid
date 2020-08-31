package org.hyperledger.indy.lock.authenticator;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.security.KeyChain;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import org.hyperledger.indy.lock.authenticator.pin.PincodeActivity;
import org.hyperledger.indy.lock.logger.IWLogger;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * IndyAndroid
 * Class: PinAuthenticator
 * Created by djpark on 2020/08/24.
 * <p>
 * Description:
 */
public class PinAuthenticator implements IWAuthenticator {

    private Context context;
    private IWAuthenticatorCallback callback;

    public PinAuthenticator(Context context) {

        this.context = context;
    }

    @Override
    public void registration(IWAuthenticatorCallback callback) {

        this.callback = callback;
        Intent intent = new Intent(context, PincodeActivity.class);
        // PincodeActivity에 this를 어떻게 넘길지...
        ((Activity)context).startActivityForResult(intent, 0);
    }

    @Override
    public void authentication(IWAuthenticatorCallback callback) {

        this.callback = callback;

    }

    @Override
    public void deregistration(IWAuthenticatorCallback callback) {

        this.callback = callback;
    }
}
