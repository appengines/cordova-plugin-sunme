package com.openmove.sunmi;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.util.Log;

import com.openmove.sunmi.CallBack;
import com.openmove.sunmi.ESCUtil;
import com.sunmi.peripheral.printer.ExceptionConst;
import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallbcak;
import com.sunmi.peripheral.printer.SunmiPrinterService;


public class SunmiHelper {

    public static int NoSunmiPrinter = 0x00000000;
    public static int CheckSunmiPrinter = 0x00000001;
    public static int FoundSunmiPrinter = 0x00000002;
    public static int LostSunmiPrinter = 0x00000003;

    public static final int STATUS_OK = 1;
    public static final int STATUS_UPDATE = 2;
    public static final int STATUS_EXCEPTION = 3;
    public static final int OUT_OF_PAPER = 4;
    public static final int OVERHEATING = 5;
    public static final int COVER_OPEN = 6;
    public static final int CUTTER_ABNORMAL = 7;
    public static final int CUTTER_RECOVERY = 8;
    public static final int NO_BLACK_MARK = 9;
    public static final int PRINTER_NOT_EXISTS = 505;

    public int connectionStatus = CheckSunmiPrinter;

    private SunmiPrinterService sunmiPrinterService;

    private static SunmiHelper helper = new SunmiHelper();

    private SunmiHelper() {}

    public static SunmiHelper getInstance() {
        return helper;
    }

