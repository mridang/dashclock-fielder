package com.mridang.fielder;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcManager;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class FielderWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.fielder.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {
		return new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED");
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.fielder.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.mridang.fielder.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return null;
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d(getTag(), "Fetching near-field communication information");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);

		try {

			NfcManager nfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
			if (nfcManager != null && nfcManager.getDefaultAdapter() != null) {

				Log.d(getTag(), "Checking if near-field communication is enabled");
				if (nfcManager.getDefaultAdapter().isEnabled()) {

					Log.d(getTag(), "Near-field communication is enabled");
					edtInformation.visible(true);
					edtInformation.status(getString(R.string.title));
					edtInformation.clickIntent(new Intent("android.settings.NFC_SETTINGS"));

					if (nfcManager.getDefaultAdapter().isNdefPushEnabled()) {
						edtInformation.expandedBody(getString(R.string.beam_enabled));
					} else {
						edtInformation.expandedBody(getString(R.string.beam_disabled));
					}

				} else {
					Log.d(getTag(), "Near-field communication is disabled");
				}

			} else {
				Log.d(getTag(), "Device doesn't support near-field communication");
			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.fielder.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
		onUpdateData(UPDATE_REASON_MANUAL);
	}

}