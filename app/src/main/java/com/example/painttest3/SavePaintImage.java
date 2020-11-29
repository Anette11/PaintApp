package com.example.painttest3;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SavePaintImage {
    private Context context;
    private String nameOfFolder = "/My_Paint";
    private String nameOfFile = "MyPaintImage";

    public void saveImage(Context context, Bitmap bitmapImageToSave) {
        this.context = context;
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + nameOfFolder;
        String currentDateAndTime = getCurrentDateAndTime();

        File fileDir = new File(filePath);

        if (!fileDir.exists()) {
            boolean b = fileDir.mkdirs();
            Log.d("In method saveImage: ", "boolean b: " + b);
        }

        File file = new File(fileDir, nameOfFile + currentDateAndTime + ".jpg");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmapImageToSave.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            checkIfFileWasCreated(file);
            toastMakeTextSaved();
        } catch (FileNotFoundException e) {
            toastMakeTextNotSaved();
            Log.d("Image NOT Saved: ", Log.getStackTraceString(e));
        } catch (IOException e) {
            toastMakeTextNotSaved();
            Log.d("Image NOT Saved: ", Log.getStackTraceString(e));
        }
    }

    private void checkIfFileWasCreated(File file) {
        MediaScannerConnection.scanFile(context, new String[]{file.toString()},
                null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.d("checkIfFileWasCreated: ", "path: " + path);
                        Log.d("checkIfFileWasCreated: ", "uri: " + uri);
                    }
                });
    }

    private String getCurrentDateAndTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
        return simpleDateFormat.format(calendar.getTime());
    }

    private void toastMakeTextNotSaved() {
        Toast.makeText(context, R.string.not_saved, Toast.LENGTH_SHORT).show();
    }

    private void toastMakeTextSaved() {
        Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show();
    }
}
