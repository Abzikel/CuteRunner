package com.abzikel.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
            String normalCursorPath = "/images/cursor_normal.png";
            Image normalCursorImage = new ImageIcon(
                    Objects.requireNonNull(CursorUtil.class.getResource(normalCursorPath))
            ).getImage().getScaledInstance(CURSOR_SIZE, CURSOR_SIZE, Image.SCALE_SMOOTH);

            // Load image for the click cursor
            String clickCursorPath = "/images/cursor_click.png";
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

    // Add the cursor behavior after pressing
    public static void addCursorBehavior(Component component, Runnable action, String actionCommand, long minPressDuration) {
        component.addMouseListener(new MouseAdapter() {
            // Variable to record the time when the mouse is pressed
            private long pressTime;

            @Override
            public void mousePressed(MouseEvent e) {
                // Change the cursor to the "click" cursor and record the time of the press
                applyClickCursor(component);
                pressTime = System.currentTimeMillis();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Calculate the time elapsed since the mouse was pressed
                long elapsedTime = System.currentTimeMillis() - pressTime;

                if (elapsedTime < minPressDuration) {
                    // If the press duration is less than the minimum, wait for the remaining time
                    long remainingTime = minPressDuration - elapsedTime;
                    Timer timer = new Timer((int) remainingTime, event -> {
                        // Change the cursor back to the normal cursor and trigger the action
                        applyNormalCursor(component);
                        triggerAction(action, component, actionCommand);
                        ((Timer) event.getSource()).stop();
                    });

                    // Start time
                    timer.setRepeats(false);
                    timer.start();
                } else {
                    // If the press duration is sufficient, immediately change the cursor back to normal
                    applyNormalCursor(component);
                    triggerAction(action, component, actionCommand);
                }
            }
        });
    }

    // Trigger an acton when is required
    private static void triggerAction(Runnable action, Component component, String actionCommand) {
        if (action != null) {
            if (actionCommand != null) {
                // Create an ActionEvent for the given component and action command
                new ActionEvent(component, ActionEvent.ACTION_PERFORMED, actionCommand);
            }

            // Execute the provided action
            action.run();
        }
    }

}
