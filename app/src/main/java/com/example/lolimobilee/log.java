package com.example.lolimobilee;

public class log {
    private String userinput;
    private String lolioutput;
    private String time;

    public log() {} // Default constructor for Firestore

    public log(String userinput, String lolioutput, String time) {
        this.userinput = userinput;
        this.lolioutput = lolioutput;
        this.time = time;
    }

    public String getUserinput() {
        return userinput;
    }

    public String getLolioutput() {
        return lolioutput;
    }

    public String getTime() {
        return time;
    }
}
