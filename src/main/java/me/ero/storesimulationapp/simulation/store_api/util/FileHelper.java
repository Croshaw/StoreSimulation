package me.ero.storesimulationapp.simulation.store_api.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Random;

public class FileHelper {
    public static String getRandomString(String filePath, Random random) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {
            LinkedList<String> temp = new LinkedList<>(reader.lines().toList());
            int lineId = random.nextInt(0, temp.size());
            return temp.get(lineId);
        } catch (IOException e) {
            return "";
        }
    }
    public static void writeToFile(String filePath, String data) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(data);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getText(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
