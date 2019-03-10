package app.septs.idrw.usb;

@SuppressWarnings("PointlessBitwiseExpression")
class Constants {
    static int TIMEOUT = 5000;

    private static int REQUEST_TYPE_STANDARD = 0x00 << 5;
    static int REQUEST_GET_DESCRIPTOR = 0x06;
    static int DT_REPORT = 0x22;

    static int HID_GET_REPORT = 0x01;
    static int HID_SET_REPORT = 0x09;
    private static int HID_REPORT_TYPE_FEATURE = 0x03;

    private static int ENDPOINT_IN = 0x80;
    private static int ENDPOINT_OUT = 0x00;

    private static int REQUEST_TYPE_CLASS = 0x01 << 5;
    private static int RECIPIENT_INTERFACE = 0x01;

    static int CONTROL_IN = ENDPOINT_IN | REQUEST_TYPE_CLASS | RECIPIENT_INTERFACE;
    static int CONTROL_OUT = ENDPOINT_OUT | REQUEST_TYPE_CLASS | RECIPIENT_INTERFACE;
    static int CONTROL_INIT = ENDPOINT_IN | REQUEST_TYPE_STANDARD | RECIPIENT_INTERFACE;

    static int SEND_PACKET_CONTROL = (HID_REPORT_TYPE_FEATURE << 8) | 0x00;
}
