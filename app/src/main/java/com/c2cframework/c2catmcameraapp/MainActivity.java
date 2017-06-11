package com.c2cframework.c2catmcameraapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final String TAG  = this.getClass().getName();

    ImageView ivCamera, ivUpload, ivImage;

    CameraPhoto cameraPhoto;
    final int CAMERA_REQUEST = 13323;

    String selectedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraPhoto = new CameraPhoto(getApplicationContext());

        ivImage = (ImageView)findViewById(R.id.ivImage);
        ivCamera = (ImageView)findViewById(R.id.ivCamera);
        ivUpload = (ImageView)findViewById(R.id.ivUpload);


        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(),CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while taking photos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivUpload.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                AsyncTaskrunner ru=new AsyncTaskrunner();
                try{
                    Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(1024,1024).getBitmap();
                    Bitmap rotatedbitmap = rotateImage(bitmap,180);
                    String encodedImage = ImageBase64.encode(rotatedbitmap);
                    ru.execute(encodedImage);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == CAMERA_REQUEST){
                String photoPath = cameraPhoto.getPhotoPath();
                selectedPhoto = photoPath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(1024,1024).getBitmap();
                    ivImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while loading photos", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class AsyncTaskrunner extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String text = params[0];
            String JsonResponse = null;
            BufferedReader reader = null;
            HttpURLConnection httpURLConnection = null;
            try {
                URL url = new URL("http://192.168.43.48:59056/Images"); //Enter URL here
                httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                httpURLConnection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                httpURLConnection.connect();

                JSONObject image = new JSONObject();
                image.put("imagePath", text);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(image.toString());
                wr.flush();
                wr.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            InputStream inputStream = null;
            try {
                inputStream = httpURLConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
//input stream
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String inputLine;
            try {
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (buffer.length() == 0) {
                // Stream was empty. No point in parsing.
                return null;
            }
            JsonResponse = buffer.toString();
//response data
            Log.d("TAG",JsonResponse);
            //send to post execute
            return JsonResponse;

        }


            //BufferedReader reader = null;
            //String text = params[0];
            //String JsonResponse = null;
            //HttpURLConnection urlConnection = null;
            //JSONObject image = new JSONObject();

//            try {
//                image.put("imagePath", text);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//            try {
//                URL url = new URL(
//                        "http://192.168.43.48:59056/Images"
//                );
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setDoOutput(true);
//                // is output buffer writter
//                urlConnection.setRequestMethod("POST");
//                urlConnection.setRequestProperty("Content-Type", "application/json");
////                urlConnection.setRequestProperty("Accept", "application/json");
////set headers and method
//                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
//                writer.write(image.toString());
//// json data
//                writer.close();
//                InputStream inputStream = urlConnection.getInputStream();
////input stream
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null) {
//                    // Nothing to do.
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String inputLine;
//                while ((inputLine = reader.readLine()) != null)
//                    buffer.append(inputLine + "\n");
//                if (buffer.length() == 0) {
//                    // Stream was empty. No point in parsing.
//                    return null;
//                }
//                JsonResponse = buffer.toString();
////response data
//                Log.d("TAG",JsonResponse);
//                //send to post execute
//                return JsonResponse;
//
//            } catch (Exception ex) {
//                return ex.toString();
//            } finally {
//                try {
//                    reader.close();
//                } catch (Exception ex) {
//                    return ex.toString();
//                }
//            }

//        }
//        @Override
//        protected String doInBackground(String... params) {
//            BufferedReader reader = null;
//            String text = params[0];
//            String JsonResponse = null;
//            HttpURLConnection httpURLConnection = null;
//
//            try {
//                    buffer.append(inputLine + "\n");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            if (buffer.length() == 0) {
//                return null;
//            }
//            JsonResponse = buffer.toString();
//            Log.d("TAG",JsonResponse);
////            if(JsonResponse.equals("uploaded_success")){
////                Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_SHORT);
////            }
////            else{
////                Toast.makeText(MainActivity.this, "Upload Unsuccessful", Toast.LENGTH_SHORT);
////            }
//            return JsonResponse;
//
//        }
//
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
            } catch (Exception e) {

            }
        }
    }
}
