package org.hyperledger.indy.lock.authenticator.biometric

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import org.hyperledger.indy.lock.logger.IWLogger


import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.ISO_8859_1
import java.security.*
import java.security.cert.CertificateException
import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec

/**
 * IndyAndroid
 * Class: BiometricPromptCallback
 * Created by djpark on 2020/08/24.
 *
 *
 * Description:
 */
@RequiresApi(api = Build.VERSION_CODES.P)
class BiometricPromptCallback(context: Context, authenticationListener: AuthenticationListener) {

    private val KEY_NAME = "IW_FiNGER_WARP"
    private var mContext: Context? = null
    private var androidKeyStore: KeyStore? = null

    private var mRegBiometricPrompt: BiometricPrompt? = null
    private var mRegPromptInfo: PromptInfo? = null

    private var mAuthBiometricPrompt: BiometricPrompt? = null
    private var mAuthPromptInfo: PromptInfo? = null

    private var cipher: Cipher? = null
    private var keyGenerator: KeyGenerator? = null
    private var keyGenParameterSpec: KeyGenParameterSpec? = null

    var mAuthenticationListener: AuthenticationListener? = null

    init {
        IWLogger.d("====== BiometricPromptCallback")
        this.mContext = context
        this.mAuthenticationListener = authenticationListener
        val biometricManager = BiometricManager.from(this.mContext!!)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> IWLogger.d("====== App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> IWLogger.d("====== No biometric features available on this device.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> IWLogger.d("====== Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> IWLogger.d("====== The user hasn't associated " + "any biometric credentials with their account.")
        }

        try {
            this.keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            this.keyGenParameterSpec = KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true) // Invalidate the keys if the user has registered a new biometric
                    // credential, such as a new fingerprint. Can call this method only
                    // on Android 7.0 (API level 24) or higher. The variable
                    // "invalidatedByBiometricEnrollment" is true by default.
                    .setInvalidatedByBiometricEnrollment(true)
                    .build()
            this.keyGenerator!!.init(keyGenParameterSpec)
        } catch (e: Exception) {
            IWLogger.d("====== Exception occurred. [Message : " + e.message + "]")
            authenticationListener!!.failure(-1, "exception $e")
        } finally {

        }
    }

    private fun createKey() {
        try {
            val secretKey: SecretKey = keyGenerator!!.generateKey()
            //Encrypt data
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding")
            this.cipher!!.init(Cipher.ENCRYPT_MODE, secretKey)

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
    }

    private fun deleteKey() {
        IWLogger.d("====== deleteKey method was called.")
        try {
            this.androidKeyStore!!.load(null)
            this.androidKeyStore!!.deleteEntry(KEY_NAME)
        } catch (e: Exception) {
            IWLogger.d("====== Exception occurred. [Message : " + e.message + "]")
        }
    }

    fun registration() {

        createKey()

        this.mRegBiometricPrompt = BiometricPrompt((this.mContext as FragmentActivity?)!!, this.mContext!!.getMainExecutor(), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                IWLogger.d("====== Error Code : $errorCode")
                IWLogger.d("====== Error Message : $errString")
                mAuthenticationListener!!.failure(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                IWLogger.d("====== onAuthenticationSucceeded result: " + result.cryptoObject!!.cipher)
                val cipher = result.cryptoObject!!.cipher
                val ivBytes = cipher!!.iv
                // iv 값 저장
                setBytes("ivBytes", ivBytes)

                val array = ByteArray(16)
                Random().nextBytes(array)
                val randomString = String(array, Charset.forName("UTF-8"))
                IWLogger.d("====== generatedString :$randomString")
                try {
                    val encryptedBytes = cipher.doFinal(randomString.toByteArray())
                    IWLogger.d("====== createKey encryptedBytes $encryptedBytes")
                    // 비밀키 AES 암호화 한 값 저장
                    setBytes("encryptedBytes", encryptedBytes)
                } catch (e: BadPaddingException) {
                    e.printStackTrace()
                } catch (e: IllegalBlockSizeException) {
                    e.printStackTrace()
                }
                mAuthenticationListener!!.success(1, "success", null)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                IWLogger.d("====== onAuthenticationFailed method was called.")
                mAuthenticationListener!!.failure(-1, "onAuthenticationFailed")
            }
        })

        this.mRegPromptInfo = PromptInfo.Builder()
                .setTitle("Biometric Unlock for your wallet")
                .setSubtitle("Unlock wallet in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()
        this.mRegBiometricPrompt!!.authenticate(this.mRegPromptInfo!!, BiometricPrompt.CryptoObject(this.cipher!!))
    }

    private fun loadKey(): Boolean {
        try {
            this.androidKeyStore = KeyStore.getInstance("AndroidKeyStore")
            this.androidKeyStore!!.load(null)
            val aliases: Enumeration<String> = this.androidKeyStore!!.aliases()
            while (aliases.hasMoreElements()) {
                IWLogger.d("====== aliases :" + aliases.nextElement())
            }
            val secretKeyEntry = this.androidKeyStore!!.getEntry(KEY_NAME, null) as KeyStore.SecretKeyEntry
            val secretKey = secretKeyEntry.secretKey

            //Decrypt data
            this.cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, getBytes("ivBytes"))
            this.cipher!!.init(Cipher.DECRYPT_MODE, secretKey, spec)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        } catch (e: UnrecoverableEntryException) {
            e.printStackTrace()
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        }
        return false
    }

