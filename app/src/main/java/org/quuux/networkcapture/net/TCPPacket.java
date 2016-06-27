package org.quuux.networkcapture.net;

import org.quuux.networkcapture.util.Util;

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

    public int getHeaderSize() {
        return getDataOffset() * 4;
    }

    @Override
    public String inspect() {
        return String.format("TCP: src port=%s / dest port=%s / seq=%s / ack=%s / data offset=%s / flags=%s",
                getSourcePort(), getDestPort(), getSeqNumber(), getAckNumber(), getDataOffset(), Integer.toBinaryString(getFlags()));
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
        return (buffer.get(12) & 0xff) >>> 4;
    }

    public int getFlags() {
        return buffer.getShort(12) & 0x1ff;
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

    public void setWindowSize(short windowSize) {
        buffer.putShort(14, windowSize);
    }

    public short getWindowSize() {
        return buffer.getShort(14);
    }

    public void setChecksum(final IPv4Packet ipPacket) {
        buffer.putShort(16, (short) 0);

        int ipSegmentSize = ipPacket.getLength() - ipPacket.getHeaderSize();
        int tcpSegmentSize = ipSegmentSize - getHeaderSize();
        int pseudoSegmentSize = ipSegmentSize + 12;
        final ByteBuffer pseudoBuffer = ByteBuffer.allocate(pseudoSegmentSize);

        // write pseudo header
        byte[] addr = new byte[4];
        ipPacket.getSource(addr);

        for (int i=0; i<4; i++)
            pseudoBuffer.put(i, addr[i]);

        ipPacket.getDest(addr);
        for (int i=0; i<4; i++)
            pseudoBuffer.put(4 + i, addr[i]);

        pseudoBuffer.put(9, (byte) (ipPacket.getProtocol() & 0xff));
        pseudoBuffer.putShort(10, (short) tcpSegmentSize);

        // copy tcp header + segment
        pseudoBuffer.position(12);
        pseudoBuffer.put(buffer.array(), 0, ipSegmentSize);
        pseudoBuffer.position(0);

        long checksum = Util.calculateChecksum(pseudoBuffer.array(), pseudoSegmentSize);
        buffer.putShort(16, (short) checksum);
    }

    public short getChecksum() {
        return buffer.getShort(16);
    }

    public void setUrgentPointer(short urgentPointer) {
        buffer.putShort(18, urgentPointer);
    }

    public short getUrgentPointer() {
        return buffer.getShort(18);
    }
}
