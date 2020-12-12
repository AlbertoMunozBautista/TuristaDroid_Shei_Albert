package com.example.turistadroid_shei_albert

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Para que desaparezcan las partes de arriba de la pantalla
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        this.supportActionBar!!.hide()

        setContentView(R.layout.activity_splash)


        //Animaciones
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_arriba)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.desplazamiento_abajo)

        tvSplashTexto.setAnimation(animation1)
        tvSplashTexto2.setAnimation(animation1)
        imaSplashLogo.setAnimation(animation2)

        //Se lanza el main activity a los 3 segundos
        Handler().postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000)


    }
}