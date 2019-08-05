package edu.buffalo.cse.cse486586.groupmessenger2;


import java.io.Serializable;
import java.util.Comparator;

public class MessageContents implements Serializable, Comparator<MessageContents> {

    float agreedSequence;
    int senderPort;
    String TextMessage;
    int flag;
    int recieverPort;
    String crashedPort;

    public MessageContents() {
    }

    public MessageContents( float agreedSequence, int senderPort , String TextMessage, int flag , int Msgid , int recieverPort,String crashedPort) {

        this.agreedSequence = agreedSequence;
        this.senderPort = senderPort;
        this.TextMessage = TextMessage;
        this.flag = flag;
        this.recieverPort = recieverPort;
        this.crashedPort = crashedPort;
    }



    public float getAgreedSequence() {

        return agreedSequence;
    }

    public void setAgreedSequence(float agreedSequence) {

        this.agreedSequence = agreedSequence;
    }

    public int getSenderPort() {

        return senderPort;
    }

    public void setSenderPort(int senderPort) {

        this.senderPort = senderPort;
    }

    public String getTextMessage() {

        return TextMessage;
    }

    public void setTextMessage(String TextMessage) {

        this.TextMessage = TextMessage;
    }

    public int getFlag() {

        return flag;
    }

    public void setFlag(int flag) {

        this.flag = flag;
    }


    public int getRecieverPort() {

        return recieverPort;
    }

    public void setRecieverPort(int recieverPort) {

        this.recieverPort = recieverPort;
    }

    public float getNewproposedseq(int a , int b){
//        int c;
//        c = a * b;
        Float d ;
        String c  = b + "." + a;
        d = Float.parseFloat(c);
        return d;
    }

    public String getCrashedPort() {

        return crashedPort;
    }

    public void setCrashedPort(String crashPort) {

        this.crashedPort = crashPort;
    }




    @Override
    public int compare(MessageContents lhs, MessageContents rhs) {
        if(lhs.getAgreedSequence() < rhs.getAgreedSequence()) return -1;
        else if(lhs.getAgreedSequence() > rhs.getAgreedSequence()) return 1;
        else
        {
            if(lhs.getSenderPort() > rhs.getSenderPort()) return 1;
            else return -1;
        }
    }
}
