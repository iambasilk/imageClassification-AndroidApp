package com.yep.user21.internprj1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;

import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.yep.user21.internprj1.R.id.imageView;
import static com.yep.user21.internprj1.R.id.txtDescription;
import static com.yep.user21.internprj1.R.id.txtLocation;


public class MainActivity extends AppCompatActivity {

    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("YOUR_API_KEY :) ");
    public Bitmap imageBitmap;
    public ByteArrayInputStream inputStream;


    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    double latitude,longitude ;
    GPSTracker gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnSave = (Button) findViewById(R.id.btnSave);
        Button btnTake = (Button) findViewById(R.id.btnTake);

        gps = new GPSTracker(MainActivity.this);

        if (gps.canGetLocation()) {

             latitude = gps.getLatitude();
             longitude = gps.getLongitude();

            //
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
                    + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {

            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }


        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);

                // If any permission above not allowed by user, this condition will
                // execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });


    }

    static final int REQUEST_IMAGE_CAPTURE = 1;


    public void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);


         //   System.out.println("Bas location called");



            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            final AsyncTask<InputStream, String, String> visionTask = new AsyncTask<InputStream, String, String>() {
                ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

                @Override
                protected String doInBackground(InputStream... params) {
                    try {
                        publishProgress("Recognizing....");
                        String[] features = {"Description"};
                        String[] details = {};
                        AnalysisResult result = visionServiceClient.analyzeImage(params[0], features, details);

                        String strResult = new Gson().toJson(result);
                        return strResult;

                    } catch (Exception e) {
                        return null;
                    }
                }


                @Override
                protected void onPreExecute() {

                    mDialog.show();
                }


                @Override
                protected void onPostExecute(String s) {
                    mDialog.dismiss();

                    AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                    EditText textView = (EditText) findViewById(R.id.txtDescription);
                    EditText txtLocation=(EditText)findViewById(R.id.txtLocation);
                    StringBuilder stringBuilder = new StringBuilder();

                    for (Caption caption : result.description.captions) {
                        stringBuilder.append(caption.text);
                    }
                    textView.setText(stringBuilder);
                    txtLocation.setText(latitude+" , "+longitude);
                }


                @Override
                protected void onProgressUpdate(String... values) {
                    mDialog.setMessage(values[0]);
                }


            };
            visionTask.execute(inputStream);
        }
    }
}


