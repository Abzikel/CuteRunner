package com.abzikel;

import com.abzikel.utils.CursorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Menu extends JFrame {
    private final List<Image> idleSprites;
    private final Image background;
    private int currentSpriteIndex = 0;
    private int sawCount = 0;

    public Menu() {
        // Window configuration
        setTitle("Cute Runner - Menu");
        setSize(800, 600);
        setResizable(false);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load sprites and background image
        idleSprites = loadIdleSprites();
        background = loadImage();

        // Main panel to adjust to the window
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Draw background image
                if (background != null)
                    g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

                // Draw current sprite
                if (!idleSprites.isEmpty())
                    g.drawImage(idleSprites.get(currentSpriteIndex), 100, 100, this);
            }
        };

        // Apply the normal cursor to the main panel
        CursorUtil.applyNormalCursor(mainPanel);

        // Add a mouse listener to handle click events globally in this panel
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                CursorUtil.applyClickCursor(mainPanel);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Timer timer = new Timer(200, event -> {
                    CursorUtil.applyNormalCursor(mainPanel);
                    ((Timer) event.getSource()).stop(); // Stop the timer after execution
                });
                timer.setRepeats(false); // Ensure the timer runs only once
                timer.start();
            }
        });

        // Grid Bag Layout to center the buttons
        mainPanel.setLayout(new GridBagLayout());

        // Create button and add them to the panel
        JButton playButton = createButton("Play", e -> {
            // Dispose window and show GameWindow
            dispose();
            new GameWindow(sawCount);
        });

        JButton rulesButton = createButton("Rules", e -> showRules());

        JButton exitButton = createButton("Exit", e -> System.exit(0));

        // Add buttons to the panel
        addButtonsToPanel(mainPanel, playButton, rulesButton, exitButton);

        // Start idle animation
        startAnimation(mainPanel);

        // Add main panel to the window
        getContentPane().add(mainPanel);
        setVisible(true);
    }

    private JButton createButton(String text, java.awt.event.ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 24));

        // Add mouse listener for cursor and delayed action
        button.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                CursorUtil.applyClickCursor(button);
            }

            public void mouseReleased(MouseEvent e) {
                Timer timer = new Timer(200, event -> {
                    CursorUtil.applyNormalCursor(button);

                    // Create a new ActionEvent and trigger the action
                    ActionEvent actionEvent = new ActionEvent(
                            button, // Source (button itself)
                            ActionEvent.ACTION_PERFORMED, // Event type
                            text // Command name (button's text as actionCommand)
                    );

                    actionListener.actionPerformed(actionEvent); // Trigger the action listener
                    ((Timer) event.getSource()).stop(); // Stop the timer after execution
                });
                timer.setRepeats(false); // Ensure the timer runs only once
                timer.start();
            }
        });

        return button;
    }



    private void addButtonsToPanel(JPanel panel, JButton... buttons) {
        // Find the maximum width among the buttons
        int maxWidth = 0;
        for (JButton button : buttons) {
            maxWidth = Math.max(maxWidth, button.getPreferredSize().width);
        }

        // Set all buttons to the maximum width
        for (JButton button : buttons) {
            Dimension buttonSize = new Dimension(maxWidth, button.getPreferredSize().height);
            button.setPreferredSize(buttonSize);
        }

        // Add buttons to the panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;

        for (int i = 0; i < buttons.length; i++) {
            gbc.gridy = i;
            panel.add(buttons[i], gbc);
        }
    }

    private List<Image> loadIdleSprites() {
        // Create temporal List<Image>
        List<Image> sprites = new ArrayList<>();
        for (int index = 1; index <= 16; index++) {
            // Get path and add the ImageIcon to the List
            String path = String.format("/sprites/Idle (%d).png", index);
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(getClass().getResource(path)));
            sprites.add(icon.getImage());
        }

        return sprites;
    }

    private Image loadImage() {
        // Load image from a specific path
        return new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/menu_background.png"))).getImage();
    }

    private void showRules() {
        while (true) {
            // Create the String input for the JOptionPane
            String input = JOptionPane.showInputDialog(
                    this,
                    """
                            To win, avoid 'x' number of saws.
                            Enter 0 for infinite mode.
                            
                            Enter the number of saws:""",
                    "Game Rules",
                    JOptionPane.QUESTION_MESSAGE
            );

            try {
                // Verify if the value is a number
                int value = Integer.parseInt(input);
                if (value >= 0) {
                    sawCount = value; // Store the valid number of saws
                    break;
                } else {
                    // Show error message
                    JOptionPane.showMessageDialog(
                            this,
                            "Please enter a number greater than or equal to 0.",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            } catch (NumberFormatException e) {
                // Show error message
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

}
