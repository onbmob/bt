/*
 * Copyright (C) The Android Open Source Project
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
package com.stemsc.bt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epson.moverio.btcontrol.DisplayControl;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.common.api.CommonStatusCodes;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSource;
//import com.google.android.gms.samples.vision.barcodereader.ui.camera.CameraSourcePreview;

//import com.google.android.gms.samples.vision.barcodereader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
//import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.stemsc.bt.ui.camera.CameraSource;
import com.stemsc.bt.ui.camera.CameraSourcePreview;
import com.stemsc.bt.ui.camera.GraphicOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
//import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
//public final class BarcodeCaptureActivity extends AppCompatActivity {
public final class BarcodeCaptureActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "Barcode-reader";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    public static final String BarcodeObject = "Barcode";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;


    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    static RelativeLayout lLay;
    static RelativeLayout lLay2;

    static TextView tvSost;
    static TextView tvNeedT;
    static TextView tvNeed;
    static TextView tvNeedAT;
    static TextView tvNeedA;
    static TextView tvNeedBT;
    static TextView tvNeedB;
    static TextView tvNeedQT;
    static TextView tvNeedQ;
    static TextView tvCellT;
    static TextView tvCell;
    static TextView tvTrayT;
    static TextView tvTray;

    private SeekBar sbS;
    private SeekBar sbW;
    private SeekBar sbH;
    private TextView target;

    private SharedPreferences sPref;
    private boolean autoFocus;
    private boolean useFlash;
    private boolean mRaw;
    private boolean mPV;
    private int mS;
    private int mH;
    private int mW;

    DisplayControl _displayControl;
    int _mode, _backlight;


    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //чтобы небыло заголовка активити
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //убрать статусную строку
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View view = this.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }


        setContentView(R.layout.barcode_capture);
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);

        // read parameters from the intent used to launch the activity.
//        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
//        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
        // Теперь берем из настроек
        sPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        autoFocus = sPref.getBoolean("af", true);
        useFlash = sPref.getBoolean("fl", false);
        mRaw = sPref.getBoolean("raw", false);
        mPV = sPref.getBoolean("pv", false);
        mS = sPref.getInt("s", 50);
        mH = sPref.getInt("h", 360);
        mW = sPref.getInt("v", 640);
//        Log.d(TAG,"=== mS === "+mS);
//        Log.d(TAG,"=== mH === "+mH);
//        Log.d(TAG,"=== mW === "+mW);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

//        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
//        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

//        Snackbar.make(mGraphicOverlay, "Tap to capture. Pinch/Stretch to zoom",
//                Snackbar.LENGTH_LONG)
//                .show();

        tvSost = (TextView) findViewById(R.id.tvSost);
        lLay = (RelativeLayout) findViewById(R.id.lLayout1);

        if (mPV) {
            mPreview.setVisibility(View.VISIBLE);
        }else{
            mPreview.setVisibility(View.INVISIBLE);
        }

        sbS = (SeekBar) findViewById(R.id.seekBarS);
        sbW = (SeekBar) findViewById(R.id.seekBarW);
        sbH = (SeekBar) findViewById(R.id.seekBarH);

        sbS.setOnSeekBarChangeListener(this);
        sbW.setOnSeekBarChangeListener(this);
        sbH.setOnSeekBarChangeListener(this);

        target = (TextView) findViewById(R.id.target);
        RelativeLayout.LayoutParams MyParams = new RelativeLayout.LayoutParams(64*mS/10,72*mS/10);
        MyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        MyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        MyParams.topMargin= mW;
        MyParams.leftMargin=mH;
        target.setLayoutParams(MyParams);

        sbS.setProgress(mS);
        sbW.setProgress(mW);
        sbH.setProgress(mH);
        ((TextView) findViewById(R.id.taPar)).setText(""+mS+" "+mW+" "+mH);

        // create instance
        _displayControl = new DisplayControl(this);
        // get current display condition
        _mode = _displayControl.getMode();
//        _backlight = _displayControl.getBacklight();

        if (_mode == DisplayControl.DISPLAY_MODE_2D){
            _mode = DisplayControl.DISPLAY_MODE_3D;
            _displayControl.setMode(_mode,true);
        }



        if (mRaw) {
            tvSost.setText("Тестовый(raw) режим");
            lLay.setVisibility(View.INVISIBLE);

            return;   // !!!!!!!!! Внимание !!!!!!!

        }

        lLay2 = (RelativeLayout) findViewById(R.id.lLayout2);

        tvNeed = (TextView) findViewById(R.id.tvNeed);
        tvNeedT = (TextView) findViewById(R.id.tvNeedT);
        tvNeedAT = (TextView) findViewById(R.id.tvNeedAT);
        tvNeedA = (TextView) findViewById(R.id.tvNeedA);
        tvNeedBT = (TextView) findViewById(R.id.tvNeedBT);
        tvNeedB = (TextView) findViewById(R.id.tvNeedB);
        tvNeedQT = (TextView) findViewById(R.id.tvNeedQT);
        tvNeedQ = (TextView) findViewById(R.id.tvNeedQ);

        tvCellT = (TextView) findViewById(R.id.tvCellT);
        tvCell = (TextView) findViewById(R.id.tvCell);
        tvTrayT = (TextView) findViewById(R.id.tvTrayT);
        tvTray = (TextView) findViewById(R.id.tvTray);

        tvNeedAT.setVisibility(View.INVISIBLE);
        tvNeedA.setVisibility(View.INVISIBLE);
        tvNeedBT.setVisibility(View.INVISIBLE);
        tvNeedB.setVisibility(View.INVISIBLE);
        tvNeedQT.setVisibility(View.INVISIBLE);
        tvNeedQ.setVisibility(View.INVISIBLE);

        tvCellT.setVisibility(View.INVISIBLE);
        tvCell.setVisibility(View.INVISIBLE);
        tvTrayT.setVisibility(View.INVISIBLE);
        tvTray.setVisibility(View.INVISIBLE);

        // Получаем json
//        AsyncTask<Void, Void, String> tmp = new ParseTask("http://onbqth.com/route2.json");
//        AsyncTask<Void, Void, String> dStr = tmp.execute();
        new ParseTask("http://onbqth.com/order.json").execute();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        boolean b = scaleGestureDetector.onTouchEvent(e);
//
//        boolean c = gestureDetector.onTouchEvent(e);
//
//        return b || c || super.onTouchEvent(e);
//    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
//                .setBarcodeFormats(Barcode.EAN_13 | Barcode.QR_CODE) // !!!! ограничение кодов
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
//        Log.d(TAG, "onResume surfaceView");
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.goods., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        RelativeLayout.LayoutParams MyParams = new RelativeLayout.LayoutParams(64*sbS.getProgress()/10,72*sbS.getProgress()/10);
        MyParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        MyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        MyParams.topMargin= sbW.getProgress();
        MyParams.leftMargin=sbH.getProgress();
        target.setLayoutParams(MyParams);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt("s", sbS.getProgress());
        ed.putInt("v", sbW.getProgress());
        ed.putInt("h", sbH.getProgress());
        ed.apply();
        ((TextView) findViewById(R.id.taPar)).setText(""+sbS.getProgress()+" "+sbW.getProgress()+" "+sbH.getProgress());
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    // получаем JSON
    private class ParseTask extends AsyncTask<Void, Void, String> {
        //        private static final String LOG_TAG = "JSON Barcode-reader ";
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        URL url = null;

        ParseTask(String str) {
            try {
                url = new URL(str);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            try {
                JSONObject order = new JSONObject(strJson);

                BarcodeGraphic.trays = new JSONObject(order.getString("trays"));
                Iterator<String> iter = BarcodeGraphic.trays.keys();
                String s = "";
                while (iter.hasNext()) s += BarcodeGraphic.trays.getString(iter.next()) + '\n';
                tvNeed.setText(s);

                BarcodeGraphic.bcPlace = order.getString("place");

                BarcodeGraphic.aTasks = new JSONArray(order.getString("tasks"));

                BarcodeGraphic.step = 0;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
