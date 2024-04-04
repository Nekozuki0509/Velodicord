package velodicord

import com.sun.jna.ptr.{IntByReference, PointerByReference}
import dev.capslock.voicevoxcore4s.{Core, Util}

import java.io.FileOutputStream

object VoiceVox extends App {
  def init(): Unit = {
    // Extract dictionary files from JAR into real file system.
    // Files will go to temporary directory in OS.
    val dictionaryDirectory = Util.extractDictFiles()

    // Load required library
    val searchPath = (os.pwd / "lib").toString
    com.sun.jna.NativeLibrary.addSearchPath("voicevox_core", searchPath)
    com.sun.jna.NativeLibrary.addSearchPath("onnxruntime", searchPath)
    System.load((os.pwd / "lib" / "libonnxruntime.so.1.13.1").toString)

    // vocevox_core will be loaded automatically.
    val core = Core()
    println(core.voicevox_get_version())

    val initializeOptions = core.voicevox_make_default_initialize_options()
    initializeOptions.open_jtalk_dict_dir = dictionaryDirectory
    initializeOptions.acceleration_mode =
      Core.VoicevoxAccelerationMode.VOICEVOX_ACCELERATION_MODE_CPU.code
    println(initializeOptions)

    val initialized = core.voicevox_initialize(initializeOptions)
    println(s"VoiceVoxInit, voicevoxcore4s! initialized? -> (${initialized})")

  }

  def tts(m:String): Unit = {
    if (initialized == Core.VoicevoxResultCode.VOICEVOX_RESULT_OK.code) {
      val model_shikoku_metan = 2
      val loadResult = core.voicevox_load_model(model_shikoku_metan)
      println(s"model loaded: $loadResult")

      // Generating voice.
      // First, we should have two pointers: result length, result wav buffer.
      val bytesLength = new IntByReference()
      val wavBuffer = new PointerByReference()

      // Second, prepare TTS(talk to speech) options
      val ttsOpts = core.voicevox_make_default_tts_options()
      ttsOpts.kana = false

      // Run TTS.
      // VOICEVOX will rewrite memory beyond pointer.
      val tts = core.voicevox_tts(
        m,
        model_shikoku_metan,
        ttsOpts,
        bytesLength, // this will be modified
        wavBuffer // this will be modified
      )
      if (tts == Core.VoicevoxResultCode.VOICEVOX_RESULT_OK.code) {
        // You can acquire data from pointer using getValue()
        val length = bytesLength.getValue()
        val resultPtr = wavBuffer.getValue()
        val resultArray = resultPtr.getByteArray(0, bytesLength.getValue())
        println(s"length: $length bytes")

        // Write out buffer.
        val fs = new FileOutputStream("./result.wav")
        fs.write(resultArray)
        println("wrote array into file")
        fs.close()

        // Release allocated memory.
        core.voicevox_wav_free(resultPtr)
        println("freed array")
      } else {
        println(s"tts failed: $tts")
      }
    }
  }

  def fin(): Unit = {
    // When program exit, bury VOICEVOX instance.
    core.voicevox_finalize()

    // Delete dictionary directory extracted into temporary directory.
    os.remove.all(os.Path(dictionaryDirectory))
  }
}