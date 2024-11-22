package com.abzikel;

import com.abzikel.pojos.Cloud;
import com.abzikel.pojos.Saw;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameWindow extends JFrame {
    private final List<Image> runSprites;
    private final List<Image> jumpSprites;
    private final List<Image> deathSprites;
    private final List<Cloud> clouds = new ArrayList<>();
    private final List<Saw> saws = new ArrayList<>();
    private final Image background, sawImage;
    private TexturePaint texturePaint;
    private final int sawCount;
    private final int groundY;
    private int x1, x2;
    private int currentFrame = 0;
    private int characterY;
    private int jumpVelocity = 0;
    private int sawsDodged = 0;
    private boolean isJumping = false;
    private boolean isGameOver = false;

    public GameWindow(int sawCount) {
        // Window configuration
        setTitle("Cute Runner - Game");
        setSize(800, 600);
        setResizable(false);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize saw count
        this.sawCount = sawCount;

        // Load background and sprites
        background = loadImage("/images/game_background.png");
        sawImage = loadImage("/images/saw.png");
        runSprites = loadRunSprites();
        jumpSprites = loadJumpSprites();
        deathSprites = loadDeathSprites();

        // Initialize character position
        characterY = getHeight() - 200;
        groundY = characterY;

        // Set initial background positions
        x1 = 0;
        x2 = getWidth();

        // Create the texture to be applied over the gradient
        createTexture();

        // Create initial clouds
        createClouds();

        // Create saws
        createSaws();

        // Create and configure the main panel
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGradientBackground(g); // Draw gradient background
                drawTexturizeOverlay(g);   // Apply texture overlay
                drawBackground(g);         // Draw ground images
                drawClouds(g);             // Draw clouds
                drawSaws(g);               // Draw saws
                drawCharacter(g);          // Draw the character
                drawScore(g);              // Draw score
            }
        };

        // Start animations
        startAnimations(gamePanel);

        // Add key listener for jump action
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    initiateJump();
                }
            }
        });

        // Add panel to the window and display it
        getContentPane().add(gamePanel);
        setVisible(true);
    }

    private void startAnimations(JPanel panel) {
        Timer timer = new Timer(20, e -> {
            if (!isGameOver) {
                updateGameObjects();
                checkCollision();
                checkWinCondition();
            }
            panel.repaint();
        });
        timer.start();
    }

    private void updateGameObjects() {
        // Move the background to the left
        x1 -= 5;
        x2 -= 5;

        // Reset background position when it moves out of the screen
        if (x1 + getWidth() <= 0) x1 = x2 + getWidth();
        if (x2 + getWidth() <= 0) x2 = x1 + getWidth();

        // Move character
        currentFrame = (currentFrame + 1) % (isJumping ? jumpSprites.size() : runSprites.size());

        // Move each saw to the left and reset when it exits the screen

        for (Saw saw : saws) {
            saw.rotation += 10; // Increase rotation
            if (saw.rotation >= 360) saw.rotation = 0; // Reset rotation
        }
    }

    private void checkCollision() {
        // Define the horizontal range where collision can occur
        int minHorizontalRange = 50;
        int maxHorizontalRange = 150;

        // Define the safe vertical height for the character to avoid collision
        int safeCharacterHeight = 325;

        // Check each saw for potential collision with the character
        for (Saw saw : saws) {
            // Check if the saw is within the defined horizontal range
            if (saw.x / 2 >= minHorizontalRange && saw.x / 2 <= maxHorizontalRange) {
                // If the character is below the safe height, trigger game over
                if (characterY > safeCharacterHeight) {
                    triggerGameOver(); // Stop the game on collision
                    break;
                }
            }
        }
    }

    private void checkWinCondition() {
        if (sawCount > 0 && sawsDodged >= sawCount) {
            isGameOver = true;
            showEndGameDialog("Congratulations! You won by dodging " + sawsDodged + " saws.");
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        new Timer(100, e -> {
            currentFrame++;
            if (currentFrame >= deathSprites.size()) {
                ((Timer) e.getSource()).stop();
                showEndGameDialog("Game Over! You dodged " + sawsDodged + " saws.");
            }
            repaint();
        }).start();
    }

    private void showEndGameDialog(String message) {
        // Create a custom JPanel with padding for the message
        JPanel panel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add margins
        panel.add(messageLabel, BorderLayout.CENTER);

        // Create buttons
        JButton restartButton = new JButton("Restart");
        JButton menuButton = new JButton("Menu");

        // Button panel to hold the buttons horizontally
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(restartButton);
        buttonPanel.add(menuButton);

        // Add button panel to the main panel
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Create the dialog
        JDialog dialog = new JDialog(this, "Game Over", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        // Add action listeners to the buttons
        restartButton.addActionListener(e -> {
            dialog.dispose();
            restartGame();
        });

        menuButton.addActionListener(e -> {
            dialog.dispose();
            returnToMenu();
        });

        dialog.setVisible(true);
    }

    private void restartGame() {
        dispose();
        new GameWindow(sawCount);  // Start a new game with the same saw count
    }

    private void returnToMenu() {
        dispose();
        new Menu();  // Return to the main menu
    }

    private void drawScore(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.BLACK);
        g.drawString("Saws Dodged: " + sawsDodged, 10, 30);
    }

    private void createClouds() {
        // Create clouds with random positions within the width of the screen
        Random rand = new Random();
        for (int index = 0; index < 20; index++) {
            int x = rand.nextInt(getWidth() * 2);  // Random initial X position within the screen width
            int y = rand.nextInt(200);  // Random Y position in the upper part of the screen (0-200px)
            double scale = 0.5 + rand.nextDouble();  // Random scaling factor for the cloud size
            double speed = 2 + rand.nextDouble(3);  // Random speed for each cloud
            clouds.add(new Cloud(x, y, scale, speed));  // Add the new cloud to the list
        }
    }

    private void drawClouds(Graphics g) {
        Graphics2D originalG2D = (Graphics2D) g.create();

        for (Cloud cloud : clouds) {
            Graphics2D g2d = (Graphics2D) originalG2D.create();

            // Apply transformations
            AffineTransform transform = new AffineTransform();
            transform.translate(cloud.x, cloud.y);
            transform.scale(cloud.scale, cloud.scale);
            transform.shear(0.1, 0);
            g2d.setTransform(transform);

            // Draw the cloud
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(0, 0, 100, 50);

            // Stop movement if the game is over**
            if (!isGameOver) {
                cloud.x -= (int) cloud.speed;
                if (cloud.x + 100 < 0) {
                    cloud.x = getWidth() * 2;
                    cloud.y = new Random().nextInt(200);
                }
            }

            g2d.dispose();
        }

        originalG2D.dispose();
    }

    private void createSaws() {
        while (saws.isEmpty()) {
            int x = getWidth() * 2 + 200; // Generate beyond screen
            int y = getWidth() + 50; // Near the ground
            saws.add(new Saw(x, y)); // Random speed
        }
    }

    private void drawSaws(Graphics g) {
        Graphics2D originalG2D = (Graphics2D) g.create();

        for (Saw saw : saws) {
            Graphics2D g2d = (Graphics2D) originalG2D.create();

            int centerX = sawImage.getWidth(this) / 2;
            int centerY = sawImage.getHeight(this) / 2;

            // Apply transformations
            AffineTransform transform = new AffineTransform();
            transform.translate(saw.x, saw.y);
            transform.rotate(Math.toRadians(saw.rotation), centerX, centerY);
            g2d.setTransform(transform);

            // Draw the saw
            g2d.drawImage(sawImage, 0, 0, this);

            // Stop movement if the game is over**
            if (!isGameOver) {
                saw.x -= 20;
                if (saw.x + sawImage.getWidth(this) < 0) {
                    saw.x = getWidth() * 3 + new Random().nextInt(getWidth());
                    sawsDodged++;
                }
            }

            g2d.dispose();
        }

        originalG2D.dispose();
    }

    private void initiateJump() {
        // Verify if the character is already in the air
        if (!isJumping) {
            isJumping = true;
            jumpVelocity = -15; // Initial jump velocity
            new Timer(20, e -> {
                characterY += jumpVelocity;
                jumpVelocity += 1; // Gravity effect

                if (characterY >= groundY) {
                    characterY = groundY;
                    isJumping = false; // Reset jump state
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            }).start();
        }
    }

    private void setCustomCursor() {
        // Change cursor to a custom
        String cursorPath = "/images/cursor.png";
        ImageIcon cursorIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource(cursorPath)));
        Image cursorImage = cursorIcon.getImage();
        Cursor customCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "Cute Cursor");
        setCursor(customCursor);
    }

    private List<Image> loadRunSprites() {
        return loadSprites("/sprites/Run (%d).png", 20);
    }

    private List<Image> loadJumpSprites() {
        return loadSprites("/sprites/Jump (%d).png", 30);
    }

    private List<Image> loadDeathSprites() {
        return loadSprites("/sprites/Dead (%d).png", 30);
    }

    private Image loadImage(String path) {
        return new ImageIcon(Objects.requireNonNull(getClass().getResource(path))).getImage();
    }

    private List<Image> loadSprites(String pathFormat, int count) {
        List<Image> sprites = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String path = String.format(pathFormat, i);
            sprites.add(loadImage(path));
        }
        return sprites;
    }

    private void createTexture() {
        // Create the texture
        BufferedImage textureImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = textureImage.createGraphics();
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillOval(4, 4, 4, 4);
        g2d.fillOval(12, 12, 2, 2);
        g2d.dispose();
        texturePaint = new TexturePaint(textureImage, new Rectangle(0, 0, 16, 16));
    }

    private void drawGradientBackground(Graphics g) {
        // Create the gradient
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 182, 193),
                0, height, new Color(221, 160, 221)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
    }

    private void drawTexturizeOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(texturePaint);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawBackground(Graphics g) {
        g.drawImage(background, x1, getHeight() - 512, getWidth(), 512, this);
        g.drawImage(background, x2, getHeight() - 512, getWidth(), 512, this);
    }

    private void drawCharacter(Graphics g) {
        // Determine the correct sprite to draw
        Image currentImage;

        // Check if the death sprite should be drawn
        if (isGameOver && (sawCount == 0 || sawsDodged != sawCount)) {
            // Use death sprite if the game is over and either in infinite mode or the player hasn't won
            currentImage = deathSprites.get(Math.min(currentFrame, deathSprites.size() - 1));
            g.drawImage(currentImage, 100, characterY, 135, 110, this);  // Draw death sprite
        } else {
            // Use running or jumping sprites otherwise
            currentImage = isJumping
                    ? jumpSprites.get(currentFrame % jumpSprites.size())
                    : runSprites.get(currentFrame % runSprites.size());
            g.drawImage(currentImage, 100, characterY, 100, 100, this);  // Draw normal sprite
        }
    }

}
