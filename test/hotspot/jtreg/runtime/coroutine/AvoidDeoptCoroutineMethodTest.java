/*
 * @test TestAvoidDeoptCoroutineMethod
 * @library /test/lib
 * @build sun.hotspot.WhiteBox
 * @run driver ClassFileInstaller sun.hotspot.WhiteBox
 * @build AvoidDeoptCoroutineMethodTest
 * @run main/othervm -XX:+EnableCoroutine -Xmx10m -Xms10m -Xbootclasspath/a:. -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI AvoidDeoptCoroutineMethodTest
 * @summary test avoid coroutine intrinsic method to be deoptimized
 */

import sun.hotspot.WhiteBox;
import java.dyn.Coroutine;
import java.io.*;

public class AvoidDeoptCoroutineMethodTest {
    public static void main(String[] args) throws Exception {
        WhiteBox whiteBox = WhiteBox.getWhiteBox();
        Coroutine threadCoro = Thread.currentThread().getCoroutineSupport().threadCoroutine();
        runSomeCoroutines(threadCoro);
        // deoptimize all
        whiteBox.deoptimizeAll();
        // if intrinsic methods of coroutine have been deoptimized, it will crash here
        runSomeCoroutines(threadCoro);
    }

    private static void runSomeCoroutines(Coroutine threadCoro) throws Exception {
        for (int i = 0; i < 10000; i++) {
            new Coroutine(() -> {});
            Coroutine.yieldTo(threadCoro); // switch to new created coroutine and let it die
        }
        System.out.println("end of run");
    }
}

