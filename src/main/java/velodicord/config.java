package velodicord;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.plugin.annotation.DataDirectory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class config {

    public static Properties p;

    public static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String, String> dic;

    @DataDirectory
    public static Path dataDirectory;

    public static Path dicjson;

    public static void init() throws IOException, InterruptedException {
        if (Files.notExists(dataDirectory))
            try {
                Files.createDirectory(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException("Velodicordのconfigディレクトリを作れませんでした");
            }

        if (Files.notExists(dicjson))
            Files.copy(Objects.requireNonNull(Velodicord.class.getResourceAsStream("/dic.json")), dicjson);

        TypeReference<HashMap<String, String>> reference = new TypeReference<>(){};
        dic = mapper.readValue(mapper.readTree(new File(String.valueOf(dicjson))).toString(), reference);

        p.load(new FileReader(String.valueOf(dataDirectory.resolve("config.properties"))));

        if (Files.notExists(dataDirectory.resolve("voicevox_core"))) {
            Velodicord.velodicord.logger.info("VOICEVOXのライブラリをダウンロード中");
            Runtime.getRuntime().exec((System.getProperty("os.name").startsWith("Win") ? "Invoke-WebRequest https://github.com/VOICEVOX/voicevox_core/releases/latest/download/download-windows-x64.exe -OutFile ./velodicord/download.exe" :
                    "curl -sSfL https://github.com/VOICEVOX/voicevox_core/releases/latest/download/download-" + (System.getProperty("os.name").startsWith("Mac") ? "osx" : "linux") + "-" + (System.getProperty("os.arch").startsWith("aarch64") ? "arm64" : "x64")
                            + " -o ./velodicord/download;chmod +x download") + ";./velodicord/download").waitFor();
            Velodicord.velodicord.logger.info("VOICEVOXのライブラリダウンロード完了");
        }
        VoiceVox.init();
    }
}
