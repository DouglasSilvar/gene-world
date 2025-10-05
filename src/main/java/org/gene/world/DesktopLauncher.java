package org.gene.world;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import org.gene.world.world.GeneWorldGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Gene World - Pixel Chunks");
        cfg.setWindowedMode(1000, 1000);
        cfg.setHdpiMode(HdpiMode.Pixels); // 🔑 evita meio-pixel com escala 125%/150%
        cfg.useVsync(true);
        cfg.setForegroundFPS(60);
        new Lwjgl3Application(new GeneWorldGame(), cfg);
    }
}
