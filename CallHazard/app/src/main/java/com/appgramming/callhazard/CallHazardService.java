/*
 * Copyright (C) 2014-2017 Appgramming
 * http://www.appgramming.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appgramming.callhazard;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.widget.Toast;

import java.security.SecureRandom;

/**
 * An IntentService that selects a random contact number and calls it.
 */
public class CallHazardService extends IntentService {

    /**
     * The maximum number of tries to select a random contact and a non empty phone number.
     */
    private static final int MAX_TRIES = 100;

    /**
     * The secure random number generator to select random contacts and phone numbers.
     */
    private SecureRandom mRandom;

    /**
     * Creates the CallHazardService IntentService.
     */
    public CallHazardService() {
        super("CallHazardService");
    }

    /**
     * Do the actual work on the worker thread: select a random contact number and start calling the number.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Initialize the secure random number generator
        mRandom = new SecureRandom();

        try {
            // Select a random phone number of a random contact
            String phoneNumber = this.getRandomNumberOfRandomContact();

            // Call the number, or show an error if it's empty
            if (!TextUtils.isEmpty(phoneNumber)) {
                Utils.callNumber(this, phoneNumber);
            } else {
                Utils.toast(this, getString(R.string.toast_no_contact), Toast.LENGTH_SHORT);
            }
        } catch (Exception | Error e) {
            // Log and show any exceptions or errors to the user
            String errorMessage = e.getLocalizedMessage();
            if (TextUtils.isEmpty(errorMessage)) errorMessage = e.toString();
            Utils.toast(this, errorMessage, Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    /**
     * Selects a random contact and randomly selects and returns one of its phone numbers.
     */
    private String getRandomNumberOfRandomContact() {

        // Query the contacts
        Cursor cursor = getContentResolver().query(Contacts.CONTENT_URI,
                new String[]{Contacts._ID, Contacts.HAS_PHONE_NUMBER}, Contacts.HAS_PHONE_NUMBER + "=1", null, null);
        if (cursor == null) return "";

        try {
            int cursorSize = cursor.getCount();
            if (cursorSize == 0) return "";

            for (int i = 0; i < MAX_TRIES; i++) {

                // Select a random contact
                cursor.moveToPosition(mRandom.nextInt(cursorSize));

                // Test if the current selected contact has at least one phone number
                Boolean hasPhone = Integer.parseInt(cursor.getString(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER))) > 0;
                if (hasPhone) {
                    // Randomly select and return a non-empty phone number for the current contact
                    String contactId = cursor.getString(cursor.getColumnIndex(Contacts._ID));
                    String phoneNumber = this.getRandomPhoneNumber(contactId);
                    if (!TextUtils.isEmpty(phoneNumber)) return phoneNumber;
                }
            }
        } finally {
            cursor.close();
        }

        return "";
    }

    /**
     * Returns a random phone number for a given contact.
     */
    private String getRandomPhoneNumber(String contactId) {
        // Query the phone numbers of the contact
        Cursor phones = getContentResolver().query(
                Phone.CONTENT_URI, new String[]{Phone.NUMBER}, Phone.CONTACT_ID + " = " + contactId, null, null);
        if (phones != null) {
            try {
                int phonesCount = phones.getCount();
                if (phonesCount > 0) {
                    // Select and return a random phone number
                    phones.moveToPosition(this.mRandom.nextInt(phonesCount));
                    return phones.getString(phones.getColumnIndex(Phone.NUMBER));
                }
            } finally {
                phones.close();
            }
        }

        return "";
    }
}