    private InnerPrinterCallback innerPrinterCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            sunmiPrinterService = service;
            checkSunmiPrinterService(service);
            Log.d("TAG", "onConnected SunmiHelpr");
        }

        @Override
        protected void onDisconnected() {
            sunmiPrinterService = null;
            connectionStatus = LostSunmiPrinter;
            Log.d("TAG", "onDisconnected SunmiHelpr");

        }
    };

    public void initSunmiPrinterService(Context context){
        try {
            boolean ret =  InnerPrinterManager.getInstance().bindService(context,
                    innerPrinterCallback);
            if(!ret){
                connectionStatus = NoSunmiPrinter;
            }
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }

    public void deInitSunmiPrinterService(Context context){
        try {
            if(sunmiPrinterService != null){
                InnerPrinterManager.getInstance().unBindService(context, innerPrinterCallback);
                sunmiPrinterService = null;
                connectionStatus = LostSunmiPrinter;
            }
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
    }

    private void checkSunmiPrinterService(SunmiPrinterService service){
        boolean ret = false;
        try {
            ret = InnerPrinterManager.getInstance().hasPrinter(service);
        } catch (InnerPrinterException e) {
            e.printStackTrace();
        }
        connectionStatus = ret?FoundSunmiPrinter:NoSunmiPrinter;
    }

    public void checkPrinterStatus() throws Exception{
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        String result = "Interface is too low to implement interface";
        try {
            int res = sunmiPrinterService.updatePrinterState();
            switch (res){
                case SunmiHelper.STATUS_OK:
                    result = "printer is running";
                    break;
                case SunmiHelper.STATUS_UPDATE:
                    result = "printer found but still initializing";
                    throw new Exception("");
                case SunmiHelper.STATUS_EXCEPTION:
                    result = "printer hardware interface is abnormal and needs to be reprinted";
                    throw new Exception("");
                case SunmiHelper.OUT_OF_PAPER:
                    result = "printer is out of paper";
                    throw new Exception("");
                case SunmiHelper.OVERHEATING:
                    result = "printer is overheating";
                    throw new Exception("");
                case SunmiHelper.COVER_OPEN:
                    result = "printer's cover is not closed";
                    throw new Exception("");
                case SunmiHelper.CUTTER_ABNORMAL:
                    result = "printer's cutter is abnormal";
                    throw new Exception("");
                case SunmiHelper.CUTTER_RECOVERY:
                    result = "printer's cutter is normal";
                    throw new Exception("");
                case SunmiHelper.NO_BLACK_MARK:
                    result = "not found black mark paper";
                    throw new Exception("");
                case SunmiHelper.PRINTER_NOT_EXISTS:
                    result = "printer does not exist";
                    throw new Exception("");
                default:
                    break;
            }
        } catch (RemoteException e) {
            throw e;
        }
    }

    public int getPrinterStatus() throws Exception{
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        try {
            return sunmiPrinterService.updatePrinterState();
        } catch (RemoteException e) {
            throw e;
        }
    }

    /**
     *  Some conditions can cause interface calls to fail
     *  For example: the version is too low、device does not support
     *  You can see {@link ExceptionConst}
     *  So you have to handle these exceptions
     */
    private void handleRemoteException(RemoteException e){
        //TODO process when get one exception
    }

    public void simplePrintText(String content, float size, final CallBack callBack) throws Exception {
        if(sunmiPrinterService == null){
           throw new Exception(); //TODO handle exceptions.
        }

        if(connectionStatus != FoundSunmiPrinter) {
            throw new Exception(); //TODO handle exceptions.
        }

        int printState = sunmiPrinterService.updatePrinterState();

        if (printState != 1) {
            checkPrinterStatus();
        }

        sunmiPrinterService.printTextWithFont(content, null, size, new InnerResultCallbcak() {
            @Override
            public void onRunResult(boolean isSuccess) throws RemoteException {
                if (isSuccess) {
                    callBack.onSuccess();
                }else{
                    callBack.onError(new Exception("onRunResult success false"));
                }
            }

            @Override
            public void onReturnString(String result) throws RemoteException {

            }

            @Override
            public void onRaiseException(int code, String msg) throws RemoteException {
                callBack.onError(new Exception("onRaiseException"));
            }

            @Override
            public void onPrintResult(int code, String msg) throws RemoteException {
                callBack.onError(new Exception("onPrintResult"));
            }
        });
    }

    public void printText(String text) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        // 0 right, 1 center, 2 left
        sunmiPrinterService.printText(text + "\n", null);
    }

    public void printTextWithFont(String text, int size, boolean isBold) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        if (isBold) {
            sunmiPrinterService.sendRAWData(ESCUtil.boldOn(), null);
        } else {
            sunmiPrinterService.sendRAWData(ESCUtil.boldOff(), null);
        }
        // 0 right, 1 center, 2 left
        sunmiPrinterService.printTextWithFont(text + "\n", null, size, null);

        sunmiPrinterService.sendRAWData(ESCUtil.boldOff(), null);
    }

    public void concatText(String text, int size) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        // 0 right, 1 center, 2 left
        sunmiPrinterService.printTextWithFont(text, null, size, null);
    }

    public void setAlignment(int val) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        sunmiPrinterService.setAlignment(val, null);
    }

    public void printLine() throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        int paper = sunmiPrinterService.getPrinterPaper();

        if(paper == 1){
            sunmiPrinterService.printText("--------------------------------\n", null);
        }else{
            sunmiPrinterService.printText("------------------------------------------------\n",
                    null);
        }
    }

    public void printNewline(int line) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        /*
        String newLines = "\n";

        for(int i = 1; i < line; i++){
            newLines += newLines;
        }
        */

        //sunmiPrinterService.printText(newLines, null);
        sunmiPrinterService.lineWrap(line, null);
    }

    public void printBitmap(Bitmap bitmap) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        sunmiPrinterService.printBitmap(bitmap, null);
    }

    public void printQRCode(String data) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        sunmiPrinterService.printQRCode(data, 10, 0, null);
    }

    public void printBarCode(String data) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        // 7, corrisponde a encode CODE93
        // possibili symbology: "UPC-A", "UPC-E", "EAN13", "EAN8", "CODE39", "ITF", "CODABAR", "CODE93", "CODE128A", "CODE128B", "CODE128C"
        // 0, corrisponde a text null
        // "text null" "upon barcode" "beneath barcode" "up and beneath"
        // height: min 1, max 255
        // width: min 2, max 6
        sunmiPrinterService.printBarCode(data, 7, 128, 2, 0, null);
    }

    public void beginTransaction() throws Exception{
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        sunmiPrinterService.enterPrinterBuffer(true);
        sunmiPrinterService.printerInit(null);
    }

    public void endTransaction(InnerResultCallbcak callback) throws Exception {
        if(sunmiPrinterService == null){
            throw new Exception(); //TODO handle exceptions.
        }

        int printerState = sunmiPrinterService.updatePrinterState();
        if(printerState != SunmiHelper.STATUS_OK){
            callback.onPrintResult(printerState, "Wrong printer state");
        }else{
            sunmiPrinterService.exitPrinterBufferWithCallback(true, callback);
        }
    }
}
