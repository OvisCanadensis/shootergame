package graphics;

import java.util.Random;

import entity.mob.Player;
import level.tile.Tile;

public class Screen {

    public int width, height;
    public int[] pixels;
    public final int MAP_SIZE = 64;
    public final int MAP_SIZE_MASK = MAP_SIZE - 1;
    public int xOffset, yOffset;
    public int[] tiles = new int[MAP_SIZE * MAP_SIZE];
    private Random random = new Random();

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];

        for (int i = 0; i < MAP_SIZE * MAP_SIZE; i++) {
            tiles[i] = random.nextInt(0xFFFFFF);
        }
    }

    public void clear() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
    }

    public void renderTile(int xp, int yp, Tile tile) {
        xp -= xOffset; //because walking right is map scrolling left
        yp -= yOffset;
        for (int y = 0; y < tile.sprite.SIZE; y++) {
            int ya = y + yp; //absolute value that gets offset
            for (int x = 0; x < tile.sprite.SIZE; x++) {
                int xa = x + xp;
                if (xa < -tile.sprite.SIZE || xa >= width || ya < 0 || ya >= height) break; //if a tile would exit the screen, stop the loop b/c tilemap can be really big
                if (xa < 0) xa = 0;
                pixels[xa + ya * width] = tile.sprite.pixels[x + y * tile.sprite.SIZE]; //pixels on the sprite are not offset
            }
        }
    }

    public void renderPlayer(int xp, int yp, Sprite sprite, int flip) {
        xp -= xOffset; //because walking right is map scrolling left
        yp -= yOffset;
        for (int y = 0; y < 32; y++) {
            int ya = y + yp; //absolute value that gets offset
            int ys = y;
            if (flip == 2 || flip == 3) ys = 31 - y;
            for (int x = 0; x < 32; x++) {
                int xa = x + xp;
                int xs = x;
                if (flip == 1 || flip == 3) xs = 31 - x; //flips the sprite, 31 to 0
                if (xa < -32|| xa >= width || ya < 0 || ya >= height) break; //if a tile would exit the screen, stop the loop b/c tilemap can be really big
                if (xa < 0) xa = 0;
                int color = sprite.pixels[xs + ys * 32];
                if (color != 0xFFFF00FF) pixels[xa + ya * width] = color; //pixels on the sprite are not offset, extra FF because of Game pixels[] not using RGB?
            }
        }
    }

    public void setOffset(int xOffset, int yOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

}
