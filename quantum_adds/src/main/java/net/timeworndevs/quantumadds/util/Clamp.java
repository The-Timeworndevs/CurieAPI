package net.timeworndevs.quantumadds.util;

public class Clamp {
    public static double clamp(double x, double y, double z) {
        double ret = x;
        if (x<y) {
            ret = y;
        } else if (x>z) {
            ret = z;
        }
        return ret;
    }
}
