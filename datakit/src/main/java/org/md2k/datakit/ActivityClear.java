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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.md2k.datakit.configuration.ConfigurationManager;
import org.md2k.mcerebrum.commons.dialog.Dialog;
import org.md2k.mcerebrum.commons.dialog.DialogCallback;
import org.md2k.mcerebrum.commons.storage.Storage;
import org.md2k.utilities.FileManager;

import es.dmoral.toasty.Toasty;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * This activity allows the user to clear, or delete, the application data.
 */
public class ActivityClear extends AppCompatActivity {

    /** <code>Subscription</code> object used for observing the file deletion. */
    Subscription subscription;

    /** User facing dialog. */
    private MaterialDialog dialog;

    /**
     * Calls <code>super.onCreate()</code> and <code>clearData()</code>.
     *
     * @param savedInstanceState Previous state of this activity, if it existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear);
        clearData();
    }

    /**
     * Asks the user if they would like to delete the database and archive files.
     */
    void clearData() {
        Dialog.simple(this, "Delete Database & Archive Files?", "Delete Database & Archive Files?"
            + "\n\nData can't be recovered after deletion", "Yes", "Cancel", new DialogCallback() {

            /**
             * Calls <code>deleteData()</code> if necessary.
             *
             * @param value The option the user chose, either "Yes" or "Cancel".
             */
            @Override
            public void onSelected(String value) {
                if (value.equals("Yes"))
                    deleteData();
                else finish();
            }
        }).autoDismiss(true).show();
    }

    /**
     * Deletes the database and archive files.
     */
    void deleteData() {
        subscription = Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Boolean, Boolean>() {
                    /**
                     *
                     * @param aBoolean
                     * @return
                     */
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        dialog = Dialog.progressIndeterminate(ActivityClear.this, "Deleting files...").build();
                        dialog.show();

                        String location = ConfigurationManager.getInstance(ActivityClear.this).configuration.archive.location;
                        String directory = FileManager.getDirectory(ActivityClear.this, location);
                        Storage.deleteDir(directory);
                        location = ConfigurationManager.getInstance(ActivityClear.this).configuration.database.location;

                        if (!directory.equals(FileManager.getDirectory(ActivityClear.this, location))) {
                            directory = FileManager.getDirectory(ActivityClear.this, location);
                            Storage.deleteDir(directory);
                        }
                        return true;
                    }
                }).subscribe(new Observer<Boolean>() {
                    /**
                     * User is notified when the deletion is complete.
                     */
                    @Override
                    public void onCompleted() {
                        dialog.dismiss();
                        Toasty.success(ActivityClear.this, "Files are deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Boolean aBoolean) {}
                });
    }

    /**
     * Upon destruction, any remaining dialogs are dismissed and the subscription is unsubscribed.
     */
    @Override
    public void onDestroy(){
        if(dialog != null) dialog.dismiss();
        if(subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        super.onDestroy();
    }
}
