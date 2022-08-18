package com.example.set12ma.ui.main;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.*;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

public class FragmentBluetooth extends Fragment {

    // for BluetoothSoketThread
    final String PBAP_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;

    private BluetoothSoketThread bluetoothSoketThread;
    private BluetoothConnectedInputThread bluetoothConnectedInputThread;
    private BluetoothConnectedOutputThread bluetoothConnectedOutputThread;
    private long timer = 5000;

    private final int ADDRESS_DEVICE = 10;
    private final int READ = 56;
    private final int WRITE = 57;
    private final int INIT_LOAD = 58;
    private final int LOAD = 51;
    private final int EXTEND = 60;
    private final int UPLOAD = 52;


    private volatile int currentCommand = INIT_LOAD;


    // for BluetoothConnectedThread
    byte[] bytesToSend = null;
    byte[] bytesToCreateCRC = null;;
    OutputStream outputStream = null;
    private InputStream inputStream;
    private byte addressDevice = 10;
    private byte readCommand = 56;
    private byte writeCommand = 57;
    private byte initLoadCommand = 58;
    private byte loadCommand = 51;
    private byte extendCommand = 60;
    private byte uploadCommand = 52;
    private int countBytes = 8;
    private int addressSpaceNumber = 0;
    // setFlagWaitingAnswerInitLoad sets the flag to wait the answer from controller to start the loading to memory
    private boolean flagWaitingAnswerInitLoad = false;
    //
    private boolean flagWaitingAnswerLoad = false;
    //
    private boolean flagWaitingAnswerFinishLoad = false;
    private boolean flagWaitingAnswerUpload = false;

    //
    private boolean latchLoad = false;
    // latchFinish
    private boolean latchFinish = false;

    private boolean latchQueue = false;

    private boolean latchDownloadLog = false;

    private boolean isStatusReading = false;
    private boolean isModeSending = false;
    private int statement = 0;
    private int previousByte = 0;
    private int currentByte = 0;
    private int nextByte = 0;
    private int counterUnsuccessfulSending = 0;
    private int maxValueUnsuccessfulSending = 10;
    private int counterAttemptsToConection = 0;

    // for
    private static final String ARG_SECTION_NUMBER = "BT";
    private static final String LOG_TAG = "AndroidExample";
    private String stringConnectedToDevice;
    private SpaceAddress spaceAddress;
    private ResultReceiverAddressSpace resultReceiverAddressSpace;

    private SpaceMemory spaceMemory;
    private ResultReceiverMemorySpace resultReceiverMemorySpace;

    private SpaceStatus spaceStatus;
    private ResultReceiverStatusSpace resultReceiverStatusSpace;

    private ArrayList<BluetoothDevice> arrayListAvailableDevices;                               // список устройств, доступных к сопряжению
    private ArrayList<BluetoothDevice> arrayListConnectedDevices;                               // список устройств, доступных к сопряжению
    private int itemSelectedFromConnectedDevices = 0;
    private BluetoothAdapter bluetooth;
    private ArrayAdapter<String> adapterConnectedDevices;
    private String firstStringAdapterConnectedDevices = "Выберите устройство";
    private ArrayAdapter<String> adapterAvailableDevices;
    private String firstStringAdapterAvailableDevices = "Выберите устройство";

