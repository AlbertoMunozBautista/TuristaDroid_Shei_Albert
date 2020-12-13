package com.example.turistadroid_shei_albert

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.turistadroid_shei_albert.Usuarios.Usuario
import com.example.turistadroid_shei_albert.Usuarios.UsuarioController

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    var usuarioActual : Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_misSitios, R.id.nav_cerca, R.id.nav_linterna, R.id.nav_perfil, R.id.nav_cerrarSesion), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        //Inicializamos la bbdd
        UsuarioController.initRealm(this)
        //Recibimos el usuario que se ha logueado
        var correo = intent.getSerializableExtra("IDCorreo") as String
        usuarioActual = UsuarioController.consultaPorID(correo)

        //Cambiamos los datos del perfil en el navigation (correo, nombre y foto)
        var textMainCorreo : TextView = navView.getHeaderView(0).findViewById(R.id.tvMainCorreo)
        var textMainNombre : TextView = navView.getHeaderView(0).findViewById(R.id.tvMainNombre)
        var imagenMainFoto : ImageView = navView.getHeaderView(0).findViewById(R.id.imaMainFoto)
        textMainCorreo.text = correo
        textMainNombre.text = usuarioActual?.nombre.toString()
        var imagen: Bitmap? = Utilidades.base64ToBitmap(usuarioActual?.fotoUsuario)
        imagenMainFoto.setImageBitmap(imagen)


        //Si pulsamos la foto, cerramos la sesion. Para ello necesitamos confirmacion
        imagenMainFoto.setOnClickListener(){
            Log.i("Salir", "Saliendo...")
            AlertDialog.Builder(this)
                .setIcon(R.drawable.logo1)
                .setTitle("Cerrar sesión actual")
                .setMessage("¿Desea salir de la sesión actual?")
                .setPositiveButton(getString(R.string.si)){ dialog, which -> cerrarSesion()}
                .setNegativeButton(getString(R.string.no),null)
                .show()
        }

    }

    //Cerramos sesion y volvemos al login
    private fun cerrarSesion() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this?.finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
