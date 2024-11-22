package com.abzikel.utils;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CursorUtil {
    private static final Logger logger = Logger.getLogger(CursorUtil.class.getName());
    private static final int CURSOR_SIZE = 36;
    private static Cursor normalCursor;
    private static Cursor clickCursor;

    // Initialize cursors
    static {
        try {
            // Load image for the normal cursor
            String normalCursorPath = "/images/normal_cursor.png";
            Image normalCursorImage = new ImageIcon(
                    Objects.requireNonNull(CursorUtil.class.getResource(normalCursorPath))
            ).getImage().getScaledInstance(CURSOR_SIZE, CURSOR_SIZE, Image.SCALE_SMOOTH);

            // Load image for the click cursor
            String clickCursorPath = "/images/click_cursor.png";
            Image clickCursorImage = new ImageIcon(
                    Objects.requireNonNull(CursorUtil.class.getResource(clickCursorPath))
            ).getImage().getScaledInstance(CURSOR_SIZE, CURSOR_SIZE, Image.SCALE_SMOOTH);

            // Create the cursor
            normalCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    normalCursorImage, new Point(0, 0), "Normal Cursor"
            );
            clickCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                    clickCursorImage, new Point(0, 0), "Click Cursor"
            );
        } catch (Exception e) {
            // Error log
            logger.log(Level.SEVERE, "Error initializing cursors", e);
        }
    }

    // Apply normal cursor
    public static void applyNormalCursor(Component component) {
        component.setCursor(normalCursor);
    }

    // Apply click cursor
    public static void applyClickCursor(Component component) {
        component.setCursor(clickCursor);
    }
}

