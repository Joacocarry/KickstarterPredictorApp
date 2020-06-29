package com.example.kickstarterpredictor.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import com.example.kickstarterpredictor.R

class Fragment1 : Fragment() {

        lateinit var btnConfirm : Button
        lateinit var v : View
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

        btnConfirm.setOnClickListener{
        val action1to2 = Fragment1Directions.actionFragment1ToFragment2()
        v.findNavController().navigate(action1to2)
        }
    }
}