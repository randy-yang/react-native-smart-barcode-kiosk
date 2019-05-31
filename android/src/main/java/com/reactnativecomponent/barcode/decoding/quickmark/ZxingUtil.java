package com.reactnativecomponent.barcode.decoding.quickmark;

import tw.com.quickmark.sdk.BarcodeFormat;
import tw.com.quickmark.sdk.Result;


/**
 * Created by randy on 6/30/17.
 */
public class ZxingUtil {
    public static final com.google.zxing.Result toZxingResult(Result result) {
        com.google.zxing.Result newResult = new com.google.zxing.Result(
                result.getText(), null, null, toZxingBarcodeFormat(result.getBarcodeFormat()));
        return newResult;
    }

    public static final com.google.zxing.BarcodeFormat toZxingBarcodeFormat(BarcodeFormat format) {
        return com.google.zxing.BarcodeFormat.valueOf(format.getName());
    }
}
