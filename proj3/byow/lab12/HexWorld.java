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
    private static  int height = 50;
    private static  int width = 30;
//    public HexWorld(int _width, int _height) {
//        width=_width;
//        height=_height;
//    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TERenderer testw = new TERenderer();
        testw.initialize(height, width);
        TETile[][] test=new TETile[height][width];
        TETile[][] ttest=new TETile[width][height];
        for(int i=0;i<height;i++){
            for(int j=0;j<width;j++){
                test[i][j]=Tileset.NOTHING;
            }
        }for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                ttest[i][j]=Tileset.NOTHING;
            }
        }
        int x = sc.nextInt();
        int y = sc.nextInt();
        int s = sc.nextInt();
        addHexagon(test,ttest,x,y,s,randomTile());
        testw.renderFrame(ttest);
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
    public static void addHexagon(TETile[][] world,TETile[][] tworld, int x, int y, int s, TETile tile){
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
        int cnt1=cnt;
        for (int i = x + s; i < x + (s * 2); i++) {
            for (int j = y - cnt; j < y - cnt + (cnt * 2 + s); j++) {
                world[i][j] = tile;
            }
            cnt--;
        }
        System.out.println(cnt1);
        int n=x+s*2;
        int m=y - cnt1 + (cnt1 * 2 + s);
        // 旋转中心点（六边形的中心）
        int centerX = x + s;
        int centerY = y;
        for (int i = x; i < x + 2 * s; i++) {
            for (int j = y - cnt1; j < y + cnt1 + s; j++) {
                if (world[i][j] == tile) {
                    // 计算相对于中心的坐标
                    int relX = i - centerX;
                    int relY = j - centerY;
                    // 应用90度顺时针旋转公式：(x,y) -> (y,-x)
                    int rotatedX = relY;
                    int rotatedY = -relX;
                    // 转换回绝对坐标
                    int newX = centerX + rotatedX;
                    int newY = centerY + rotatedY;
                    // 确保坐标在 tworld 范围内
                    if (newX >= 0 && newX < tworld.length && newY >= 0 && newY < tworld[0].length) {
                        tworld[newX][newY] = tile;
                    }
                }
            }
        }
    }
}
