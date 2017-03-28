package com.example.shivani.customcamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener
{
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;
    View viewControl;
    private int currentCameraId;
    int index = 0;
    private CustomPagerAdapter mCustomPagerAdapter;
    final int[]array_image = new int[]{R.drawable.aa, R.drawable.bb};
    public boolean inPreview;
    final static int FLIP_VERTICAL = 1;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

        camera = Camera.open(currentCameraId);
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();

        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        controlInflater = LayoutInflater.from(getBaseContext());
        viewControl = controlInflater.inflate(R.layout.control, null);
        viewControl.setSaveEnabled(true);
        viewControl.setDrawingCacheEnabled(true);

        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
        mCustomPagerAdapter = new CustomPagerAdapter(this);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.image);
        mViewPager.setAdapter(mCustomPagerAdapter);

        ImageButton buttonTakePicture = (ImageButton) findViewById(R.id.takepicture);
        ImageButton toggleButton = (ImageButton) findViewById(R.id.toggleBtn);

        buttonTakePicture.setOnClickListener(this);
        toggleButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId())
        {
            case R.id.takepicture:
                camera.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);
                break;
            case R.id.toggleBtn:
                if (inPreview) {
                    camera.stopPreview();
                }//NB: if you don't release the current camera before switching, you app will crash
                camera.release();
//swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                }
                camera = Camera.open(currentCameraId);
                // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                camera.setDisplayOrientation(90);
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                break;
        }

    }
    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
        }
    };
    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            // TODO Auto-generated method stub
        }
    };
    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] arg0, Camera camera) {
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
            if(currentCameraId== Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                cameraBitmap=rotate(cameraBitmap,180);
                cameraBitmap= (flip(cameraBitmap,FLIP_VERTICAL));
            }
            int wid = cameraBitmap.getWidth();
            int hgt = cameraBitmap.getHeight();
            Bitmap newImage = Bitmap.createBitmap(wid, hgt, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(newImage);
            canvas.drawBitmap(cameraBitmap, 0f, 0f, null);

            viewControl.setDrawingCacheEnabled(true);
            Bitmap viewCapture = Bitmap.createBitmap(viewControl.getDrawingCache());
            viewControl.setDrawingCacheEnabled(false);

            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(viewCapture, 960, 1400, true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            Drawable drawable = new BitmapDrawable(getResources(), rotatedBitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth() * 1 / 2, drawable.getIntrinsicHeight() * 1 / 2);
            drawable.draw(canvas);
            File storagePath = new File(Environment.getExternalStorageDirectory() + "/My Photo /");
            storagePath.mkdirs();
            File myImage = new File(storagePath, Long.toString(System.currentTimeMillis()) + ".jpg");

            try {
                FileOutputStream out = new FileOutputStream(myImage);
                ExifInterface exif = new ExifInterface(cameraBitmap.toString());
                Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                    newImage = rotate(newImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                    newImage = rotate(newImage, 270);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                    newImage = rotate(newImage, 180);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
                    newImage = rotate(newImage, 90);
                }

                newImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                camera.startPreview();
                String name= myImage.toString();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                newImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                
                newImage.recycle();
                newImage = null;
                out.write(arg0);
                out.flush();
                out.close();
                
            }
            catch (FileNotFoundException e)
            {
                Log.d("In Saving File", e + "");
            }
            catch (IOException e)
            {
                Log.d("In Saving File", e + "");
            }
        }
    };



    public static Bitmap flip(Bitmap src, int type) {
        // create new matrix for transformation
        Matrix matrix = new Matrix();
        // if vertical
        if(type == FLIP_VERTICAL) {
            matrix.preScale(1.0f, -1.0f);
        } else {
            return null;
        }

        // return transformed image
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
        } catch (RuntimeException e) {
            //Toast.makeText(getApplicationContext(), ,
            // Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
// TODO Auto-generated method stub
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }


    public Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

   
}
 
   

