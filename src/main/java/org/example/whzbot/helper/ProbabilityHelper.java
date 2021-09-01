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

    public static double normal_distribution(double x) {
        if (x < 0)
            return 0;
        if (x > 16)
            return 0.5;
        x = x * 256 + 129;
        int j = (int) x;
        double prob = 0;
        for (int i = 129; i < j; i++)
            prob += binomial_distribution(256 - i, 256, 0.5);
        double a = binomial_distribution(255 - j, 256, 0.5);
        return (prob + a * (x - j));
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
