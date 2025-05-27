package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    public static void addHexagon(TETile[][] world, int x, int y, int s, TETile tile){
        if (x <= 1) {
            return;
        }
        int cnt=0;
        if(x<s||y<s*2){
            return;
        }
        for (int i = x; i <= x + s - 1; i++) {
            for (int j = y - cnt; j < y - cnt + (cnt * 2 + s); j++) {
                world[i][j] = tile;
            }
            cnt++;
        }
        cnt--;
        for (int i = x + s; i <= x + (s * 2); i++) {
            for (int j = y - cnt; j < y - cnt + (cnt * 2 + s); j++) {
                world[i][j] = tile;
            }
            cnt--;
        }
    }
}
