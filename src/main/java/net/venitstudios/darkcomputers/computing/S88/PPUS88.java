package net.venitstudios.darkcomputers.computing.S88;

import javax.swing.plaf.PanelUI;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.random.RandomGenerator;

public class PPUS88 {
    public int screenWidth = 28;
    public int screenHeight = 16;
    public byte[] charBuf = new byte[(screenWidth + 1) * (screenHeight + 1)];
    public byte[] charRom = new byte[512];
    public static final String romPath = "assets/darkcomputers/ppu_char/main.dcfont";
    public PPUS88() {
        resetPPU();
    }


    public void resetPPU() {
        charBuf = new byte[(screenWidth + 1) * (screenHeight + 1)];
        charRom = new byte[512];
        loadDefaultCharRom();
    }
    public void loadDefaultCharRom() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(romPath);
        if (inputStream != null) {
            try {
                charRom = inputStream.readAllBytes();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
