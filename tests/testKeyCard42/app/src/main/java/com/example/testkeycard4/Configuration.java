package com.example.testkeycard4;

public class Configuration {

    private static boolean useNetCardChannel = true;
    private static String netCardChannelHostname = "10.0.2.2";

    private static String netCardChannelPort="38099";

    public  static  boolean  isUseNetCardChannel() {
        return  Configuration.useNetCardChannel;
    }

    public static  void setUseNetCardChannel(boolean useNetCardChannel_) {
        Configuration.useNetCardChannel = useNetCardChannel_;
    }

    public static  String getNetCardChannelHostname() {
        return  Configuration.netCardChannelHostname;
    }

    public static  void setNetCardChannelHostname(String netCardChannelHostname_) {
        Configuration.netCardChannelHostname = netCardChannelHostname_;
    }

    public static String getNetCardChannelPort() {
        return  Configuration.netCardChannelPort;
    }

    public static void setNetCardChannelPort(String netCardChannelPort) {
        Configuration.netCardChannelPort = netCardChannelPort;
    }
}
