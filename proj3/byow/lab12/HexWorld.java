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
    private static  int width = 60;
    private static  int height = 30;
//    public HexWorld(int _width, int _height) {
//        width=_width;
//        height=_height;
//    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TERenderer testw = new TERenderer();
        testw.initialize(width, height);
        TETile[][] test=new TETile[width][height];
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                test[i][j]=Tileset.NOTHING;
            }
        }
        int x = sc.nextInt();
        int y = sc.nextInt();
        int s = sc.nextInt();
        addHexagon(test,x,y,s,randomTile());
        testw.renderFrame(test);
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
    public static void addHexagon(TETile[][] world, int x, int y, int s, TETile tile){
        if (x <= 1) {
            return;
        }
        int cnt = 0;
        for (int i = x; i <= x + s - 1; i++) {
            for (int j = y - cnt; j < y - cnt + (cnt * 2 + s); j++) {
                world[i][j] = tile;
            }
            cnt++;
        }
        cnt--;
        for (int i = x + s; i < x + (s * 2); i++) {
            for (int j = y - cnt; j < y - cnt + (cnt * 2 + s); j++) {
                world[i][j] = tile;
            }
            cnt--;
        }
    }
}
