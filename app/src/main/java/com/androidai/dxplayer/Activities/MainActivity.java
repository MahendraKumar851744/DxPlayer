package com.androidai.dxplayer.Activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.androidai.dxplayer.R;
import com.androidai.dxplayer.adapters.VideoFoldersAdapter;
import com.androidai.dxplayer.models.MediaFiles;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.onesignal.OneSignal;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Dialog dialog;
    public static final int STORAGE_PERMISSION_ABOVE10 = 1009;
    public static final int STORAGE_PERMISSION = 109;
    public static final int READ_STORAGE_PERMISSION_BELOW11 = 1;
    public static final int READ_STORAGE_PERMISSION_ABOVE11 = 2;
    public static final int READ_STORAGE_PERMISSION_ABOVE13 = 3;


    private ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
    private ArrayList<String> allFolderList = new ArrayList<>();
    RecyclerView recyclerView;
    VideoFoldersAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.folders_rv);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_folders);

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        OneSignal.setAppId("a1ba4b5f-e318-48cd-96e6-2346068ed11c");

        requestPermission();
        appupdate(this);
        showFolders();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showFolders();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }



    private void showFolders() {
        mediaFiles = fetchMedia();
        adapter = new VideoFoldersAdapter(mediaFiles, allFolderList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL,false));
        adapter.notifyDataSetChanged();
    }
    @SuppressLint("Range")
    public ArrayList<MediaFiles> fetchMedia(){
        ArrayList<MediaFiles> mediaFilesArrayList = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(uri, null,
                null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles mediaFiles = new MediaFiles(id, title, displayName, size, duration, path,
                        dateAdded);

                int index = path.lastIndexOf("/");
                String subString = path.substring(0, index);
                if (!allFolderList.contains(subString)) {
                    allFolderList.add(subString);
                }
                mediaFilesArrayList.add(mediaFiles);
            } while (cursor.moveToNext());
        }


        return mediaFilesArrayList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rateus:
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id="
                        + getApplicationContext().getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.refresh_folders:
                finish();
                startActivity(getIntent());
                break;
            case R.id.share_app:
                Intent share_intent = new Intent();
                share_intent.setAction(Intent.ACTION_SEND);
                share_intent.putExtra(Intent.EXTRA_TEXT,"Check this app via\n"+
                        "https://play.google.com/store/apps/details?id="
                                + getApplicationContext().getPackageName());
                share_intent.setType("text/plain");
                startActivity(Intent.createChooser(share_intent,"Share app via"));
                break;
        }
        return true;
    }

    void requestPermission(){
        if (Util.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showFolders();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_BELOW11);
            }
        } else {
            if(Environment.isExternalStorageManager()){
                showFolders();
            }else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this, new String[]{READ_MEDIA_VIDEO}, READ_STORAGE_PERMISSION_ABOVE13);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_ABOVE11);
                }
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_PERMISSION_BELOW11) {
            String per = permissions[0];
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                boolean showRationale = shouldShowRequestPermissionRationale(per);
                if (!showRationale) {
                    showPermissionDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_BELOW11);
                }
            } else {
                showFolders();
            }
        } else if (requestCode == READ_STORAGE_PERMISSION_ABOVE11) {
            String per = permissions[0];
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                boolean showRationale = shouldShowRequestPermissionRationale(per);
                if (!showRationale) {
                    showPermissionDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_ABOVE11);
                }
            } else {
                showFolders();
            }
        }else{
            String per = permissions[0];
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                boolean showRationale = shouldShowRequestPermissionRationale(per);
                if (!showRationale) {
                    showPermissionDialog();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_ABOVE13);
                }
            } else {
                showFolders();
            }
        }

    }

    private void showPermissionDialog(){
        dialog=new Dialog(this);
        dialog.setContentView(R.layout.lay_permissions);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button allow_access =  dialog.findViewById(R.id.allow_access);
        allow_access.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent,5);
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1011){
            Toast.makeText(this, "Download Started", Toast.LENGTH_SHORT).show();
            if(resultCode!= RESULT_OK){
                Toast.makeText(this, "Download Failed", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Util.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if(dialog!=null){
                    dialog.dismiss();
                }
                showFolders();
            }
        } else {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                if(dialog!=null){
                    dialog.dismiss();
                }
                showFolders();
            }
        }
    }

    private void appupdate(Context context){
        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(context);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try{
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.IMMEDIATE,MainActivity.this,1011);
                }catch (IntentSender.SendIntentException exception){


                }

            }
        });
    }



}