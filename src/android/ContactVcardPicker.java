/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.contactVcardpicker;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.media.RingtoneManager;
import org.apache.cordova.PluginResult;
import android.util.Log;
import android.content.Intent;
import android.provider.Settings;
import android.net.Uri;
import android.app.Activity;
import android.media.Ringtone;
import org.json.JSONObject;
import android.provider.ContactsContract;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.provider.Contacts;
/**
 * This class provides access to vibration on the device.
 */
public class ContactVcardPicker extends CordovaPlugin {
	public String notification_uri;
	private CallbackContext callbackContext = null;
    /**
     * Constructor.
     */
    public ContactVcardPicker() {
    }

    @Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
    	if (action.equals("getContactVcard")) { 
        	Log.d("customPlugin", " getContactVcard ");
        	
        	Runnable getContactVcard = new Runnable() {

                    @Override
                    public void run() {
                    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                	cordova.getActivity().startActivityForResult(contactPickerIntent, 5);
                    }
                };
        	this.cordova.getActivity().runOnUiThread(getContactVcard);
        	return true;
    	}
   		else {
        	return false;
   	 	}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
     	Log.d("customPlugin", "Calling onActivityResult");
    	if (resultCode == Activity.RESULT_OK && requestCode == 5)
    	{
    		String vCard = null;
			try {
				Uri contactData = data.getData();

				@SuppressWarnings("deprecation")
				Cursor cursor = cordova.getActivity().getContentResolver().query(contactData, null, null, null, null);
				cursor.moveToFirst();
				String lookupKey = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
				Uri uri = Uri.withAppendedPath(
					ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
				AssetFileDescriptor fd = cordova.getActivity().getContentResolver()
					.openAssetFileDescriptor(uri, "r");
				FileInputStream fis = fd.createInputStream();
				byte[] b = new byte[(int) fd.getDeclaredLength()];
				fis.read(b);
				vCard = new String(b);
				System.out.println("VACRD :" + vCard);
				
			String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

            String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            	
				String returnText = "{\"contact\": {\"name\": \""+name+"\",\"phone\": \""+phone+"\",\"vCard\": \""+vCard+"\"}}";

        		PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, returnText);
        		pluginResult.setKeepCallback(true);
        		this.callbackContext.sendPluginResult(pluginResult);
        		
				// readVCard(vCard);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
        	Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        	Ringtone ringtone = RingtoneManager.getRingtone(this.cordova.getActivity(), uri);
        	String title = ringtone.getTitle(this.cordova.getActivity());
       	 	Log.d("customPlugin", "I picked this ringtone " + uri);
       	 	Log.d("customPlugin", "I picked this ringtone title" + title);
       	 	
        	if (uri != null)
        	{
        		Log.d("customPlugin", "Setting ringtone to  " + notification_uri);
        		
        	}
    	}
	}

}
