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

/*
1.02.17
Добавлен имидж для вывода баркода
Заброкирован поворот экрана
*/


package com.stemsc.bt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    public static Context contextOfApplication;

    // use a compound button so either checkbox or switch widgets work.
//    private CompoundButton autoFocus;
//    private CompoundButton useFlash;

    private TextView statusMessage;
    private TextView barcodeValue;
    private ImageView imageView;
    private ImageView imageViewBC;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        contextOfApplication = getApplicationContext();
       contextOfApplication = MainActivity.this;


        setContentView(R.layout.activity_main);

        statusMessage = (TextView) findViewById(R.id.status_message);
        barcodeValue = (TextView) findViewById(R.id.barcode_value);

//        autoFocus = (CompoundButton) findViewById(R.id.m_auto_focus);
//        useFlash = (CompoundButton) findViewById(R.id.m_use_flash);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);
        imageViewBC = (ImageView) findViewById(R.id.imageViewBC);
        imageViewBC.setVisibility(View.INVISIBLE);

        findViewById(R.id.read_barcode).setOnClickListener(this);
        findViewById(R.id.bt_menu).setOnClickListener(this);

    }
    public static Context getContextOfApplication(){
        return contextOfApplication;
    }
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_barcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);
            // Теперь берем из настроек
//            SharedPreferences sPref = getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);
//            boolean autoFocus = sPref.getBoolean("af",true);
//            boolean useFlash = sPref.getBoolean("fl",false);
//            Log.route(TAG," intent.putExtra autoFocus "+autoFocus);
//            Log.route(TAG,"useFlash "+useFlash);

//            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus);
//            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash);

//            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus.isChecked());
//            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
            startActivityForResult(intent, RC_BARCODE_CAPTURE);

        } else if (v.getId() == R.id.bt_menu){
            Intent intent = new Intent(this, SettingAct.class);
            startActivity(intent);
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Bitmap img = data.getParcelableExtra("img");
                    imageViewBC.setImageBitmap(img);
                    imageViewBC.setVisibility(View.VISIBLE);
//                    statusMessage.setText(R.string.barcode_success);
//                    barcodeValue.setText(barcode.displayValue);
//                    Bundle bndl = intent.getExtras();
//                    if (bndl != null) {
//                        Object obj = intent.getExtras().get("data");
//                        if (barcode instanceof Bitmap) {
//                            Bitmap bitmap = (Bitmap) barcode;
//                            Log.route(TAG, "bitmap " + bitmap.getWidth() + " x "
//                                    + bitmap.getHeight());
//                           imageViewBC.setImageBitmap(img);
//                        }
//                    }

                    int sw = 0;
                    String s1 = "", s2 = "", s3 = "", s4 = "";

                    if (barcode.rawValue.substring(0, 4).equals("cells")) sw = 1;

                    else if (barcode.rawValue.substring(0, 4).equals("сell")) sw = 1;

                    else if (barcode.rawValue.equals("4036021032504")) {
                        sw = 2;
                        s1 = " Код: 134743";
                        s2 = " Артикул: E640L";
                        s3 = " Бренд: Hengst";
                        s4 = " Фильтр воздушный TOYOTA";
                        imageView.setImageResource(R.drawable.f);
                    } else if (barcode.rawValue.equals("2900001132541")) {
                        sw = 2;
                        s1 = " Код: 295717";
                        s2 = " Артикул: SH 112 295";
                        s3 = " Бренд: SACHS";
                        s4 = " Амортизатор ВАЗ 2108 задн. газ. (пр-во SACHS)";
                        imageView.setImageResource(R.drawable.a);
                    } else if (barcode.rawValue.equals("2000000151717")) {
                        sw = 2;
                        s1 = " Код: 106675";
                        s2 = " Артикул: 3302-2913012-10";
                        s3 = " Бренд: ЧМЗ";
                        s4 = " Рессора задн. Газель 3-х лист. 1050мм доп. (пр-во ЧМЗ)";
                        imageView.setImageResource(R.drawable.r);
                    }
                    imageView.setVisibility(View.INVISIBLE);
                    switch (sw) {
                        case 1:
                            String[] separated = barcode.displayValue.split(";");
                            statusMessage.setText("Ячейка:\n " + separated[2]);
                            barcodeValue.setText("\n\nПолный штрихкод: \n " + barcode.displayValue);
                            break;
                        case 2:
                            statusMessage.setText("Товар");
                            barcodeValue.setText(s1 + '\n' + s2 + '\n' + s3 + '\n' + " Название: " + s4);
                            imageView.setVisibility(View.VISIBLE);

                            break;
                        default:
                            statusMessage.setText("Сосканированный штрихкод:");
                            barcodeValue.setText(barcode.displayValue + "\nВ базе данных отсутствует");
                    }
                } else {
                    statusMessage.setText(R.string.barcode_failure);
//                    Log.route(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
