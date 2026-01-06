package org.example.duelmasters.Utils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class RandomOrg {
    /**
     * Fetches a single random number from Random.org
     */
    public int getRandomNumber(int min, int max) {
        try {
            String urlString = "https://www.random.org/integers/?num=1&min=" + min +
                    "&max=" + max + "&col=1&base=10&format=plain&rnd=new";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);  // 3 seconds timeout
            conn.setReadTimeout(3000);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String numberStr = in.readLine();
            in.close();

            return Integer.parseInt(numberStr.trim());

        } catch (Exception e) {
            //e.printStackTrace();
            // fallback to Java Random if Random.org fails
            return new java.util.Random().nextInt(max - min + 1) + min;
        }
    }
}
