package com.abzikel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        // Set the global custom cursor
        setGlobalCustomCursor();

        // Launch the menu window on the Swing event thread
        SwingUtilities.invokeLater(Menu::new);
    }

    private static void setGlobalCustomCursor() {
        // Load the cursor image
        String cursorPath = "/images/cursor.png";
        ImageIcon cursorIcon = new ImageIcon(
                Objects.requireNonNull(Main.class.getResource(cursorPath))
        );
        Image cursorImage = cursorIcon.getImage();

        // Create the custom cursor
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImage, new Point(0, 0), "Custom Cursor"
        );

        // Listen for focus changes and apply the cursor to the focused component
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", evt -> {
            if (evt.getNewValue() instanceof Component comp) {
                comp.setCursor(customCursor);
            }
        });

        // Apply the cursor to every window and component when generated or clicked
        Toolkit.getDefaultToolkit().addAWTEventListener(e -> {
            Object source = e.getSource();
            if (source instanceof Window window) {
                if (e.getID() == WindowEvent.WINDOW_OPENED || e.getID() == WindowEvent.WINDOW_ACTIVATED) {
                    applyCursorToWindow(window, customCursor);
                }
            } else if (source instanceof Component component) {
                if (e.getID() == MouseEvent.MOUSE_ENTERED) {
                    component.setCursor(customCursor);
                }
            }
        }, AWTEvent.WINDOW_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
    }

    private static void applyCursorToWindow(Window window, Cursor cursor) {
        // Set the cursor for the window and all components within it
        window.setCursor(cursor);
        for (Component component : getAllComponents(window)) {
            applyCursorToComponent(component, cursor);
        }

        // Ensure the cursor stays applied even when the window opens or gains focus
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                applyCursorToWindow(window, cursor);
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                applyCursorToWindow(window, cursor);
            }
        });
    }

    private static void applyCursorToComponent(Component component, Cursor cursor) {
        // Set the cursor for the component
        component.setCursor(cursor);

        // If it's a container, apply the cursor recursively to its children
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyCursorToComponent(child, cursor);
            }
        }

        // Add a mouse listener to ensure the cursor updates on interaction
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setCursor(cursor);
            }
        });
    }

    private static Component[] getAllComponents(Container container) {
        // Define all components to change the cursor
        Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof Container) {
                Component[] childComponents = getAllComponents((Container) component);
                components = concat(components, childComponents);
            }
        }
        return components;
    }

    private static Component[] concat(Component[] a, Component[] b) {
        Component[] result = new Component[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }
}
