package com.udayraj.vehicleinfolive.utils;

import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;


public class SimplePermissions {
    private static final String TAG = "SimplePermissions";
    private static final int MY_PERMISSIONS_REQUEST_TOKEN= 101;
    private String[] PermissionsList;
    private AppCompatActivity activity;
    public SimplePermissions(AppCompatActivity activity, String[] PermissionsList) {
        this.activity = activity;
        this.PermissionsList = PermissionsList;
    }
    public void grantPermissions() {
        if(!hasAllPermissions()) {
            Log.d(TAG, "Asking runtime Permissions");
        }
        // This runs in a separate thread : Not necessarily prompts the user beforehand!
        ActivityCompat.requestPermissions(this.activity, this.PermissionsList, MY_PERMISSIONS_REQUEST_TOKEN);
    }

    public boolean hasAllPermissions() {
        for (String permission : this.PermissionsList) {
            if (ActivityCompat.checkSelfPermission(this.activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
