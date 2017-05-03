package net.mitrol.codec;

import net.mitrol.codec.opus.Opus;

/**
 * Created by francisco.decuzzi on 01/09/2016.
 */
public class OpusCipherOptions {

    public static final int DEFAULT_COMPLEXITY = 0;
    public static final int DEFAULT_BANDWIDTH = Opus.OPUS_BANDWIDTH_NARROWBAND;
    public static final int DEFAULT_SIGNAL_TYPE = Opus.OPUS_SIGNAL_VOICE;
    public static final int DEFAULT_APPLICATION_TYPE = Opus.OPUS_APPLICATION_VOIP;

    private int complexity;
    private int bandwidth;
    private int signalType;
    private int applicationType;

    public OpusCipherOptions() {
        this.complexity = DEFAULT_COMPLEXITY;
        this.bandwidth = DEFAULT_BANDWIDTH;
        this.signalType = DEFAULT_SIGNAL_TYPE;
        this.applicationType = DEFAULT_APPLICATION_TYPE;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int getSignalType() {
        return signalType;
    }

    public void setSignalType(int signalType) {
        this.signalType = signalType;
    }

    public int getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(int applicationType) {
        this.applicationType = applicationType;
    }
}
