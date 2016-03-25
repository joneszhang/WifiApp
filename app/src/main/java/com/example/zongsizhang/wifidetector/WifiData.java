package com.example.zongsizhang.wifidetector;

/**
 * Created by zongsizhang on 3/24/16.
 */
public class WifiData {
    private String time = "";
    private String ssid = "";
    private int level = 0;
    private String mac_adress = "";

    public String toText(){
        return "[SSID]"+ ssid + "  [LEVEL]" + level + "  [MAC_ADRESS]" + mac_adress + "  [TIME]"+time;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getMac_adress() {
        return mac_adress;
    }

    public void setMac_adress(String mac_adress) {
        this.mac_adress = mac_adress;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
