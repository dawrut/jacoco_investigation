package research;

import java.util.Random;

public class WithBranchesClass {
    private static final Random random = new Random();

    public void method1() {
        if (random.nextBoolean()) {
            System.out.println(true);
        } else {
            System.out.println(false);
        }
    }

    public void method2() {
        if (random.nextBoolean()) {
            System.out.println(false);
        } else {
            System.out.println(true);
        }
    }

}