package com.example.turistadroid_shei_albert

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toFile
import com.example.turistadroid_shei_albert.Usuarios.Usuario
import com.example.turistadroid_shei_albert.Usuarios.UsuarioController
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_registro.*
import java.io.IOException

class RegistroActivity : AppCompatActivity() {
    //Declaracion de variables
    private val GALERIA = 1
    private val CAMARA = 2
    private val IMAGEN_DIR = "/TuristaDroid"
    private lateinit var IMAGEN_URI: Uri
    private lateinit var IMAGEN_MEDIA_URI: Uri
    private val PROPORCION = 600
    private var IMAGEN_NOMBRE = ""
    private var IMAGEN_COMPRES = 30
    private var imagenUsuario: String = ""
    var correo: String = ""
    var nombre : String = ""
    var usuario : String =""
    var contraseña : String =""




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        //Inicializamos la base de datos Realm
        UsuarioController.initRealm(this)


        imaRegistroFoto.setImageResource(R.drawable.fotoredonda)
        val etNombre: EditText = findViewById(R.id.etRegistroNombre)
        val etUsuario : EditText = findViewById(R.id.etRegistroUsuario)
        val etCorreo : EditText = findViewById(R.id.etRegistroCorreo)
        val etContra : EditText = findViewById(R.id.etRegistroContrasena)
        val ivImagen : ImageView = findViewById(R.id.imaRegistroFoto)

        //Se inicializa la variable del dialogo de la cámara
        var dialog = Dialog(this)

        val actionbar = supportActionBar
        //Establecer el nombre
        actionbar!!.title = "Registro"
        //Muestra la flechita de volver atrás
        actionbar.setDisplayHomeAsUpEnabled(true)


        //Para que los et y los btn esten por delante de la imagen
        etRegistroNombre.bringToFront()
        etRegistroUsuario.bringToFront()
        etRegistroCorreo.bringToFront()
        etRegistroContrasena.bringToFront()
        btnRegistroCancelar.bringToFront()
        btnRegistroRegistrarse.bringToFront()

        //Cuando se pulsa el botón cancelar
        btnRegistroCancelar.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        //Cuando se pulsa el botón regirstarse
        btnRegistroRegistrarse.setOnClickListener(){
            //Recogen los datos del layout de registro
            nombre = etNombre.text.toString()
            usuario = etUsuario.text.toString()
            correo = etCorreo.text.toString()
            contraseña = etContra.text.toString()
            //Se comprueban los campos
            comprobarCampos(nombre, usuario, correo, contraseña)

        }



        //Cuando pulsamos para elegir una foto para nuestro registro
        imaRegistroFoto.setOnClickListener { view ->

            //Abrimos un dialog con las 2 opciones (camara o galeria)
            dialog.setContentView(R.layout.camara_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Se rescatan las imágenes del layout de la cámara (si no se rescatan no funciona)
            var imagenCamaraCamara: ImageView = dialog.findViewById(R.id.imaCamaraCamara)
            var imagenCamaraGaleria: ImageView = dialog.findViewById(R.id.imaCamaraGaleria)

            imagenCamaraCamara.setOnClickListener(){
                fotoCamara()
                dialog.dismiss()
            }
            imagenCamaraGaleria.setOnClickListener(){
                fotoGaleria()
                dialog.dismiss()
            }
            dialog.show()
        }

    }

    /**Comprobamos que los campos no esten vacios, para ello recibimos todos del layout*/
    private fun comprobarCampos(nombre: String, usuario: String, correo: String, contraseña: String) {
        //Creamos una lista de Usuarios y guardamos todos los que esten en la bbd
        val listaUsuarios: MutableList<Usuario>
        listaUsuarios = Realm.getDefaultInstance().where<Usuario>().findAll()

        var isRegistrado :Boolean = false

        //Si existe algun campo vacio, mostramos un Toast
        if (nombre.equals("") || usuario.equals("") || correo.equals("") || contraseña.equals("")
            || imagenUsuario.equals("")
        ) {
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
            //Si estan todos los campos rellenos se comprueba que ese correo (PK) no haya sido registrado en la bbdd previamente
        } else {
            Log.i("click", "campos completos")
            for(u in 0..listaUsuarios.size - 1) {
                if (listaUsuarios[u].correo.equals(correo)) {
                    Log.i("click", "exiten 2 correos registados iguales")
                    Toast.makeText(this, "Ya hay un usuario registrado con ese correo", Toast.LENGTH_SHORT).show()
                    isRegistrado=true
                }
            }

            //Si el usuario no esta registrado, accedemos a la bbdd y hacemos un insert
            if(!isRegistrado) {
                nuevoElemento()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("IDCorreo", etRegistroCorreo.text.toString())
                startActivity(intent)
            }
        }
    }


