package com.example.turistadroid_shei_albert.ui.CerrarSesion

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.turistadroid_shei_albert.LoginActivity
import com.example.turistadroid_shei_albert.R


class CerrarSesionFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cerrar_sesion, container, false)

        //AlergtDialog que nos pregunta si queremos cerrar sesion o no
        AlertDialog.Builder(this.context)
            .setIcon(R.drawable.logo1)
            .setTitle("Cerrar sesión actual")
            .setMessage("¿Desea salir de la sesión actual?")
            .setPositiveButton(getString(R.string.si)){ dialog, which -> cerrarSesion()}
            .setNegativeButton(getString(R.string.no),null)
            .show()

        return root

    }
    //Si cerramos sesion, volvemos al Login
    private fun cerrarSesion() {
        val intent = Intent(this.context, LoginActivity::class.java)
        startActivity(intent)
    }
}

