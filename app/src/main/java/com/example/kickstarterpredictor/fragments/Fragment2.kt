package com.example.kickstarterpredictor.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kickstarterpredictor.R
import com.example.kickstarterpredictor.databinding.Fragment2Binding

class Fragment2 : Fragment() {
    private  var _binding : Fragment2Binding? = null
    private val binding get() = _binding!!
    private lateinit var v : View
    private val TAG = "Fragment2"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Fragment2Binding.inflate(inflater, container,false)
        v = binding.root
        return v
    }

    override fun onStart() {
        super.onStart()
        val mBundle = this.arguments
        if (mBundle == null) {
            var num: Float? = mBundle?.getFloat("probability")
            binding.probTextView.text = num.toString()
        }
        else{
            Log.w(TAG, "Bundle is null")
        }
    }


}