package tw.imonkey.fu2go;

import java.util.HashMap;
import java.util.Map;

public class rvDevice {
    private String pin;
    private String pinId;
    private String pinFunction ;
    private String pinType;
    private String pinState;
    private String memberEmail;
    private Long timeStamp;
    public rvDevice() {
    }

    public rvDevice(String pin,String pinId,String pinType,String pinState,String function, String memberEmail,Long timeStamp) {

        this.pin=pin;
        this.pinId=pinId;
        this.pinFunction=pinFunction;
        this.pinType=pinType;
        this.pinState=pinState;
        this.memberEmail=memberEmail;
        this.timeStamp=timeStamp;

    }

    public String getPin() {
        return pin;
    }
    public String getPinId() {
        return pinId;
    }
    public String getPinFunction() {
        return pinFunction;
    }
    public String getPinType() {
        return pinType;
    }
    public String getPinState() {
        return pinState;
    }
    public String getMemberEmail() {
        return memberEmail;
    }
    public Long getTimeStamp() {
        return timeStamp;
    }
}
