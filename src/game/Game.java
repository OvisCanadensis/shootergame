package game;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

import graphics.Screen;
import entity.mob.Player;
import input.Keyboard;
import level.Level;
import level.RandomLevel;

public class Game extends Canvas implements Runnable {
    private static final long serialVersionUID = 1L;

    public static int width = 300;
    public static int height = (width / 16) * 9;
    // low res to be scaled up so larger graphics but less processing
    public static int scale = 4;
    public static String title = "Game";

    // create a sub-process within java.exe, do multiple simultaneous things
    private Thread thread;
    private JFrame frame;
    private Keyboard key;
    private Level level;
    private Player player;
    private boolean running = false;

    private Screen screen;

    private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

    public Game() {
        Dimension size = new Dimension(width * scale, height * scale);
        setPreferredSize(size);

        screen = new Screen(width, height);
        frame = new JFrame();
        key = new Keyboard();
        level = new RandomLevel(64, 64);
        player = new Player(key);

        addKeyListener(key);
    }

    // "synchronized" ensures that there's no overlaps in instructions in the
    // thread
    public synchronized void start() {
        running = true;
        thread = new Thread(this, "Display");
        thread.start();
    }

    // wait for the thread to die
    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        // game cannot be formed of both logic and rendering together because of timing issues
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1000000000.0 / 60.0; //the right side means delta updates 60 times a second
        double delta = 0;
        int frames = 0, updates = 0;
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            requestFocus();
            while (delta >= 1) {
                update(); // aka tick(), logic - restricted to 60 times a second
                updates++;
                delta--;
            }
            render(); // images - unlimited
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frame.setTitle(title + " | " + updates + " ups, " + frames + " fps");
                updates = 0;
                frames = 0;
            }
        }
    }

    public void update() {
        key.update();
        player.update();

    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3); // 2 back buffers, queues up the next image to be placed before other buffer ends
            return;
        }

        screen.clear();
        int xScroll = player.x - screen.width / 2;
        int yScroll = player.y - screen.height / 2;
        level.render(xScroll, yScroll, screen);
        player.render(screen);

        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = screen.pixels[i];
        }

        Graphics g = bs.getDrawGraphics(); // creates a link between graphics (to which you can write data) and the BufferStrategy
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        g.dispose();
        bs.show(); // shows the next available buffer
    }

    // static implies it has no relation to the rest of the class
    public static void main(String[] args) {
        Game game = new Game();
        game.frame.setResizable(false);
        game.frame.setTitle(Game.title);
        game.frame.add(game);
        game.frame.pack(); // sets the size of the frame to that of its components aka Constructor Dimension
        game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.frame.setLocationRelativeTo(null); // centers frame
        game.frame.setVisible(true);

        game.start();
    }

}
