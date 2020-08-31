package org.hyperledger.indy.sdk.utils;


import android.content.Context;

import org.hyperledger.indy.sdk.LibIndy;

import java.io.File;
import android.content.Context;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

public class InitHelper {
	public static void init(Context context) {
//		try {
//			Os.setenv("EXTERNAL_STORAGE", getIndyModule(context), true);
//		} catch (ErrnoException e) {
//			e.printStackTrace();
//		}
		if (LibIndy.isInitialized() == false) {
//			System.loadLibrary("indy");
//			LibIndy.init("indy");
			LibIndy.init();
			File f = new File(getIndyModule(context));
			if(f.exists()) {
				Log.d("indy","not found libindy.so");
			} else {
				Log.d("indy","found libindy.so");
			}

		} else {
			Log.d("indy","isInitialized");
		}
	}

	public static String getIndyModule(Context context) {
		Log.d("indy", context.getApplicationInfo().nativeLibraryDir + File.separator + System.mapLibraryName("indy"));
		return context.getApplicationInfo().nativeLibraryDir + File.separator + System.mapLibraryName("");
	}
}
