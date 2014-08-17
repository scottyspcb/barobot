package com.barobot.isp.enums;

public enum Board {
	BAROBOT_CARRET              ("auno",ChipTypes.M328P,  UploadProtocols.STK500,  115200, "Barobot Carret"),
	BAROBOT_MAINBOARD           ("auno",ChipTypes.M328P,  UploadProtocols.STK500,  115200, "Barobot Mainboard"),
	BAROBOT_UPANEL              ("auno",ChipTypes.M8,     UploadProtocols.STK500,  115200, "Barobot U-Panel"),
	BAROBOT_TESTER              ("auno",ChipTypes.M328P,  UploadProtocols.STK500,  115200, "Barobot Test board"),

    ARDUINO_UNO                 ("auno",ChipTypes.M328P,  UploadProtocols.STK500,  115200, "Arduino Uno"),
    ARDUINO_MEGA_2560_ADK       ("mg25",ChipTypes.M2560,  UploadProtocols.STK500V2,115200, "Arduino Mega 2560 or ADK"),
    ARDUINO_PRO_5V_328          ("pm53",ChipTypes.M328P,  UploadProtocols.STK500,   57600, "Arduino Pro or Pro Mini (5V, 16MHz) ATmega328"),
    ARDUINO_PRO_5V_168          ("pm51",ChipTypes.M168,   UploadProtocols.STK500,   19200, "Arduino Pro or Pro Mini (5V, 16MHz) ATmega168");

    public final String idText;
    public final int    chipType;
    public final int    uploadProtocol;
    public final int    uploadBaudrate;
    public final String text;

    private Board( String idText, int chipType, int uploadProtocol, int uploadBaudrate,String text) {
        this.idText         = idText;
        this.chipType       = chipType;
        this.uploadProtocol = uploadProtocol;
        this.uploadBaudrate = uploadBaudrate;
        this.text           = text;
    }
    public class UploadProtocols {
        public static final int STK500       = 1;
        public static final int STK500V2     = 2;
    }
    public class ChipTypes {
        public static final int M8           = 1;
        public static final int M168         = 2;
        public static final int M328P        = 3;
        public static final int M1280        = 4;
        public static final int M2560        = 5;
        public static final int ATMEGA32U4   = 6;
        public static final int M1284P       = 7;
    }


}
