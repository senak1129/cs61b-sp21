package gitlet;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) {
        while(true) {
            System.out.println("输入n");
            Scanner input = new Scanner(System.in);
            int n = input.nextInt();
            String[] arr = new String[n];
            for (int i = 0; i < n; i++) {
                arr[i] = input.next();
            }
            Main.main(arr);
        }
    }
}
