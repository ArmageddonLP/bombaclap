package helpers;

import proxy.Field;
import proxy.Player;

/**
 * DebugHelper provides static methods used for printing maps and tick-stats to the server-console
 */
public class DebugHelper {

    /**
     * Prints the map to the console in a human-readable way
     *
     * @param map Field-Array of the game representing the map
     */
    public static void printMapHumanReadable(Field[] map) {
        int length = map.length;
        int width = (int) Math.sqrt(length);
        int i = 0;
        for (Field field : map) {
            StringBuilder f = new StringBuilder("|");
            if (field.getX() < 10) {
                f.append(" ");
            }
            f.append(field.getX());
            if (field.getY() < 10) {
                f.append(" ");
            }
            f.append(field.getY());
            f.append("|");
            for (Player p : field.getPlayers()) {
                if (p == null) {
                    f.append(" ");
                } else {
                    switch (p.getColor()) {
                        case BLUE -> f.append("B");
                        case RED -> f.append("R");
                        case GREEN -> f.append("G");
                        case YELLOW -> f.append("Y");
                    }
                }

            }
            if (field.getBombState() == null) {
                f.append("  ");
            } else {
                switch (field.getBombState()) {
                    case BLACK_STATE -> f.append("b1");
                    case RED_STATE -> f.append("b2");
                    case EXPLODING_STATE -> f.append("b3");
                    case FIRE_STATE -> f.append("b4");
                    case NO_BOMB -> f.append("b0");
                }
            }
            if (field.getGround() == null) {
                f.append("  ");
            } else {
                switch (field.getGround()) {
                    case SOLID_WALL -> f.append("g1");
                    case BRITTLE_WALL -> f.append("g2");
                    case EXPLODED_GRASS -> f.append("g3");
                    case DIRT -> f.append("g4");
                    case EXPLODED_DIRT -> f.append("g5");
                    case EXPLODED_WALL -> f.append("g6");
                    case GRASS -> f.append("g0");
                }
            }
            System.out.print(f);
            if (i % width == width - 1) {
                System.out.println();
            }
            i++;
        }
        System.out.println();
        System.out.println("______________".repeat(width));
        System.out.println();
    }

    /**
     * Takes start-time, end-time and sleep-time of a loop and prints them to the server console
     *
     * @param start     Long start-time of loop
     * @param end       Long end-time of loop
     * @param sleepTime Long time slept of loop
     */
    public static void printTickStats(long start, long end, long sleepTime) {
        System.out.println("time used: " + (end - start) +
                "ms | time slept: " + sleepTime + "ms");
    }
}
