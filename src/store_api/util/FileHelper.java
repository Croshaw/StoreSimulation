package store_api.util;

import java.io.*;
import java.util.Random;

public class FileHelper {
    public static String getRandomString(String filePath, Random random) {
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            long lineId = random.nextLong(0, bufferedReader.lines().count());
            long id = 0;
            String result = "";
            while(id != lineId) {
                result = bufferedReader.readLine();
                id++;
            }
            return result;
        } catch (IOException e) {
            return "";
        }

    }
}
