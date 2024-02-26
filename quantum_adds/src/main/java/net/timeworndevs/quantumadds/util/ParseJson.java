package net.timeworndevs.quantumadds.util;


import java.util.*;
import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
public class ParseJson {
    public static Map<String, JsonArray> parseJson(String json) { //I hate this nearly as much as I hate java


        Type type = new TypeToken<Map<String, JsonArray>>(){}.getType();
        Map<String, JsonArray> myMap = new Gson().fromJson(json, type);


        return myMap;

        /*IHateMyselfForThatPieceOfCodeTMHashMap<String, HashMap<String, String>> njson = new HashMap<>();


        HashMap<String, String> cur = new HashMap<>();
        String mKey = "";
        String key = "";
        String val = "";
        for(String line: json.split("\n")) {
            if (line.contains("{") && line.length()>5) {
                mKey = line.split(": ")[0];
            } else {
                if (!line.contains("{") && !line.contains("}")) {
                    key = line.split(": ")[0];
                    val = line.split(": ")[1].replace(",", "");
                    cur.put(key, val);
                    Quantum.LOGGER.info(key + ":" + val);

                }
            }
            if (line.contains("}")) {
                njson.put(mKey, cur);

                Quantum.LOGGER.info("ADDING " + cur + " TO NJSON");
                cur = new HashMap<>();
            }
            Quantum.LOGGER.info("LINE: " + line);
        }
        if (njson.containsKey("quantum:uranium")) {
            Quantum.LOGGER.info("uranium exists");
            if (njson.get("quantum:uranium").containsKey("alpha")) {
                Quantum.LOGGER.warn(String.valueOf(njson.get("quantum:uranium").get("alpha")));
            }
        }
        return njson;*/
    }
}