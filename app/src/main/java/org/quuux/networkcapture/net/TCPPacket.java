package org.quuux.networkcapture.net;

import java.nio.ByteBuffer;

public class TCPPacket extends Packet{
    public static final int PROTOCOL_NUMBER = 6;

    public static class Flags {
        public static int FIN = 1<<0;
        public static int SYN = 1<<1;
        public static int RST = 1<<2;
        public static int PSH = 1<<3;
        public static int ACK = 1<<4;
        public static int URG = 1<<5;
        public static int ECE = 1<<6;
        public static int CWR = 1<<7;
        public static int NS = 1<<8;
    }


    public TCPPacket(final ByteBuffer buffer) {
        super(buffer);
    }

    public void setSourcePort(short src) {
        buffer.putShort(0, src);
    }

    public int getSourcePort() {
        return buffer.getShort(0) & 0xffff;
    }

    public void setDestPort(short dest) {
        buffer.putShort(2, dest);
    }

    public int getDestPort() {
        return buffer.getShort(2) & 0xffff;
    }

    public void putSeqNumber(int seq) {
        buffer.putInt(4, seq);
    }

    public long getSeqNumber() {
        return buffer.getInt(4);
    }

    public void putAckNumber(int ack) {
        buffer.putInt(8, ack);
    }

    public long getAckNumber() {
        return buffer.getInt(8);
    }

    public void setDataOffsetAndFlags(byte offset, short flags) {
        short s = (short) (((offset & 0xf) << 4) | (flags & 0x1ff));
        buffer.putShort(12, s);
    }

    public int getDataOffset() {
        return buffer.get(12) >> 4;
    }

    public int getFlags() {
        return buffer.getInt(12) & 0x1ff;
    }

    public boolean isSet(final int flag) {
        return (getFlags() & flag) != 0;
    }

    public boolean isFin() {
        return isSet(Flags.FIN);
    }

    public boolean isSyn() {
        return isSet(Flags.SYN);
    }

    public boolean isRst() {
        return isSet(Flags.RST);
    }

    public boolean isPsh() {
        return isSet(Flags.PSH);
    }

    public boolean isAck() {
        return isSet(Flags.ACK);
    }

    public boolean isUrg() {
        return isSet(Flags.URG);
    }

    public boolean isECE() {
        return isSet(Flags.ECE);
    }

    public boolean isCWR() {
        return isSet(Flags.CWR);
    }

    public boolean isNS() {
        return isSet(Flags.NS);
    }

    public int getHeaderSize() {
        return getDataOffset() * 4;
    }

    @Override
    public String inspect() {
        return String.format("TCP: src port=%s / dest port=%s", getSourcePort(), getDestPort());
    }

}