    private PageViewModel pageViewModel;
    private TextView textViewConnectedDevices;
    private Spinner spinnerConnectedDevices;
    private Button buttonConnectToDevice;
    private static TextView textViewConnectedToDevice;
    private static ProgressBar progressBarConnectedToDevice;
    private TextView textViewAvailableDevices;
    private Spinner spinnerAvailableDevices;
    private Button buttonFindNewDevices;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        resultReceiverAddressSpace = (ResultReceiverAddressSpace) context;
        resultReceiverMemorySpace = (ResultReceiverMemorySpace) context;
        resultReceiverStatusSpace = (ResultReceiverStatusSpace) context;
    }


    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(LOG_TAG, "starting");
            }

            // При обнаружении устройства проверяется наличие дубликата в имеющемся адаптере и добавление нового устройства в адаптер
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                boolean checkingDuplicates = false;
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for (int i = 0; i < adapterAvailableDevices.getCount(); i++) {
                    if (String.valueOf(device.getName()).equals("null")) {
                        if (device.getAddress().equals(adapterAvailableDevices.getItem(i))) {
                            Log.i(LOG_TAG, String.valueOf(adapterAvailableDevices.getItem(i)));
                            checkingDuplicates = true;
                        }
                    } else {
                        if (device.getName().equals(adapterAvailableDevices.getItem(i))) {
                            Log.i(LOG_TAG, String.valueOf(adapterAvailableDevices.getItem(i)));
                            checkingDuplicates = true;
                        }
                    }

                }
                if (!checkingDuplicates) {
                    if (String.valueOf(device.getName()).equals("null")) {
                        adapterAvailableDevices.add(device.getAddress());
                    } else {
                        adapterAvailableDevices.add(String.valueOf(device.getName()));
                    }
                    arrayListAvailableDevices.add(device);
                }

            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                showConnectedDevices();
                showAvailableDevices();
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(LOG_TAG, "finishing");
            }

        }
    };


    public static FragmentBluetooth newInstance(int index) {
        FragmentBluetooth fragment = new FragmentBluetooth();
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

        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(broadcastReceiver, filterFound);

        IntentFilter filterConnected = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        getContext().registerReceiver(broadcastReceiver, filterConnected);

        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        getContext().registerReceiver(broadcastReceiver, filterStart);

        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getContext().registerReceiver(broadcastReceiver, filterFinish);



    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, final ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        spaceAddress = resultReceiverAddressSpace.getSpaceAddress();
        spaceMemory = resultReceiverMemorySpace.getSpaceMemory();
        spaceStatus = resultReceiverStatusSpace.getSpaceStatus();

        textViewConnectedDevices = root.findViewById(R.id.textView_tip_find_file);
        spinnerConnectedDevices = root.findViewById(R.id.spinner_connected_devices);
        buttonConnectToDevice = root.findViewById(R.id.button_connect_to_device);
        textViewConnectedToDevice = root.findViewById(R.id.textView_path_to_load_file_for_sp6);
        progressBarConnectedToDevice = root.findViewById(R.id.progressBar_loading_to_flesh);
        textViewAvailableDevices = root.findViewById(R.id.textView_available_devices);
        spinnerAvailableDevices = root.findViewById(R.id.spinner_available_devices);
        buttonFindNewDevices = root.findViewById(R.id.button_find_new_devices);

        checkEnableBluetooth();
        showConnectedDevices();
        showAvailableDevices();


        buttonConnectToDevice.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {         if (bluetooth.isEnabled()) {
                try {
                    setConnecting();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else
                Toast.makeText(getActivity(), "Необходимо включить Bluetooth", Toast.LENGTH_SHORT).show();
            }
        });

        buttonFindNewDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findNewDevices(container);
            }
        });

        return root;
    }


    private void setConnecting() throws InterruptedException {
        if (!adapterConnectedDevices.getItem(itemSelectedFromConnectedDevices + 1).equals("Выберите устройство")) {

            spaceStatus.setReadyFlagToExchangeData(false);
            spaceStatus.setReadyFlagToLoadSoftware(false);
            spaceStatus.setReadyFlagToUpdateSoftware(false);
            spaceStatus.setReadyFlagToFinishOfLoadingSoftware(false);
            spaceStatus.setReadyFlagToFinishOfUpdatingSoftware(false);
            spaceStatus.setStatusProcessOfLoadingSoftware(false);
            spaceStatus.setStatusProcessOfUpdatingSoftware(false);

            latchLoad = false;
            latchFinish = false;
            latchQueue = false;
            latchDownloadLog = false;

            setCommand(INIT_LOAD);

            if (buttonConnectToDevice.getText().equals("Подключить")) {
                stringConnectedToDevice = arrayListConnectedDevices.get(itemSelectedFromConnectedDevices).getName();
                bluetoothDevice = arrayListConnectedDevices.get(itemSelectedFromConnectedDevices);
                progressBarConnectedToDevice.setVisibility(View.VISIBLE);
                buttonConnectToDevice.setText("Отключить");
                textViewConnectedToDevice.setText("Подключение к " + stringConnectedToDevice);
                textViewConnectedToDevice.setVisibility(View.VISIBLE);
                currentByte = 48;
                statement = 1;
                counterUnsuccessfulSending = 0;
                counterAttemptsToConection = 0;
                bluetoothSoketThread = new BluetoothSoketThread();
                bluetoothSoketThread.start();
            } else {
                buttonConnectToDevice.setText("Подключить");
                textViewConnectedToDevice.setText("Отключено от " + stringConnectedToDevice);
                progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
                spaceStatus.setReadyFlagToExchangeData(false);
                spaceStatus.setDevice("");
                getActivity().findViewById(R.id.menu_indicator).setVisibility(View.VISIBLE);
                spaceStatus.setReadyFlagRecordingInitialValues(false);
                bluetoothSoketThread.cancel();
            }
        } else {
            if (buttonConnectToDevice.getText().equals("Подключить")) {
                Toast.makeText(getActivity(), "Для подключения необходимо выбрать сопряженное устройство", Toast.LENGTH_SHORT).show();
            } else {
                buttonConnectToDevice.setText("Подключить");
                textViewConnectedToDevice.setText("Отключено от " + stringConnectedToDevice);
                progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
                spaceStatus.setReadyFlagToExchangeData(false);
                spaceStatus.setDevice("");
                getActivity().findViewById(R.id.menu_indicator).setVisibility(View.VISIBLE);
                bluetoothSoketThread.cancel();
            }

        }
    }

    private void checkEnableBluetooth(){
        if (bluetooth.isEnabled()) {
        }
        else
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 12);
        }
    }

    private void showConnectedDevices(){

        arrayListConnectedDevices = new ArrayList<>();
        adapterConnectedDevices = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        adapterConnectedDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();

        if(pairedDevices.size()>0){
            textViewConnectedDevices.setText("Сопряженные устройства");
            adapterConnectedDevices.add(firstStringAdapterConnectedDevices);
            for(BluetoothDevice device: pairedDevices){
                adapterConnectedDevices.add(device.getName());
                arrayListConnectedDevices.add(device);
            }
            spinnerConnectedDevices.setAdapter(adapterConnectedDevices);

            AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    if (spinnerConnectedDevices.getSelectedItemId() != 0) {
                    itemSelectedFromConnectedDevices = spinnerConnectedDevices.getSelectedItemPosition() - 1;
                    Log.i(LOG_TAG, String.valueOf(spinnerConnectedDevices.getSelectedItemPosition()));
//                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            };
            spinnerConnectedDevices.setOnItemSelectedListener(itemSelectedListener);
            spinnerConnectedDevices.setVisibility(View.VISIBLE);
        } else {
            textViewConnectedDevices.setText("Нет сопряженных устройств");
            spinnerConnectedDevices.setVisibility(View.INVISIBLE);
        }
    }

    private void showAvailableDevices(){
        arrayListAvailableDevices = new ArrayList<>();
        adapterAvailableDevices = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        adapterAvailableDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapterAvailableDevices.add(firstStringAdapterAvailableDevices);
        spinnerAvailableDevices.setAdapter(adapterAvailableDevices);

        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerAvailableDevices.getSelectedItemId() != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (!arrayListAvailableDevices.get(spinnerAvailableDevices.getSelectedItemPosition()-1).createBond()) Log.i(LOG_TAG, "Не удается выполнить сопряжение");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerAvailableDevices.setOnItemSelectedListener(itemSelectedListener);
        spinnerAvailableDevices.setVisibility(View.VISIBLE);
    }

    private void findNewDevices(ViewGroup container) {
        if (bluetooth.isDiscovering()) {
            Toast.makeText(container.getContext(), "Идёт сканирование", Toast.LENGTH_LONG).show();
        } else {
            if (bluetooth.startDiscovery()) {
                Toast.makeText(container.getContext(), "Поиск", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(container.getContext(), "Нет доступных устройств", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class BluetoothSoketThread extends Thread {

        public BluetoothSoketThread() {
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(ParcelUuid.fromString(PBAP_UUID).getUuid());
            } catch (IOException e) {
                Log.i(LOG_TAG, e.toString());
            }
            Log.i(LOG_TAG, String.valueOf(bluetoothSocket.getRemoteDevice()));
        }

        @Override
        public void run() {
            bluetoothConnectedInputThread = new BluetoothConnectedInputThread();
            bluetoothConnectedOutputThread = new BluetoothConnectedOutputThread();
            boolean connect = false;
            try {
                bluetoothSocket.connect();
                connect = true;
            } catch (IOException e) {
                textViewConnectedToDevice.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewConnectedToDevice.setText("Не удалось подключиться к устройству. Проверьте питание " + spinnerConnectedDevices.getSelectedItem());
                        Toast.makeText(getContext(), "Не удалось подключиться к устройству. Проверьте питание " + spinnerConnectedDevices.getSelectedItem(), Toast.LENGTH_LONG).show();
                        progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
                        buttonConnectToDevice.setText("Подключить");
                    }
                });
                cancel();
            }
            // If a connection was accepted
            if (connect) {
                spaceStatus.setReadyFlagRecordingInitialValues(true);
                // Do work to manage the connection (in a separate thread)
                try {
                    manageConnectedSocket();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            } else {
            }
        }

        public void cancel() {
            try {
                interrupt();
                bluetoothSocket.close();
            } catch (IOException e) { }
        }

        private void manageConnectedSocket() throws InterruptedException, IOException {
            bluetoothConnectedInputThread.start();
            while (!bluetoothConnectedInputThread.isAlive()) {
                BluetoothSoketThread.sleep(1);
            }

            bluetoothConnectedOutputThread.start();
            while (!bluetoothConnectedOutputThread.isAlive()) {
                BluetoothSoketThread.sleep(1);
            }

            int countByte = 0;
            byte[] bufferByte = null;

            int crc = 0;
            int high = 0;

            String answerTest = "";
            int countWaitConnection = 0;

           boolean latchInit = true;


            boolean getAnswer = false;

            while (!isInterrupted()) {
                if (!spaceAddress.isEmptyByteQueue()) {
                    byte[] buffer = spaceAddress.getByteQueue();
                    switch (getCommand()) {
                        case READ:

                            Log.i(LOG_TAG, "count byte " + buffer.length);

                            if (countByte == 0) {
                                // здесь нужно проверять не countByte, а было ли в буфере начало нового сообщения
                                bufferByte = new byte[10];
                                Log.i(LOG_TAG, "Начало сообщения");
                            }

                            if (buffer.length > 10 - countByte) {
                                // получили избыточное количество байт в посылке
                                // остаток положим во временный буфер
                                Log.i(LOG_TAG, "Избыточное количество байт");
                            } else {
                                // получили байты
                                for (int i = 0; i < buffer.length; i++) {
                                    bufferByte[i + countByte] = buffer[i];
                                }
                                countByte = countByte + buffer.length;
                                Log.i(LOG_TAG, "Текущее количестов принятых байт " + String.valueOf(countByte));
                            }

                            if (countByte == 10) {
                                Log.i(LOG_TAG, "Получили нужное количество байт");
                                answerTest = "";
                                for (byte readByte: bufferByte) {
                                    int bufInt = 0;
                                    if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
                                    answerTest = answerTest + " " + bufInt;
                                }
                                Log.i(LOG_TAG, answerTest);
                                countByte = 0;
                                if ((bufferByte[0] == ADDRESS_DEVICE) & (bufferByte[1] == READ)) {
                                    Log.i(LOG_TAG, "Идентификатор корректен");
                                    // проверяем корректность сообщения по идентификатору
                                    byte[] bytesToCreateCRC = new byte[bufferByte.length - 2];
                                    for (int i = 0; i < bytesToCreateCRC.length; i++) {
                                        bytesToCreateCRC[i] = bufferByte[i];
                                    }
                                    crc = (CRC16.getCRC4(bytesToCreateCRC));
                                    high = crc/256;
//                                    answerTest = "";
//                                    for (byte readByte: buffer) {
//                                        int bufInt = 0;
//                                        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//                                        answerTest = answerTest + " " + bufInt;
//                                    }
//                                    Log.i(LOG_TAG, answerTest);
                                    if ((bufferByte[bytesToCreateCRC.length] == (byte) (crc - high*256)) & (bufferByte[bytesToCreateCRC.length + 1] == (byte) high)) {
                                        Log.i(LOG_TAG, "ЦРЦ в порядке");

                                        if (currentByte == 47) {
                                            nextByte = 96;
                                        }

                                        if (currentByte == 143) {
                                            nextByte = 208;
                                        }

                                        if (currentByte == 255) {
                                            nextByte = 0;
                                        }


                                        if ((currentByte == 47) || (currentByte == 143) || (currentByte == 255)) currentByte = nextByte;
                                        else currentByte++;
                                    } else {
                                        textViewConnectedToDevice.setText("CRC не совпало");
                                        Log.i(LOG_TAG, "CRC не совпало");
                                    }
                                } else {
                                    Log.i(LOG_TAG, "Не смогли идентифицировать сообщение");
                                }
                                getAnswer = true;
                            }
//        buffer = new byte[10];  // buffer store for the stream
//        //                            inputStream.read(buffer);
//        bufferPrepeared = null;
//        bufferPrepeared = new byte[10];
//        lastByte = 0;
//        bytes = 0;
//        int countToBreakeFromWhile = 0;
//        while (lastByte != 10) {
////                            Log.i(LOG_TAG, "Проверяем lastByte != buffer.length");
//        test("Проверяем lastByte != buffer.length");
//        try {
//        bytes = inputStream.read(buffer);
//        for (int i = 0; i < bytes; i++) {
//        if (lastByte + i < 10) {
//        bufferPrepeared[lastByte + i] = buffer[i];
//        } else break;
//        }
//        lastByte = lastByte + bytes;
////                                Log.i(LOG_TAG, String.valueOf(lastByte));
//        test(String.valueOf(lastByte));
//        } catch (IOException ex) {
////                                Log.i(LOG_TAG, "Здесь мы чтоли??");
//        test("Здесь мы чтоли??");
//        ex.printStackTrace();
//        }
//        if (lastByte > 10) {
//        break;
//        }
//        countToBreakeFromWhile++;
//        if (countToBreakeFromWhile > 2) {
////                                Log.i(LOG_TAG, "Вышел по условию countToBreakeFromWhile > 2");
//        test("Вышел по условию countToBreakeFromWhile > 2");
//        break;
//        }
//        }
//        test(String.valueOf(lastByte));
//        test(String.valueOf(lastByte));
//
//        if (lastByte == bufferPrepeared.length) {
//        bytes = lastByte;


                            break;
                        case WRITE:
                            break;
                        case INIT_LOAD:
                            break;
                        case LOAD:
                            break;
                        case EXTEND:
                            break;
                        case UPLOAD:
                            break;
                    }
                } else {
//                    BluetoothSoketThread.sleep(timer);
                    setCommand(READ);

                    if (getAnswer) {
                        latchInit = false;
                        getAnswer = false;
                        bluetoothConnectedOutputThread.read();
                    } else {
                        if (latchInit) {
                            if (countWaitConnection < 500000) {
                                countWaitConnection = countWaitConnection + 1;
                            } else {
                                countWaitConnection = 0;
                                bluetoothConnectedOutputThread.read();
                            }
                        }

                    }

//                    if (isStatusReading) {
//                        isStatusReading = false;
//                        if (spaceStatus.isReadyFlagToLoadSoftware()) {
//                            if (spaceStatus.isStatusProcessOfLoadingSoftware()) {
//                                if (!latchLoad) {
//                                    bluetoothConnectedOutputThread.load();
//                                    latchLoad = true;
//                                } else {
//                                    if (spaceStatus.isReadyFlagToFinishOfLoadingSoftware()) {
//                                        spaceStatus.setReadyFlagToLoadSoftware(false);
//                                        spaceStatus.setStatusProcessOfLoadingSoftware(false);
//                                        latchLoad = false;
//                                        isStatusReading = true;
//                                    }
//
//                                }
//                            } else {
//                                spaceStatus.setStatusProcessOfLoadingSoftware(true);
//                                bluetoothConnectedOutputThread.initLoad();
//                            }
//                        } else if (spaceStatus.isReadyFlagToUpdateSoftware()) {
//                            spaceStatus.setReadyFlagToFinishOfLoadingSoftware(false);
//                            if (!latchFinish) {
//                                spaceStatus.setStatusProcessOfUpdatingSoftware(true);
//                                bluetoothConnectedOutputThread.startToLoad();
//                                latchFinish = true;
//                                Log.i(LOG_TAG, "зАЙДЕМ сюда!");
//                            } else {
//                                if (spaceStatus.isReadyFlagToFinishOfUpdatingSoftware()) {
//                                    latchFinish = false;
//                                    spaceStatus.setReadyFlagToUpdateSoftware(false);
//                                    spaceStatus.setStatusProcessOfUpdatingSoftware(false);
//                                    spaceStatus.setReadyFlagToLoadSoftware(false);
//                                    spaceStatus.setReadyFlagToFinishOfLoadingSoftware(false);
//                                    Log.i(LOG_TAG, "всё в ноль!!!!");
//                                    isStatusReading = true;
//                                }
//                            }
//                        } else if (spaceStatus.isReadyFlagToDownloadLog()) {
//                            if (!latchDownloadLog) {
//                                bluetoothConnectedOutputThread.downloadLogs();
//                                latchDownloadLog = true;
//                                Log.i(LOG_TAG, "We  are here!");
//                            } else {
//                                if (spaceStatus.isReadyFlagToFinishOfDownloadingLogs()) {
//                                    latchDownloadLog = false;
//                                    spaceStatus.setReadyFlagToDownloadLog(false);
//                                    Log.i(LOG_TAG, "Finish of downloadlog");
//                                    isStatusReading = true;
//                                }
//                            }
//                        } else {
//                            bluetoothConnectedOutputThread.communication();
//                            if (!spaceStatus.isReadyFlagToExchangeData()) {
//                                isStatusReading = true;
//                            }
//                        }
//                    }
                }

            }
            bluetoothConnectedInputThread.interrupt();
            bluetoothConnectedOutputThread.interrupt();
        }


    }

    private synchronized int getCommand() { return currentCommand; }
    private synchronized void setCommand(int currentCommand) { this.currentCommand = currentCommand; }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            byte[] buf = new byte[msg.arg2];
            buf = (byte[]) msg.obj;
            byte[] buffer = new byte[msg.arg1];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = buf[i];
            }

            Log.i(LOG_TAG, "Принято байт в handler " + buffer.length);

            spaceAddress.setByteQueue(buffer);


        }
    };

    public class BluetoothConnectedInputThread extends Thread {
        public BluetoothConnectedInputThread() {
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                inputStream = bluetoothSocket.getInputStream();
            } catch (IOException e) { Log.i(LOG_TAG,"don't get Streams");}
        }

        public void run() {
            byte[] buffer = new byte[16];  // buffer store for the stream
            int bytes = 20; // bytes returned from read()
            while (!isInterrupted()) {
                try {
                    // Read from the InputStream
                    bytes = inputStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    handler.obtainMessage(1, bytes, buffer.length, buffer)
                            .sendToTarget();
//                    isStatusReading = true;
                    changeStateIndicator();
//                    spaceStatus.setReadyFlagToExchangeData(true);
                } catch (IOException e) {
                    Log.i(LOG_TAG,e.toString());
                    break;
                }
            }
            try {
                Log.i(LOG_TAG, "INPUT STREAM CLOSE");
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void changeStateIndicator() {
            if (getActivity().findViewById(R.id.menu_indicator).getVisibility() == View.VISIBLE) {
                getActivity().findViewById(R.id.menu_indicator).setVisibility(View.INVISIBLE);
            } else {
                getActivity().findViewById(R.id.menu_indicator).setVisibility(View.VISIBLE);
            }
        }
    }

    public class BluetoothConnectedOutputThread extends Thread {

        public BluetoothConnectedOutputThread() {
            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) { Log.i(LOG_TAG,"don't get Streams");}
        }

        public void run() {
            while (!isInterrupted()) {
//                try {
//                    if (flagWaitingAnswerInitLoad) {
//                        bytesToCreateCRC = new byte[bytes-4];
//                        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//                            bytesToCreateCRC[i] = buffer[i];
//                        }
//                        int crc = (CRC16.getCRC4(bytesToCreateCRC));
//                        int high = crc/256;
//                        if ((buffer[2] == (byte) (crc - high*256)) & (buffer[3] == (byte) high)) {
//                            Log.i("LOG_TAG_1", "CRC is good from InitLoad");
//                        } else {
//                            Log.i("LOG_TAG_1", "CRC is bed from InitLoad");
//                        }
//                        flagWaitingAnswerInitLoad = false;
//                        String answerTest = "";
//                        for (byte readByte: buffer) {
//                            int bufInt = 0;
//                            if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//                            answerTest = answerTest + " " + bufInt;
//                        }
//                        Log.i("LOG_TAG_1", answerTest);
//                    } else if (flagWaitingAnswerLoad) {
//                        bytesToCreateCRC = new byte[bytes-4];
//                        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//                            bytesToCreateCRC[i] = buffer[i];
//                        }
//                        int crc = (CRC16.getCRC4(bytesToCreateCRC));
//                        int high = crc/256;
//                        if ((buffer[2] == (byte) (crc - high*256)) & (buffer[3] == (byte) high)) {
//                            Log.i("LOG_TAG_1", "CRC is good from Load");
//                            spaceStatus.setReadyFlagToFinishOfLoadingSoftware(true);
//                        } else {
//                            Log.i("LOG_TAG_1", "CRC is bed from Load");
//                        }
//                        flagWaitingAnswerLoad = false;
//                        String answerTest = "";
//                        for (byte readByte: buffer) {
//                            int bufInt = 0;
//                            if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//                            answerTest = answerTest + " " + bufInt;
//                        }
//                        Log.i("LOG_TAG_1", answerTest);
//                    } else if (flagWaitingAnswerFinishLoad) {
//                        bytesToCreateCRC = new byte[bytes-4];
////                        for (int i = 0; i < bytesToCreateCRC.length; i++) {
////                            bytesToCreateCRC[i] = buffer[i];
////                        }
////                        int crc = (CRC16.getCRC4(bytesToCreateCRC));
////                        int high = crc/256;
////                        if ((buffer[14] == (byte) (crc - high*256)) & (buffer[15] == (byte) high)) {
////                            Log.i("LOG_TAG_1", "CRC is good from FinishLoad");
////                        } else {
////                            Log.i("LOG_TAG_1", "CRC is bed from FinishLoad");
////                        }
//                        spaceStatus.setLastNumberError(buffer[6]);
//                        spaceStatus.setReadyFlagToFinishOfUpdatingSoftware(true);
//                        flagWaitingAnswerFinishLoad = false;
//                        String answerTest = "";
//                        for (byte readByte: buffer) {
//                            int bufInt = 0;
//                            if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//                            answerTest = answerTest + " " + bufInt;
//                        }
//                        Log.i("LOG_TAG_1", answerTest);
//                    } else {
//                        if (bytes == 8) {
//                            bytesFromBuffer = new byte[bytes];
//                            bytesToCreateCRC = new byte[bytes - 2];
//                            for (int i = 0; i < bytesFromBuffer.length; i++) {
//                                bytesFromBuffer[i] = buffer[i];
//                            }
//                            for (int i = 0; i < bytesToCreateCRC.length; i++) {
//                                bytesToCreateCRC[i] = bytesFromBuffer[i];
//                            }
//                            int crc = (CRC16.getCRC4(bytesToCreateCRC));
//                            int high = crc/256;
//                            if ((bytesFromBuffer[bytesToCreateCRC.length] == (byte) (crc - high*256)) & (bytesFromBuffer[bytesToCreateCRC.length + 1] == (byte) high)) {
//                                if (spaceStatus.isReadyFlagToExchangeData()) {
//                                    spaceAddress.setAddressSpace(currentByte, bytesFromBuffer[2]);
//                                    // Это счетчик битов, ктр увеличивает значение при каждом удачном приеме;
//                                    String answerTest = "";
//                                    for (byte readByte: bytesFromBuffer) {
//                                        int bufInt = 0;
//                                        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//                                        answerTest = answerTest + " " + bufInt;
//                                    }
//                                    Log.i(LOG_TAG, answerTest);
//                                    if (currentByte == 207) {
//                                        spaceStatus.setReadyFlagRecordingInitialValues(false);
//                                    }
//                                    if ((currentByte == 47) || (currentByte == 95) || (currentByte == 143) || (currentByte == 207) || (currentByte == 255)) currentByte = nextByte;
//                                    else currentByte++;
//                                } else {
//                                    textViewConnectedToDevice.setText("Поключено к " + stringConnectedToDevice);
//                                    progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
//                                    spaceStatus.setReadyFlagToExchangeData(true);
//                                }
//                                isStatusReading = true;
//                                changeStateIndicator();
//                            } else {
//                                Log.i(LOG_TAG, "CRC не совпало");
//                                textViewConnectedToDevice.setText("CRC не совпало");
//                            }
//                        } else if (bytes == 6) {
//                            bytesFromBuffer = new byte[bytes];
//                            bytesToCreateCRC = new byte[bytes - 4];
//                            for (int i = 0; i < bytesFromBuffer.length; i++) {
//                                bytesFromBuffer[i] = buffer[i];
//                            }
//                            for (int i = 0; i < bytesToCreateCRC.length; i++) {
//                                bytesToCreateCRC[i] = bytesFromBuffer[i];
//                            }
//                            int crc = (CRC16.getCRC4(bytesToCreateCRC));
//                            int high = crc/256;
//                            if ((bytesFromBuffer[bytesToCreateCRC.length] == (byte) (crc - high*256)) & (bytesFromBuffer[bytesToCreateCRC.length + 1] == (byte) high)) {
//                                if (spaceStatus.isReadyFlagToExchangeData()) {
//                                    String answerTest = "";
//                                    for (byte readByte: bytesFromBuffer) {
//                                        int bufInt = 0;
//                                        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//                                        answerTest = answerTest + " " + bufInt;
//                                    }
//                                    Log.i(LOG_TAG, answerTest);
//                                    if (currentByte == 207) {
//                                        spaceStatus.setReadyFlagRecordingInitialValues(false);
//                                    }
//                                    if ((currentByte == 47) || (currentByte == 95) || (currentByte == 143) || (currentByte == 207) || (currentByte == 255)) currentByte = nextByte;
//                                    else currentByte++;
//                                } else {
//                                    textViewConnectedToDevice.setText("Поключено к " + stringConnectedToDevice);
//                                    progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
//                                    spaceStatus.setReadyFlagToExchangeData(true);
//                                }
//                                isStatusReading = true;
//                                changeStateIndicator();
//                            } else {
//                                Log.i(LOG_TAG, "CRC не совпало");
//                                textViewConnectedToDevice.setText("CRC не совпало");
//                            }
//                        }
//                    }
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void write() {
            bytesToSend = new byte[12];
            bytesToCreateCRC = new byte[10];
            bytesToSend[0] = ADDRESS_DEVICE;
            bytesToSend[1] = WRITE;
            bytesToSend[2] = 5;
            bytesToSend[3] = 0;
            bytesToSend[4] = 0;
            bytesToSend[5] = 0;
            int data = spaceAddress.getAddressSpace(5);
            int high = data / 256;
            bytesToSend[6] = (byte) (data - high * 256);
            bytesToSend[7] = (byte) high;
            bytesToSend[8] = 0;
            bytesToSend[9] = 0;

            for (int i = 0; i < bytesToCreateCRC.length; i++) {
                bytesToCreateCRC[i] = bytesToSend[i];
            }
            int crc = (CRC16.getCRC4(bytesToCreateCRC));
            high = crc / 256;
            bytesToSend[10] = (byte) (crc - high * 256);
            bytesToSend[11] = (byte) high;

            try {
                outputStream.write(bytesToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void read() {
            bytesToSend = new byte[8];
            bytesToCreateCRC = new byte[6];
            bytesToSend[0] = ADDRESS_DEVICE;
            bytesToSend[1] = READ;
            bytesToSend[2] = (byte) 5;
            bytesToSend[3] = 0;
            bytesToSend[4] = 0;
            bytesToSend[5] = 0;
            for (int i = 0; i < bytesToCreateCRC.length; i++) {
                bytesToCreateCRC[i] = bytesToSend[i];
            }
            int crc = (CRC16.getCRC4(bytesToCreateCRC));
            int high = crc / 256;
            bytesToSend[6] = (byte) (crc - high * 256);
            bytesToSend[7] = (byte) high;
            Log.i(LOG_TAG, "Отправляем сообщения");

            try {
                outputStream.write(bytesToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void communication() {
            if (spaceStatus.isReadyFlagToExchangeData()) {
                if (counterUnsuccessfulSending < maxValueUnsuccessfulSending) {
                    if (spaceStatus.isReadyFlagRecordingInitialValues()) {
                        if (isStatusReading) {
                            counterUnsuccessfulSending = 0;
                            switch (statement) {
                                // запись out
                                case 1:
                                    Log.i(LOG_TAG, "statement 1");
                                    sending(1);
                                    isStatusReading = false;
                                    if (currentByte == 95) {
                                        statement = 3;
                                        nextByte = 144;
                                    }
                                    break;
                                // запись ТК
                                case 3:
                                    Log.i(LOG_TAG, "statement 3");
                                    sending(1);
                                    isStatusReading = false;
                                    if (currentByte == 207) {
                                        statement = 0;
                                        spaceStatus.setReadyFlagRecordingInitialValues(false);
                                        nextByte = 0;
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            sending(1);
                            counterUnsuccessfulSending++;
                        }

                    } else {
                        if (isStatusReading) {
                            counterUnsuccessfulSending = 0;
                            if (!spaceAddress.isEmptyQueue()) {
                                if (!latchQueue) {
                                    previousByte = currentByte;
                                    latchQueue = true;
                                }
                                ElementQueue elementQueue = spaceAddress.getElementQueue();
                                if (elementQueue.getSectionNumber() == 0) {
                                    currentByte = 48;
                                }
                                if (elementQueue.getSectionNumber() == 1) {
                                    currentByte = 144;
                                }
                                currentByte = currentByte + elementQueue.getId();
                                Log.i(LOG_TAG, "SUPRIM");
                                sending(1);
                                isStatusReading = false;
                                isModeSending = true;
                            } else {
                                latchQueue = false;
                                if (isModeSending) {
                                    currentByte = previousByte;
                                    isModeSending = false;
                                }
                                switch (statement) {
                                    // чтение IN
                                    case 0:
                                        Log.i(LOG_TAG, "statement 0");
                                        sending(0);
                                        isStatusReading = false;
                                        if (currentByte == 47) {
                                            statement = 2;
                                            nextByte = 96;
                                        }
                                        break;
                                    // чтение ADC
                                    case 2:
                                        Log.i(LOG_TAG, "statement 2");
                                        sending(0);
                                        isStatusReading = false;
                                        if (currentByte == 143) {
                                            statement = 4;
                                            nextByte = 208;
                                        }
                                        break;
                                    // чтение ADC
                                    case 4:
                                        Log.i(LOG_TAG, "statement 4");
                                        sending(0);
                                        isStatusReading = false;
                                        if (currentByte == 255) {
                                            statement = 0;
                                            nextByte = 0;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        } else {
                            if (isModeSending) sending(1); else sending(0);
                            counterUnsuccessfulSending++;
                        }
                    }
                    Log.i(LOG_TAG, String.valueOf(currentByte));
                } else {
                    textViewConnectedToDevice.post(new Runnable() {
                        @Override
                        public void run() {
                            buttonConnectToDevice.setText("Подключить");
                            textViewConnectedToDevice.setText("Процессорный модуль не отвечает. Проверьте соединение.");
                            progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
                            spaceStatus.setReadyFlagToExchangeData(false);
                            spaceStatus.setDevice("");
                            getActivity().findViewById(R.id.menu_indicator).setVisibility(View.VISIBLE);
                            spaceStatus.setReadyFlagRecordingInitialValues(false);
                            bluetoothSoketThread.cancel();
                            Toast.makeText(getContext(), "Процессорный модуль не отвечает. Проверьте соединение.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                if (counterAttemptsToConection < 10) {
                    bytesToSend = new byte[countBytes];
                    bytesToCreateCRC = new byte[countBytes - 2];
                    bytesToSend[0] = addressDevice;
                    bytesToSend[1] = readCommand;
                    bytesToSend[2] = 6;
                    bytesToSend[3] = 32;
                    bytesToSend[4] = 0;
                    bytesToSend[5] = 0;
                    for (int i = 0; i < bytesToCreateCRC.length; i++) {
                        bytesToCreateCRC[i] = bytesToSend[i];
                    }
                    int crc = (CRC16.getCRC4(bytesToCreateCRC));
                    int high = crc / 256;
                    bytesToSend[6] = (byte) (crc - high * 256);
                    bytesToSend[7] = (byte) high;
                    counterAttemptsToConection++;
                    try {
                        outputStream.write(bytesToSend);
                    } catch (IOException e) {}
                } else {
                    textViewConnectedToDevice.post(new Runnable() {
                        @Override
                        public void run() {
                            buttonConnectToDevice.setText("Подключить");
                            textViewConnectedToDevice.setText("Не удается связаться с процессорным модулем. Проверьте соединение.");
                            progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
                            spaceStatus.setReadyFlagToExchangeData(false);
                            spaceStatus.setDevice("");
                            getActivity().findViewById(R.id.menu_indicator).setVisibility(View.VISIBLE);
                            spaceStatus.setReadyFlagRecordingInitialValues(false);
                            bluetoothSoketThread.cancel();
                            Toast.makeText(getContext(), "Не удается связаться с процессорным модулем. Проверьте соединение.", Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        }

        private void sending(int mode) {
            if (mode == 0) {
                bytesToSend = new byte[countBytes];
                bytesToCreateCRC = new byte[countBytes - 2];
                bytesToSend[0] = addressDevice;
                bytesToSend[1] = readCommand;
                bytesToSend[2] = (byte) currentByte;
                bytesToSend[3] = 0;
                bytesToSend[4] = 0;
                bytesToSend[5] = 0;
                for (int i = 0; i < bytesToCreateCRC.length; i++) {
                    bytesToCreateCRC[i] = bytesToSend[i];
                }
                int crc = (CRC16.getCRC4(bytesToCreateCRC));
                int high = crc / 256;
                bytesToSend[6] = (byte) (crc - high * 256);
                bytesToSend[7] = (byte) high;
            } else {
                bytesToSend = new byte[countBytes + 4];
                bytesToCreateCRC = new byte[countBytes + 2];
                bytesToSend[0] = addressDevice;
                bytesToSend[1] = writeCommand;
                bytesToSend[2] = (byte) currentByte;
                bytesToSend[3] = 0;
                bytesToSend[4] = 0;
                bytesToSend[5] = 0;
                int data = spaceAddress.getAddressSpace(currentByte);
                int high = data / 256;
                bytesToSend[6] = (byte) (data - high * 256);
                bytesToSend[7] = (byte) high;
                bytesToSend[8] = 0;
                bytesToSend[9] = 0;

                for (int i = 0; i < bytesToCreateCRC.length; i++) {
                    bytesToCreateCRC[i] = bytesToSend[i];
                }
                int crc = (CRC16.getCRC4(bytesToCreateCRC));
                high = crc / 256;
                bytesToSend[10] = (byte) (crc - high * 256);
                bytesToSend[11] = (byte) high;
            }
            try {
                outputStream.write(bytesToSend);
            } catch (IOException e) {}

        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                outputStream.close();
            } catch (IOException e) { }
        }

        public void initLoad() throws IOException {
            bytesToSend = new byte[12];
            bytesToCreateCRC = new byte[bytesToSend.length - 2];
            bytesToSend[0] = addressDevice;
            bytesToSend[1] = initLoadCommand;
            byte[] bytesToSendBuf = new byte[5];
            bytesToSendBuf = determineDownloadMode();
            Log.i("LoGF", String.valueOf(bytesToSendBuf[0]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[1]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[2]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[3]));
            bytesToSend[2] = bytesToSendBuf[0];
            bytesToSend[3] = bytesToSendBuf[1];
            bytesToSend[4] = bytesToSendBuf[2];
            bytesToSend[5] = bytesToSendBuf[3];
            int i = (spaceMemory.getMemorySpaceArrayListSize() - 1)* spaceMemory.getMemorySpaceByteLength() + spaceMemory.getMemorySpaceByteLength(spaceMemory.getMemorySpaceArrayListSize() - 1);
            int highH = i/16777216;
            bytesToSend[9] = (byte) highH;
            int highL = (i - (highH*16777216))/65536;
            bytesToSend[8] = (byte) highL;
            int lowH = (i - (highH*16777216) - (highL*65536))/256;
            bytesToSend[7] = (byte) lowH;
            int lowL = i - (highH*16777216) - (highL*65536) - (lowH*256);
            bytesToSend[6] = (byte) lowL;
            for (int j = 0; j < bytesToCreateCRC.length; j++) {
                bytesToCreateCRC[j] = bytesToSend[j];
            }
            int crc = (CRC16.getCRC4(bytesToCreateCRC));
            int high = crc / 256;
            bytesToSend[10] = (byte) (crc - high * 256);
            bytesToSend[11] = (byte) high;

            flagWaitingAnswerInitLoad = true;
            outputStream.write(bytesToSend);
        }

        private byte[] determineDownloadMode() {
            String deviceSelected = spaceStatus.getDevice();
            byte[] bytesToSendBuf = new byte[5];
            if (deviceSelected.equals("TMS2812")) {
                bytesToSendBuf[0] = 0;
                bytesToSendBuf[1] = 0;
                bytesToSendBuf[2] = 10;
                bytesToSendBuf[3] = 0;
                bytesToSendBuf[4] = 10;
            } else if (deviceSelected.equals("SP2main")) {
                bytesToSendBuf[0] = 0;
                bytesToSendBuf[1] = 0;
                bytesToSendBuf[2] = 10;
                bytesToSendBuf[3] = 0;
                bytesToSendBuf[4] = 1;
            } else if (deviceSelected.equals("SP2")) {
                bytesToSendBuf[0] = 0;
                bytesToSendBuf[1] = 0;
                bytesToSendBuf[2] = 10;
                bytesToSendBuf[3] = 0;
                bytesToSendBuf[4] = 6;
            } else {
                bytesToSendBuf[0] = 0;
                bytesToSendBuf[1] = 0;
                bytesToSendBuf[2] = 10;
                bytesToSendBuf[3] = 0;
                bytesToSendBuf[4] = 10;
            }
            return bytesToSendBuf;
        }

        public void load() throws IOException {
            bytesToSend = new byte[2 + (spaceMemory.getMemorySpaceArrayListSize() - 1)* spaceMemory.getMemorySpaceByteLength() + spaceMemory.getMemorySpaceByteLength(spaceMemory.getMemorySpaceArrayListSize() - 1) + 2];
            bytesToCreateCRC = new byte[bytesToSend.length - 2];
            bytesToSend[0] = addressDevice;
            bytesToSend[1] = loadCommand;
            for (int i = 0; i < spaceMemory.getMemorySpaceArrayListSize(); i++) {
                byte[] bytesBuffer = spaceMemory.getMemorySpaceByte(i);
                for (int j = 0; j < bytesBuffer.length; j++) {
                    bytesToSend[i* spaceMemory.getMemorySpaceByteLength()+j+2] = bytesBuffer[j];
                }
            }
            for (int i = 0; i < bytesToCreateCRC.length; i++) {
                bytesToCreateCRC[i] = bytesToSend[i];
            }
            int crc = (CRC16.getCRC4(bytesToCreateCRC));
            int high = crc / 256;
            bytesToSend[bytesToSend.length-2] = (byte) (crc - high * 256);
            bytesToSend[bytesToSend.length-1] = (byte) high;
            Log.i(LOG_TAG, "Загрузка в память");

            flagWaitingAnswerLoad = true;
            outputStream.write(bytesToSend);
        }

        public void startToLoad() throws IOException {
            Log.i(LOG_TAG, "startToLoad");
            bytesToSend = new byte[18];
            bytesToCreateCRC = new byte[16];
            bytesToSend[0] = addressDevice;
            bytesToSend[1] = extendCommand;
            byte[] bytesToSendBuf = new byte[5];
            bytesToSendBuf = determineDownloadMode();
            Log.i("LoGF", String.valueOf(bytesToSendBuf[0]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[1]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[2]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[3]));
            Log.i("LoGF", String.valueOf(bytesToSendBuf[4]));
            bytesToSend[2] = bytesToSendBuf[0];
            bytesToSend[3] = bytesToSendBuf[1];
            bytesToSend[4] = bytesToSendBuf[2];
            bytesToSend[5] = bytesToSendBuf[3];
            bytesToSend[6] = 0;
            bytesToSend[7] = 0;
            bytesToSend[8] = 0;
            bytesToSend[9] = 0;
            int i = (spaceMemory.getMemorySpaceArrayListSize() - 1)* spaceMemory.getMemorySpaceByteLength() + spaceMemory.getMemorySpaceByteLength(spaceMemory.getMemorySpaceArrayListSize() - 1);
            int highH = i/16777216;
            bytesToSend[13] = (byte) highH;
            int highL = (i - (highH*16777216))/65536;
            bytesToSend[12] = (byte) highL;
            int lowH = (i - (highH*16777216) - (highL*65536))/256;
            bytesToSend[11] = (byte) lowH;
            int lowL = i - (highH*16777216) - (highL*65536) - (lowH*256);
            bytesToSend[10] = (byte) lowL;
            bytesToSend[14] = bytesToSendBuf[4];
            bytesToSend[15] = (byte) spaceStatus.getAddressOfDevice();
            for (int j = 0; j < bytesToCreateCRC.length; j++) {
                bytesToCreateCRC[j] = bytesToSend[j];
            }
            int crc = (CRC16.getCRC4(bytesToCreateCRC));
            int high = crc / 256;
            bytesToSend[bytesToSend.length-2] = (byte) (crc - high * 256);
            bytesToSend[bytesToSend.length-1] = (byte) high;

//            spaceStatus.setStatusProcessOfUpdatingSoftware(true);

            outputStream.write(bytesToSend);
            flagWaitingAnswerFinishLoad = true;
        }

        public void downloadLogs() throws IOException {
            bytesToSend = new byte[12];
            bytesToCreateCRC = new byte[bytesToSend.length - 2];
            bytesToSend[0] = addressDevice;
            bytesToSend[1] = uploadCommand;
            bytesToSend[2] = 0;
            bytesToSend[3] = 0;
            bytesToSend[4] = 10;
            bytesToSend[5] = 0;
            bytesToSend[9] = 0;
            bytesToSend[8] = 0;
            bytesToSend[7] = 0;
            bytesToSend[6] = 8;
            for (int j = 0; j < bytesToCreateCRC.length; j++) {
                bytesToCreateCRC[j] = bytesToSend[j];
            }
            int crc = (CRC16.getCRC4(bytesToCreateCRC));
            int high = crc / 256;
            bytesToSend[bytesToSend.length-2] = (byte) (crc - high * 256);
            bytesToSend[bytesToSend.length-1] = (byte) high;
            Log.i(LOG_TAG, "Команда на чтение 8 байт");

            flagWaitingAnswerUpload = true;
            outputStream.write(bytesToSend);
        }
    }
}

//while (!isInterrupted()) {
//        switch (test3()) {
//        case 1:
//        buffer = null;
//        buffer = new byte[6];  // buffer store for the stream
//        try {
//        bytes = inputStream.read(buffer);
//        } catch (IOException e) {
//        e.printStackTrace();
//        }
//        bytesToCreateCRC = new byte[bytes - 4];
//        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//        bytesToCreateCRC[i] = buffer[i];
//        }
//        crc = (CRC16.getCRC4(bytesToCreateCRC));
//        high = crc / 256;
//        if ((buffer[2] == (byte) (crc - high * 256)) & (buffer[3] == (byte) high)) {
////                            Log.i(LOG_TAG, "CRC is good from InitLoad");
//        } else {
////                            Log.i(LOG_TAG, "CRC is bed from InitLoad");
//        }
//        answerTest = "";
//        for (byte readByte : buffer) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256;
//        else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
////                        Log.i(LOG_TAG, answerTest);
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
//        break;
//        case 2:
//        buffer = null;
//        buffer = new byte[4];  // buffer store for the stream
//        try {
//        bytes = inputStream.read(buffer);
//        } catch (IOException e) {
//        e.printStackTrace();
//        }
//
//
//        bytesToCreateCRC = new byte[2];
//        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//        bytesToCreateCRC[i] = buffer[i];
//        }
//        crc = (CRC16.getCRC4(bytesToCreateCRC));
//        high = crc / 256;
//        if ((buffer[2] == (byte) (crc - high * 256)) & (buffer[3] == (byte) high)) {
////                            Log.i(LOG_TAG, "CRC is good from Load");
//        spaceStatus.setReadyFlagToFinishOfLoadingSoftware(true);
//        } else {
////                            Log.i(LOG_TAG, "CRC is bed from Load");
//        }
//        answerTest = "";
//        for (byte readByte : buffer) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256;
//        else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
////                        Log.i(LOG_TAG, answerTest);
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
//        break;
//        case 3:
////                        Log.i(LOG_TAG, "answerTest");
//        test("waite");
//        buffer = null;
//        buffer = new byte[18];  // buffer store for the stream
//        try {
//        bytes = inputStream.read(buffer);
//        } catch (IOException e) {
//        e.printStackTrace();
//        }
////                        Log.i(LOG_TAG, String.valueOf(bytes));
//        test(String.valueOf(bytes));
//        bytesToCreateCRC = new byte[bytes - 4];
//        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//        bytesToCreateCRC[i] = buffer[i];
//        }
//        crc = (CRC16.getCRC4(bytesToCreateCRC));
//        high = crc / 256;
//        if ((buffer[14] == (byte) (crc - high * 256)) & (buffer[15] == (byte) high)) {
////                            Log.i(LOG_TAG, "CRC is good from FinishLoad");
//        test("CRC is good from FinishLoad");
//        } else {
////                            Log.i(LOG_TAG, "CRC is bed from FinishLoad");
//        test("CRC is bed from FinishLoad");
//        }
//        spaceStatus.setLastNumberError(buffer[6]);
//        spaceStatus.setReadyFlagToFinishOfUpdatingSoftware(true);
//        answerTest = "";
//        for (byte readByte : buffer) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256;
//        else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
////                        Log.i(LOG_TAG, answerTest);
//        test(answerTest);
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
//        break;
//        case 4:
//        buffer = null;
//        buffer = new byte[20];
//        bufferPrepeared = null;
//        bufferPrepeared = new byte[20];
//        lastByte = 0;
//        bytes = 0;
//        while (lastByte != buffer.length) {
//        try {
//        bytes = inputStream.read(buffer, 0, buffer.length - bytes);
//        for (int i = 0; i < bytes; i++) {
//        if (lastByte + i < bufferPrepeared.length) {
//        bufferPrepeared[lastByte + i] = buffer[i];
//        } else break;
//        }
//        lastByte = lastByte + bytes;
//        } catch (IOException ex) {
//        ex.printStackTrace();
//        }
//        if (lastByte > buffer.length) {
//        break;
//        }
//        }
//
////                        Log.i(LOG_TAG, String.valueOf(lastByte));
//        test(String.valueOf(lastByte));
//
////                            bytesToCreateCRC = new byte[bytes-4];
////                            for (int i = 0; i < bytesToCreateCRC.length; i++) {
////                                bytesToCreateCRC[i] = buffer[i];
////                            }
////                            int crc = (CRC16.getCRC4(bytesToCreateCRC));
////                            int high = crc/256;
////                            if ((buffer[2] == (byte) (crc - high*256)) & (buffer[3] == (byte) high)) {
////                                Log.i("LOG_TAG_1", "CRC is good from InitLoad");
////                            } else {
////                                Log.i("LOG_TAG_1", "CRC is bed from InitLoad");
////                            }
//
//        answerTest = "";
//        for (byte readByte: bufferPrepeared) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
////                        Log.i(LOG_TAG, answerTest);
//        test(answerTest);
//        spaceStatus.setReadyFlagToFinishOfDownloadingLogs(true);
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
//        break;
//        case 5:
////                        Log.i(LOG_TAG, "Читаем в цикле");
//        test("Читаем в цикле");
//        buffer = null;
//        buffer = new byte[10];  // buffer store for the stream
//        //                            inputStream.read(buffer);
//        bufferPrepeared = null;
//        bufferPrepeared = new byte[10];
//        lastByte = 0;
//        bytes = 0;
//        int countToBreakeFromWhile = 0;
//        while (lastByte != 10) {
////                            Log.i(LOG_TAG, "Проверяем lastByte != buffer.length");
//        test("Проверяем lastByte != buffer.length");
//        try {
//        bytes = inputStream.read(buffer);
//        for (int i = 0; i < bytes; i++) {
//        if (lastByte + i < 10) {
//        bufferPrepeared[lastByte + i] = buffer[i];
//        } else break;
//        }
//        lastByte = lastByte + bytes;
////                                Log.i(LOG_TAG, String.valueOf(lastByte));
//        test(String.valueOf(lastByte));
//        } catch (IOException ex) {
////                                Log.i(LOG_TAG, "Здесь мы чтоли??");
//        test("Здесь мы чтоли??");
//        ex.printStackTrace();
//        }
//        if (lastByte > 10) {
//        break;
//        }
//        countToBreakeFromWhile++;
//        if (countToBreakeFromWhile > 2) {
////                                Log.i(LOG_TAG, "Вышел по условию countToBreakeFromWhile > 2");
//        test("Вышел по условию countToBreakeFromWhile > 2");
//        break;
//        }
//        }
//        test(String.valueOf(lastByte));
//        test(String.valueOf(lastByte));
//
//        if (lastByte == bufferPrepeared.length) {
//        bytes = lastByte;
//        bytesFromBuffer = new byte[bytes];
//        bytesToCreateCRC = new byte[bytes - 2];
//        for (int i = 0; i < bytesFromBuffer.length; i++) {
//        bytesFromBuffer[i] = bufferPrepeared[i];
//        }
//        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//        bytesToCreateCRC[i] = bytesFromBuffer[i];
//        }
//        crc = (CRC16.getCRC4(bytesToCreateCRC));
//        high = crc/256;
//        answerTest = "";
//        for (byte readByte: bytesFromBuffer) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
////                            Log.i(LOG_TAG, answerTest);
//        test(answerTest);
//        if ((bytesFromBuffer[bytesToCreateCRC.length] == (byte) (crc - high*256)) & (bytesFromBuffer[bytesToCreateCRC.length + 1] == (byte) high)) {
//        spaceAddress.setAddressSpace(currentByte, bytesFromBuffer[2]);
//
//
//        if (currentByte == 47) {
//        nextByte = 96;
//        }
//
//        if (currentByte == 143) {
//        nextByte = 208;
//        }
//
//
//        if (currentByte == 255) {
//        nextByte = 0;
//        }
//
//
//        if ((currentByte == 47) || (currentByte == 143) || (currentByte == 255)) currentByte = nextByte;
//        else currentByte++;
//
//        changeStateIndicator();
//        isStatusError = false;
//        } else {
////                                Log.i(LOG_TAG, "CRC не совпало");
//        test("LOG_TAG, CRC не совпало");
//        textViewConnectedToDevice.setText("CRC не совпало");
//        isStatusError = true;
//        }
//        } else {
//        isStatusError = true;
//        }
//
//
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
//        break;
//        case 6:
////                        Log.i(LOG_TAG, "Пишем задание");
//        test("Пишем задание");
//        buffer = null;
//        buffer = new byte[6];  // buffer store for the stream
//        try {
//        bytes = inputStream.read(buffer);
//        } catch (IOException ex) {
//        ex.printStackTrace();
//        }
////                        Log.i(LOG_TAG, String.valueOf(bytes));
//        test(String.valueOf(bytes));
//        if (bytes == buffer.length) {
//        bytesFromBuffer = new byte[bytes];
//        bytesToCreateCRC = new byte[bytes - 4];
//        for (int i = 0; i < bytesFromBuffer.length; i++) {
//        bytesFromBuffer[i] = buffer[i];
//        }
//        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//        bytesToCreateCRC[i] = bytesFromBuffer[i];
//        }
//        crc = (CRC16.getCRC4(bytesToCreateCRC));
//        high = crc/256;
//
//        answerTest = "";
//        for (byte readByte: bytesFromBuffer) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
//        test(answerTest);
//
//        if ((bytesFromBuffer[bytesToCreateCRC.length] == (byte) (crc - high*256)) & (bytesFromBuffer[bytesToCreateCRC.length + 1] == (byte) high)) {
//        if (currentByte == 207) {
//        spaceStatus.setReadyFlagRecordingInitialValues(false);
//        nextByte = 0;
//        }
//        if (currentByte == 95) {
//        nextByte = 144;
//        }
//        if ((currentByte == 95) || (currentByte == 207)) currentByte = nextByte;
//        else currentByte++;
//
//        changeStateIndicator();
//        isStatusError = false;
//        } else {
////                                Log.i(LOG_TAG, "ЧУДЕСА");
//        test("ЧУДЕСА");
//        textViewConnectedToDevice.setText("CRC не совпало");
//        isStatusError = true;
//        }
//        } else {
//        isStatusError = true;
//        }
//
//
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
////                        Log.i(LOG_TAG, "Сколько раз мы здесь???");
//        test("Сколько раз мы здесь???");
//        break;
//        case 7:
//        buffer = null;
//        buffer = new byte[10];  // buffer store for the stream
//        try {
//        bytes = inputStream.read(buffer);
//        } catch (IOException ex) {
//        ex.printStackTrace();
//        }
//        Log.i(LOG_TAG, String.valueOf(bytes));
//        if (bytes == buffer.length) {
//        bytesFromBuffer = new byte[bytes];
//        bytesToCreateCRC = new byte[bytes - 2];
//        for (int i = 0; i < bytesFromBuffer.length; i++) {
//        bytesFromBuffer[i] = buffer[i];
//        }
//        for (int i = 0; i < bytesToCreateCRC.length; i++) {
//        bytesToCreateCRC[i] = bytesFromBuffer[i];
//        }
//        crc = (CRC16.getCRC4(bytesToCreateCRC));
//        high = crc/256;
//        if ((bytesFromBuffer[bytesToCreateCRC.length] == (byte) (crc - high*256)) & (bytesFromBuffer[bytesToCreateCRC.length + 1] == (byte) high)) {
//        textViewConnectedToDevice.setText("Поключено к " + stringConnectedToDevice);
//        progressBarConnectedToDevice.setVisibility(View.INVISIBLE);
//        spaceStatus.setReadyFlagToExchangeData(true);
//        answerTest = "";
//        for (byte readByte: bytesFromBuffer) {
//        int bufInt = 0;
//        if (readByte < 0) bufInt = readByte + 256; else bufInt = readByte;
//        answerTest = answerTest + " " + bufInt;
//        }
////                                Log.i(LOG_TAG, answerTest);
//        test(answerTest);
//        } else {
////                                Log.i(LOG_TAG, "CRC не совпало");
//        test("CRC не совпало");
//        textViewConnectedToDevice.setText("CRC не совпало");
//        }
//        } else {
////                            isStatusError = true;
//        }
//
//
////                        stateWaitingAnswer = 0;
////                        isStatusReading = true;
//        break;
////                    default:
////                        try {
////                            bluetoothConnectedThread.sleep(1);
////                        } catch (InterruptedException e) {
////                            e.printStackTrace();
////                        }
////                        break;
//
//        }
//        test2(0);
//        test5(true);
//        }

