package com.designv2;

import static android.content.Context.BLUETOOTH_SERVICE;
import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.content.ContextCompat.getSystemService;
import static java.lang.Integer.sum;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;

import com.facebook.react.bridge.Callback;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiCallback;
import com.polar.sdk.api.PolarBleApiDefaultImpl;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.model.PolarHrData;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.widget.TextView;

import com.facebook.react.bridge.Promise;

import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;


public class HRActivityModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private static final int PERMISSION_REQUEST_CODE = 1;
    //private HRActivityJava hrActivity;
    private static final String TAG = "HRActivityJava";
    private boolean isInitialized = false;
    private boolean isInitializing = false;

    private boolean isConnected = false;

    private PolarBleApi api;
    public String deviceId;
    private Disposable hrDisposable;
    private TextView textViewRR;
    private int HeartRateValue = 0;

    HRActivityModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;

        Set<PolarBleApi.PolarBleSdkFeature> sdkFeatures = new HashSet<>(
                Arrays.asList(
                        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                        PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                        PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
                )
        );

        api = PolarBleApiDefaultImpl.defaultImplementation(
                reactContext.getApplicationContext(),
                sdkFeatures
        );

        api.setApiLogger(str -> Log.d("SDK", str));
        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean powered) {
                Log.d(TAG, "BluetoothStateChanged " + powered);
            }

            @Override
            public void deviceConnected(PolarDeviceInfo polarDeviceInfo) {
                deviceId = polarDeviceInfo.getDeviceId();
                Log.d(TAG, "Device connected " + polarDeviceInfo.getDeviceId());
                Toast.makeText(reactContext.getApplicationContext(), "Connected: " + " " + polarDeviceInfo.getDeviceId(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG, "Device connecting " + polarDeviceInfo.getDeviceId());
                Toast.makeText(reactContext.getApplicationContext(), R.string.connecting, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG, "Device disconnected " + polarDeviceInfo.getDeviceId());
                Toast.makeText(reactContext.getApplicationContext(), R.string.disconnect, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void bleSdkFeatureReady(String identifier, PolarBleApi.PolarBleSdkFeature feature) {
                Log.d(TAG, "feature ready " + feature);

                switch (feature) {
                    case FEATURE_POLAR_ONLINE_STREAMING:

                        Log.d(TAG, "switch feature");
                        break;
                    default:
                        //nothing
                        break;
                }
            }
/*
            @Override
            public void disInformationReceived(String identifier, UUID uuid, String value) {
                if (uuid.equals(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))) {
                    String msg = "Firmware: " + value.trim();
                    Log.d(TAG, "Firmware: " + identifier + " " + value.trim());
                    textViewFwVersion.append(msg);
                }
            }

            @Override
            public void batteryLevelReceived(String identifier, int level) {
                Log.d(TAG, "Battery level " + identifier + " " + level + "%");
                String batteryLevelText = "Battery level: " + level + "%";
                textViewBattery.append(batteryLevelText);
            }
*/
            @Override
            public void hrNotificationReceived(String identifier, PolarHrData.PolarHrSample data) {
                // Deprecated
            }

            @Override
            public void polarFtpFeatureReady(String identifier) {
                // Deprecated
            }

            @Override
            public void streamingFeaturesReady(String identifier, Set<? extends PolarBleApi.PolarDeviceDataType> features) {
                // Deprecated
            }

            @Override
            public void hrFeatureReady(String identifier) {
                // Deprecated
            }
        });
    }

    @NonNull
    @Override
    public String getName() {
        return "HRActivityModule";
    }



    @ReactMethod
    public void initializeHRActivity(final Promise promise) {

        //Toast.makeText(reactContext, "ENTER INIT", Toast.LENGTH_LONG).show();
        if (isInitializing) {
            Toast.makeText(reactContext, "initializeHRActivity: isInitializing", Toast.LENGTH_LONG).show();
            promise.reject("Already initializing", "HRActivityModule is already being initialized.");
            return;
        }

        if (isInitialized) {
            Toast.makeText(reactContext, "initializeHRActivity: isInitialized", Toast.LENGTH_LONG).show();
            promise.resolve("HRActivityModule is already initialized.");
            return;
        }

        final Activity activity = getCurrentActivity();
        if (activity != null) {
            isInitializing = true;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //hrActivity = new HRActivityJava();
                    if(api==null) {
                        Toast.makeText(reactContext, "initializeHRActivity: the object is still null.", Toast.LENGTH_LONG).show();
                        isInitializing = false;
                        promise.reject("Null Init", "Null INIT. Problem to initialize HRActivityModule.");
                    }
                    else{
                        isInitialized = true;
                        isInitializing = false;
                        Toast.makeText(reactContext, "HRActivityModule initialized successfully.", Toast.LENGTH_LONG).show();
                        promise.resolve("HRActivityModule initialized successfully.");
                    }
                    // any other UI-related operations can be performed here if needed

                }
            });
        } else {
            Toast.makeText(reactContext, "Activity is null", Toast.LENGTH_LONG).show();
            promise.reject("Activity is null", "Activity is null. Unable to initialize HRActivityModule.");
            //Toast.makeText(reactContext, "HRActivityModule failed initialisation", Toast.LENGTH_LONG).show();
        }
    }


    @ReactMethod
    public void show() {
        Toast.makeText(reactContext, "HRActivityModule here", Toast.LENGTH_LONG).show();
    }

    @ReactMethod
    public boolean IsActivated() {
        if (isInitialized == true) {
            Toast.makeText(reactContext, "HRActivityModule has been activated", Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(reactContext, "HRActivityModule is null", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @ReactMethod
    public boolean IsInActivity() {
        if (api == null) {
            Toast.makeText(reactContext, "IsInActivity: No, no Activity detected", Toast.LENGTH_LONG).show();
            return false;
        } else {
            Toast.makeText(reactContext, "IsInActivity: Yes, some Activity", Toast.LENGTH_LONG).show();
            return true;
        }
    }

    @ReactMethod
    public void TestHr(boolean start_stop, Promise promise) {
        if (!isInitialized) {
            Toast.makeText(reactContext, "TestHr: Not initialized yet", Toast.LENGTH_LONG).show();
            //  promise.reject("Stream error");
        } else {
            //  if(start_stop) {
            Toast.makeText(reactContext, "initialized, attempt to stream", Toast.LENGTH_LONG).show();
            // Already initialized, proceed with starting the HR data stream
            try {
                // Start the HR data stream (assuming the streamHR() function is already available)
                Toast.makeText(reactContext, "In the try", Toast.LENGTH_LONG).show();
               // hrActivity.streamHR();
                promise.resolve("HR data stream started successfully.");
                //Toast.makeText(reactContext, "The Stream has begun", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                promise.reject("Stream error", e.getMessage());
                Toast.makeText(reactContext, "Stream error", Toast.LENGTH_LONG).show();
            }
        }
    }

    @ReactMethod
    public void checkBT() {
        Toast.makeText(reactContext, "BT", Toast.LENGTH_LONG).show();
        BluetoothManager btManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = btManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(reactContext, "BT: BTAdapter null", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(reactContext, "BT: Activation", Toast.LENGTH_LONG).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //bluetoothOnActivityResultLauncher.launch(enableBtIntent);
            if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            getCurrentActivity().startActivity(enableBtIntent);
        }

        //It seems it is about managing different android versions, for now I put it in comments (I don t really understand what is done here)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getCurrentActivity().requestPermissions( new String[]{android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
            } else {
                getCurrentActivity().requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            }
        } else {
            getCurrentActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        }

       // Toast.makeText(reactContext, "BT: end ", Toast.LENGTH_LONG).show();
    }

    @ReactMethod
    public void connectDevice(String DeviceName, final Promise promise) {
        if (api != null) {
            try {
                deviceId = DeviceName;
                api.connectToDevice(deviceId); //"9E265923"
                promise.resolve("Device connection initiated.");
            } catch (PolarInvalidArgument e) {
                promise.reject("Connection error", e.getMessage());
            }
        } else {
            promise.reject("HRActivityJava instance not found.", "HRActivityJava instance is null. Please initialize HRActivityModule first.");
        }
    }

    @ReactMethod
    public void disconnectDevice(final Promise promise) {
        if (api != null) {
            try {
                api.disconnectFromDevice(deviceId);
                promise.resolve("Device disconnected");
            } catch (PolarInvalidArgument e) {
                promise.reject(" Error disconnection", e.getMessage());
            }
        } else {
            promise.reject("HRActivityJava instance not found.", "HRActivityJava instance is null. Please initialize HRActivityModule first.");
        }
    }

    @ReactMethod
    public int getHeartRateValue() {
        return HeartRateValue;
    }

    @ReactMethod
    public void SetPolarId(String NewID) {
        deviceId = NewID;
    }

    @ReactMethod
    public String GetPolarId() {
        return deviceId;
    }

    @ReactMethod
    public void streamHR(final Promise promise) {
        // Check if the API is initialized
        if (api == null) {
            Toast.makeText(reactContext, "streamHR: api null", Toast.LENGTH_LONG).show();
            promise.reject("HRActivityJava instance not found.", "HRActivityJava instance is null. Please initialize HRActivityModule first.");
            return;
        }

        // Check if the device is connected
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(reactContext, "streamHR: deviceId null or empty", Toast.LENGTH_LONG).show();
            promise.reject("Device not connected.", "Please connect to a device before streaming heart rate data.");
            return;
        }

        // Start or stop the heart rate streaming
        if (hrDisposable == null || hrDisposable.isDisposed()) {
            // Start the heart rate streaming
            hrDisposable = api.startHrStreaming(deviceId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            (hrData) -> {
                                //Toast.makeText(reactContext, "streamHR: getting samples", Toast.LENGTH_LONG).show();
                                // Process the heart rate data here
                                for (PolarHrData.PolarHrSample sample : hrData.getSamples()) {
                                    // Get the heart rate value from the sample and update the HeartRateValue variable
                                    HeartRateValue = sample.getHr();
                                    Toast.makeText(reactContext, "streamHR: sample = " + sample.getHr() , Toast.LENGTH_LONG).show();
                                }
                            },
                            (error) -> {
                                // Handle the error if heart rate streaming fails
                                promise.reject("Heart rate streaming error", error.getMessage());
                            },
                            () -> {
                                // Called when heart rate streaming completes (not used in this example)
                            }
                    );

            // Resolve the promise indicating that heart rate streaming has started
            promise.resolve("Heart rate streaming started successfully.");
        } else {
            // Stop the heart rate streaming
            hrDisposable.dispose();
            hrDisposable = null;

            // Resolve the promise indicating that heart rate streaming has stopped
            promise.resolve("Heart rate streaming stopped.");
        }
    }


}

