package velodicord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Config {

    public static LinkedHashMap<String, String> dic;

    public static ArrayList<String> detectbot;

    public static ArrayList<String> ignorecommand;

    public static HashMap<String, Integer> disspeaker;

    public static HashMap<String, Integer> minespeaker;

    public static HashMap<String, String> config;

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Type intmaptype = new TypeToken<HashMap<String, Integer>>() {}.getType();

    public static Type strmaptype = new TypeToken<HashMap<String, String>>() {}.getType();

    public static Type listtype = new TypeToken<ArrayList<String>>() {}.getType();

    @DataDirectory
    public static Path dataDirectory;

    public static Path dicjson;

    public static Path detectbotjson;

    public static Path ignorecommandjson;

    public static Path disspeakerjson;

    public static Path minespeakerjson;

    public static Path configjson;

    public static void init() throws IOException, InterruptedException {
        if (Files.notExists(dataDirectory))
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Velodicordのconfigディレクトリを作れませんでした");
            }

        if (Files.notExists(dicjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/object.json")), dicjson);

        if (Files.notExists(detectbotjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/array.json")), detectbotjson);

        if (Files.notExists(ignorecommandjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/array.json")), ignorecommandjson);

        if (Files.notExists(disspeakerjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/object.json")), disspeakerjson);

        if (Files.notExists(minespeakerjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/object.json")), minespeakerjson);

        if (Files.notExists(configjson)) {
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/config.json")), configjson);
            Velodicord.velodicord.logger.info("Velodicordのconfigを設定してください");
            System.exit(0);
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(dicjson)), StandardCharsets.UTF_8))) {
            dic = ((HashMap<String, String>) gson.fromJson(reader, strmaptype)).entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparingInt(String::length).reversed()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(detectbotjson)), StandardCharsets.UTF_8))) {
            detectbot = gson.fromJson(reader, listtype);
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(ignorecommandjson)), StandardCharsets.UTF_8))) {
            ignorecommand = gson.fromJson(reader, listtype);
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(disspeakerjson)), StandardCharsets.UTF_8))) {
            disspeaker = gson.fromJson(reader, intmaptype);
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(minespeakerjson)), StandardCharsets.UTF_8))) {
            minespeaker = gson.fromJson(reader, intmaptype);
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
            config = gson.fromJson(reader, strmaptype);
        }

        VOICEVOX.init();
    }
}
