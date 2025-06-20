package net.venitstudios.darkcomputers.computing.components.computer;

import java.util.Arrays;
import java.util.HashMap;

public class DD_DC16 {
    public static final char[] charSet = "@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_ !\"#$%&'()*+,-./0123456789:;<=>?".toCharArray();

    public DD_DC16() {
        resetBuffer();
        System.out.println("Creating New Display Driver");
    }

    public char[] screenBuffer;
    public BusDC16 bus;
    public void resetBuffer() {
        screenBuffer = new char[29 * 17];

        for (int i = 0; i < 29 * 17; i++) {
            screenBuffer[i] = charSet[32];
        }

//        System.out.println("Reset Char Buffer");
    }
    public char readBuffer(int idx) {
        if (idx > 0 && idx < screenBuffer.length) {
            return screenBuffer[idx];
        }
        return '?';
    }
    public void writeBuffer(int idx, int chr) {
        if (idx >= 0 && idx < screenBuffer.length) {
            if (chr >= 0 && chr < charSet.length) {
//                System.out.println("setting char at " + idx + " to " + charSet[chr]);
                screenBuffer[idx] = charSet[chr];
            } else {
                screenBuffer[idx] = charSet[63];
            }
        }
    }

}
