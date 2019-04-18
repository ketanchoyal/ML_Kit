package com.service.parking.ml_kit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.common.util.Base64Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.service.parking.ml_kit.Helper.GraphicOverlay;
import com.service.parking.ml_kit.Helper.RectOverlay;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraKitView cameraKitView;
    GraphicOverlay graphicOverlay;
    Button buttonDetect;

    AlertDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraKitView = findViewById(R.id.camera_view);
        graphicOverlay = findViewById(R.id.graphics_overlay);
        buttonDetect = findViewById(R.id.detect_btn);

        cameraKitView.onStart();
        cameraKitView.startVideo();

        waitingDialog = new SpotsDialog.Builder().setContext(this)
                .setMessage("please wait")
                .setCancelable(false).build();

//        cameraKitView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraKitView.onStart();
//            }
//        });

        //event
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                cameraKitView.onStart();
                cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                    @Override
                    public void onImage(CameraKitView cameraKitView, byte[] bytes) {
                        waitingDialog.show();

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        bitmap = Bitmap.createScaledBitmap(bitmap,cameraKitView.getWidth(),cameraKitView.getHeight(),false);
//                        cameraKitView.onStop();

                        runFaceDetectior(bitmap);

                    }
                });
                graphicOverlay.clear();
            }
        });



    }

    private void runFaceDetectior(Bitmap bitmap) {
        cameraKitView.onPause();
        Log.d("Here : ","yes Here ");
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionFaceDetectorOptions options = new FirebaseVisionFaceDetectorOptions.Builder().build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
            @Override
            public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                processFaceResults(firebaseVisionFaces);
            }
        });
    }

    private void processFaceResults(List<FirebaseVisionFace> firebaseVisionFaces) {
        int count = 0;
        for (FirebaseVisionFace face : firebaseVisionFaces) {
            Rect bounds = face.getBoundingBox();
            //Draw Rectangle

            RectOverlay rectOverlay = new RectOverlay(graphicOverlay,bounds);
            graphicOverlay.add(rectOverlay);
            count++;
        }
        waitingDialog.dismiss();
        Toast.makeText(this,String.format("Detected %d faces in image",count),Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }
}
