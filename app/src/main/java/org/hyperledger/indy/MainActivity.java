package org.hyperledger.indy;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.hyperledger.indy.lock.data.IWAuthenticatorResponse;
import org.hyperledger.indy.lock.logger.IWLogger;
import org.hyperledger.indy.lock.manager.IWResultCallback;
import org.hyperledger.indy.lock.manager.IWAuthenticatorManager;
import org.hyperledger.indy.lock.operation.IWRequestOperation;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.apache.commons.io.FileUtils;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    public Wallet wallet;
    private AlertDialog alertDialog;

    private void showAlert(int errorCode, String message, byte[] key) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            alertDialog = new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Wallet lock")
                    .setMessage("result\n[result Code: " + errorCode + "\nMessage: " + message+"\nKey: "+key)
                    .setCancelable(false)
                    .setPositiveButton("확인", null)
                    .create();

            alertDialog.show();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IWLogger.INSTANCE.setEnabled(true);

        findViewById(R.id.reg_button).setOnClickListener(view -> {
            IWLogger.INSTANCE.d("reg_button click");
            IWAuthenticatorManager.INSTANCE.registration(this, IWRequestOperation.AuthenticatorType.PIN, new IWResultCallback() {
                @Override
                public void onCompleted(IWAuthenticatorResponse response) {
                    MainActivity.this.showAlert(response.getErrorCode(), response.getErrorMessage(), response.getKey());
                }
            });
        });

        findViewById(R.id.auth_button).setOnClickListener(view -> {

            IWAuthenticatorManager.INSTANCE.authentication(this, IWRequestOperation.AuthenticatorType.PIN, new IWResultCallback() {
                @Override
                public void onCompleted(IWAuthenticatorResponse response) {
                    showAlert(response.getErrorCode(), response.getErrorMessage(), response.getKey());
                }
            });
        });

        findViewById(R.id.dereg_button).setOnClickListener(view -> {

            IWAuthenticatorManager.INSTANCE.deregistration(this, IWRequestOperation.AuthenticatorType.PIN, new IWResultCallback() {
                @Override
                public void onCompleted(IWAuthenticatorResponse response) {
                    showAlert(response.getErrorCode(), response.getErrorMessage(), response.getKey());
                }
            });
        });

//        // libindy.so 파일을 찾기 위한 설정
//        try {
//            Os.setenv("EXTERNAL_STORAGE", getExternalFilesDir(null).getAbsolutePath(), true);
//        } catch (ErrnoException e) {
//            e.printStackTrace();
//        }
//
//        org.hyperledger.indy.sdk.utils.InitHelper initHelper = new InitHelper();
//        initHelper.init(getApplicationContext());
//
//        try {
//            importWalletTest();
//        } catch (IndyException e) {
//            e.printStackTrace();
//        }




//        String walletName = "myWallet";
//        String poolName = "pool";
//        String stewardSeed = "000000000000000000000000Steward1";
//        String poolConfig = "{\"genesis_txn\": \"/home/vagrant/code/evernym/indy-sdk/cli/docker_pool_transactions_genesis\"}";
//
//        try {
//            // 1.
//            Log.d("djpark","\n1. Creating a new local pool ledger configuration that can be used later to connect pool nodes.\n");
//            Pool.createPoolLedgerConfig(poolName, poolConfig).get();
//            // 2
//            Log.d("djpark","\n2. Open pool ledger and get the pool handle from libindy.\n");
//            Pool pool = Pool.openPoolLedger(poolName, "{}").get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IndyException e) {
//            e.printStackTrace();
//        }
    }

    protected static final String WALLET = "Wallet1";
    protected static final String TYPE = "default";
    protected static final String WALLET_CREDENTIALS = "{\"key\":\"8dvfYSt5d1taSd6yJdpjq4emkwsPDDLYxkNFysFD2cZY\", \"key_derivation_method\":\"RAW\"}";
    protected static final String WALLET_CONFIG = "{ \"id\":\"" + WALLET + "\", \"storage_type\":\"" + TYPE + "\"}";
    protected static final String METADATA = "some metadata";
    private static final String EXPORT_KEY = "export_key";
    protected static final String EXPORT_PATH = getTmpPath("export_wallet");
    protected static final String EXPORT_CONFIG_JSON = "{ \"key\":\"" + EXPORT_KEY + "\", \"path\":\"" + EXPORT_PATH+ "\"}";

    public static String getTmpPath() {
        return FileUtils.getTempDirectoryPath() + "/indy_client/";
    }
    public static String getTmpPath(String filename) {
        return getTmpPath() + filename;
    }
    private void importWalletTest () throws IndyException {
        try {
            IWLogger.INSTANCE.d("importWalletTest start");
            Wallet.createWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
            this.wallet = Wallet.openWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
            IWLogger.INSTANCE.d("wallet :"+wallet);
            String did = Did.createAndStoreMyDid(wallet, "{}").get().getDid();
            IWLogger.INSTANCE.d("did :"+did);
            Did.setDidMetadata(wallet, did, METADATA).get(); String didWithMetaBefore = null;
            didWithMetaBefore = Did.getDidWithMeta(wallet, did).get();
            IWLogger.INSTANCE.d("didWithMetaBefore: "+didWithMetaBefore);
            Wallet.exportWallet(wallet, EXPORT_CONFIG_JSON).get();
            wallet.closeWallet().get();
            Wallet.deleteWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();
            Wallet.importWallet(WALLET_CONFIG, WALLET_CREDENTIALS, EXPORT_CONFIG_JSON).get();
            wallet = Wallet.openWallet(WALLET_CONFIG, WALLET_CREDENTIALS).get();

            String didWithMetaAfter = Did.getDidWithMeta(wallet, did).get();
            IWLogger.INSTANCE.d("didWithMetaAfter: "+didWithMetaAfter);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}