package com.example.kickstarterpredictor.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.kickstarterpredictor.R
import com.example.kickstarterpredictor.classes.SpinnerItem
import com.example.kickstarterpredictor.databinding.Fragment1Binding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Fragment1 : Fragment() {
    private var _binding: Fragment1Binding ? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private val TAG = "Fragment1"
    lateinit var btnConfirm: Button
    lateinit var v: View
    lateinit var interpreter: Interpreter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Fragment1Binding.inflate(inflater, container, false)
        v = binding.root
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        val categoryList : ArrayList<SpinnerItem> = featureListGenerator(R.raw.categories)
        val typeList : ArrayList<SpinnerItem> = featureListGenerator(R.raw.theme)
        val countryList : ArrayList<SpinnerItem> = featureListGenerator(R.raw.countries)
        val currencyList : ArrayList<SpinnerItem> = featureListGenerator(R.raw.currencies)

        val typeAdapter = ArrayAdapter<SpinnerItem>(requireActivity(), android.R.layout.simple_spinner_item, typeList)
        val currencyAdapter = ArrayAdapter<SpinnerItem>(requireActivity(), android.R.layout.simple_spinner_item, categoryList)
        val categoryAdapter = ArrayAdapter<SpinnerItem>(requireActivity(), android.R.layout.simple_spinner_item, currencyList)
        val countryAdapter = ArrayAdapter<SpinnerItem>(requireActivity(), android.R.layout.simple_spinner_item, countryList)

        binding.categorySpinner.adapter = categoryAdapter
        binding.currencySpinner.adapter = currencyAdapter
        binding.typeSpinner.adapter = typeAdapter
        binding.countrySpinner.adapter = countryAdapter

        var x: FloatArray = FloatArray(1)
        var res = arrayOf(x)

        binding.buttonConfirm.isEnabled = false

        binding.buttonConfirm.setOnClickListener{
            //Hardcoded values are drawn from dataset evaluation...
            val mCategory = binding.categorySpinner.selectedItem as SpinnerItem
            val mCurrency = binding.currencySpinner.selectedItem as SpinnerItem
            val mType = binding.typeSpinner.selectedItem as SpinnerItem
            val mCountry = binding.countrySpinner.selectedItem as SpinnerItem
            if(binding.editTextNumber.text.toString().isNotEmpty() && binding.deltaTimeEditText.text.toString().isNotEmpty()) {
                val currencySelected = standardScaling(value = mCurrency.value.toFloat(), mean = 1.32f, std = 1.41f)
                val typeSelected = standardScaling(value = mType.value.toFloat(), mean = 4.98f, std = 4.12f)
                val countrySelected = standardScaling(value = mCountry.value.toFloat(), mean = 1.63f, std = 2.68f)
                var categorySelected = standardScaling(value =mCategory.value.toFloat(), mean = 38.97f, std = 34.42f)
                var goalSelected = standardScaling(value = binding.editTextNumber.text.toString().toFloat(), std = 1120000f, mean = 450000f)
                val deltaTime = standardScaling(value = binding.deltaTimeEditText.text.toString().toFloat(), std= 60.68f, mean = 33.40f)
                Log.d(
                    TAG,
                    "Cat is $categorySelected. Currency is $currencySelected Type is $typeSelected Country is $countrySelected Time is $deltaTime and goal is $goalSelected"
                )

                val inputDataBuffer: ByteBuffer = ByteBuffer.allocateDirect(24).order(ByteOrder.nativeOrder())
                try {
                    inputDataBuffer
                        .putFloat(0, categorySelected)
                        .putFloat(4, typeSelected)
                        .putFloat(8, currencySelected)
                        .putFloat(12, countrySelected)
                        .putFloat(16, goalSelected)
                        .putFloat(20, deltaTime)

                    interpreter.run(inputDataBuffer, res)
                    Log.d(TAG, "${res[0][0]}")
                    binding.resultTextView.text = "${res[0][0] * 100} %"
                } catch (exc: Exception) {
                    Log.w(TAG, "was $exc")
                }
            }
            else{
                Snackbar.make(v, "Completa todos los campos", Snackbar.LENGTH_SHORT).show()
            }
        }


        val remoteModel = FirebaseCustomRemoteModel.Builder("nonflat")
            .build()
        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
            .addOnCompleteListener { task ->
                val modelFile = task.result
                if (modelFile != null) {
                    Log.d(TAG, "downloaded")
                    interpreter = Interpreter(modelFile)
                    testModel(res)
                } else {
                    Log.w(TAG, "Failure")
                    Snackbar.make(v, "Estás operando online", Snackbar.LENGTH_LONG).show()
                    binding.buttonConfirm.isEnabled = true
                    interpreter = Interpreter(loadModelFile(this.requireActivity()))
                    testModel(res)
                }
            }
    }

    private fun testModel(res: Array<FloatArray>) {
        val mByteBuffer = ByteBuffer.allocateDirect(24).order(ByteOrder.nativeOrder())
        try {
            mByteBuffer
                .putFloat(0, 1f)
                .putFloat(1, 2f)
                .putFloat(2, 3f)
                .putFloat(3, 4f)
                .putFloat(4, 5f)
                .putFloat(5, 6f)
                .putFloat(5, 6f)

        } catch (exc: Exception) {
            Log.d(TAG, "was ${exc.toString()}")
        }
        try {
            interpreter.run(mByteBuffer, res)
            Log.d(TAG, "${res[0][0]}")
            binding.buttonConfirm.isEnabled = true
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "saw $e")
        }
    }

    private fun featureListGenerator(id: Int): ArrayList<SpinnerItem> {
        val featureString = resources.openRawResource(id).bufferedReader().use{
            it.readText()
        }
        val featureJSON = JSONObject(featureString)
        val featureKeys = featureJSON.keys()
        val featureList : ArrayList<SpinnerItem> = ArrayList()
        featureKeys.forEach { it
            val newItem = SpinnerItem(featureJSON.get(it) as Int, it)
            featureList.add(newItem)
        }
        return featureList
    }

    public fun standardScaling(value : Float, std: Float, mean: Float): Float {
        return (value - mean) / std
    }

    fun getModel(){

    }

    /** Memory-map the model file in Assets.  */
    @Throws(IOException::class)
    private fun loadModelFile(activity: Activity): MappedByteBuffer {
        val fileDescriptor = activity.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}