package com.mesutgolcuk.rehberuygulamasi;

/**
 * Relation of a contact with the user
 */
public class Relation {

    private int incomingCalls;
    private int outgoingCalls;
    private int missCalls;
    private double incomingDuration;
    private double outgoingDuration;
    private int receivedSms;
    private int sentSms;

    public Relation() {
        incomingCalls = 0;
        outgoingCalls = 0;
        missCalls = 0;
        incomingDuration = 0.0;
        outgoingDuration = 0.0;
        receivedSms = 0;
        sentSms = 0;
    }
    /**
     * new incoming call
     */
    public void incrementIncomingCalls(){
        incomingCalls++;
    }

    /**
     * new outgoing call
     */
    public void incrementOutgingCalls(){
        outgoingCalls++;
    }

    /**
     * new miss call
     */
    public void incrementMissCalls(){
        missCalls++;
    }

    /**
     * new incoming call duration
     * @param duration duration of call
     */
    public void addIncomingDuration(double duration){
        incomingDuration += duration;
    }
    /**
     * new outgoing call duration
     * @param duration duration of call
     */
    public void addOutGoingDuration(double duration){
        outgoingDuration += duration;
    }

    /**
     * resets all data
     */
    public void reset(){
        setIncomingCalls(0);
        setIncomingDuration(0);
        setMissCalls(0);
        setOutgoingCalls(0);
        setOutgoingDuration(0);
    }
    public int getIncomingCalls() {
        return incomingCalls;
    }

    public void setIncomingCalls(int incomingCalls) {
        this.incomingCalls = incomingCalls;
    }

    public int getOutgoingCalls() {
        return outgoingCalls;
    }

    public void setOutgoingCalls(int outgoingCalls) {
        this.outgoingCalls = outgoingCalls;
    }

    public int getMissCalls() {
        return missCalls;
    }

    public void setMissCalls(int missCalls) {
        this.missCalls = missCalls;
    }

    public double getIncomingDuration() {
        return incomingDuration;
    }

    public void setIncomingDuration(double incomingDuration) {
        this.incomingDuration = incomingDuration;
    }

    public double getOutgoingDuration() {
        return outgoingDuration;
    }

    public void setOutgoingDuration(double outgoingDuration) {
        this.outgoingDuration = outgoingDuration;
    }

    public int getReceivedSms() {
        return receivedSms;
    }

    public void setReceivedSms(int receivedSms) {
        this.receivedSms = receivedSms;
    }

    public int getSentSms() {
        return sentSms;
    }

    public void setSentSms(int sentSms) {
        this.sentSms = sentSms;
    }


}
