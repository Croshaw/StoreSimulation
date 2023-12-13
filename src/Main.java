import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Random random = new Random(5);
        int max = 5;
        int min = 2;
        while(true) {
            //System.out.println(random.nextInt((max - min)+1)+min);
            System.out.println(random.nextFloat()% .3f);
        }
    }
}