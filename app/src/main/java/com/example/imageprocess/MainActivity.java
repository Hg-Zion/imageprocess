package com.example.imageprocess;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import static java.lang.Math.pow;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //通过id找到相应的三个组件
       /* ImageView img1 = findViewById(R.id.imageView1);*/
        final ImageView img2 = findViewById(R.id.imageView2);
        Button btn1 = findViewById(R.id.button1);
        Button btn2 = findViewById(R.id.button2);
        Button btn3 = findViewById(R.id.button3);
        Button btn4 = findViewById(R.id.button4);
        final TextView txv = findViewById(R.id.textView);

        //将图片转换为一个位图
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pic1);
        /*img1.setImageBitmap(bitmap);*/
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm = convertToGray(bitmap);
                img2.setImageBitmap(bm);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm = convertToGray(bitmap);
                img2.setImageBitmap(bm);

                bm = MedianFilter(bm);
                img2.setImageBitmap(bm);
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm = convertToGray(bitmap);
                img2.setImageBitmap(bm);

                bm = MedianFilter(bm);
                img2.setImageBitmap(bm);

                bm = AverageFilter(bm);
                img2.setImageBitmap(bm);
            }
        });
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm = convertToGray(bitmap);
                img2.setImageBitmap(bm);

                bm = MedianFilter(bm);
                img2.setImageBitmap(bm);

                /*bm = AverageFilter(bm);
                img2.setImageBitmap(bm);*/

                txv.setText(String.format("%d", otsuThreshold(bm)));

                bm = ostuFilter(bm);
                img2.setImageBitmap(bm);
            }
        });
    }


    /**
     * 将彩色图转换为灰度图
     *
     * @param bitmap 位图
     * @return 返回转换好的位图
     */
    public Bitmap convertToGray(Bitmap bitmap) {
        //获取源位图的宽、高,并创建一个等宽高的bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        //根据宽高创建像素点数组,并将bitmap的rgb值赋给它
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //获取当前元素rgb值并以此修改为灰度化后的值
                int rgb = pixels[width * i + j];
                int red = (rgb & 0x00FF0000) >> 16;
                int green = (rgb & 0x0000FF00) >> 8;
                int blue = rgb & 0x000000FF;
                int grey = (int) ((float) red * 0.299 + (float) green * 0.587 + (float) blue * 0.114);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 通过中值滤波去噪
     *
     * @param bitmap 位图
     * @return 返回转换好的位图
     */
    public Bitmap MedianFilter(Bitmap bitmap) {
        //获取源位图的宽、高,并创建一个等宽高的bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        //根据宽高创建像素点数组,并将bitmap的rgb值赋给它，同时创建一个空的等大数组
        int[] pixels = new int[width * height];
        int[] newpixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //创建一个3*3的模板用来辅助计算中值
        int[] module = new int[9];
        int alpha = 0xFF << 24;
        int mid;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i != 0 && i != height - 1 && j != 0 && j != width - 1) {
                    module[0] = pixels[width * (i - 1) + (j - 1)];
                    module[1] = pixels[width * (i - 1) + j];
                    module[2] = pixels[width * (i - 1) + (j + 1)];
                    module[3] = pixels[width * i + (j - 1)];
                    module[4] = pixels[width * i + j];
                    module[5] = pixels[width * i + (j + 1)];
                    module[6] = pixels[width * (i + 1) + (j - 1)];
                    module[7] = pixels[width * (i + 1) + j];
                    module[8] = pixels[width * (i + 1) + (j + 1)];
                    Arrays.sort(module);
                    mid = module[4];
                    newpixels[width * i + j] = alpha | mid << 16 | mid << 8 | mid;
                } else {
                    newpixels[width * i + j] = pixels[width * i + j];
                }
            }
        }
        result.setPixels(newpixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 通过均值滤波去噪
     *
     * @param bitmap 位图
     * @return 返回转换好的位图
     */
    public Bitmap AverageFilter(Bitmap bitmap) {
        //获取源位图的宽、高,并创建一个等宽高的bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        //根据宽高创建像素点数组,并将bitmap的rgb值赋给它，同时创建一个空的等大数组
        int[] pixels = new int[width * height];
        int[] newpixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //创建一个3*3的模板用来辅助计算均值
        int[] module = new int[9];
        int alpha = 0xFF << 24;
        int mid;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i != 0 && i != height - 1 && j != 0 && j != width - 1) {
                    module[0] = pixels[width * (i - 1) + (j - 1)];
                    module[1] = pixels[width * (i - 1) + j];
                    module[2] = pixels[width * (i - 1) + (j + 1)];
                    module[3] = pixels[width * i + (j - 1)];
                    module[4] = pixels[width * i + j];
                    module[5] = pixels[width * i + (j + 1)];
                    module[6] = pixels[width * (i + 1) + (j - 1)];
                    module[7] = pixels[width * (i + 1) + j];
                    module[8] = pixels[width * (i + 1) + (j + 1)];

                    int sum = 0;
                    for (int num : module)
                        sum += num;
                    int avg = sum / 9;
                    newpixels[width * i + j] = alpha | avg << 16 | avg << 8 | avg;
                } else {
                    newpixels[width * i + j] = pixels[width * i + j];
                }
            }
        }
        result.setPixels(newpixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 通过OSTU二值化
     *
     * @param bitmap 位图
     * @return 返回转换好的位图
     */
    public Bitmap ostuFilter(Bitmap bitmap) {
        //获取源位图的宽、高,并创建一个等宽高的bitmap
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        //根据宽高创建像素点数组,并将bitmap的rgb值赋给它，同时创建一个空的等大数组
        int[] pixels = new int[width * height];
        int[] newpixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //创建一个3*3的模板用来辅助计算均值
        int[] module = new int[9];
        int alpha = 0xFF << 24;
        int black =0;
        int white =255;
        final int threshold = otsuThreshold(bitmap);
        black = alpha | (black << 16) | (black << 8) | black;
        white = alpha | (white << 16) | (white << 8) | white;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if((pixels[width * i + j]& 0x000000FF)<threshold)
                    newpixels[width * i + j] = black;
                else newpixels[width * i + j] = white;
            }
        }
        result.setPixels(newpixels, 0, width, 0, 0, width, height);
        return result;
    }
    /**
     * 通过OSTU寻找二值化的最佳阈值
     *
     * @param bitmap 位图
     * @return 返回阈值大小
     */
    public int otsuThreshold(Bitmap bitmap) {
        //获取源位图的宽、高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        //根据宽高创建像素点数组,并将bitmap的rgb值赋给它
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        //创建两个大小为256的数组，用来保存灰度级中每个像素
        // 在整幅图像中的个数和在图中所占比例,先暂时初始为0
        int[] pixelCount = new int[256];
        float[] pixelPro = new float[256];
        for (int i = 0; i < 256; i++) {
            pixelCount[i] = 0;
            pixelPro[i] = 0;
        }

        //统计每个像素在整幅图像中的个数
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = pixels[width * i + j];

                int grey = rgb & 0x000000FF;

                pixelCount[grey]++;
            }
        }
        //统计每个像素占整幅图像中的比例
        for (int i = 0; i < 256; i++) {
            pixelPro[i] = (float) pixelCount[i] / (width * height);
        }
        Log.d("hhhhhhhhhhhhhhhhhhhhhhh","ghggggggggggg");

        //遍历灰度级[0,255]
        float w0, w1, u0tmp, u1tmp, u0, u1, u, deltaTmp, deltaMax=0;
        int threshold = 0;
        for (int i = 0; i < 256; i++)     // i作为阈值
        {
            w0 = w1 = u0tmp = u1tmp = u0 = u1 = u = deltaTmp = 0;
            for (int j = 0; j < 256; j++) {

                if (j <= i)   //背景部分
                {
                    w0 += pixelPro[j];
                    u0tmp += j * pixelPro[j];
                } else   //前景部分
                {
                    w1 += pixelPro[j];
                    u1tmp += j * pixelPro[j];
                }
            }
            if(w0 == 0 || w1 == 0) {
                u0 = u0tmp;
                u1 = u1tmp;
                u = u0tmp + u1tmp;
                deltaTmp = 0;
            } else {
                u0 = u0tmp / w0;
                u1 = u1tmp / w1;
                u = u0tmp + u1tmp;
                deltaTmp = w0 * (u0 - u) * (u0 - u) + w1 * (u1 - u) * (u1 - u);
            }
            Log.d("deltaTmppppppppppppppp","i:"+i+"u0:"+u0+"     u1:"+u1+"       deltatmp:"+deltaTmp);
            if (deltaTmp > deltaMax) {
                deltaMax = deltaTmp;
                threshold = i;
            }
        }

        return threshold;

    }
}

