package com.example.turistadroid_shei_albert

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    //Variables de la clase
    var correo: String = ""
    var contraseña: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Iniciamos la base de datos realm
        ///////////////UsuarioController.initRealm(this)

        //Pedimos los permisos con dexter
        initPermisos()

        //Para que desaparezcan las partes de arriba de la pantalla
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        this.supportActionBar!!.hide()

        setContentView(R.layout.activity_login)

        //Al pulsar el text view registrarse nos lleva al RegistroActivity
        tvLoginRegistrarse.setOnClickListener(){

            ////////////////startActivity(Intent(this, RegistroActivity::class.java))
            finish()

        }

        //Al pulsar el botón entrar nos lleva al método de recoger los datos
        btnLoginEntrar.setOnClickListener(){
            //////////////recogerDatos()
        }


    }

    /**
     * Recogermos los datos de los edit text y los text view, comprobamos que no
     * están vacíos y entramos en el main activity
     */

    /**
    private fun recogerDatos() {

        //Declaración de variable

        //lista con todos los usuarios de la aplicación
        val listaUsuarios: MutableList<Usuario>

        var usuarioActual : Usuario? = null
        var encontrado : Boolean = false;

        //Consulta que devuelve todos los usuarios de la aplicación
        listaUsuarios = Realm.getDefaultInstance().where<Usuario>().findAll()

        correo = etLoginCorreo.text.toString()
        contraseña = etLoginContrasena.text.toString()


        //si los campos correo o contraseña están vacíos nos pide que los rellenemos
        if (correo.isEmpty() || contraseña.isEmpty()){
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()

        } else {

            //si no están vacíos se recorre la lista y si el usuario y la contraseña están en la base de
            //datos, entramos al main
            for(u in 0..listaUsuarios.size - 1) {
                if (listaUsuarios[u].correo.equals(correo) && listaUsuarios[u].contraseña.equals(contraseña)){
                    usuarioActual = listaUsuarios[u]
                    encontrado = true
                }
            }

            if (encontrado){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("IDCorreo", usuarioActual?.correo.toString())
                startActivity(intent)

                finish()
            } else {
                Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_SHORT).show()
            }

        }

    }
    */

    /**
     * Función con la que vamos a comprobar los permisos de la aplicación
     * lo vamos a hacer con la librería dexter
     */
    private fun initPermisos() {

        Dexter.withContext(this)
                //Permisos que queremos comprobar
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.INTERNET
                )

                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        //si le damos todos los permisos nos saltará un toast diciendolo
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(applicationContext, "Permisos concedidos", Toast.LENGTH_SHORT).show()
                        }

                        //si no tenemos todos los permisos nos lo recordará
                        if (report.isAnyPermissionPermanentlyDenied) {
                            Toast.makeText(applicationContext, "Faltan permisos!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                            permissions: List<PermissionRequest?>?,
                            token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).withErrorListener { Toast.makeText(applicationContext, "Existe errores! ", Toast.LENGTH_SHORT).show() }
                .onSameThread()
                .check()
    }

}