package com.mridang.fielder;

import java.util.Random;

import org.acra.ACRA;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.nfc.NfcManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class FielderWidget extends DashClockExtension {

	/* This is the instance of the receiver that deals with cellular status */
	private ToggleReceiver objReceiver;

	/*
	 * This class is the receiver for getting hotspot toggle events
	 */
	private class ToggleReceiver extends BroadcastReceiver {

		/*
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
		@Override
		public void onReceive(Context ctxContext, Intent ittIntent) {

			onUpdateData(0);

		}

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onInitialize
	 * (boolean)
	 */
	@Override
	protected void onInitialize(boolean booReconnect) {

		super.onInitialize(booReconnect);

		if (objReceiver != null) {

			try {

				Log.d("FielderWidget", "Unregistered any existing status receivers");
				unregisterReceiver(objReceiver);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		objReceiver = new ToggleReceiver();
		registerReceiver(objReceiver, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"));
		Log.d("FielderWidget", "Registered the status receiver");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("FielderWidget", "Created");
		ACRA.init(new AcraApplication(getApplicationContext()));

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d("FielderWidget", "Fetching near-field communcation information");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(true);

		try {

			Log.d("FielderWidget", "Airplane-mode is off");
			NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);

			Log.d("FielderWidget", "Checking if the device supports near-field communication");
			if (nfcManager != null && nfcManager.getDefaultAdapter() != null) {

				Log.d("FielderWidget", "Checking if near-field communication is enabled");
				if (nfcManager.getDefaultAdapter().isEnabled()) {

					Log.d("FielderWidget", "Connected to a wireless network");
					edtInformation.visible(true);
					edtInformation.status(getString(R.string.title));
					edtInformation.expandedBody(getString(R.string.enabled));
					edtInformation.clickIntent(new Intent("android.settings.NFC_SETTINGS"));

				} else {
					Log.d("FielderWidget", "Near-field communication is enabled");
				}

			} else {
				Log.d("FielderWidget", "Device doesn't support near-field communication");
			}

			if (new Random().nextInt(5) == 0 && !(0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {

				PackageManager mgrPackages = getApplicationContext().getPackageManager();

				try {

					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);

				} catch (NameNotFoundException e) {

					Integer intExtensions = 0;
					Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
					String strPackage;

					for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {

						strPackage = info.serviceInfo.applicationInfo.packageName;
						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0);

					}

					if (intExtensions > 1) {

						edtInformation.visible(true);
						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri
								.parse("market://details?id=com.mridang.donate")));
						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
						edtInformation
								.expandedBody("Thank you for using "
										+ intExtensions
										+ " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
						setUpdateWhenScreenOn(true);

					}

				}

			} else {
				setUpdateWhenScreenOn(true);
			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e("FielderWidget", "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		publishUpdate(edtInformation);
		Log.d("FielderWidget", "Done");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
	 */
	public void onDestroy() {

		super.onDestroy();

		if (objReceiver != null) {

			try {

				Log.d("FielderWidget", "Unregistered the status receiver");
				unregisterReceiver(objReceiver);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		Log.d("FielderWidget", "Destroyed");

	}

}