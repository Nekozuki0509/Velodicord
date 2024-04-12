package velodicord;

import V4S4J.V4S4J.V4S4J;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class config {

    public static Map<String, String> dic;

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Type type = new TypeToken<Map<String, String>>() {}.getType();

    public static Map<String, String> config;

    @DataDirectory
    public static Path dataDirectory;

    public static Path dicjson;

    public static Path configjson;

    public static void init() throws IOException, InterruptedException {
        if (Files.notExists(dataDirectory))
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Velodicordのconfigディレクトリを作れませんでした");
            }

        if (Files.notExists(dicjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/dic.json")), dicjson);

        if (Files.notExists(configjson)) {
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/config.json")), configjson);
            Velodicord.velodicord.logger.info("Velodicordのconfigを設定してください");
            System.exit(0);
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(dicjson)), StandardCharsets.UTF_8))) {
            dic = gson.fromJson(reader, type);
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(String.valueOf(configjson)), StandardCharsets.UTF_8))) {
            config = gson.fromJson(reader, type);
        }

        String rawOsName = System.getProperty("os.name");
        if (Files.notExists(dataDirectory.resolve("voicevox_core"))) {
            Velodicord.velodicord.logger.info("VOICEVOXのライブラリをダウンロード中");
            String rawOsArch = System.getProperty("os.arch");
            String osName, osArch;
            if (rawOsName.startsWith("Win")) {
                try (InputStream in = new URL("https://github.com/VOICEVOX/voicevox_core/releases/latest/download/download-windows-x64.exe").openStream()) {
                    Files.copy(in, dataDirectory.resolve("download.exe"));
                }
                new ProcessBuilder("cmd.exe", "/c", "cd /d", dataDirectory.toString()).directory(dataDirectory.toFile()).start().waitFor();
                switch (config.get("VOICEVOX-type")) {
                    case "2" -> new ProcessBuilder("cmd.exe", "/c", "download --device directml").directory(dataDirectory.toFile()).start().waitFor();
                    case "3" -> new ProcessBuilder("cmd.exe", "/c", "download --device cuda").directory(dataDirectory.toFile()).start().waitFor();
                    default ->  new ProcessBuilder("cmd.exe", "/c", "download").directory(dataDirectory.toFile()).start().waitFor();
                }
            }else {
                if (rawOsName.startsWith("Mac")) {
                    osName = "macos";
                } else if (rawOsName.startsWith("Linux")) {
                    osName = "linux";
                } else {
                    throw new RuntimeException("Unsupported OS: " + rawOsName);
                }
                if (rawOsArch.equals("x86_64") || rawOsArch.equals("amd64")) {
                    osArch = "x64";
                } else if (rawOsArch.equals("aarch64")) {
                    osArch = "arm64";
                } else {
                    throw new RuntimeException("Unsupported OS architecture: " + rawOsArch);
                }
                try (InputStream in = new URL("https://github.com/VOICEVOX/voicevox_core/releases/latest/download/download-"+osName+"-"+osArch).openStream()) {
                    Files.copy(in, dataDirectory.resolve("download"));
                }
                new ProcessBuilder("chmod", "+x", dataDirectory.resolve("download").toString()).start().waitFor();
                switch (config.get("VOICEVOX-type")) {
                    case "2" -> new ProcessBuilder("bash", "-c", "./download --device directml").directory(dataDirectory.toFile()).start().waitFor();
                    case "3" -> new ProcessBuilder("bash", "-c", "./download --device cuda").directory(dataDirectory.toFile()).start().waitFor();
                    default -> new ProcessBuilder("bash", "-c", "./download").directory(dataDirectory.toFile()).start().waitFor();
                }
            }
            Velodicord.velodicord.logger.info("VOICEVOXのライブラリダウンロード完了");
        }
        V4S4J.init(String.valueOf(dataDirectory.resolve("voicevox_core")), !rawOsName.startsWith("Win"));
    }
}
