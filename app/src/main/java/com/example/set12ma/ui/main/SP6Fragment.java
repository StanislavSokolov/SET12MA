package com.example.set12ma.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.set12ma.R;

import java.io.*;

import static android.app.Activity.RESULT_OK;

public class SP6Fragment extends Fragment {
    private static final String ARG_SECTION_NUMBER = "SP6";
    private static final String LOG_TAG = "AndroidExample";
    private TextView textViewPathToLoadFile;
    private TextView textViewStatusOfLoading;
    private TextView textViewInformationAboutDevice;

    private Button buttonChoicePath;
    private Button buttonLoadToFlesh;
    private Button buttonStartLoadSP6;

    private ProgressBar progressBarLoadToFlesh;
    private ProgressBar progressBarLoadToDevice;

    Uri selectedFile;
    Uri selectedFile0;
    private MemorySpace memorySpace;
    private ResultReceiverMemorySpace resultReceiverMemorySpace;

    private Spinner spinnerAddressOfDevice;
    private ArrayAdapter<String> adapterAddressOfDevice;
    private int itemSelectedFromConnectedDevices = 0;



    private UpDateGraphicalDisplay upDateGraphicalDisplay;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        resultReceiverMemorySpace = (ResultReceiverMemorySpace) context;
    }

    private PageViewModel pageViewModel;

    public static SP6Fragment newInstance(int index) {
        SP6Fragment fragment = new SP6Fragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        upDateGraphicalDisplay = new UpDateGraphicalDisplay();
        upDateGraphicalDisplay.start();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sp6, container, false);
        buttonChoicePath = root.findViewById(R.id.button_choice_path);
        buttonChoicePath.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) { openFile();
            }
        });
        buttonLoadToFlesh = root.findViewById(R.id.button_load_to_flesh);
        buttonLoadToFlesh.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                try {
                    loadFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        buttonStartLoadSP6 = root.findViewById(R.id.button_start_load_sp6);
        buttonStartLoadSP6.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) { startLoad();
            }
        });
        textViewPathToLoadFile = root.findViewById(R.id.textView_path_to_load_file);
        textViewStatusOfLoading = root.findViewById(R.id.textView_status_of_loading);
        textViewInformationAboutDevice = root.findViewById(R.id.textView_information_about_device);

        progressBarLoadToFlesh = root.findViewById(R.id.progressBar_load_to_flesh);
        progressBarLoadToDevice = root.findViewById(R.id.progressBar_load_to_device);

        spinnerAddressOfDevice = root.findViewById(R.id.spinner_address_of_device_sp6);
        adapterAddressOfDevice = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        adapterAddressOfDevice.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < 16; i++) {
            adapterAddressOfDevice.add(String.valueOf(i));
        }
        spinnerAddressOfDevice.setAdapter(adapterAddressOfDevice);
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSelectedFromConnectedDevices = spinnerAddressOfDevice.getSelectedItemPosition();
                textViewInformationAboutDevice.setText("Устройство с адресом " + itemSelectedFromConnectedDevices + " готово к обновлению ПО");
                memorySpace.setAddressOfDevice(itemSelectedFromConnectedDevices);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerAddressOfDevice.setOnItemSelectedListener(itemSelectedListener);


        memorySpace = resultReceiverMemorySpace.getMemorySpace();
        return root;
    }

    private void openFile() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    }

    private void loadFile() throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = getContext().getContentResolver().openInputStream(selectedFile0);
            memorySpace.setMemorySpaceByte();
            byte[] data = new byte[memorySpace.getMemorySpaceByteLength()];
            int count = inputStream.read(data);
            while (count != -1) {
                byte[] dataLastByte = new byte[count];
                for (int i = 0; i < count; i++) {
                    dataLastByte[i] = data[i];
                }
                memorySpace.setMemorySpaceArrayListByte(dataLastByte);
                count = inputStream.read(data);
            }
            memorySpace.setReadyFlagToLoad(true);
            memorySpace.setDevice("SP6");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
    }

    private void startLoad() {
        memorySpace.setReadyFlagToStart(true);
    }

        @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            selectedFile = data.getData(); //The uri with the location of the file
            selectedFile0 = selectedFile;
            Toast.makeText(getContext(), selectedFile.toString(), Toast.LENGTH_LONG).show();
            textViewPathToLoadFile.setText(data.getDataString());
        }
    }

    public class UpDateGraphicalDisplay extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                try {
                    UpDateGraphicalDisplay.sleep(300);
                    Log.i(LOG_TAG, "start thread");
                    if (memorySpace.isStatusLoadToDevice()) {
                        progressBarLoadToDevice.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBarLoadToDevice.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        progressBarLoadToDevice.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBarLoadToDevice.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    if (memorySpace.isStatusLoadToFlesh()) {
                        progressBarLoadToFlesh.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBarLoadToFlesh.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        progressBarLoadToFlesh.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBarLoadToFlesh.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public UpDateGraphicalDisplay() {
        }
    }
}