    //Función para meterle funcionalidad a la flechita de atrás
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->
                startActivity(Intent(this, LoginActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    //Elegimos foto de la galeria
    private fun fotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALERIA)
    }

    //Tomamos foto de la camara
    private fun fotoCamara() {
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = Utilidades.crearNombreFichero()
        // Salvamos el fichero
        val fichero = Utilidades.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, applicationContext)!!
        IMAGEN_URI = Uri.fromFile(fichero)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGEN_URI)
        startActivityForResult(intent, CAMARA)
    }


    //Se ejecuta siempre al realizar una acción
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Si conseguimos entrar en la galeria
        if (requestCode == GALERIA) {
            Log.i("FOTO", "Hemos abierto la galeria")
            if (data != null) {
                // Obtenemos su URI con su dirección temporal
                val contentURI = data.data!!
                try {
                    // Obtenemos el bitmap de su almacenamiento externo
                    val bitmap: Bitmap
                    //Comprobamos la version del SDK
                    if (Build.VERSION.SDK_INT < 28) {
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentURI);
                    } else {
                        val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
                        bitmap = ImageDecoder.decodeBitmap(source)
                    }
                    Toast.makeText(this, "¡Foto rescatada de la galería!", Toast.LENGTH_SHORT).show()


                    //La imagen obtenida en bitmap la pasamos a 1ºbyteArray y 2ºbase64 para guardarla en la bbdd
                    var bm: ByteArray? = Utilidades.toBiteArray(bitmap)
                    imagenUsuario = Base64.encodeToString(bm, Base64.DEFAULT)

                    // Cambiamos a la imagen que hemos elegido
                    imaRegistroFoto.setImageBitmap(bitmap)
                    Utilidades.copiarImagen(bitmap, IMAGEN_DIR, IMAGEN_COMPRES, applicationContext)

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "¡Fallo Galeria!", Toast.LENGTH_SHORT).show()
                }
            }
            //Si hemos conseguido entrar a la camara
        } else if (requestCode == CAMARA) {

            Log.i("FOTO", "Hemos abierto la camara")

            try {
                val foto: Bitmap
                //Comprobamos la version del SDK
                if (Build.VERSION.SDK_INT < 28) {
                    foto = MediaStore.Images.Media.getBitmap(contentResolver, IMAGEN_URI)
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, IMAGEN_URI)
                    foto = ImageDecoder.decodeBitmap(source)
                }

                // Comprimimos foto
                IMAGEN_COMPRES = 10 * 10
                Utilidades.comprimirImagen(IMAGEN_URI.toFile(), foto, IMAGEN_COMPRES)

                var bm: ByteArray? = Utilidades.toBiteArray(foto)
                imagenUsuario = Base64.encodeToString(bm, Base64.DEFAULT)
                //Cambiamos la foto a la nueva que hayamos tomado
                imaRegistroFoto.setImageBitmap(foto)
                Toast.makeText(this, "¡Foto Salvada!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "¡Fallo Camara!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //Recogemos los datos que hayamos introducido e insertado el nuevo Usuario en la bbdd
    private fun nuevoElemento() {
        val usuarioNuevo = Usuario(etRegistroCorreo.text.toString(), etRegistroNombre.text.toString(),
            etRegistroUsuario.text.toString(), etRegistroContrasena.text.toString(), "", "", imagenUsuario)
        UsuarioController.insertDato(usuarioNuevo)
    }

}