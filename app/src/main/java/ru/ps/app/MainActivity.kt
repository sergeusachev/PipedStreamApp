package ru.ps.app

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import java.io.PipedReader
import java.io.PipedWriter

class MainActivity : AppCompatActivity() {

    companion object {
        private const val BUFFER_SIZE = 1024 * 4
    }

    private lateinit var pipedReader: PipedReader
    private lateinit var pipedWriter: PipedWriter
    private lateinit var worker: Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pipedReader = PipedReader(BUFFER_SIZE)
        pipedWriter = PipedWriter()
        pipedWriter.connect(pipedReader)

        setContentView(R.layout.activity_main)
        val editText = findViewById<EditText>(R.id.etInput)
        editText.addTextChangedListener(
            onTextChanged = { text, start, before, count ->
                if (count > before) {
                    pipedWriter.write(text!!.substring(before until count))
                }
            }
        )
        worker = MyReaderThread(pipedReader)
        worker.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        worker.interrupt()
        pipedWriter.close()
        pipedReader.close()
    }

    class MyReaderThread(private val pipedReader: PipedReader) : Thread() {

        override fun run() {
            super.run()
            while (currentThread().isInterrupted.not()) handleData()
        }

        private fun handleData() {
            pipedReader.use {
                it.forEachLine { line -> line.forEach { c -> Log.d("PIPE", c.toString()) } }
            }
        }
    }
}

