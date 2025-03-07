package com.abzikel.utils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class ImageUtil {

    public static void loadSprites(List<Image> spriteList, int spriteCount, String spriteName) {
        for (int index = 1; index <= spriteCount; index++) {
            String path = String.format("/Sprites/%s (%d).png", spriteName, index);
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(ImageUtil.class.getResource(path)));
            spriteList.add(icon.getImage());
        }
    }

    public static Image loadImage(String path) {
        // Load image from a specific path
        return new ImageIcon(Objects.requireNonNull(ImageUtil.class.getResource(path))).getImage();
    }

}
