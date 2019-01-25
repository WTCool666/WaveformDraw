package com.example.waveform.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.waveform.R;

import java.util.ArrayList;
import java.util.List;

public class BasePermissionActivity extends AppCompatActivity {

    private final String TAG="BasePermissionActivity";
    private PermissionsGrant mPermissionGrant = new PermissionsGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case CODE_ACCESS_RECORD_AUDIO:
                    Toast.makeText(getApplicationContext(), "Result Permission Grant CODE_ACCESS_RECORD_AUDIO", Toast.LENGTH_SHORT).show();
                    break;
                case CODE_ACCESS_WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(getApplicationContext(), "Result Permission Grant CODE_ACCESS_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case CODE_ACCESS_READ_EXTERNAL_STORAGE:
                    Toast.makeText(getApplicationContext(), "Result Permission Grant CODE_ACCESS_READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case CODE_MULTI_PERMISSION:
                    Toast.makeText(getApplicationContext(), "Result Permission Grant CODE_MULTI_PERMISSION", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestMultiPermissions(this, mPermissionGrant);
    }

    public static final int CODE_ACCESS_RECORD_AUDIO = 0;
    public static final int CODE_ACCESS_WRITE_EXTERNAL_STORAGE= 1;
    public static final int CODE_ACCESS_READ_EXTERNAL_STORAGE= 2;
    public static final int CODE_MULTI_PERMISSION = 100;

    public static final String PERMISSION_ACCESS_READ_EXTERNAL_STORAGE=Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_ACCESS_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMISSION_ACCESS_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private static final String[] requestPermissions = {
            PERMISSION_ACCESS_RECORD_AUDIO,
            PERMISSION_ACCESS_WRITE_EXTERNAL_STORAGE,
            PERMISSION_ACCESS_READ_EXTERNAL_STORAGE,
    };

    public interface PermissionsGrant {
        void onPermissionGranted(int requestCode);
    }

    public void requestPermission(final Activity activity, final int requestCode, PermissionsGrant permissionGrant) {
        if (activity == null) {
            LogUtils.v(TAG+"requestPermission acitvity is null!!!");
            return;
        }

        LogUtils.v(TAG+"requestPermission requestCode:" + requestCode);
        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            LogUtils.v(TAG+"requestPermission illegal requestCode:" + requestCode);
            return;
        }

        final String requestPermission = requestPermissions[requestCode];

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 个人建议try{}catch(){}单独处理，提示用户开启权限。
//        if (Build.VERSION.SDK_INT < 23) {
//            return;
//        }

        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
        } catch (RuntimeException e) {
            Toast.makeText(activity, "please open this permission:"+requestPermission, Toast.LENGTH_SHORT)
                    .show();
            LogUtils.v(TAG+"requestPermission RuntimeException:" + e.getMessage());
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            LogUtils.v( TAG+"requestPermission startRequestPermission");
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,requestPermission)) {
                LogUtils.v(TAG+"requestPermission shouldShowRequestPermissionRationale");
                shouldShowRationale(activity, requestCode, requestPermission);
            } else {
                LogUtils.v(TAG+"requestPermission Permission granted is prohibited");
                ActivityCompat.requestPermissions(this,new String[]{requestPermission}, requestCode);
            }

        } else {
            LogUtils.v(TAG+"requestPermission Permission has been granted");
            // Toast.makeText(activity, "opened:" + requestPermission, Toast.LENGTH_SHORT).show();
            permissionGrant.onPermissionGranted(requestCode);
        }
    }

    public void requestMultiPermissions(final Activity activity, PermissionsGrant grant) {
        final List<String> permissionsList = getNoGrantedPermission(activity, false);
        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(activity, true);
        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return;
        }
        LogUtils.v(TAG+"requestMultiPermissions permissionsList:" + permissionsList.size() + ",shouldRationalePermissionsList:" + shouldRationalePermissionsList.size());

        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(this,permissionsList.toArray(new String[permissionsList.size()]),
                    CODE_MULTI_PERMISSION);
        } else if (shouldRationalePermissionsList.size() > 0) {
            showMessageOKCancel(activity, "should open those permission",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getParent(),shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]),
                                    CODE_MULTI_PERMISSION);
                        }
                    });
        } else {
            grant.onPermissionGranted(CODE_MULTI_PERMISSION);
        }
    }

    private void requestMultiResult(Activity activity, String[] permissions, int[] grantResults, PermissionsGrant permissionGrant) {
        if (activity == null) {
            LogUtils.v(TAG+"requestPermission acitvity is null!!!");
            return;
        }

        ArrayList<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permissions[i]);
            }
        }

        if (notGranted.size() == 0) {
            Toast.makeText(activity, TAG+"all permission success" + notGranted, Toast.LENGTH_SHORT)
                    .show();
            permissionGrant.onPermissionGranted(CODE_MULTI_PERMISSION);
        } else {
            openSettingActivity(activity, "those permission need granted!");
        }

    }

    private void shouldShowRationale(final Activity activity, final int requestCode, final String requestPermission) {
        //TODO
        String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
        showMessageOKCancel(activity, "Rationale: " + permissionsHint[requestCode], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{requestPermission},
                        requestCode);
            }
        });
    }

    private void showMessageOKCancel(final Activity context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

    /**
     * @param activity
     * @param requestCode  Need consistent with requestPermission
     * @param permissions
     * @param grantResults
     */
    public void requestPermissionsResult(final Activity activity, final int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults, PermissionsGrant permissionGrant) {
        if (activity == null) {
            LogUtils.v(TAG+"requestPermissionsResult acitvity is null!!!");
            return;
        }
        LogUtils.v(TAG+"requestPermissionsResult requestCode:" + requestCode);

        if (requestCode == CODE_MULTI_PERMISSION) {
            requestMultiResult(activity, permissions, grantResults, permissionGrant);
            return;
        }

        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            LogUtils.v( TAG+"requestPermissionsResult illegal requestCode:" + requestCode);
            return;
        }

        LogUtils.v( TAG+"requestPermissionsResult requestCode:" + requestCode + ",permissions:" + permissions[0]
                + ",grantResults:" + grantResults[0] + ",length:" + grantResults.length);

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionGrant.onPermissionGranted(requestCode);
        } else {
            String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
            openSettingActivity(activity,  "Result:" + permissionsHint[requestCode]);
        }

    }

    private void openSettingActivity(final Activity activity, String message) {
        showMessageOKCancel(activity, message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                LogUtils.v( TAG+"getPackageName(): " + activity.getPackageName());
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
    }

    public ArrayList<String> getNoGrantedPermission(Activity activity, boolean isShouldRationale) {
        ArrayList<String> permissions = new ArrayList<>();
        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];
            //TODO checkSelfPermission
            int checkSelfPermission = -1;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT)
                        .show();
                LogUtils.v(TAG+"getNoGrantedPermission RuntimeException:" + e.getMessage());
                return null;
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                LogUtils.v( TAG+"getNoGrantedPermission != PackageManager.PERMISSION_GRANTED:" + requestPermission);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,requestPermission)) {
                    LogUtils.v(TAG+"getNoGrantedPermission shouldShowRequestPermissionRationale");
                    if (isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                } else {
                    if (!isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                    LogUtils.v(TAG+"getNoGrantedPermission Permission granted is prohibited");
                }
            }
        }
        return permissions;
    }

}
