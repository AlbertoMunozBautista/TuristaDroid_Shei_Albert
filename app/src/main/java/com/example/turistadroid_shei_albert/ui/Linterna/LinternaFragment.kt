package com.example.turistadroid_shei_albert.ui.Linterna

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.turistadroid_shei_albert.R

class LinternaFragment : Fragment() {

    private lateinit var appContext : Context
    private lateinit var camera: android.hardware.Camera
    var isFlash = false
    var isOn = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_linterna, container, false)

        appContext = context!!.applicationContext
        val imagenLinternaLinterna: ImageView = root.findViewById(R.id.ivLinterna)
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.CAMERA
            ) !== PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity!!, arrayOf<String>(Manifest.permission.CAMERA), 1000)
        } else {
            if (activity!!.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                camera = Camera.open()
                var p : Camera.Parameters
                p =camera.getParameters()
                isFlash = true
            }

            //Si pulsamos linterna
            imagenLinternaLinterna.setOnClickListener {
                var p : Camera.Parameters
                p =camera.getParameters()
                //y el la vriable flash esta a true, la encendemos
                if (isFlash) {
                    isOn = if (!isOn) {
                        //cambiamos el icono de la linterna, para visualizar en pantalla que el flash deberia
                        //estar encendido
                        imagenLinternaLinterna.setImageResource(R.drawable.ic_linterna_amarilla)
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH)
                        camera.setParameters(p)
                        camera.startPreview()
                        true
                        //Si el valor de la variable isFlash cambia
                    } else {
                        //Cambiamos de nuevo el icono de la linterna, para que se vea apagada
                        imagenLinternaLinterna.setImageResource(R.drawable.ic_linterna_negra)
                        //Apagamos el flash
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF)
                        camera.setParameters(p)
                        camera.stopPreview()
                        false
                    }
                } else {
                }
            }
        }
        return root
    }

}