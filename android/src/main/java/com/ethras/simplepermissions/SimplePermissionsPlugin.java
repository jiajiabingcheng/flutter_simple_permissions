package com.ethras.simplepermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * SimplePermissionsPlugin
 */
public class SimplePermissionsPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    private Result result;
    private Activity activity;

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "simple_permissions");
        SimplePermissionsPlugin simplePermissionsPlugin = new SimplePermissionsPlugin(registrar.activity());
        channel.setMethodCallHandler(simplePermissionsPlugin);
        registrar.addRequestPermissionsResultListener(simplePermissionsPlugin);
    }

    private SimplePermissionsPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        String method = call.method;
        String permission;
        switch (method) {
            case "getPlatformVersion":
                result.success("Android " + android.os.Build.VERSION.RELEASE);
                break;
            case "getPermissionStatus":
                permission = call.argument("permission");
                int value = checkPermission(permission) ? 3 : 2;
                result.success(value);
                break;
            case "checkPermission":
                permission = call.argument("permission");
                result.success(checkPermission(permission));
                break;
            case "requestPermission":
                permission = call.argument("permission");
                this.result = result;
                requestPermission(permission);
                break;
            case "openSettings":
                openSettings();
                result.success(true);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    private String getManifestPermission(String permission) {
        String res;
        switch (permission) {
            case "RECORD_AUDIO":
                res = Manifest.permission.RECORD_AUDIO;
                break;
            case "CAMERA":
                res = Manifest.permission.CAMERA;
                break;
            case "WRITE_EXTERNAL_STORAGE":
                res = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
            case "READ_EXTERNAL_STORAGE":
                res = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
            case "ACCESS_COARSE_LOCATION":
                res = Manifest.permission.ACCESS_COARSE_LOCATION;
                break;
            case "WHEN_IN_USE_LOCATION":
            case "ACCESS_FINE_LOCATION":
            case "ALWAYS_LOCATION":
                res = Manifest.permission.ACCESS_FINE_LOCATION;
                break;
            case "READ_CONTACTS":
                res = Manifest.permission.READ_CONTACTS;
                break;
            case "VIBRATE":
                res = Manifest.permission.VIBRATE;
                break;
            case "WRITE_CONTACTS":
                res = Manifest.permission.WRITE_CONTACTS;
                break;
            default:
                res = "ERROR";
                break;
        }
        return res;
    }

    private void requestPermission(String permission) {
        permission = getManifestPermission(permission);
        Log.i("SimplePermission", "Requesting permission : " + permission);
        String[] perm = {permission};
        activity.requestPermissions(perm, 0);
    }

    private boolean checkPermission(String permission) {
        permission = getManifestPermission(permission);
        Log.i("SimplePermission", "Checking permission : " + permission);
        return PackageManager.PERMISSION_GRANTED == activity.checkSelfPermission(permission);
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] grantResults) {
        boolean res = false;
        if (requestCode == 0 && grantResults.length > 0) {
            res = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.i("SimplePermission", "Requesting permission result : " + res);
            result.success(res);
        }
        return res;
    }
}
