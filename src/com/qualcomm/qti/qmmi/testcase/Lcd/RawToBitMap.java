package com.qualcomm.qti.qmmi.testcase.Lcd;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Raw转Bitmap
 *
 */
public class RawToBitMap {
    /**
     * 从流中读取数组
     *
     * @param stream 输入流
     * @return
     */
    public static byte[] readByteArrayFormStream(InputStream stream) {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] tmp = new byte[1024];
            while ((len = stream.read(tmp)) != -1) {
                outStream.write(tmp, 0, len);
            }

            byte[] data = outStream.toByteArray();

            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * 8位灰度转Bitmap
     * <p>
     * 图像宽度必须能被4整除
     *
     * @param data   裸数据
     * @param width  图像宽度
     * @param height 图像高度
     * @return
     */
    public static Bitmap convert8bit(byte[] data, int width, int height) {
        		
		int x;
		int k;
		int numPixel = 224*172*5;
		int nByte = numPixel*3/2;
		char[] Raw = new char[numPixel];
		byte[] Bits = new byte[nByte * 4]; //RGBA 数组
		byte[] b = new byte[3];
		// 3 ge 8bit to 2 ge 16bit
		for(x = 0,k =0; x < nByte;){
		b[0] = data[x];x++;
		b[1] = data[x];x++;
		b[2] = data[x];x++;
		Raw[k] =(char) ((b[0]<<4)|((b[2]>>0)&0xF));k++;
		Raw[k] =(char) ((b[1]<<4)|((b[2]>>4)&0xF));k++;
		}
		
        int i;
        for (i = 0; i < numPixel; i++) {
            // 原理：4个字节表示一个灰度，则RGB  = 灰度值，最后一个Alpha = 0xff;
            char rawdata = Raw[i];
			rawdata = (char) (rawdata>>8);
			byte rawdata1 =(byte) rawdata;
            Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = rawdata1;
            Bits[i * 4 + 3] = -1; //0xff
        }

        // Bitmap.Config.ARGB_8888 表示：图像模式为8位
        Bitmap bmp = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return bmp;
    }

    /**
     * 24位灰度转Bitmap
     * <p>
     * 图像宽度必须能被4整除
     *
     * @param data   裸数据
     * @param width  图像宽度
     * @param height 图像高度
     * @return
     */
    public static Bitmap convert24bit(byte[] data, int width, int height) {
        byte[] Bits = new byte[data.length * 4]; //RGBA 数组

        int i;

        // data.length / 3 表示 3位为一组
        for (i = 0; i < data.length / 3; i++) {
            // 原理：24位是有彩色的，所以要复制3位，最后一位Alpha = 0xff;
            Bits[i * 4] = data[i * 3];
            Bits[i * 4 + 1] = data[i * 3 + 1];
            Bits[i * 4 + 2] = data[i * 3 + 2];
            Bits[i * 4 + 3] = -1;
        }

        // Bitmap.Config.ARGB_8888 表示：图像模式为8位
        Bitmap bmp = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return bmp;
    }

    /**
     * 8位灰度转Bitmap
     *
     * @param stream 输入流
     * @param width  图像宽度
     * @param height 图像高度
     * @return
     */
    public static Bitmap convert8bit(InputStream stream, int width, int height) {
        return convert8bit(readByteArrayFormStream(stream), width, height);
    }

    /**
     * 24位灰度转Bitmap
     *
     * @param stream   输入流
     * @param width  图像宽度
     * @param height 图像高度
     * @return
     */
    public static Bitmap convert24bit(InputStream stream, int width, int height) {
        return convert24bit(readByteArrayFormStream(stream), width, height);
    }
}
