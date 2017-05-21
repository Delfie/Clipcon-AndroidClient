package com.sprout.clipcon.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.sprout.clipcon.R;
import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.server.EndpointInBackGround;

import java.io.IOException;

/**
 * Created by Yongwon on 2017. 5. 1..
 */

public class TransparentActivity extends Activity {

    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private Uri uri;
    private static Bitmap bitmap;

    public static Bitmap getBitmap() {
        return bitmap;
    }

    //ask user about permission to save image into basic gallery apps.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transparent_activity);

        String action = getIntent().getAction();
        String type = getIntent().getType();

        if (Intent.ACTION_SEND.equals(action)) {
            Toast.makeText(getApplicationContext(), "이미지 전송 완료", Toast.LENGTH_SHORT).show();
            uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);

            Log.d("delf", "[DEBUG] shared date type is " + type);
            ContentResolver cR = getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String mimeType = mime.getExtensionFromMimeType(cR.getType(uri)); // get "file name extension"
            Log.d("delf", "[DEBUG] shared date mime type is " + mimeType);

            if (type.startsWith("image/")) {
                System.out.println("이미지임");

                bitmap = getBitmapByUri(uri);
                getPermission();

                new EndpointInBackGround() // TODO: 17-05-16 change name
                        .setSendBitmapImage(bitmap)
                        .execute(Message.UPLOAD, "image");

            } else {
                System.out.println("이미지아님");

                Log.d("delf", "[DEBUG] uri.getPath() = " + uri.getPath());

                String filePath = getPathFromUri(uri);
                new EndpointInBackGround()
                        .setFilePath(filePath)
                        .execute(Message.UPLOAD, "file");
            }



            /*ClipData clip = ClipData.newRawUri("test", uri);
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(clip);*/
        }

        finish();
    }

    public String getPathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }

//    private void bitmapToImage() {
//        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/";
//        String fileName = "Image" + createName(System.currentTimeMillis()) + ".png";
//
//        File newFile = new File(filePath, fileName);
//        OutputStream out;
//        try {
//            Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri); // 비트맵 객체 보유
//
//            newFile.createNewFile();
//            out = new FileOutputStream(newFile);
//            bm.compress(Bitmap.CompressFormat.PNG, 100, out);
//
//            out.flush();
//            out.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private Bitmap getBitmapByUri(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
//            bitmapToImage();
        }
    }

//    private String createName(long dateTaken) {
//        Date date = new Date(dateTaken);
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//        return dateFormat.format(date);
//    }
}

