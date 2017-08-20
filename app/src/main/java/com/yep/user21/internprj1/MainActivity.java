package com.yep.user21.internprj1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;

import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.yep.user21.internprj1.R.id.btnSave;
import static com.yep.user21.internprj1.R.id.imageView;
import static com.yep.user21.internprj1.R.id.txtDescription;
import static com.yep.user21.internprj1.R.id.txtLocation;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CODE_PERMISSION = 2;
    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("6a628b5b0e3a4c119ba99dc4b9cb972d");

    public Bitmap imageBitmap;
    public TextView textView;
    public  ImageView imageView;
    public TextView txtLocation;
    public EditText txtUser;
    public Button btnSave, btnTake;
    public ByteArrayInputStream inputStream;
    public ByteArrayOutputStream outputStream;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    double latitude, longitude;
    GPSTracker gps;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnSave = (Button) findViewById(R.id.btnSave);
        btnTake = (Button) findViewById(R.id.btnTake);

        btnSave.setEnabled(false);


        gps = new GPSTracker(MainActivity.this);

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            //
            Toast.makeText(getApplicationContext(), " Location Fetched ", Toast.LENGTH_LONG).show();
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

        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                writeData();
                Toast.makeText(MainActivity.this,"Data Saved Succesfully",Toast.LENGTH_LONG).show();
                btnSave.setEnabled(false);

//               broadcastReceiver=new CheckInternetBroadcast() {
//                    @Override
//                    public void onReceive(Context context, Intent intent) {
//                        int[] type={ConnectivityManager.TYPE_WIFI,ConnectivityManager.TYPE_MOBILE};
//                        if(isNetworkAvailable(context,type))
//                        {
//
//                            Toast.makeText(MainActivity.this,"internet connection AAAAAAvailable",Toast.LENGTH_LONG).show();
//                           // return;
//                        }
//                        else
//                        {
//                            writeData();
//                            Toast.makeText(MainActivity.this,"internet connection not available",Toast.LENGTH_LONG).show();
//                        }
//
//                    }
//                };
//                registerReceiver(broadcastReceiver,intentFilter);



                //for decoding
//                byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
//                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                myimageview.setImageBitmap(decodedByte);

                //controller.addEntry(tempName,outputStream.toByteArray());

//                final Cursor cursor = controller.getAllPersons();
//                String [] columns = new String[] {
//                        DatabaseHelper.KEY,
//                        DatabaseHelper.PERSON_COLUMN_NAME
//                };
            }
        });


    }




    public void writeData(){
        final String tempName = textView.getText().toString();
        final String tempLoc=  txtLocation.getText().toString();
        txtUser=(EditText)findViewById(R.id.userDescription);
        final String textUser=  txtUser.getText().toString();

        //loading the names again
        //loadNames();

        Firebase.setAndroidContext(MainActivity.this);
        Firebase ref = new Firebase(Config.FIREBASE_URL);

        // ref.keepSynced(true);




        MyData Mydataobject=new MyData();

        Mydataobject.setLocation(tempLoc);
        Mydataobject.setDesc(tempName);
        Mydataobject.setUser(textUser);
        byte[] byteFormat = outputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        Mydataobject.setImage(encodedImage);





        ref.child("Details").push().setValue(Mydataobject);

        //Toast.makeText(getApplicationContext(), "Data sent to cloud successfully", Toast.LENGTH_LONG).show();

          imageView.setImageDrawable(null);
        textView.setText("");
        txtLocation.setText("");
        txtUser.setText("");

        System.out.println(tempName+" SAved to cloud succesfully basil!!!");

    }



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
                    txtLocation= (TextView) findViewById(R.id.txtLocation);
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



