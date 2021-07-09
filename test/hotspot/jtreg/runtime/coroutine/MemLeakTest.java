/*
 * @test
 * @summary test of memory leak while creating and destroying coroutine/thread
 * @run main/othervm -XX:+EnableCoroutine  -Xmx10m  -Xms10m MemLeakTest
 */

import java.dyn.Coroutine;
import java.io.*;

public class MemLeakTest {
    private final static Runnable r = () -> {};

    public static void main(String[] args) throws Exception {
        testThreadCoroutineLeak();
        testUserCreatedCoroutineLeak();
    }


    /**
     * Before fix:  35128kB -> 40124kB
     * After fix :  28368kB -> 28424kB
     */
    private static void testThreadCoroutineLeak() throws Exception {
        // occupy rss
        for (int i = 0; i < 20000; i++) {
            Thread t = new Thread(r);
            t.start();
            t.join();
        }

        int rss0 = getRssInKb();
        System.out.println(rss0);

        for (int i = 0; i < 20000; i++) {
            Thread t = new Thread(r);
            t.start();
            t.join();
        }

        int rss1 = getRssInKb();
        System.out.println(rss1);

        if (rss1 - rss0 > 1024) { // 1M
            throw new Error("thread coroutine mem leak");
        }
    }

    /**
     * Before fix:  152892kB -> 280904kB
     * After fix :  25436kB -> 25572kB
     */
    private static void testUserCreatedCoroutineLeak() throws Exception {
        // occupy rss
        for (int i = 0; i < 200000; i++) {
            new Coroutine(r);
            Coroutine.yield(); // switch to new created coroutine and let it die
        }

        int rss0 = getRssInKb();
        System.out.println(rss0);

        for (int i = 0; i < 200000; i++) {
            new Coroutine(r);
            Coroutine.yield();
        }

        int rss1 = getRssInKb();
        System.out.println(rss1);
        if (rss1 - rss0 > 1024) { // 1M
            throw new Error("user created coroutine mem leak");
        }
    }


    private static int getRssInKb() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/self/status"))) {
            int rss = -1;
            String line;
            while ((line = br.readLine()) != null) {
                //i.e.  VmRSS:       360 kB
                if (line.trim().startsWith("VmRSS:")) {
                    int numEnd = line.length() - 3;
                    int numBegin = line.lastIndexOf(" ", numEnd - 1) + 1;
                    rss = Integer.parseInt(line.substring(numBegin, numEnd));
                    break;
                }
            }
            return rss;
        }
    }
}

