package com.mobileappengines.sunmi;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Color;
import android.util.Log;
import android.content.res.Configuration;
import android.provider.Settings;

import com.mobileappengines.sunmi.SunmiHelper;
import com.sunmi.peripheral.printer.InnerResultCallbcak;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class SunMiPrinter extends CordovaPlugin {
    String TAG = "SunMiPrinter";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext){
        Log.e(TAG, action);
        if (action.equals("print")){
            SunmiHelper sunmiHelper = SunmiHelper.getInstance();
    		try{
    		    sunmiHelper.beginTransaction();
                for(int i = 0; i < data.length(); i++){
					JSONObject object = data.getJSONObject(i);
					sunmiHelper.printNewline(1);
                    if(object.has("logo")){
                        String logo = object.getString("logo");
                        byte[] decodedString = Base64.decode(logo, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        Bitmap bLogo = BitmapHelper.getScaledDownBitmap(decodedByte, 368, true);
                        if(bLogo != null) {
							for(int x = 0; x<bLogo.getWidth(); x++){
								for(int y = 0; y<bLogo.getHeight(); y++){
									if(bLogo.getPixel(x, y) == Color.TRANSPARENT){
										bLogo.setPixel(x, y, Color.WHITE);
									}
								}
							}
                            sunmiHelper.printBitmap(bLogo);
							sunmiHelper.printNewline(1);
                        }
                    }
					String title = object.getString("title");
					String subtitle =  object.getString("subtitle");
					String priceVal =  object.getString("price");
					String date =  object.getString("date");
					String details =  object.getString("details");
                    String qr =  object.getString("qr");
					String code =  object.getString("code");
                    sunmiHelper.setAlignment(1);
					sunmiHelper.printTextWithFont(title, 32, true);

					sunmiHelper.printNewline(0);

					sunmiHelper.printTextWithFont(subtitle, 24, true);

					sunmiHelper.printNewline(0);

					String ticketCurrency = "EUR";
					String price = priceVal.replaceAll("0", "Ø");
					sunmiHelper.printTextWithFont(price + " " + ticketCurrency, 32, true);

					sunmiHelper.printNewline(0);

					sunmiHelper.printText(details);

					sunmiHelper.printNewline(0);

					sunmiHelper.printText(date);

					String qrcode = qr;

					//sunmiHelper.setAlignment(0);
					sunmiHelper.printQRCode(qrcode);
					sunmiHelper.setAlignment(1);
					sunmiHelper.printNewline(1);

					sunmiHelper.concatText("Cod. ", 24);
					String[] stringsCode = code.replaceAll("(.{4})(?!$)", "$1 ").split("");
					for(String s : stringsCode){
						if(s.equals("0"))
							s = "Ø";

						sunmiHelper.concatText(s,24);
					}
					sunmiHelper.printNewline(5);
				}

    		    sunmiHelper.endTransaction(new InnerResultCallbcak() {
    		        @Override
    		        public void onRunResult(boolean isSuccess) throws RemoteException {

    		        }

    		        @Override
    		        public void onReturnString(String result) throws RemoteException {

    		        }

    		        @Override
    		        public void onRaiseException(int code, String msg) throws RemoteException {
    		            Exception e = new Exception("onPrintResult code: " + code + " msg: " + msg);
    		        }

    		        @Override
    		        public void onPrintResult(int code, String msg) throws RemoteException {
    		            SunmiHelper sunmiHelper = SunmiHelper.getInstance();
    		            int printStatus = Integer.MAX_VALUE;
    		            RemoteException myException = null;

    		            try {
    		                // wait interface
    		                do {
    		                    printStatus = sunmiHelper.getPrinterStatus();
    		                } while (code == 1 && printStatus == SunmiHelper.STATUS_OK);

    		                switch (printStatus){
    		                    case SunmiHelper.STATUS_OK:
    		                        Log.e(TAG, "OK");
                                    callbackContext.success("done");
    		                        return;
    		                    case SunmiHelper.STATUS_UPDATE:
    		                        myException = new RemoteException("printer found but still initializing");
    		                        break;
    		                    case SunmiHelper.STATUS_EXCEPTION:
    		                        myException = new RemoteException("printer hardware interface is abnormal and needs to be reprinted");
    		                        break;
    		                    case SunmiHelper.OUT_OF_PAPER:
    		                        myException = new RemoteException("printer is out of paper");
    		                        break;
    		                    case SunmiHelper.OVERHEATING:
    		                        myException = new RemoteException("printer is overheating");
    		                        break;
    		                    case SunmiHelper.COVER_OPEN:
    		                        myException = new RemoteException("printer's cover is not closed");
    		                        break;
    		                    case SunmiHelper.NO_BLACK_MARK:
    		                        myException = new RemoteException("not found black mark paper");
    		                        break;
    		                    case SunmiHelper.PRINTER_NOT_EXISTS:
    		                        myException = new RemoteException("printer does not exist");
    		                        break;
    		                    default:
    		                        myException = new RemoteException("unknown exception");
    		                        break;
    		                }
    		            }catch (Exception statusExecption){
    		                myException = new RemoteException(statusExecption.getMessage());
    		            }
                        callbackContext.error(myException.getMessage());

    		        }
    		    });
    		}catch (Exception e){
    		    e.printStackTrace();
                callbackContext.error(e.getMessage());
                return true;
    		}
            return true;



    	}else if (action.equals("printRow")){
			//init every time?
    		SunmiHelper.getInstance().initSunmiPrinterService(cordova.getActivity());

            SunmiHelper sunmiHelper = SunmiHelper.getInstance();
			//Alignment: 0: left; 1: center; 2: right.
            try{
                sunmiHelper.beginTransaction();

				//Currency header top right:
				sunmiHelper.printRow("","","GBP",2,7,3);

				//Items ordered:
                for(int i = 0; i < data.length(); i++){
                    JSONObject object = data.getJSONObject(i);
                    String quantString =  object.optString("quant","");
//                    String itemString =  object.optString("item","-");
                    String itemString =  object.getString("item");
                    String priceString =  object.optString("price","");
                    int width1 =  object.optInt("width1",2);
                    int width2 =  object.optInt("width2",7);
                    int width3 =  object.optInt("width3",3);
//					sunmiHelper.setAlignment(0);
//                    sunmiHelper.printText(printLeft);
//					sunmiHelper.setAlignment(2);
//                    sunmiHelper.printText(printRight);
                    sunmiHelper.printRow(quantString,itemString,priceString,width1,width2,width3);
                    sunmiHelper.printNewline(1);
                }
				sunmiHelper.setAlignment(1);
				sunmiHelper.printText("---------------------------------");
				sunmiHelper.setAlignment(0);
				sunmiHelper.printNewline(2);

                sunmiHelper.endTransaction(new InnerResultCallbcak() {
                    @Override
                    public void onRunResult(boolean isSuccess) throws RemoteException {

                    }

                    @Override
                    public void onReturnString(String result) throws RemoteException {

                    }

                    @Override
                    public void onRaiseException(int code, String msg) throws RemoteException {
                        Exception e = new Exception("onPrintResult code: " + code + " msg: " + msg);
                    }

                    @Override
                    public void onPrintResult(int code, String msg) throws RemoteException {
                        SunmiHelper sunmiHelper = SunmiHelper.getInstance();
                        int printStatus = Integer.MAX_VALUE;
                        RemoteException myException = null;

                        try {
                            // wait interface
                            do {
                                printStatus = sunmiHelper.getPrinterStatus();
                            } while (code == 1 && printStatus == SunmiHelper.STATUS_OK);

                            switch (printStatus){
                                case SunmiHelper.STATUS_OK:
                                    Log.e(TAG, "OK");
                                    callbackContext.success("done");
                                    return;
                                case SunmiHelper.STATUS_UPDATE:
                                    myException = new RemoteException("printer found but still initializing");
                                    break;
                                case SunmiHelper.STATUS_EXCEPTION:
                                    myException = new RemoteException("printer hardware interface is abnormal and needs to be reprinted");
                                    break;
                                case SunmiHelper.OUT_OF_PAPER:
                                    myException = new RemoteException("printer is out of paper");
                                    break;
                                case SunmiHelper.OVERHEATING:
                                    myException = new RemoteException("printer is overheating");
                                    break;
                                case SunmiHelper.COVER_OPEN:
                                    myException = new RemoteException("printer's cover is not closed");
                                    break;
                                case SunmiHelper.NO_BLACK_MARK:
                                    myException = new RemoteException("not found black mark paper");
                                    break;
                                case SunmiHelper.PRINTER_NOT_EXISTS:
                                    myException = new RemoteException("printer does not exist");
                                    break;
                                default:
                                    myException = new RemoteException("unknown exception");
                                    break;
                            }
                        }catch (Exception statusExecption){
                            myException = new RemoteException(statusExecption.getMessage());
                        }
                        callbackContext.error(myException.getMessage());

                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                callbackContext.error(e.getMessage());
                return true;
            }
            return true;

        }else if(action.equals("init")){
    		SunmiHelper.getInstance().initSunmiPrinterService(cordova.getActivity());
    		callbackContext.success("initialized");
            return true;
    	}else if(action.equals("deinit")){
    		SunmiHelper.getInstance().deInitSunmiPrinterService(cordova.getActivity());
    		callbackContext.success("deinitialized");
            return true;
    	}else {
            return false;
        }
    }
}

