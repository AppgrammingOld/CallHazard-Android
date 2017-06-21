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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

/**
 * CallHazard zero interface activity: asks for runtime permissions, starts the call hazard intent service, and finishes.
 */
public class MainActivity extends Activity {

    /**
     * Id to identify the required multiple permissions request.
     */
    private static final int PERMISSION_REQUEST_MULTIPLE = 0;

    /**
     * When the activity is starting: ask for runtime permissions or start the call hazard intent service, and finish.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check and ask for runtime permissions to read contacts and call phone
            if ((checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)) {
                // We have the required runtime permissions: start the hazard call
                startHazardCall();
            } else {
                // Ask for the required runtime permissions
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE},
                        PERMISSION_REQUEST_MULTIPLE);
            }
        } else {
            // Runtime permissions are not used, we have the permissions from manifest: start the hazard call
            startHazardCall();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_MULTIPLE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions have been granted: start the hazard call
                startHazardCall();
            } else {
                // Permissions request was denied: show a toast
                Toast.makeText(getApplicationContext(), R.string.toast_no_permissions, Toast.LENGTH_LONG).show();
                // And finish the activity
                finish();
            }
        }
    }

    /**
     * Starts the CallHazard intent service to find and call a random contact number.
     */
    private void startHazardCall() {

        // Start the CallHazard intent service
        final Intent intent = new Intent(this, CallHazardService.class);
        startService(intent);

        // Finish the activity
        finish();
    }
}