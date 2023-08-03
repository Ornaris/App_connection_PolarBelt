package com.designv2;

import static androidx.core.app.ActivityCompat.requestPermissions;
import static androidx.core.content.ContextCompat.getSystemService;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint;
import com.facebook.react.defaults.DefaultReactActivityDelegate;

import expo.modules.ReactActivityDelegateWrapper;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;

public class MainActivity extends ReactActivity {

  private static final String TAG = "Polar_MainActivity";
  private static final String SHARED_PREFS_KEY = "polar_device_id";
  private static final int PERMISSION_REQUEST_CODE = 1;

  private SharedPreferences sharedPreferences;
  private final ActivityResultLauncher<Intent> bluetoothOnActivityResultLauncher = registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          (ActivityResult result) -> {
            if (result.getResultCode() != Activity.RESULT_OK) {
              Log.w(TAG, "Bluetooth off");
            }
          }
  );

  private String deviceId = "9E265923";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // Set the theme to AppTheme BEFORE onCreate to support 
    // coloring the background, status bar, and navigation bar.
    // This is required for expo-splash-screen.
    setTheme(R.style.AppTheme);
   // super.onCreate(null);


    super.onCreate(savedInstanceState);
    sharedPreferences = getPreferences(MODE_PRIVATE);
    deviceId = sharedPreferences.getString(SHARED_PREFS_KEY, "");

  }
/*
  private void  onClickChangeID(View view) {
    showDialog(view);
  }
*/
  /**
   * Returns the name of the main component registered from JavaScript.
   * This is used to schedule rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "main";
  }

  /**
   * Returns the instance of the {@link ReactActivityDelegate}. Here we use a util class {@link
   * DefaultReactActivityDelegate} which allows you to easily enable Fabric and Concurrent React
   * (aka React 18) with two boolean flags.
   */
  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new ReactActivityDelegateWrapper(this, BuildConfig.IS_NEW_ARCHITECTURE_ENABLED, new DefaultReactActivityDelegate(
            this,
            getMainComponentName(),
            // If you opted-in for the New Architecture, we enable the Fabric Renderer.
            DefaultNewArchitectureEntryPoint.getFabricEnabled(), // fabricEnabled
            // If you opted-in for the New Architecture, we enable Concurrent React (i.e. React 18).
            DefaultNewArchitectureEntryPoint.getConcurrentReactEnabled() // concurrentRootEnabled
    ));
  }

  /**
   * Align the back button behavior with Android S
   * where moving root activities to background instead of finishing activities.
   *
   * @see <a href="https://developer.android.com/reference/android/app/Activity#onBackPressed()">onBackPressed</a>
   */
  @Override
  public void invokeDefaultOnBackPressed() {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
      if (!moveTaskToBack(false)) {
        // For non-root activities, use the default implementation to finish them.
        super.invokeDefaultOnBackPressed();
      }
      return;
    }

    // Use the default back button implementation on Android S
    // because it's doing more than {@link Activity#moveTaskToBack} in fact.
    super.invokeDefaultOnBackPressed();
  }

/*
  private void showDialog(View view) {
    AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.PolarTheme);
    dialog.setTitle("Enter your Polar device's ID");
    View viewInflated = LayoutInflater.from(getApplicationContext()).inflate(R.layout.device_id_dialog_layout, (ViewGroup) view.getRootView(), false);
    EditText input = viewInflated.findViewById(R.id.input);
    if (deviceId != null && !deviceId.isEmpty()) {
      input.setText(deviceId);
    }
    input.setInputType(InputType.TYPE_CLASS_TEXT);
    dialog.setView(viewInflated);
    dialog.setPositiveButton("OK", (DialogInterface dialogInterface, int i) -> {
      deviceId = input.getText().toString().toUpperCase();
      SharedPreferences.Editor editor = sharedPreferences.edit();
      editor.putString(SHARED_PREFS_KEY, deviceId);
      editor.apply();
    });
    dialog.setNegativeButton("Cancel", (DialogInterface dialogInterface, int i) -> dialogInterface.cancel());
    dialog.show();
  }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      if (requestCode == PERMISSION_REQUEST_CODE) {
        for (int index = 0; index <= grantResults.length - 1; index++) {
          if (grantResults[index] == PackageManager.PERMISSION_DENIED) {
            Log.w(TAG, "Needed permissions are missing");
            //showToast("Needed permissions are missing");
            return;
          }
        }
        Log.d(TAG, "Needed permissions are granted");
      }
    }
*/
}





