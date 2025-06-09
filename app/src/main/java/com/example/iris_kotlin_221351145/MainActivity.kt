package com.example.iris_kotlin_221351145

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.iris_kotlin_221351022.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val modelPath = "iris.tflite"

    private lateinit var resultText: TextView
    private lateinit var edtSepalLengthCm: EditText
    private lateinit var edtSepalWidthCm: EditText
    private lateinit var edtPetalLengthCm: EditText
    private lateinit var edtPetalWidthCm: EditText
    private lateinit var checkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi view
        resultText = findViewById(R.id.txtResult)
        edtSepalLengthCm = findViewById(R.id.edtSepalLengthCm)
        edtSepalWidthCm = findViewById(R.id.edtSepalWidthCm)
        edtPetalLengthCm = findViewById(R.id.edtPetalLengthCm)
        edtPetalWidthCm = findViewById(R.id.edtPetalWidthCm)
        checkButton = findViewById(R.id.btnCheck)

        // Inisialisasi interpreter TensorFlow Lite
        try {
            initInterpreter()
            Log.d("MainActivity", "Interpreter initialized successfully")
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to init interpreter: ${e.message}")
            resultText.text = "Gagal load model"
        }

        // Tombol Predict ditekan
        checkButton.setOnClickListener {
            val input1 = edtSepalLengthCm.text.toString()
            val input2 = edtSepalWidthCm.text.toString()
            val input3 = edtPetalLengthCm.text.toString()
            val input4 = edtPetalWidthCm.text.toString()

            if (input1.isEmpty() || input2.isEmpty() || input3.isEmpty() || input4.isEmpty()) {
                resultText.text = "Input tidak boleh kosong"
                return@setOnClickListener
            }

            val result = try {
                doInference(input1, input2, input3, input4)
            } catch (e: Exception) {
                Log.e("MainActivity", "Inference failed: ${e.message}")
                -1
            }

            resultText.text = when (result) {
                0 -> "iris-setosa"
                1 -> "iris-versicolor"
                2 -> "iris-virginica"
                else -> "Gagal memprediksi"
            }
        }
    }

    private fun initInterpreter() {
        val options = Interpreter.Options().apply {
            setNumThreads(4)
            setUseNNAPI(true)
        }
        interpreter = Interpreter(loadModelFile(assets, modelPath), options)
    }

    private fun doInference(input1: String, input2: String, input3: String, input4: String): Int {
        val inputVal = arrayOf(floatArrayOf(
            input1.toFloat(),
            input2.toFloat(),
            input3.toFloat(),
            input4.toFloat()
        ))

        val output = Array(1) { FloatArray(3) }
        interpreter.run(inputVal, output)

        Log.d("MainActivity", "Inference output: ${output[0].toList()}")

        val maxVal = output[0].maxOrNull() ?: return -1
        return output[0].indexOfFirst { it == maxVal }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onDestroy() {
        interpreter.close()
        super.onDestroy()
    }
}
