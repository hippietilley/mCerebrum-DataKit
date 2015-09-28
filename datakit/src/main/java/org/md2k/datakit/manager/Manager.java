package org.md2k.datakit.manager;

import android.os.Bundle;
import android.os.Message;

import org.md2k.datakit.logger.DatabaseLogger;
import org.md2k.datakitapi.status.Status;
import org.md2k.datakitapi.status.StatusCodes;
import org.md2k.utilities.Report.Log;

/**
 * Copyright (c) 2015, The University of Memphis, MD2K Center
 * - Syed Monowar Hossain <monowar.hossain@gmail.com>
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
public class Manager {
    private static final String TAG = Manager.class.getSimpleName();
    DatabaseLogger databaseLogger = null;

    public Manager(){
        databaseLogger = DatabaseLogger.getInstance(null);
        Log.d(TAG, "databaseLogger=" + databaseLogger);
    }
    public Message prepareMessage(Bundle bundle, int messageType){
        Message message = Message.obtain(null, 0, 0, 0);
        message.what=messageType;
        message.setData(bundle);
        return message;
    }
    public static Message prepareErrorMessage(int messageType){
        Bundle bundle=new Bundle();
        bundle.putSerializable(Status.class.getSimpleName(),new Status(StatusCodes.FAILED));
        Message message = Message.obtain(null, 0, 0, 0);
        message.what=messageType;
        message.setData(bundle);
        return message;
    }

}
