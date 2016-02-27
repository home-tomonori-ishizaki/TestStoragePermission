package com.example.teststoragepermission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;

public class MainActivity extends Activity {

    private static final int REQUEST_ID_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String externalDir = getSdPath(this);
        TextView textExternalDir = (TextView)findViewById(R.id.textExternalDir);
        textExternalDir.setText(externalDir);

        String usbDir = getUsbPath(this);
        if (usbDir == null) {
            usbDir = "USB is not mounted";
        }
        TextView textUsb = (TextView)findViewById(R.id.textUSB);
        textUsb.setText(usbDir);

        Button buttonSd = (Button)findViewById(R.id.buttonExternalDir);
        buttonSd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testWrite(false);
            }
        });

        Button buttonUsb = (Button)findViewById(R.id.buttonUSB);
        buttonUsb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testWrite(true);
            }
        });

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_ID_PERMISSION);
        }
    }

    private void testWrite(boolean isUsb) {
        String filename = isUsb ? getUsbPath(this) : getSdPath(this);
        if (filename == null) {
            Toast.makeText(this, "not found", Toast.LENGTH_SHORT).show();
            return;
        }

        filename += "/test.data";
        boolean status = true;
        OutputStream outputStream = null;
        try {
            outputStream  = new FileOutputStream(new File(filename));
            outputStream.write("hogehoge".getBytes());
        } catch (Exception e) {
            status = false;
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    status = false;
                }
            }
        }

        Toast.makeText(this, status ? "success" : "fail", Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_ID_PERMISSION) {
            return;
        }
    }

    private String getSdPath(Context context) {
        //String externalDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        return "/sdcard";
    }

    private String getUsbPath(Context context) {
        try {
            StorageManager sm = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[])getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
                Method getPath = volume.getClass().getDeclaredMethod("getPath");
                Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                Method getState = volume.getClass().getDeclaredMethod("getState");
                String path = (String)getPath.invoke(volume);
                boolean removable = (Boolean)isRemovable.invoke(volume);
                String state = (String)getState.invoke(volume);
                //Log.i(TAG, "path:" + path + " removable:" + removable + " state:" + state);
                if (removable && TextUtils.equals(state, Environment.MEDIA_MOUNTED)) {
                    return path;
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
