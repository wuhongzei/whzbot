package org.example.whzbot.helper;

public class ProbabilityHelper {
    public static double uniform_distribution(float x) {
        return uniform_distribution(x, 0, 1);
    }

    public static double uniform_distribution(float x, float a) {
        return uniform_distribution(x, 0, a);
    }

    public static double uniform_distribution(float x, float a, float b) {
        if (a == b)
            return -1;
        return b > a ? 1 / (a - b) : 1 / (b - a);
    }

    public static double binomial_distribution(int x, int n, double p) {
        return pi(Math.pow(p, x) * Math.pow(1 - p, n - x),
                n - x + 1, n) / pi(0, x);
    }

    final static int SIMULATE_NUM = 4096;
    static int SIMULATE_HALF = 2048;
    static int SIMULATE_MAX = 128;
    static int SIMULATE_BIAS = 32;

    public static double normal_distribution(double x) {
        x *= SIMULATE_BIAS;
        if (x < 0)
            return 0;
        if (x > SIMULATE_MAX)
            return 0.5;
        int m = SIMULATE_HALF - (int) x;

        // def k as the smallest partition in desired area (left most)
        double k = SIMULATE_NUM;
        for (int i = 1; i < m; i++) {
            k *= (SIMULATE_NUM - i) / (double) (i + 1);
        }
        // f are the extracted nested factors
        double f = 1 + 1.0 / SIMULATE_HALF;
        for (int i = SIMULATE_HALF - 1; i > m; i--) {
            f = 1.0 + f;
            f = f * (SIMULATE_NUM - i + 1) / (double) i;
        }

        return Math.pow(0.5, SIMULATE_NUM) * k * (x - (int) x + f);
    }

    private static double pi(int a, int b) {
        double result = 1.0;
        if (a < 2)
            a = 2;
        for (; a <= b; a++)
            result *= a;
        return result;
    }

    private static double pi(double i, int a, int b) {
        double result = i;
        if (a < 2)
            a = 2;
        for (; a <= b; a++)
            result *= a;
        return result;
    }
}
