package com.example.set12ma;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.set12ma.ui.main.*;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements ResultReceiverAddressSpace, ResultReceiverMemorySpace, ResultReceiverStatusSpace, ResultReceiverFileLogsSpace, ResultReceiverSettingSpace {

    // Адресное пространство приложения
    SpaceAddress spaceAddress;
    // Пространство памяти для загрузки бинарных файлов в железо
    SpaceMemory spaceMemory;
    // Пространство памяти для отображения состояния приложения
    SpaceStatus spaceStatus;
    // Пространство памяти для хранения считанных логов
    SpaceFileLogs spaceFileLogs;
    SpaceSetting spaceSetting;
    // SET12MA
    TabLayout tabsSET12MA;
    ViewPager viewPagerDataInput;
    ViewPager viewPagerDataOutput;
    // LoadSoftware
    ViewPager viewPagerLoadingSoftware;
    // Logging
    ViewPager viewPagerLogging;
    // Connection
    ViewPager viewPagerConnecting;

    ViewPager viewPagerSetting;

    MainActivitySectionsPagerAdapterDataInput sectionsPagerAdapterDataInput;
    MainActivitySectionsPagerAdapterDataOutput sectionsPagerAdapterDataOutput;
    MainActivitySectionsPagerAdapterLoadingSoftware sectionsPagerAdapterLoadingSoftware;
    MainActivitySectionsPagerAdapterLogging sectionsPagerAdapterLogging;
    MainActivitySectionsPagerAdapterConnecting sectionsPagerAdapterConnecting;
    MainActivitySectionsPagerAdapterSetting sectionsPagerAdapterSetting;

    String selectedTabPosition = "selectedTabPosition";

    int viewPagerNumber = 0;


    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("AndroidExample", "onCreate");
        setContentView(R.layout.activity_main);
        sectionsPagerAdapterDataInput = new MainActivitySectionsPagerAdapterDataInput(this, getSupportFragmentManager());
        sectionsPagerAdapterDataOutput = new MainActivitySectionsPagerAdapterDataOutput(this, getSupportFragmentManager());
        sectionsPagerAdapterLoadingSoftware = new MainActivitySectionsPagerAdapterLoadingSoftware(this, getSupportFragmentManager());
        sectionsPagerAdapterLogging = new MainActivitySectionsPagerAdapterLogging(this, getSupportFragmentManager());
        sectionsPagerAdapterConnecting = new MainActivitySectionsPagerAdapterConnecting(this, getSupportFragmentManager());
        sectionsPagerAdapterSetting = new MainActivitySectionsPagerAdapterSetting(this, getSupportFragmentManager());

        // Terminal
        viewPagerDataInput = findViewById(R.id.view_pager_dataInput);
        viewPagerDataInput.setAdapter(sectionsPagerAdapterDataInput);
        viewPagerNumber = 0;
        viewPagerDataOutput = findViewById(R.id.view_pager_dataOutput);
        viewPagerDataOutput.setAdapter(sectionsPagerAdapterDataOutput);
        // LoadingSoftware
        viewPagerLoadingSoftware = findViewById(R.id.view_pager_loadingSoftware);
        viewPagerLoadingSoftware.setAdapter(sectionsPagerAdapterLoadingSoftware);
//        viewPagerLoadingSoftware.setVisibility(View.INVISIBLE);
        // Logging
        viewPagerLogging = findViewById(R.id.view_pager_logging);
        viewPagerLogging.setAdapter(sectionsPagerAdapterLogging);
//        viewPagerLogging.setVisibility(View.INVISIBLE);
        // Connecting
        viewPagerConnecting = findViewById(R.id.view_pager_connecting);
        viewPagerConnecting.setAdapter(sectionsPagerAdapterConnecting);
//        viewPagerConnecting.setVisibility(View.INVISIBLE);
        viewPagerSetting = findViewById(R.id.view_pager_setting);
        viewPagerSetting.setAdapter(sectionsPagerAdapterSetting);

        // main tab
        tabsSET12MA = findViewById(R.id.tabs_SET12MA);
        tabsSET12MA.setupWithViewPager(viewPagerDataInput);

        upDateViewPager(viewPagerNumber);

//        FloatingActionButton fab = findViewById(R.id.fab_SET12MA);

        spaceAddress = new SpaceAddress(300);
        spaceMemory = new SpaceMemory();
        spaceStatus = new SpaceStatus();
        spaceFileLogs = new SpaceFileLogs();
        spaceSetting = new SpaceSetting();
//        addressSpace.setAddressSpace(150, 1);
//        addressSpace.setAddressSpace(210, 150);

//        indicator = findViewById(R.id.menu_indicator);
//        indicator.setEnabled(false);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);
//        ActivityCompat.requestPermissions(MainActivity.this,
//                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                1);

        sharedPreferences = getSharedPreferences("Setting", MODE_PRIVATE);
        dataRecovery();
    }

    @SuppressLint("CommitPrefEdits")
    private void dataRecovery() {
        if (!sharedPreferences.contains("start")) {
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 16; i++) {
                    sharedPreferences.edit().putString("adc_" + j + "_" + i + "_name", spaceSetting.getAdcArrayList().get(i+j*16).getName());
                    sharedPreferences.edit().putInt("adc_" + j + "_" + i + "_plus", spaceSetting.getAdcArrayList().get(i+j*16).getPlus());
                    sharedPreferences.edit().putInt("adc_" + j + "_" + i + "_minus", spaceSetting.getAdcArrayList().get(i+j*16).getMinus());
                    sharedPreferences.edit().putInt("adc_" + j + "_" + i + "_color", spaceSetting.getAdcArrayList().get(i+j*16).getColor());
                    sharedPreferences.edit().putInt("adc_" + j + "_" + i + "_register", spaceSetting.getAdcArrayList().get(i+j*16).getRegister());

                    sharedPreferences.edit().putString("in_" + j + "_" + i + "_name", spaceSetting.getInArrayList().get(i+j*16).getName());
                    sharedPreferences.edit().putInt("in_" + j + "_" + i + "_register", spaceSetting.getInArrayList().get(i+j*16).getRegister());

                    sharedPreferences.edit().putString("out_" + j + "_" + i + "_name", spaceSetting.getOutArrayList().get(i+j*16).getName());
                    sharedPreferences.edit().putInt("out_" + j + "_" + i + "_register", spaceSetting.getOutArrayList().get(i+j*16).getRegister());
                }
            }

            for (int j = 0; j < 8; j++) {
                for (int i = 0; i < 8; i++) {
                    sharedPreferences.edit().putString("tk_" + j + "_" + i + "_name", spaceSetting.getTkArrayList().get(i+j*8).getName());
                    sharedPreferences.edit().putInt("tk_" + j + "_" + i + "_register", spaceSetting.getTkArrayList().get(i+j*8).getRegister());
                }
            }

            sharedPreferences.edit().putString("start", "start");
            sharedPreferences.edit().apply();
        } else {
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 16; i++) {
                    spaceSetting.getAdcArrayList().get(i + j * 16).setName(sharedPreferences.getString("adc_" + j + "_" + i + "_name", "adc_" + j + "_" + i + "_name"));
                    spaceSetting.getAdcArrayList().get(i + j * 16).setPlus(sharedPreferences.getInt("adc_" + j + "_" + i + "_plus", 1024));
                    spaceSetting.getAdcArrayList().get(i + j * 16).setMinus(sharedPreferences.getInt("adc_" + j + "_" + i + "_minus", 1024));
                    spaceSetting.getAdcArrayList().get(i + j * 16).setColor(sharedPreferences.getInt("adc_" + j + "_" + i + "_color", i));
                    spaceSetting.getAdcArrayList().get(i + j * 16).setRegister(sharedPreferences.getInt("out_" + j + "_" + i + "_register", 96 + i + j*16));

                    spaceSetting.getInArrayList().get(i + j * 16).setName(sharedPreferences.getString("in_" + j + "_" + i + "_name", "in_" + j + "_" + i + "_name"));
                    spaceSetting.getInArrayList().get(i + j * 16).setRegister(sharedPreferences.getInt("in_" + j + "_" + i + "_register", i + j*16));

                    spaceSetting.getInArrayList().get(i + j * 16).setName(sharedPreferences.getString("out_" + j + "_" + i + "_name", "out_" + j + "_" + i + "_name"));
                    spaceSetting.getInArrayList().get(i + j * 16).setRegister(sharedPreferences.getInt("out_" + j + "_" + i + "_register", 48 + i + j*16));
                }
            }

            for (int j = 0; j < 8; j++) {
                for (int i = 0; i < 8; i++) {
                    spaceSetting.getInArrayList().get(i + j * 8).setName(sharedPreferences.getString("tk_" + j + "_" + i + "_name", "tk_" + j + "_" + i + "_name"));
                    spaceSetting.getInArrayList().get(i + j * 8).setRegister(sharedPreferences.getInt("tk_" + j + "_" + i + "_name", 144+i+j*8));
                    sharedPreferences.edit().putString("tk_" + j + "_" + i + "_name", spaceSetting.getTkArrayList().get(i+j*8).getName());
                    sharedPreferences.edit().putInt("tk_" + j + "_" + i + "_register", spaceSetting.getTkArrayList().get(i+j*8).getRegister());
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Необходимо разрешить в настройках телефона доступ к местоположению для использования приложения", Toast.LENGTH_SHORT).show();
//                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putInt("selectedTabPosition", tabsSET12MA.getSelectedTabPosition());
        outState.putInt("viewPagerNumber", viewPagerNumber);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        int selectedTabPosition = savedInstanceState.getInt("selectedTabPosition");
        upDateViewPager(savedInstanceState.getInt("viewPagerNumber"));
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        TextView headerView = findViewById(R.id.selectedMenuItem);
        switch(id){
            case R.id.menu_indicator:
                if (!spaceStatus.isReadyFlagToExchangeData()) {
                    Toast.makeText(MainActivity.this, "Нет обмена данными", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_dataInput:
                upDateViewPager(1);
                return true;
            case R.id.menu_dataOutput:
                upDateViewPager(2);
                return true;
            case R.id.menu_connecting:
                upDateViewPager(0);
                return true;
            case R.id.menu_logging:
                upDateViewPager(3);
                return true;
            case R.id.menu_loadingSoftware:
                upDateViewPager(4);
                return true;
            case R.id.menu_setting:
                upDateViewPager(5);
                return true;
        }
//        headerView.setText(item.getTitle());
        return super.onOptionsItemSelected(item);
    }

    private void upDateViewPager(int value) {
        if (value == 1) {
            tabsSET12MA.setupWithViewPager(viewPagerDataInput);
            tabsSET12MA.setVisibility(View.VISIBLE);
            viewPagerConnecting.setVisibility(View.INVISIBLE);
            viewPagerLogging.setVisibility(View.INVISIBLE);
            viewPagerLoadingSoftware.setVisibility(View.INVISIBLE);
            viewPagerDataInput.setVisibility(View.VISIBLE);
            viewPagerDataOutput.setVisibility(View.INVISIBLE);
            viewPagerSetting.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Входы");
            viewPagerNumber = 1;
        }
        if (value == 2) {
            tabsSET12MA.setupWithViewPager(viewPagerDataOutput);
            tabsSET12MA.setVisibility(View.VISIBLE);
            viewPagerConnecting.setVisibility(View.INVISIBLE);
            viewPagerLogging.setVisibility(View.INVISIBLE);
            viewPagerLoadingSoftware.setVisibility(View.INVISIBLE);
            viewPagerDataInput.setVisibility(View.INVISIBLE);
            viewPagerDataOutput.setVisibility(View.VISIBLE);
            viewPagerSetting.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Выходы");
            viewPagerNumber = 2;
        }
        if (value == 0) {
            tabsSET12MA.setupWithViewPager(viewPagerConnecting);
            tabsSET12MA.setVisibility(View.VISIBLE);
            viewPagerConnecting.setVisibility(View.VISIBLE);
            viewPagerLogging.setVisibility(View.INVISIBLE);
            viewPagerLoadingSoftware.setVisibility(View.INVISIBLE);
            viewPagerDataInput.setVisibility(View.INVISIBLE);
            viewPagerDataOutput.setVisibility(View.INVISIBLE);
            viewPagerSetting.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Подключение");
            viewPagerNumber = 0;
        }
        if (value == 3) {
            tabsSET12MA.setupWithViewPager(viewPagerLogging);
            tabsSET12MA.setVisibility(View.VISIBLE);
            viewPagerConnecting.setVisibility(View.INVISIBLE);
            viewPagerLogging.setVisibility(View.VISIBLE);
            viewPagerLoadingSoftware.setVisibility(View.INVISIBLE);
            viewPagerDataInput.setVisibility(View.INVISIBLE);
            viewPagerDataOutput.setVisibility(View.INVISIBLE);
            viewPagerSetting.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Логирование");
            viewPagerNumber = 3;
        }
        if (value == 4) {
            tabsSET12MA.setupWithViewPager(viewPagerLoadingSoftware);
            tabsSET12MA.setVisibility(View.INVISIBLE);
            viewPagerConnecting.setVisibility(View.INVISIBLE);
            viewPagerLogging.setVisibility(View.INVISIBLE);
            viewPagerLoadingSoftware.setVisibility(View.VISIBLE);
            viewPagerDataInput.setVisibility(View.INVISIBLE);
            viewPagerDataOutput.setVisibility(View.INVISIBLE);
            viewPagerSetting.setVisibility(View.INVISIBLE);
            getSupportActionBar().setTitle("Загрузка ПО");
            viewPagerNumber = 4;
        }
        if (value == 5) {
            tabsSET12MA.setupWithViewPager(viewPagerSetting);
            tabsSET12MA.setVisibility(View.INVISIBLE);
            viewPagerConnecting.setVisibility(View.INVISIBLE);
            viewPagerLogging.setVisibility(View.INVISIBLE);
            viewPagerLoadingSoftware.setVisibility(View.INVISIBLE);
            viewPagerDataInput.setVisibility(View.INVISIBLE);
            viewPagerDataOutput.setVisibility(View.INVISIBLE);
            viewPagerSetting.setVisibility(View.VISIBLE);
            getSupportActionBar().setTitle("Настройки");
            viewPagerNumber = 5;
        }
    }

    @Override
    public SpaceAddress getSpaceAddress() {
        return spaceAddress;
    }

    @Override
    public SpaceMemory getSpaceMemory() {
        return spaceMemory;
    }

    @Override
    public SpaceStatus getSpaceStatus() {
        return spaceStatus;
    }

    @Override
    public SpaceFileLogs getSpaceFileLogs() {
        return spaceFileLogs;
    }

    @Override
    public SpaceSetting getSpaceSetting() {
        return spaceSetting;
    }
}