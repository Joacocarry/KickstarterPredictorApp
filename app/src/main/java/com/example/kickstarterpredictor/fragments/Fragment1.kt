package com.example.kickstarterpredictor.fragments

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.kickstarterpredictor.R
import com.google.firebase.FirebaseError
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.TensorFlowLite
import java.io.FileInputStream
import java.lang.IndexOutOfBoundsException
import java.nio.BufferOverflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.AsynchronousFileChannel.open
import java.nio.channels.AsynchronousServerSocketChannel.open
import java.nio.channels.DatagramChannel.open
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.open
import kotlin.properties.Delegates

class Fragment1 : Fragment() {
    private val TAG = "Fragment1"
    lateinit var btnConfirm: Button
    lateinit var v: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v = inflater.inflate(R.layout.fragment_1, container, false)
        btnConfirm = v.findViewById(R.id.buttonConfirm)
        return v
    }

    override fun onStart() {
        super.onStart()
        val remoteModel = FirebaseCustomRemoteModel.Builder("kickpred").build()
        lateinit var interpreter: Interpreter
        var x:FloatArray = FloatArray(1)
        var y:FloatArray = FloatArray(1)
        var res = arrayOf(x)
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
            .addOnCompleteListener { task ->
                val modelFile = task.result
                if (modelFile != null) {

                    Log.d(TAG, "downloaded")
                    interpreter = Interpreter(modelFile)
                    val mByteBuffer = ByteBuffer.allocateDirect(20).order(ByteOrder.nativeOrder())
                    try {
                        mByteBuffer
                            .putFloat(0, 1f)
                            .putFloat(1, 2f)
                            .putFloat(2, 3f)
                            .putFloat(3, 4f)
                            .putFloat(4, 5f)
                            .putFloat(5, 6f)
                    } catch (exc: Exception){
                        Log.d(TAG, "was ${exc.toString()}")
                    }
                    try {
                        interpreter.run(mByteBuffer, res)
                        Log.d(TAG, "${res[0][0]}")
                    } catch (e: java.lang.Exception){
                        Log.d(TAG, "saw $e")
                    }
                    } else{
                    Log.d(TAG,"Failure")
                }
            }




        /*
            val remoteModel = FirebaseCustomRemoteModel.Builder("kickpred").build()
            val conditions = FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build()
            FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener {task->
                    Log.d(TAG, "${remoteModel}")
                    // Download complete. Depending on your app, you could enable the ML
                    // feature, or switch from the local model to the remote model, etc.
                    if(task.isSuccessful){
                        val options = FirebaseModelInterpreterOptions.Builder(remoteModel).build()
                        val interpreter = FirebaseModelInterpreter.getInstance(options)
                        Log.d(TAG, "Model with $remoteModel")
                        val ioOptions = FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1,5))
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1))
                            .build()
                        val mByteBuffer = ByteBuffer.allocateDirect(12).order(ByteOrder.nativeOrder())
                        try {
                            mByteBuffer
                                .putFloat(0, 1f)
                                .putFloat(1, 2f)
                                .putFloat(2, 3f)
                                .putFloat(3, 4f)
                                .putFloat(4, 5f)
                        } catch (exc: BufferOverflowException) {
                            Log.w(TAG, exc)
                        }
                        val inputs = FirebaseModelInputs.Builder()
                            .add(mByteBuffer)
                            .build()

                        interpreter!!.run(inputs, ioOptions)
                            .addOnSuccessListener {res->
                                Log.d(TAG, "$res")
                            }
                            .addOnFailureListener{
                                Log.w(TAG, it)
                            }

                    }
                    else{
                        Log.w(TAG,"Failure")
                    }
                    }
            btnConfirm.setOnClickListener {

            }
        */
    }


    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

}