package tw.imonkey.fu2go;

import java.util.HashMap;
import java.util.Map;

public class rvDevice {
    private String pin;
    private String pinId;
    private String description ;
    private String pinType;
    private String pinState;
    private String memberEmail;
    private Long timeStamp;
    public rvDevice() {
    }

    public rvDevice(String pin,String pinId,String pinType,String pinState,String description, String memberEmail,Long timeStamp) {

        this.pin=pin;
        this.pinId=pinId;
        this.description=description;
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
    public String getDescription() {
        return description;
    }
    public String getpinType() {
        return pinType;
    }
    public String getpinState() {
        return pinState;
    }
    public String getMemberEmail() {
        return memberEmail;
    }
    public Long getTimeStamp() {
        return timeStamp;
    }
}
