package velodicord;

import com.google.gson.reflect.TypeToken;
import jp.hiroshiba.voicevoxcore.AccelerationMode;
import jp.hiroshiba.voicevoxcore.blocking.Onnxruntime;
import jp.hiroshiba.voicevoxcore.blocking.OpenJtalk;
import jp.hiroshiba.voicevoxcore.blocking.Synthesizer;
import jp.hiroshiba.voicevoxcore.blocking.VoiceModelFile;
import jp.hiroshiba.voicevoxcore.exceptions.InvalidModelDataException;
import jp.hiroshiba.voicevoxcore.exceptions.RunModelException;
import lombok.Getter;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Voicevox {
    @Getter
    private static List<ModelInfo> voicevox = new ArrayList<>();

    private static Synthesizer synthesizer = null;

    private static final Set<String> loadedModels = new HashSet<>();

    public static void init() {
        String rawOsName = System.getProperty("os.name");
        int accelerationMode = Integer.parseInt(Config.getConfig().get("VOICEVOX-type"));
        if (Files.notExists(Config.getDataDirectory().resolve("voicevox_core"))) {
            Velodicord.getVelodicord().getLogger().info("VOICEVOXのライブラリをダウンロード中");
            String rawOsArch = System.getProperty("os.arch");
            String osName, osArch;
            if (rawOsName.startsWith("Win")) {
                try {
                    Files.copy(Objects.requireNonNull(new URL("https://github.com/VOICEVOX/voicevox_core/releases/download/0.16.4/download-windows-x64.exe").openStream()), Config.getDataDirectory().resolve("download.exe"), REPLACE_EXISTING);
                } catch (IOException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのダウンロードに失敗しました: {}", ExceptionUtils.getStackTrace(e));
                }

                try {
                    new ProcessBuilder("cmd.exe", "/c", "cd /d", Config.getDataDirectory().toString()).directory(Config.getDataDirectory().toFile()).start().waitFor();

                    switch (accelerationMode) {
                        case 2 ->
                                new ProcessBuilder("cmd.exe", "/c", "download --devices directml").directory(Config.getDataDirectory().toFile()).inheritIO().start().waitFor();
                        case 3 ->
                                new ProcessBuilder("cmd.exe", "/c", "download --devices cuda").directory(Config.getDataDirectory().toFile()).inheritIO().start().waitFor();
                        default ->
                                new ProcessBuilder("cmd.exe", "/c", "download").directory(Config.getDataDirectory().toFile()).inheritIO().start().waitFor();
                    }
                } catch (InterruptedException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのダウンロードが中断されました: {}", ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのダウンロードに失敗しました: {}", ExceptionUtils.getStackTrace(e));
                }
            } else {
                if (rawOsName.startsWith("Mac")) {
                    osName = "macos";
                } else if (rawOsName.startsWith("Linux")) {
                    osName = "linux";
                } else {
                    throw new RuntimeException("Unsupported OS: %s".formatted(rawOsName));
                }
                if (rawOsArch.equals("x86_64") || rawOsArch.equals("amd64")) {
                    osArch = "x64";
                } else if (rawOsArch.equals("aarch64")) {
                    osArch = "arm64";
                } else {
                    throw new RuntimeException("Unsupported OS architecture: %s".formatted(rawOsArch));
                }

                try {
                    Files.copy(new URL("https://github.com/VOICEVOX/voicevox_core/releases/download/0.16.4/download-%s-%s".formatted(osName, osArch)).openStream(), Config.getDataDirectory().resolve("download"), REPLACE_EXISTING);
                } catch (MalformedURLException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのURLが間違っています: {}", ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのダウンロードに失敗しました: {}", ExceptionUtils.getStackTrace(e));
                }

                try {
                    new ProcessBuilder("chmod", "+x", Config.getDataDirectory().resolve("download").toString()).start().waitFor();
                    switch (accelerationMode) {
                        case 2 ->
                                new ProcessBuilder("bash", "-c", "./download --devices directml").directory(Config.getDataDirectory().toFile()).inheritIO().start().waitFor();
                        case 1 ->
                                new ProcessBuilder("bash", "-c", "./download --devices cuda").directory(Config.getDataDirectory().toFile()).inheritIO().start().waitFor();
                        default ->
                                new ProcessBuilder("bash", "-c", "./download").directory(Config.getDataDirectory().toFile()).inheritIO().start().waitFor();
                    }
                } catch (InterruptedException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのダウンロードが中断されました: {}", ExceptionUtils.getStackTrace(e));
                } catch (IOException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのライブラリのダウンロードに失敗しました: {}", ExceptionUtils.getStackTrace(e));
                }
            }
            Velodicord.getVelodicord().getLogger().info("VOICEVOXのライブラリダウンロード完了");
        }

        voicevox = Config.getGson().fromJson(new InputStreamReader(Objects.requireNonNull(Voicevox.class.getClassLoader().getResourceAsStream("models.json"))), new TypeToken<List<ModelInfo>>() {
        }.getType());

        Path voicevoxPath = Config.getDataDirectory().resolve("voicevox_core");
        synthesizer = Synthesizer.builder(Onnxruntime.loadOnce().filename(voicevoxPath.resolve("onnxruntime").resolve("lib").resolve(Onnxruntime.LIB_VERSIONED_FILENAME).toAbsolutePath().toString()).perform(), new OpenJtalk(voicevoxPath.resolve("dict").resolve("open_jtalk_dic_utf_8-1.11").toAbsolutePath().toString())).accelerationMode(AccelerationMode.AUTO).build();
    }

    public static synchronized boolean tts(String msg, int id, Path wavPath) {
        voicevox.stream().filter(model -> model.id() == id).findFirst().ifPresent(model -> {
            if (!loadedModels.contains(model.file())) {
                try {
                    synthesizer.loadVoiceModel(new VoiceModelFile(Config.getDataDirectory().resolve("voicevox_core").resolve("models").resolve("vvms").resolve(model.file()).toAbsolutePath().toString()));
                    loadedModels.add(model.file());
                } catch (InvalidModelDataException e) {
                    Velodicord.getVelodicord().getLogger().error("VOICEVOXのモデルの読み込みに失敗しました: {}", ExceptionUtils.getStackTrace(e));
                }
            }
        });

        try {
            Files.write(wavPath, synthesizer.tts(msg, id).perform());
        } catch (RunModelException e) {
            Velodicord.getVelodicord().getLogger().error("VOICEVOXの音声合成に失敗しました: {}", ExceptionUtils.getStackTrace(e));
            return false;
        } catch (IOException e) {
            Velodicord.getVelodicord().getLogger().error("VOICEVOXの音声ファイルの書き込みに失敗しました: {}", ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    public record ModelInfo(String file, String name, int id) {
    }
}
