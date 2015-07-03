package com.bluetooth.ftp;


import java.io.IOException;
import java.util.regex.Pattern;

import com.example.javax.obex.HeaderSet;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Bluetooth OPP internal constants definition
 */
public class Constants {
    /** Tag used for debugging/logging */
    public static final String TAG = "BluetoothOpp";

    /**
     * The intent that gets sent when the service must wake up for a retry Note:
     * only retry Outbound transfer
     */
    public static final String ACTION_RETRY = "android.btopp.intent.action.RETRY";

    /** the intent that gets sent when clicking a successful transfer */
    public static final String ACTION_OPEN = "android.btopp.intent.action.OPEN";

    /** the intent that gets sent when clicking outbound transfer notification */
    public static final String ACTION_OPEN_OUTBOUND_TRANSFER = "android.btopp.intent.action.OPEN_OUTBOUND";

    /** the intent that gets sent when clicking a inbound transfer notification */
    public static final String ACTION_OPEN_INBOUND_TRANSFER = "android.btopp.intent.action.OPEN_INBOUND";

    /** the intent that gets sent when clicking an incomplete/failed transfer */
    public static final String ACTION_LIST = "android.btopp.intent.action.LIST";

    /**
     * the intent that gets sent when deleting the incoming file confirmation
     * notification
     */
    public static final String ACTION_HIDE = "android.btopp.intent.action.HIDE";

    /**
     * the intent that gets sent when deleting the notifications of outbound and
     * inbound completed transfer
     */
    public static final String ACTION_COMPLETE_HIDE = "android.btopp.intent.action.HIDE_COMPLETE";

    /**
     * the intent that gets sent when clicking a incoming file confirm
     * notification
     */
    public static final String ACTION_INCOMING_FILE_CONFIRM = "android.btopp.intent.action.CONFIRM";

    public static final String THIS_PACKAGE_NAME = "com.android.bluetooth";

    /**
     * The column that is used to remember whether the media scanner was invoked
     */
    public static final String MEDIA_SCANNED = "scanned";

    public static final int MEDIA_SCANNED_NOT_SCANNED = 0;

    public static final int MEDIA_SCANNED_SCANNED_OK = 1;

    public static final int MEDIA_SCANNED_SCANNED_FAILED = 2;

    /**
     * The MIME type(s) of we could share to other device.
     */
    /*
     * TODO: define correct type list
     */
    public static final String[] ACCEPTABLE_SHARE_OUTBOUND_TYPES = new String[] {
        "image/*", "text/x-vcard",
    };

    /**
     * The MIME type(s) of we could not share to other device. TODO: define
     * correct type list
     */
    public static final String[] UNACCEPTABLE_SHARE_OUTBOUND_TYPES = new String[] {
        "virus/*",
    };

    /**
     * The MIME type(s) of we could accept from other device.
     * This is in essence a "white list" of acceptable types.
     * Today, restricted to images, audio, video and certain text types.
     */
    public static final String[] ACCEPTABLE_SHARE_INBOUND_TYPES = new String[] {
        "image/*",
        "video/*",
        "audio/*",
        "text/x-vcard",
        "text/plain",
        "text/html",
        "application/zip",
        "application/vnd.ms-excel",
        "application/msword",
        "application/vnd.ms-powerpoint",
        "application/pdf",
    };

    /**
     * The MIME type(s) of we could not accept from other device. TODO: define
     * correct type list
     */
    public static final String[] UNACCEPTABLE_SHARE_INBOUND_TYPES = new String[] {
        "text/x-vcalendar",
    };

    /** Where we store Bluetooth received files on the external storage */
    public static final String DEFAULT_STORE_SUBDIR = "/bluetooth";

    /**
     * Debug level logging
     */
    public static final boolean DEBUG = false;

    /**
     * Verbose level logging
     */
    public static final boolean VERBOSE = false;

    /** use TCP socket instead of Rfcomm Socket to develop */
    public static final boolean USE_TCP_DEBUG = false;

    /** use simple TCP server started from TestActivity */
    public static final boolean USE_TCP_SIMPLE_SERVER = false;

    /** Test TCP socket port */
    public static final int TCP_DEBUG_PORT = 6500;

    /** use emulator to debug */
    public static final boolean USE_EMULATOR_DEBUG = false;

    public static final int MAX_RECORDS_IN_DATABASE = 1000;

    public static final int BATCH_STATUS_PENDING = 0;

    public static final int BATCH_STATUS_RUNNING = 1;

    public static final int BATCH_STATUS_FINISHED = 2;

    public static final int BATCH_STATUS_FAILED = 3;

    public static final String BLUETOOTHOPP_NAME_PREFERENCE = "btopp_names";

    public static final String BLUETOOTHOPP_CHANNEL_PREFERENCE = "btopp_channels";

    public static String filename_SEQUENCE_SEPARATOR = "-";

    public static boolean mimeTypeMatches(String mimeType, String[] matchAgainst) {
        for (String matchType : matchAgainst) {
            if (mimeTypeMatches(mimeType, matchType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean mimeTypeMatches(String mimeType, String matchAgainst) {
        Pattern p = Pattern.compile(matchAgainst.replaceAll("\\*", "\\.\\*"),
                Pattern.CASE_INSENSITIVE);
        return p.matcher(mimeType).matches();
    }

    public static void logHeader(HeaderSet hs) {
        Log.v(TAG, "Dumping HeaderSet " + hs.toString());
        try {

            Log.v(TAG, "COUNT : " + hs.getHeader(HeaderSet.COUNT));
            Log.v(TAG, "NAME : " + hs.getHeader(HeaderSet.NAME));
            Log.v(TAG, "TYPE : " + hs.getHeader(HeaderSet.TYPE));
            Log.v(TAG, "LENGTH : " + hs.getHeader(HeaderSet.LENGTH));
            Log.v(TAG, "TIME_ISO_8601 : " + hs.getHeader(HeaderSet.TIME_ISO_8601));
            Log.v(TAG, "TIME_4_BYTE : " + hs.getHeader(HeaderSet.TIME_4_BYTE));
            Log.v(TAG, "DESCRIPTION : " + hs.getHeader(HeaderSet.DESCRIPTION));
            Log.v(TAG, "TARGET : " + hs.getHeader(HeaderSet.TARGET));
            Log.v(TAG, "HTTP : " + hs.getHeader(HeaderSet.HTTP));
            Log.v(TAG, "WHO : " + hs.getHeader(HeaderSet.WHO));
            Log.v(TAG, "OBJECT_CLASS : " + hs.getHeader(HeaderSet.OBJECT_CLASS));
            Log.v(TAG, "APPLICATION_PARAMETER : " + hs.getHeader(HeaderSet.APPLICATION_PARAMETER));
        } catch (IOException e) {
            Log.e(TAG, "dump HeaderSet error " + e);
        }
    }
}
