/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reactnativecomponent.barcode.decoding.quickmark;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

//import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
//import com.google.zxing.ReaderException;
//import com.google.zxing.Result;
//import com.google.zxing.common.HybridBinarizer;
import com.reactnativecomponent.barcode.CaptureView;
import com.reactnativecomponent.barcode.R;
import com.reactnativecomponent.barcode.camera.CameraManager;
import com.reactnativecomponent.barcode.camera.PlanarYUVLuminanceSource;
//import com.reactnativecomponent.barcode.decoding.zxing.DecodeThread;

import java.util.Hashtable;

import tw.com.quickmark.sdk.qmcore;
//import tw.com.quickmark.sdk.BarcodeFormat;
//import tw.com.quickmark.sdk.Result;

final class QmDecodeHandler extends Handler {

  private static final String TAG = QmDecodeHandler.class.getSimpleName();

  private final CaptureView captureView;
  private final MultiFormatReader multiFormatReader;

  private qmcore qmDecoder;
  private int decodeFormat;

  QmDecodeHandler(CaptureView captureView, Hashtable<DecodeHintType, Object> hints) {
    multiFormatReader = new MultiFormatReader();
    multiFormatReader.setHints(hints);
    this.captureView = captureView;
    this.qmDecoder = captureView.qmDecoder;
//    decodeFormat = 31;
    decodeFormat |= qmcore.TWOD_QRCODE;
    decodeFormat |= qmcore.TWOD_DATAMATRIX;
    decodeFormat |= qmcore.ONED_EAN;
    decodeFormat |= qmcore.ONED_CODE39;
    decodeFormat |= qmcore.ONED_CODE128;
  }

  @Override
  public void handleMessage(Message message) {
    int id = message.what;
//      Log.i(TAG, "decode quit");
      if (id == R.id.decode) {
        if (captureView.decodeFlag) {
          decode((byte[]) message.obj, message.arg1, message.arg2);
        }
      } else if (id == R.id.quit) {
//        Log.i(TAG, "decode quit");
          Looper.myLooper().quit();
      }

  }

  /**
   * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
   * reuse the same reader objects from one decode to the next.
   *
   * @param data   The YUV preview frame.
   * @param width  The width of the preview frame.
   * @param height The height of the preview frame.
   */
  private void decode(byte[] data, int width, int height) {
    /*
       旋轉畫面
       +--------+      +----+
       |        |  ->  |    |
       +--------+      |    |
                       |    |
                       +----+
       1 2 3 4
       5 6 7 8
       往右 90 度
       5 1
       6 2
       7 3
       8 4
     */
    byte[] rotatedData = new byte[data.length];
    for (int y = 0; y < height; y++) {
     for (int x = 0; x < width; x++) {
       int rotatedIdx = x * height + height - y - 1;
       int sourceIdx = x + y * width;
         rotatedData[rotatedIdx] = data[sourceIdx];
     }
    }
    // 取得掃描範圍框中的畫面資料
    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(rotatedData, height, width);
//    PlanarYUVLuminanceSource source = CameraManager.get().buildLuminanceSource(data, width, height);

    byte[] matrix = source.getMatrix();

    int sWidth = source.getWidth();
    int sHeight = source.getHeight();
    tw.com.quickmark.sdk.Result qmResult = this.qmDecoder.decode(
            matrix, sWidth, sHeight, 8, decodeFormat);
    if (qmResult != null) {
      Message message = Message.obtain(
              captureView.getHandler(),
              R.id.decode_succeeded,
              ZxingUtil.toZxingResult(qmResult));
      // 下面那段應該用不到
//      Bundle bundle = new Bundle();
//      bundle.putParcelable(QmDecodeThread.BARCODE_BITMAP, source.renderCroppedGreyscaleBitmap());
//      message.setData(bundle);
//      Log.d(TAG, "Sending decode succeeded message...");
      message.sendToTarget();
    } else {
      Message message = Message.obtain(captureView.getHandler(), R.id.decode_failed);
      message.sendToTarget();
    }
  }

}
