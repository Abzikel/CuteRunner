package com.abzikel;

import com.abzikel.utils.CursorUtil;
import com.abzikel.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Menu extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private final List<Image> idleSprites = new ArrayList<>();
    private final Image background;
    private int currentSpriteIndex = 0;
    private int obstacleLimit = 0;

    public Menu() {
        // Window configuration
        setTitle("Cute Runner - Menu");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set application icon
        setIconImage(ImageUtil.loadImage("/sprites/Idle (1).png"));

        // Load sprites and background image
        ImageUtil.loadSprites(idleSprites, 16, "Idle");
        background = ImageUtil.loadImage("/images/background_menu.png");

        // Main panel to adjust to the window
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw background image
                if (background != null)
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

                // Draw current sprite
                if (!idleSprites.isEmpty()) {
                    // Get a better position for the idle sprite
                    int spriteAxisX = (int) (100 * ((double) getWidth() / 800));
                    int spriteAxisY = (int) (100 * ((double) getHeight() / 600));
                    g.drawImage(idleSprites.get(currentSpriteIndex), spriteAxisX, spriteAxisY, this);
                }
            }
        };

        // Grid Bag Layout to center the buttons
        mainPanel.setLayout(new GridBagLayout());

        // Create buttons and add them to the panel
        JButton playButton = createButton("btn_play", e -> {
            // Dispose window and show GameWindow
            dispose();
            new GameWindow(obstacleLimit);
        });
        JButton rulesButton = createButton("btn_rules", e -> showRules());
        JButton exitButton = createButton("btn_exit", e -> System.exit(0));

        // Add buttons to the panel
        addButtonsToPanel(mainPanel, playButton, rulesButton, exitButton);

        // Apply the normal cursor to the main panel
        CursorUtil.applyNormalCursor(this);

        // Add a mouse listener to handle click events globally in this panel
        CursorUtil.addCursorBehavior(this, null, null, 200);

        // Start idle animation
        startAnimation(mainPanel);

        // Add main panel to the window
        getContentPane().add(mainPanel);
        setVisible(true);
    }

    private JButton createButton(String path, ActionListener actionListener) {
        // Load the images for the button
        String normalImagePath = String.format("/images/%s_normal.png", path);
        String hoverImagePath = String.format("/images/%s_hover.png", path);

        // Get ImageIcon using the previous paths
        ImageIcon normalIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(normalImagePath)));
        ImageIcon hoverIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(hoverImagePath)));

        // Create the button and set the images (normal and hover)
        JButton button = new JButton(normalIcon);
        button.setRolloverIcon(hoverIcon);

        // Remove button decorations
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);

        // Add mouse listener for cursor and delayed action
        CursorUtil.addCursorBehavior(button, () -> {
            ActionEvent actionEvent = new ActionEvent(
                    button,
                    ActionEvent.ACTION_PERFORMED,
                    path
            );
            actionListener.actionPerformed(actionEvent);
        }, path, 200);

        return button;
    }

    private void addButtonsToPanel(JPanel panel, JButton... buttons) {
        // Configure GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        // Add buttons to the panel
        for (int index = 0; index < buttons.length; index++) {
            gbc.gridy = index;
            panel.add(buttons[index], gbc);
        }
    }

    private void showRules() {
        while (true) {
            // Show the input dialog
            String input = JOptionPane.showInputDialog(
                    this,
                    "To win, avoid 'x' number of obstacles.\n" +
                            "Enter 0 for infinite mode.\n\n" +
                            "Enter the number of obstacles:",
                    "Game Rules",
                    JOptionPane.QUESTION_MESSAGE
            );

            // Check if the user clicked "Cancel"
            if (input == null) {
                return; // Exit if canceled
            }

            try {
                // Verify if the value is a number
                int value = Integer.parseInt(input);
                if (value >= 0) {
                    obstacleLimit = value; // Store the valid number
                    break;
                } else {
                    // Show error message for invalid input
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a number greater than or equal to 0.",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            } catch (NumberFormatException e) {
                // Show error message for non-numeric input
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    private void startAnimation(JPanel panel) {
        // Cycle of sprites
        Timer animationTimer = new Timer(100, e -> {
            // Cycle of sprites
            currentSpriteIndex = (currentSpriteIndex + 1) % idleSprites.size();
            panel.repaint();
        });

        // Start time
        animationTimer.start();
    }

    public static void main(String[] args) {
        // Launch the menu window
        SwingUtilities.invokeLater(Menu::new);
    }

}
