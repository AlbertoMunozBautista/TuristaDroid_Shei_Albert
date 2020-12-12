package com.example.turistadroid_shei_albert.ui.CercaDeMi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.turistadroid_shei_albert.R

class CercaDeMiFragment : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cerca_de_mi, container, false)

        return root
    }
}