    fun authentication() {

        loadKey()

        this.mAuthBiometricPrompt = BiometricPrompt((this.mContext as FragmentActivity?)!!, this.mContext!!.getMainExecutor(), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                IWLogger.d("====== Error Code : $errorCode")
                IWLogger.d("====== Error Message : $errString")
                mAuthenticationListener!!.failure(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                IWLogger.d("====== onAuthenticationSucceeded result: " + result.cryptoObject!!.cipher)
                val resultCipher = result.cryptoObject!!.cipher
                try {
                    IWLogger.d("====== shered decryptData " + getBytes("encryptedBytes"))
                    val origindata = resultCipher!!.doFinal(getBytes("encryptedBytes"))
                    val unencryptedString = origindata.toString(Charsets.UTF_8)
                    
                    
                    IWLogger.d("====== origindata ${unencryptedString}")
                    // 복호화된 랜덤값 전
                    mAuthenticationListener!!.success(1, "success", origindata)
                } catch (e: IllegalBlockSizeException) {
                    e.printStackTrace()
                } catch (e: BadPaddingException) {
                    e.printStackTrace()
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                IWLogger.d("====== onAuthenticationFailed method was called.")
                mAuthenticationListener!!.failure(-1, "onAuthenticationFailed")
            }
        })
        this.mAuthPromptInfo = PromptInfo.Builder()
                .setTitle("Biometric Unlock for your wallet")
                .setSubtitle("Unlock wallet in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build()
        this.mAuthBiometricPrompt!!.authenticate(this.mAuthPromptInfo!!, BiometricPrompt.CryptoObject(this.cipher!!))
    }

    fun deregistration() {
        deleteKey()
    }

    fun getBytes(key: String?): ByteArray? {
        val prefs: SharedPreferences = this.mContext?.getSharedPreferences("djpark_shered", Context.MODE_PRIVATE)!!
        val str = prefs.getString(key, null)
        return str?.toByteArray(ISO_8859_1)
    }

    fun setBytes(key: String?, bytes: ByteArray?) {
        val prefs: SharedPreferences = this.mContext?.getSharedPreferences("djpark_shered", Context.MODE_PRIVATE)!!
        val e = prefs.edit()
        e.putString(key, String(bytes!!, ISO_8859_1))
        e.commit()
    }
}



/*
package org.hyperledger.indy.lock.authenticator.biometric;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;


import org.hyperledger.indy.lock.logger.IWLogger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import kotlin.text.Charsets;

@RequiresApi(api = Build.VERSION_CODES.P)
public class BiometricPromptCallback {

    private static final String KEY_NAME = "IW_FiNGER_WARP";
private Context context;
private KeyStore androidKeyStore;

private Executor executor;
private BiometricPrompt mRegBiometricPrompt;
private BiometricPrompt.PromptInfo mRegPromptInfo;

private BiometricPrompt mAuthBiometricPrompt;
private BiometricPrompt.PromptInfo mAuthPromptInfo;

private Cipher cipher;
KeyGenerator keyGenerator;
KeyGenParameterSpec keyGenParameterSpec;


    AuthenticationListener authenticationListener;

    public BiometricPromptCallback(Context context) {
        IWLogger.INSTANCE.d("====== BiometricPromptCallback");

        this.context = context;

        BiometricManager biometricManager = BiometricManager.from(this.context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                IWLogger.INSTANCE.d("====== App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                IWLogger.INSTANCE.d("====== No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                IWLogger.INSTANCE.d("====== Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                IWLogger.INSTANCE.d("====== The user hasn't associated " +
                        "any biometric credentials with their account.");
                break;
        }

        try {

            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyGenParameterSpec =
                    new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    // Invalidate the keys if the user has registered a new biometric
                    // credential, such as a new fingerprint. Can call this method only
                    // on Android 7.0 (API level 24) or higher. The variable
                    // "invalidatedByBiometricEnrollment" is true by default.
                    .setInvalidatedByBiometricEnrollment(true)
                    .build();

            keyGenerator.init(keyGenParameterSpec);

        } catch (Exception e) {
            IWLogger.INSTANCE.d("====== Exception occurred. [Message : " + e.getMessage() + "]");
            authenticationListener.failure(-1, "exception " + e.toString());
            return;
        }
    }

    public void setAuthenticationListener(AuthenticationListener authenticationListener) {

        IWLogger.INSTANCE.d("====== setAuthenticationListener");
        this.authenticationListener = authenticationListener;
    }



    public byte[] getBytes(String key) {
        SharedPreferences prefs = context.getSharedPreferences("djpark_shered", Context.MODE_PRIVATE);
        String str = prefs.getString(key, null);
        if (str != null) {
            return str.getBytes(Charsets.ISO_8859_1);
        }
        return null;
    }

    public void setBytes(String key, byte[] bytes) {
        SharedPreferences prefs = context.getSharedPreferences("djpark_shered", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        e.putString(key, new String(bytes, Charsets.ISO_8859_1));
        e.commit();
    }

    private void createKey() {

        try {

            final SecretKey secretKey = keyGenerator.generateKey();
            //Encrypt data
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            byte[] ivBytes = cipher.getIV();
//            // iv 값 저장
//            setBytes("ivBytes", ivBytes);
//
//            final byte[] encryptedBytes = cipher.doFinal(secretRandom);
//            IWLogger.INSTANCE.d("====== createKey encryptedBytes "+encryptedBytes);
//            // 비밀키 AES 암호화 한 값 저장
//            setBytes("encryptedBytes", encryptedBytes);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private void deleteKey() {

        IWLogger.INSTANCE.d("====== deleteKey method was called.");

        try {
            androidKeyStore.load(null);
            androidKeyStore.deleteEntry(KEY_NAME);

        } catch (Exception e) {
            IWLogger.INSTANCE.d("====== Exception occurred. [Message : " + e.getMessage() + "]");
        }
    }

    public void registration() {

        createKey();

        mRegBiometricPrompt = new BiometricPrompt((FragmentActivity) context, context.getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                IWLogger.INSTANCE.d("====== Error Code : " + errorCode);
                IWLogger.INSTANCE.d("====== Error Message : " + errString);

                authenticationListener.failure(errorCode, errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                IWLogger.INSTANCE.d("====== onAuthenticationSucceeded result: " + result.getCryptoObject().getCipher());

                Cipher cipher = result.getCryptoObject().getCipher();
                byte[] ivBytes = cipher.getIV();
                // iv 값 저장
                setBytes("ivBytes", ivBytes);
//                String randomString = "djpark0402";
                byte[] array = new byte[16];
                new Random().nextBytes(array);
                String randomString = new String(array, Charset.forName("UTF-8"));

                IWLogger.INSTANCE.d("====== generatedString :"+randomString);

                try {
                    final byte[] encryptedBytes = cipher.doFinal(randomString.getBytes());
                    IWLogger.INSTANCE.d("====== createKey encryptedBytes "+encryptedBytes);
                    // 비밀키 AES 암호화 한 값 저장
                    setBytes("encryptedBytes", encryptedBytes);

                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }

                authenticationListener.success(1, "success", null);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                IWLogger.INSTANCE.d("====== onAuthenticationFailed method was called.");
                authenticationListener.failure(-1, "onAuthenticationFailed");
            }
        });

        mRegPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Unlock for your wallet")
                .setSubtitle("Unlock wallet in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();

        mRegBiometricPrompt.authenticate(mRegPromptInfo, new BiometricPrompt.CryptoObject(cipher));
    }

    private boolean loadKey() {

        try {
            androidKeyStore = KeyStore.getInstance("AndroidKeyStore");
            androidKeyStore.load(null);

            Enumeration<String> aliases = androidKeyStore.aliases();
            while (aliases.hasMoreElements()) {
                IWLogger.INSTANCE.d("====== aliases :"+aliases.nextElement());
            }

            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry)androidKeyStore.getEntry(KEY_NAME, null);
            final SecretKey secretKey = secretKeyEntry.getSecretKey();

            //Decrypt data
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, getBytes("ivBytes"));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void authentication() {

        loadKey();

        mAuthBiometricPrompt = new BiometricPrompt((FragmentActivity)context, context.getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {

                super.onAuthenticationError(errorCode, errString);
                IWLogger.INSTANCE.d("====== Error Code : " + errorCode);
                IWLogger.INSTANCE.d("====== Error Message : " + errString);
                authenticationListener.failure(errorCode, errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                IWLogger.INSTANCE.d("====== onAuthenticationSucceeded result: "+result.getCryptoObject().getCipher());
                Cipher resultCipher = result.getCryptoObject().getCipher();

                try {

                    IWLogger.INSTANCE.d("====== shered decryptData "+getBytes("encryptedBytes"));

                    byte[] origindata = resultCipher.doFinal(getBytes("encryptedBytes"));
                    final String unencryptedString = new String(origindata, "UTF-8");

                    IWLogger.INSTANCE.d("====== origindata "+unencryptedString);
                    // 복호화된 랜덤값 전
                    authenticationListener.success(1, "success", origindata);

                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                IWLogger.INSTANCE.d("====== onAuthenticationFailed method was called.");
                authenticationListener.failure(-1, "onAuthenticationFailed");
            }
        });

        mAuthPromptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Unlock for your wallet")
                .setSubtitle("Unlock wallet in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();

        mAuthBiometricPrompt.authenticate(mAuthPromptInfo, new BiometricPrompt.CryptoObject(cipher));
    }

    public void deregistration() {

        deleteKey();
    }

}
*/