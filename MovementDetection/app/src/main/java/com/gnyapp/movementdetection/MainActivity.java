package com.gnyapp.movementdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    Mat curr_gray, prev_gray, rgb, diff;
    List<MatOfPoint> cnts;
    boolean is_init;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        is_init = false;

        cameraBridgeViewBase = findViewById(R.id.cameraView);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                curr_gray = new Mat();
                prev_gray = new Mat();
                rgb = new Mat();
                diff = new Mat();
                cnts = new ArrayList<>();
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                if(!is_init){
                    prev_gray = inputFrame.gray();
                    is_init = true;
                    return prev_gray;
                }

                rgb = inputFrame.rgba();
                curr_gray = inputFrame.gray();

                Core.absdiff(curr_gray, prev_gray, diff); //-----




                Imgproc.threshold(diff, diff, 40, 255, Imgproc.THRESH_BINARY);//------




                Imgproc.findContours(diff, cnts, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

                Imgproc.drawContours(rgb, cnts, -1, new Scalar(255, 0, 0), 4);





                for(MatOfPoint m: cnts){
                    Rect r = Imgproc.boundingRect(m);
                    Imgproc.rectangle(rgb, r, new Scalar(0, 0, 255), 3);
                }

                cnts.clear();

                prev_gray = curr_gray.clone();

                return rgb;
            }
        });

        if(OpenCVLoader.initDebug()) {
            cameraBridgeViewBase.enableView();
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    private void getPermission() {
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.CAMERA},101);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }
}