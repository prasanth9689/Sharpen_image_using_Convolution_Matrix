package com.skyblue.sharpenimageusingconvolutionmatrix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.skyblue.sharpenimageusingconvolutionmatrix.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private Context context = this;

    ProgressDialog pDialog;
    final static int KERNAL_WIDTH = 3;
    final static int KERNAL_HEIGHT = 3;

    int[][] kernalBlur ={
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };

    ImageView imageSource, imageAfter;
    Bitmap bitmap_Source, afterSharpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        bitmap_Source = BitmapFactory.decodeResource(getResources(), R.drawable.lena);

        sharpnssImage();
    }

    private void sharpnssImage() {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Please wait");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

        new Thread() {
            @Override
            public void run() {
                afterSharpen = processingBitmap(bitmap_Source, kernalBlur);
                try {
                    // code runs in a thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pDialog.dismiss();
                            binding.imageAfter.setImageBitmap(afterSharpen);

                        }
                    });
                } catch (final Exception ex) {
                    Log.i("---","Exception in thread");
                }
            }
        }.start();

    }

    private Bitmap processingBitmap(Bitmap src, int[][] knl){

        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        int bmWidth = src.getWidth();
        int bmHeight = src.getHeight();
        int bmWidth_MINUS_2 = bmWidth - 2;
        int bmHeight_MINUS_2 = bmHeight - 2;

        for(int i = 1; i <= bmWidth_MINUS_2; i++){
            for(int j = 1; j <= bmHeight_MINUS_2; j++){

                //get the surround 3*3 pixel of current src[i][j] into a matrix subSrc[][]
                int[][] subSrc = new int[KERNAL_WIDTH][KERNAL_HEIGHT];
                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSrc[k][l] = src.getPixel(i-1+k, j-1+l);
                    }
                }

                //subSum = subSrc[][] * knl[][]
                int subSumA = 0;
                int subSumR = 0;
                int subSumG = 0;
                int subSumB = 0;

                for(int k = 0; k < KERNAL_WIDTH; k++){
                    for(int l = 0; l < KERNAL_HEIGHT; l++){
                        subSumA += Color.alpha(subSrc[k][l]) * knl[k][l];
                        subSumR += Color.red(subSrc[k][l]) * knl[k][l];
                        subSumG += Color.green(subSrc[k][l]) * knl[k][l];
                        subSumB += Color.blue(subSrc[k][l]) * knl[k][l];
                    }
                }

                if(subSumA<0){
                    subSumA = 0;
                }else if(subSumA>255){
                    subSumA = 255;
                }

                if(subSumR<0){
                    subSumR = 0;
                }else if(subSumR>255){
                    subSumR = 255;
                }

                if(subSumG<0){
                    subSumG = 0;
                }else if(subSumG>255){
                    subSumG = 255;
                }

                if(subSumB<0){
                    subSumB = 0;
                }else if(subSumB>255){
                    subSumB = 255;
                }

                dest.setPixel(i, j, Color.argb(
                        subSumA,
                        subSumR,
                        subSumG,
                        subSumB));
            }
        }
        pDialog.dismiss();

        return dest;
    }
}