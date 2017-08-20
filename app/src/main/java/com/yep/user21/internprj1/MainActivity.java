package com.yep.user21.internprj1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.test.mock.MockPackageManager;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;
    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("6a628b5b0e3a4c119ba99dc4b9cb972d");
    public Bitmap imageBitmap;
    public TextView textView, txtLocation;
    public ImageView imageView;
    public EditText txtUser;
    public Button btnSave, btnTake;
    public ByteArrayInputStream inputStream;
    public ByteArrayOutputStream outputStream;
    GPSTracker gps;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnTake = (Button) findViewById(R.id.btnTake);

        btnSave.setEnabled(false);

        gpsCheck();

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TakePicture();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeData();
                Toast.makeText(MainActivity.this, "Data Saved Succesfully", Toast.LENGTH_LONG).show();
                btnSave.setEnabled(false);
            }
        });
    }

    public void gpsCheck() {

        gps = new GPSTracker(MainActivity.this);

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            Toast.makeText(getApplicationContext(), " Location Fetched ", Toast.LENGTH_LONG).show();
        } else {
            gps.showSettingsAlert();
        }


        try {
            if (ActivityCompat.checkSelfPermission(this, mPermission)
                    != MockPackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{mPermission},
                        REQUEST_CODE_PERMISSION);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeData() {
        final String tempName = textView.getText().toString();
        final String tempLoc = txtLocation.getText().toString();
        txtUser = (EditText) findViewById(R.id.userDescription);
        final String textUser = txtUser.getText().toString();

        Firebase.setAndroidContext(MainActivity.this);
        Firebase ref = new Firebase(Config.FIREBASE_URL);

        // ref.keepSynced(true);


        MyData Mydataobject = new MyData();

        Mydataobject.setLocation(tempLoc);
        Mydataobject.setDesc(tempName);
        Mydataobject.setUser(textUser);
        byte[] byteFormat = outputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        Mydataobject.setImage(encodedImage);


        ref.child("Details").push().setValue(Mydataobject);

        imageView.setImageDrawable(null);
        textView.setText("");
        txtLocation.setText("");
        txtUser.setText("");

        System.out.println(tempName + " SAved to cloud succesfully basil!!!");

    }

    public void TakePicture() {
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
            imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);

            btnSave.setEnabled(true);
            //   System.out.println("Bas location called");

            outputStream = new ByteArrayOutputStream();
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
                    textView = (TextView) findViewById(R.id.txtDescription);
                    txtLocation = (TextView) findViewById(R.id.txtLocation);
                    StringBuilder stringBuilder = new StringBuilder();

                    for (Caption caption : result.description.captions) {
                        stringBuilder.append(caption.text);
                    }
                    textView.setText(stringBuilder);
                    txtLocation.setText(latitude + " , " + longitude);
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



