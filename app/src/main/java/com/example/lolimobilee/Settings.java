package com.example.lolimobilee;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Collections;
import java.util.UUID;

public class Settings extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_BLUETOOTH_SCAN_PERMISSION = 2;
    private static final int REQUEST_BLUETOOTH_CONNECT_PERMISSION = 3;

    private static final UUID SERVICE_UUID = UUID.fromString("12345678-1234-5678-1234-56789abcdef0");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("87654321-4321-6789-4321-56789abcdef1");
    private static final UUID VOLUME_CHARACTERISTIC_UUID = UUID.fromString("87654321-4321-6789-4321-56789abcdef2"); // Define UUID for volume characteristic

    private Button btnConnectPi;
    private EditText etWifiSsid;
    private EditText etWifiPassword;
    private SeekBar volumeSeekBar;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.d(TAG, "Found device: " + device.getName() + " [" + device.getAddress() + "]");

            if (result.getScanRecord() != null && result.getScanRecord().getServiceUuids() != null &&
                    result.getScanRecord().getServiceUuids().contains(new ParcelUuid(SERVICE_UUID))) {
                Log.d(TAG, "Device has the correct service UUID");
                stopBluetoothScan();
                connectToDevice(device);
            } else {
                Log.d(TAG, "Device does not have the expected service UUID");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "BLE Scan failed with error: " + errorCode);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnConnectPi = findViewById(R.id.btnConnectPi);
        etWifiSsid = findViewById(R.id.etWifiSsid);
        etWifiPassword = findViewById(R.id.etWifiPassword);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        btnConnectPi.setOnClickListener(v -> startBluetoothScan());
    }

    private void startBluetoothScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(SERVICE_UUID)).build();
                ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
                bleScanner.startScan(Collections.singletonList(filter), settings, scanCallback);
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to start BLE scan due to missing permissions", e);
                Toast.makeText(this, "Failed to start BLE scan: permissions denied.", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_BLUETOOTH_SCAN_PERMISSION);
        }
    }

    private void stopBluetoothScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            try {
                bleScanner.stopScan(scanCallback);
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to stop BLE scan due to missing permissions", e);
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            Log.d(TAG, "Connected to GATT server");
                            gatt.discoverServices();
                        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                            Log.d(TAG, "Disconnected from GATT server");
                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            BluetoothGattService service = gatt.getService(SERVICE_UUID);
                            if (service != null) {
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                                if (characteristic != null) {
                                    sendWifiCredentials(gatt, characteristic);
                                } else {
                                    Log.e(TAG, "Characteristic not found");
                                }

                                BluetoothGattCharacteristic volumeCharacteristic = service.getCharacteristic(VOLUME_CHARACTERISTIC_UUID);
                                if (volumeCharacteristic != null) {
                                    setupVolumeControl(gatt, volumeCharacteristic);
                                } else {
                                    Log.e(TAG, "Volume characteristic not found");
                                }
                            } else {
                                Log.e(TAG, "Service not found");
                            }
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to connect to device due to missing permissions", e);
                Toast.makeText(this, "Failed to connect: permissions denied.", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_CONNECT_PERMISSION);
        }
    }

    private void setupVolumeControl(BluetoothGatt gatt, BluetoothGattCharacteristic volumeCharacteristic) {
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendVolumeLevel(gatt, volumeCharacteristic, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void sendVolumeLevel(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int volumeLevel) {
        byte[] volumeData = new byte[]{(byte) volumeLevel};

        characteristic.setValue(volumeData);
        boolean success = gatt.writeCharacteristic(characteristic);
        if (success) {
            Log.d(TAG, "Volume level sent: " + volumeLevel);
        } else {
            Log.e(TAG, "Failed to send volume level");
        }
    }

    private void sendWifiCredentials(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String ssid = etWifiSsid.getText().toString();
        String password = etWifiPassword.getText().toString();
        String credentials = ssid + "," + password;
        characteristic.setValue(credentials.getBytes());

        boolean success = gatt.writeCharacteristic(characteristic);
        if (success) {
            Toast.makeText(this, "Credentials sent successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Wi-Fi credentials sent");
        } else {
            Toast.makeText(this, "Failed to send credentials", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to write characteristic");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_SCAN_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothScan();
            } else {
                Toast.makeText(this, "Bluetooth scan permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_BLUETOOTH_CONNECT_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothGatt != null) {
                    connectToDevice(bluetoothGatt.getDevice());
                }
            } else {
                Toast.makeText(this, "Bluetooth connect permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
