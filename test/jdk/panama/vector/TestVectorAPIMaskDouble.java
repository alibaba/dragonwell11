
import jdk.panama.vector.*;
import java.util.Arrays;
import java.util.Random;
import java.lang.reflect.Field;
import java.io.IOException;
import jdk.panama.vector.Vector.Mask;
import jdk.panama.vector.Vector.Shape;

/*
 *  @test
 *  @modules jdk.panama.vector
 *   @run testng/othervm --add-opens jdk.panama.vector/jdk.panama.vector=ALL-UNNAMED -XX:+UseVectorAPI TestVectorAPIMaskDouble
 *
 */

public class  TestVectorAPIMaskDouble
{
    public static int size = 1024 * 16;
    static Random random = new Random();
    static final DoubleVector.DoubleSpecies Species128 = DoubleVector.species(Shape.S_128_BIT);
    static final DoubleVector.DoubleSpecies Species256 = DoubleVector.species(Shape.S_256_BIT);
    static final DoubleVector.DoubleSpecies Species512 = DoubleVector.species(Shape.S_512_BIT);
    static final DoubleVector.DoubleSpecies Species = DoubleVector.preferredSpecies();
    static boolean[] AisNull = new boolean[size];
    static double[] result = new double[size];
    static double[] resultv = new double[size];
    static double[] input = new double[size];
    static boolean equals(byte a, byte b) {
      return a == b;
    }
    static boolean equals(short a, short b) {
      return a == b;
    }
    static boolean equals(int a, int b) {
      return a == b;
    }
    static boolean equals(long a, long b) {
      return a == b;
    }
    static boolean equals(float a, float b) {
      if (a  > b) {
          return a - b < 0.0001f;
       } else if (a < b) {
          return b - a < 0.0001f;
       } else {
         return true;
       }
    }
    static boolean equals(double a, double b) {
      if (a  > b) {
          return a - b < 0.0001f;
       } else if (a < b) {
          return b - a < 0.0001f;
       } else {
         return true;
       }
    }
    public static void main(String[] args) throws  NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InstantiationException {
        long start0 = System.currentTimeMillis();
        long start128v = System.currentTimeMillis();
        long start128, start256, start512;
        long normalTime = 0;
        long vecTime = 0;
        int i = 0;
        for (i = 0; i < size; i++) {
            input[i] = random.nextDouble();
            result[i] = 0;
            resultv[i] = 0;
            if (random.nextInt(10) > 4) {
                AisNull[i] = true;
            } else {
                AisNull[i] = false;
            }
        }
        for (i = 0; i < 20000; i++) {
            normalTest();
        }
        for (i = 0; i < 20000; i++) {
            if (Species128.length() > 2) { 
                vecTest128();
            }
            vecTest256();
            vecTest512();
        }
        for (i = 0; i < size; i++) {
            result[i] = 0;
            resultv[i] = 0;
        }
        System.out.println("begin test " + Species.length());
        start0 = System.currentTimeMillis();
        for (i = 0; i < 10000; i++) {
            normalTest();
        }
        normalTime = System.currentTimeMillis() - start0;
        System.out.println("normal  time used:" + normalTime);
        if (Species128.length() > 2) {
            start128 = System.currentTimeMillis();
            for (i = 0; i < 10000; i++) {
                vecTest128();
            }
            vecTime = System.currentTimeMillis() - start128;
            System.out.println("vector 128 time used:" + vecTime);
            if (Species128.length() <= Species.length() && Species128.length() > 2) {
                if (vecTime >= normalTime) {
                    throw new RuntimeException("128 perf bad!");
                }
            }
        }
        start256 = System.currentTimeMillis();
        for (i = 0; i < 10000; i++) {
            vecTest256();
        }
        vecTime = System.currentTimeMillis() - start256;
        System.out.println("vector 256 time used:" + vecTime);
        if (Species256.length() <= Species.length()) {
            if (vecTime >= normalTime) {
                throw new RuntimeException("256 perf bad!");
            }
        }

        start512 = System.currentTimeMillis();
        for (i = 0; i < 10000; i++) {
            vecTest512();
        }
        vecTime = System.currentTimeMillis() - start512;
        System.out.println("vector 512 time used:" + vecTime);
        if (Species512.length() <= Species.length()) {
            if (vecTime >= normalTime) {
                throw new RuntimeException("512 perf bad!");
            }
        }

        for (i = 0; i < size; i++) {
            result[i] = 0;
            resultv[i] = 0;
        }
        normalTest();
        vecTest128();
        for (i = 0; i < size; i++) {
            if (!equals(result[i], resultv[i])) {
                throw new RuntimeException("Wrong result128!" + " index " + i + " vec result " + resultv[i] + " result0 " + result[i] + " mask " + AisNull[i]);
            }
        }
        for (i = 0; i < size; i++) {
            resultv[i] = 0;
        }
        vecTest256();
        for (i = 0; i < size; i++) {
            if (!equals(result[i], resultv[i])) {
                throw new RuntimeException("Wrong result256!" + " index " + i + " vec result " + resultv[i] + " result0 " + result[i] + " mask " + AisNull[i]);
            }
        }
        for (i = 0; i < size; i++) {
            resultv[i] = 0;
        }
        vecTest512();
        for (i = 0; i < size; i++) {
            if (!equals(result[i], resultv[i])) {
                throw new RuntimeException("Wrong result512!" + " index " + i + " vec result " + resultv[i] + " result0 " + result[i] + " mask " + AisNull[i]);
            }
        }
    }
    static void normalTest() {
        for (int i = 0; i < size; i++) {
             if (AisNull[i] == true) {
                 result[i] = input[i];
              } else {
                 result[i] = 0;
              }
        }
        return;
    }
    static void vecTest128() {
        DoubleVector v0;
        DoubleVector.DoubleSpecies Species = Species128;
        int i = 0;
        Mask mask0;
        for (i = 0; i + (Species.length()) <= size; i += Species.length()) {
            mask0 = Species.maskFromArray(AisNull, i);
            v0 = Species.fromArray(input, i, mask0);
            v0.intoArray(resultv, i);
        }
        return;
    }
    static void vecTest256() {
        DoubleVector v0;
        DoubleVector.DoubleSpecies Species = Species256;
        int i = 0;
        Mask mask0;
        for (i = 0; i + (Species.length()) <= size; i += Species.length()) {
            mask0 = Species.maskFromArray(AisNull, i);
            v0 = Species.fromArray(input, i, mask0);
            v0.intoArray(resultv, i);
        }
        return;
    }
    static void vecTest512() {
        DoubleVector v0;
        DoubleVector.DoubleSpecies Species = Species512;
        int i = 0;
        Mask mask0;
        for (i = 0; i + (Species.length()) <= size; i += Species.length()) {
            mask0 = Species.maskFromArray(AisNull, i);
            v0 = Species.fromArray(input, i, mask0);
            v0.intoArray(resultv, i);
        }
        return;
    }
}


