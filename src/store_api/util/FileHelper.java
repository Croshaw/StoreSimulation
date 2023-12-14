package store_api.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
}
