package velodicord;

import V4S4J.V4S4J.V4S4J;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static velodicord.Config.*;

public class VOICEVOX {
    public static Map<Integer, String> voicevox = new LinkedHashMap<>();

    public static void init() throws IOException, InterruptedException {
        String rawOsName = System.getProperty("os.name");
        if (Files.notExists(dataDirectory.resolve("voicevox_core"))) {
            Velodicord.velodicord.logger.info("VOICEVOXのライブラリをダウンロード中");
            String rawOsArch = System.getProperty("os.arch");
            String osName, osArch;
            if (rawOsName.startsWith("Win")) {
                InputStream in = new URL("https://github.com/VOICEVOX/voicevox_core/releases/download/0.15.7/download-windows-x64.exe").openStream();
                try {
                    Files.copy(in, dataDirectory.resolve("download.exe"));
                } catch (FileAlreadyExistsException ignored) {}

                new ProcessBuilder("cmd.exe", "/c", "cd /d", dataDirectory.toString()).directory(dataDirectory.toFile()).start().waitFor();
                switch (config.get("VOICEVOX-type")) {
                    case "2" ->
                            new ProcessBuilder("cmd.exe", "/c", "download --version 0.15.7 --additional-libraries-version 0.1.0 --device directml").directory(dataDirectory.toFile()).start().waitFor();
                    case "3" ->
                            new ProcessBuilder("cmd.exe", "/c", "download --version 0.15.7 --additional-libraries-version 0.1.0 --device cuda").directory(dataDirectory.toFile()).start().waitFor();
                    default ->
                            new ProcessBuilder("cmd.exe", "/c", "download --version 0.15.7 --additional-libraries-version 0.1.0").directory(dataDirectory.toFile()).start().waitFor();
                }
            } else {
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
                InputStream in = new URL("https://github.com/VOICEVOX/voicevox_core/download/0.15.7/download/download-" + osName + "-" + osArch).openStream();
                try {
                    Files.copy(in, dataDirectory.resolve("download"));
                } catch (FileAlreadyExistsException ignored) {}
                new ProcessBuilder("chmod", "+x", dataDirectory.resolve("download").toString()).start().waitFor();
                switch (config.get("VOICEVOX-type")) {
                    case "2" ->
                            new ProcessBuilder("bash", "-c", "./download --version 0.15.7 --additional-libraries-version 0.1.0 --device directml").directory(dataDirectory.toFile()).start().waitFor();
                    case "3" ->
                            new ProcessBuilder("bash", "-c", "./download --version 0.15.7 --additional-libraries-version 0.1.0 --device cuda").directory(dataDirectory.toFile()).start().waitFor();
                    default ->
                            new ProcessBuilder("bash", "-c", "./download --version 0.15.7 --additional-libraries-version 0.1.0").directory(dataDirectory.toFile()).start().waitFor();
                }
            }
            Velodicord.velodicord.logger.info("VOICEVOXのライブラリダウンロード完了");
        }


        List<Map<String, Object>> data = gson.fromJson(new FileReader(String.valueOf(dataDirectory.resolve("voicevox_core").resolve("model").resolve("metas.json"))), List.class);

        for (Map<String, Object> speakerData : data) {
            String botName = (String) speakerData.get("name");
            List<Map<String, Object>> styles = (List<Map<String, Object>>) speakerData.get("styles");
            for (Map<String, Object> style : styles) {
                String combinedName = botName + "(" + style.get("name") + ")";
                if (!voicevox.containsValue(combinedName))
                    voicevox.put(((Number) style.get("id")).intValue(), combinedName);
            }
        }


        V4S4J.init(String.valueOf(dataDirectory.resolve("voicevox_core")), !rawOsName.startsWith("Win"));
    }
}
