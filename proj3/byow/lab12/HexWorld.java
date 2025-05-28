package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Scanner;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final long SEED = 287312;
    private static final Random RANDOM = new Random(SEED);
    private static  int width = 50;
    private static  int height = 30;
    public HexWorld(int _width, int _height) {
        width=_width;
        height=_height;
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TERenderer testw = new TERenderer();
        testw.initialize(width, height);
        TETile[][] world=new TETile[width][height];
        init(world);
        int x = sc.nextInt();
        int y = sc.nextInt();
        int s = sc.nextInt();
        addHexagon(world,x,y,s,randomTile());
        testw.renderFrame(world);
    }
    private static void init(TETile[][] world) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.NOTHING;
            default: return Tileset.NOTHING;
        }
    }
    public static void addHexagon(TETile[][] world, int x, int y, int s, TETile tile) {
        if (s < 2) {
            return;
        }
        // 下半部分
        for (int i = 0; i < s; i++) {
            int width = s + 2 * i;
            int st = x - i;
            for (int j = 0; j < width; j++) {
                int idx = st + j;
                int idy = y + i;
                    world[idx][idy] = tile;
            }
        }
        // 上半部分
        for (int i = 0; i < s; i++) {
            int width = s + 2 * (s - 1 - i);
            int st = x - (s-1-i);
            for (int j = 0; j < width; j++) {
                int idx = st + j;
                int idy = y + s + i;
                    world[idx][idy] = tile;
            }
        }
    }
}
