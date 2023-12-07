package helpers;

import config.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * HighscoreHelper provides static methods used for persisting scoreboards
 */
public class HighscoreHelper {

    /**
     * Writes a list of scoreboards to a csv-file with four colors as header
     * and the totals for each color as footer
     *
     * @param scoreboards ArrayList of Integers representing scoreboards
     */
    public static void writeScoreboardByColor(ArrayList<Integer[]> scoreboards) {
        ArrayList<String[]> csvData = new ArrayList<>();
        String[] headers = new String[]{"Blue", "Red", "Green", "Yellow"};
        Integer[] totals = new Integer[Constants.MAX_PLAYER_COUNT];
        Arrays.fill(totals, 0);
        String[] totalsLine = new String[Constants.MAX_PLAYER_COUNT];
        ArrayList<String[]> lines = new ArrayList<>();
        for (Integer[] scoreboard : scoreboards) {
            String[] line = new String[Constants.MAX_PLAYER_COUNT];
            for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
                line[i] = String.valueOf(scoreboard[i]);
                totals[i] += scoreboard[i];
            }
            lines.add(line);
        }
        for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
            totalsLine[i] = String.valueOf(totals[i]);
        }
        csvData.add(headers);
        csvData.addAll(lines);
        csvData.add(totalsLine);
        File csvFile = new File(Constants.FILE_LOCATION_SCOREBOARD_COLOR);
        try (PrintWriter pw = new PrintWriter(csvFile)) {
            for (String[] line : csvData) {
                StringBuilder csvLine = new StringBuilder();
                for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
                    csvLine.append(line[i]);
                    csvLine.append(Constants.CSV_SEPARATOR);
                }
                pw.println(csvLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a csv-file of scoreboards by color from the default location,
     * ignores the header and footer row as they aren't scoreboards
     * and returns a list of scoreboards
     *
     * @return ArrayList of Integers representing scoreboards
     */
    public static ArrayList<Integer[]> readScoreboardByColor() {
        ArrayList<Integer[]> scoreboards = new ArrayList<>();
        ArrayList<String[]> lines = new ArrayList<>();
        if (new File(Constants.FILE_LOCATION_SCOREBOARD_COLOR).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(Constants.FILE_LOCATION_SCOREBOARD_COLOR))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line.split(Constants.CSV_SEPARATOR));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < lines.size(); i++) {
                String[] line = lines.get(i);
                if (i != 0 && i != lines.size() - 1) {
                    Integer[] scores = new Integer[Constants.MAX_PLAYER_COUNT];
                    for (int j = 0; j < scores.length; j++) {
                        try {
                            scores[j] = Integer.valueOf(line[j]);
                        } catch (Exception e) {
                            scores[j] = 0;
                        }
                    }
                    scoreboards.add(scores);
                }
            }
        }
        return scoreboards;
    }

    /**
     * Writes a list of scoreboards to a csv-file with alternating rows
     * showing playerNames and points achieved
     *
     * @param scoreboardsByName ArrayList of Strings representing playerNames and scoreboards
     */
    public static void writeScoreboardByName(ArrayList<String[]> scoreboardsByName) {
        File csvFile = new File(Constants.FILE_LOCATION_SCOREBOARD_NAME);
        try (PrintWriter pw = new PrintWriter(csvFile)) {
            for (String[] line : scoreboardsByName) {
                StringBuilder csvLine = new StringBuilder();
                for (int i = 0; i < Constants.MAX_PLAYER_COUNT; i++) {
                    csvLine.append(line[i]);
                    csvLine.append(Constants.CSV_SEPARATOR);
                }
                pw.println(csvLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a csv-file of scoreboards by playerName from the default location,
     * and returns a list of alternating playerNames and scoreboards
     *
     * @return ArrayList of Strings representing playerNames and scoreboards
     */
    public static ArrayList<String[]> readScoreboardByName() {
        ArrayList<String[]> lines = new ArrayList<>();
        if (new File(Constants.FILE_LOCATION_SCOREBOARD_NAME).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(Constants.FILE_LOCATION_SCOREBOARD_NAME))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] csvLine = line.split(Constants.CSV_SEPARATOR);
                    String[] csvData = new String[Constants.MAX_PLAYER_COUNT];
                    for (int j = 0; j < Constants.MAX_PLAYER_COUNT; j++) {
                        try {
                            csvData[j] = csvLine[j];
                        } catch (Exception e) {
                            csvData[j] = Constants.DEFAULT_PLAYER_NAME;
                        }
                    }
                    lines.add(csvData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lines;
    }
}
