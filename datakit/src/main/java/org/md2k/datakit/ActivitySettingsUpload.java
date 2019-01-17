/*
 * Copyright (c) 2018, The University of Memphis, MD2K Center of Excellence
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.md2k.datakit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import org.md2k.datakit.cerebralcortex.config.Config;
import org.md2k.datakit.cerebralcortex.config.ConfigManager;
import org.md2k.datakit.configuration.Configuration;
import org.md2k.datakit.configuration.ConfigurationManager;
import org.md2k.datakitapi.messagehandler.ResultCallback;
import org.md2k.mcerebrum.core.access.serverinfo.ServerCP;
import org.md2k.utilities.permission.PermissionInfo;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ActivitySettingsUpload extends AppCompatActivity {

    /** Constant used for logging. <p>Uses <code>class.getSimpleName()</code>.</p> */
    private static final String TAG = ActivitySettingsUpload.class.getSimpleName();

    Configuration configuration;
    Config config;

    /**
     * Upon creation, this activity creates a new <code>PermissionInfo</code> object to fetch permissions.
     *
     * @param savedInstanceState Previous state of this activity, if it existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration = ConfigurationManager.getInstance(this).configuration;
        try {
            config = ConfigManager.readConfig();
            config.setUrl(ServerCP.getServerAddress(this));
            ConfigManager.write(config);
            Log.d(TAG, "Upload server config updated from ServerCP.");
        } catch (IOException e) {
            Log.d(TAG, "Config file not found. Could not update upload url on creation.");
        }
        setContentView(R.layout.activity_settings_upload);
        new PermissionInfo().getPermissions(this, new ResultCallback<Boolean>() {

            /**
             * If permissions are not granted, the activity is finished.
             *
             * @param result Result of the callback from <code>.getPermissions()</code>.
             */
            @Override
            public void onResult(Boolean result) {
                if(result == false)
                    finish();

            }
        });
        getFragmentManager().beginTransaction().replace(R.id.layout_preference_fragment,
                new PrefsFragmentSettingsUpload()).commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Finishes the activity if the home button is pressed on the device.
     *
     * @param item Menu item that was selected.
     * @return Whether home or back was pressed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
