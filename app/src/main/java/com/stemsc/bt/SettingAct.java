package com.stemsc.bt;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;

public class SettingAct extends AppCompatActivity implements View.OnClickListener {

    private SharedPreferences sPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);

        sPref = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        ((CompoundButton) findViewById(R.id.m_auto_focus)).setChecked(sPref.getBoolean("af", true));
        ((CompoundButton) findViewById(R.id.m_use_flash)).setChecked(sPref.getBoolean("fl", false));
        ((CompoundButton) findViewById(R.id.m_raw)).setChecked(sPref.getBoolean("raw", false));
        ((CompoundButton) findViewById(R.id.m_pv)).setChecked(sPref.getBoolean("pv", false));
//        ((CompoundButton) findViewById(R.id.use_arrow)).setChecked(sPref.getBoolean("arr", true));
//        ((CompoundButton) findViewById(R.id.use_description)).setChecked(sPref.getBoolean("desc", true));
//        ((CompoundButton) findViewById(R.id.use_deb)).setChecked(sPref.getBoolean("deb", false));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button3) {

            SharedPreferences.Editor ed = sPref.edit();
            ed.putBoolean("af", ((CompoundButton) findViewById(R.id.m_auto_focus)).isChecked());
            ed.putBoolean("fl", ((CompoundButton) findViewById(R.id.m_use_flash)).isChecked());
            ed.putBoolean("raw", ((CompoundButton) findViewById(R.id.m_raw)).isChecked());
            ed.putBoolean("pv", ((CompoundButton) findViewById(R.id.m_pv)).isChecked());
//            ed.putBoolean("arr", ((CompoundButton) findViewById(R.id.use_arrow)).isChecked());
//            ed.putBoolean("desc", ((CompoundButton) findViewById(R.id.use_description)).isChecked());
//            ed.putBoolean("deb", ((CompoundButton) findViewById(R.id.use_deb)).isChecked());
            //  ed.commit(); // Типа во внешнюю
            ed.apply();

        } else if (view.getId() == R.id.button4) {
            finish();
        }
    }
}
