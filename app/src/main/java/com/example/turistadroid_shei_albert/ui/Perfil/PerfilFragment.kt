package com.example.turistadroid_shei_albert.ui.Perfil

import android.content.Context
import androidx.fragment.app.Fragment
import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Usuarios.Usuario
import com.example.turistadroid_shei_albert.Usuarios.UsuarioController
import com.example.turistadroid_shei_albert.Utilidades
import kotlinx.android.synthetic.main.fragment_perfil.*

import java.io.IOException

class PerfilFragment : Fragment(){
    // Declaracion de variables
    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var appContext : Context
    private lateinit var appResolver: ContentResolver
    private val IMAGEN_DIR = "/TuristaDroid"
    private lateinit var IMAGEN_URI: Uri
    private var IMAGEN_NOMBRE = ""
    private var IMAGEN_COMPRES = 30
    private var imagenMeter: String = ""
    var correo: String = ""
    var usuarioActual : Usuario? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_perfil, container, false)

        appContext = context!!.applicationContext
        appResolver = activity!!.contentResolver


        //Rescatamos los componentes asociados al layout de perfil
        val imagenPerfilFoto: ImageView = root.findViewById(R.id.imaPerfilFoto)
        val editTextPerfilCorreo: TextView = root.findViewById(R.id.etPerfilCorreo)
        val editTextPerfilNombre: TextView = root.findViewById(R.id.etPerfilNombre)
        val editTextPerfilUsuario: TextView = root.findViewById(R.id.etPerfilUsuario)
        val editTextPerfilContraseña: TextView = root.findViewById(R.id.etPerfilContrasena)
        val editTextPerfilTwitter: TextView = root.findViewById(R.id.etPerfilTwitter)
        val editTextPerfilGithub: TextView = root.findViewById(R.id.etPerfilGitHub)
        val botonPerfilGuardar: TextView = root.findViewById(R.id.btnPerfilGuardar)

        //Recogemos los datos para ponerlos en la opcion del navigation
        var textMainCorreo : TextView = activity!!.findViewById(R.id.tvMainCorreo)
        var imagenMainFoto : ImageView = activity!!.findViewById(R.id.imaMainFoto)
        var textMainNombre : TextView = activity!!.findViewById(R.id.tvMainNombre)

        correo = textMainCorreo.text.toString()

        //Consulta en el usuario para buscar el correo con el que nos hemos logueado
        usuarioActual = UsuarioController.consultaPorID(correo)

        var dialog = Dialog(activity!!)

        //Rellenamos los campos del Perfil con los datos de nuestro usuario actual
        editTextPerfilCorreo.text = correo
        editTextPerfilNombre.text = usuarioActual?.nombre.toString()
        editTextPerfilUsuario.text = usuarioActual?.usuario.toString()
        editTextPerfilContraseña.text = usuarioActual?.contraseña.toString()
        editTextPerfilTwitter.text = usuarioActual?.twitter.toString()
        editTextPerfilGithub.text = usuarioActual?.github.toString()
        //Para rescatar la imagen
        var imagen: Bitmap? = Utilidades.base64ToBitmap(usuarioActual?.fotoUsuario)
        imagenPerfilFoto.setImageBitmap(imagen)

        //Boton Guardar Cambios en el perfil
        botonPerfilGuardar.setOnClickListener(){
            //Volvmemos a coger la foto por si ha habido cambios
            var bm: ByteArray? = Utilidades.toBiteArray(imaPerfilFoto.drawable.toBitmap())
            imagenMeter = Base64.encodeToString(bm, Base64.DEFAULT)
            //Cogemos de nuevo todos los campos, para comprobar los cambios
            val usuarioEditado = Usuario(editTextPerfilCorreo.text.toString(), editTextPerfilNombre.text.toString(),
                editTextPerfilUsuario.text.toString(), editTextPerfilContraseña.text.toString(),
                editTextPerfilTwitter.text.toString(), editTextPerfilGithub.text.toString(), imagenMeter)
            //Actualizamos la bbdd con el nuevo usuario que hemos editado
            UsuarioController.updateDato(usuarioEditado)

            //Cambiamos los campos que tenemos en el perfil del navigation drawer por si hemos cambiado
            imagenMainFoto.setImageBitmap(imaPerfilFoto.drawable.toBitmap())
            textMainCorreo.text = editTextPerfilCorreo.text.toString()
            textMainNombre.text = editTextPerfilNombre.text.toString()

            Toast.makeText(appContext, "¡Datos Actualizados!", Toast.LENGTH_SHORT).show()
        }

        //Botón que nos permite elegir una foto de la galeria o tomar una foto con la cámara
        imagenPerfilFoto.setOnClickListener { view ->
            //Se nos abre un dialog que nos da a elegir entre 2 opciones
            dialog.setContentView(R.layout.camara_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Se rescatan las imágenes del layout de la cámara (si no se rescatan no funciona)
            var imagenCamaraCamara: ImageView = dialog.findViewById(R.id.imaCamaraCamara)
            var imagenCamaraGaleria: ImageView = dialog.findViewById(R.id.imaCamaraGaleria)

            //Si elegimos la camara
            imagenCamaraCamara.setOnClickListener(){
                fotoCamara()
                dialog.dismiss()
            }
            //Si elegimos la galeria
            imagenCamaraGaleria.setOnClickListener(){
                fotoGaleria()
                dialog.dismiss()
            }
            dialog.show()
        }

        return root
    }

    //Abrimos la galeria de fotos
    private fun fotoGaleria() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALERIA)
    }

    //Abrimos la camara de fotos
    private fun fotoCamara() {
        //Para usar fotos de alta calidad
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        //Llamamos al intent de la camara
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGEN_NOMBRE = Utilidades.crearNombreFichero()
        // Salvamos el fichero
        val fichero = Utilidades.salvarImagen(IMAGEN_DIR, IMAGEN_NOMBRE, appContext)!!
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
                        bitmap = MediaStore.Images.Media.getBitmap(appResolver, contentURI);
                    } else {
                        val source: ImageDecoder.Source = ImageDecoder.createSource(appResolver, contentURI)
                        bitmap = ImageDecoder.decodeBitmap(source)
                    }

                    // Cambiamos a la imagen que hemos elegido
                    imaPerfilFoto.setImageBitmap(bitmap)
                    Utilidades.copiarImagen(bitmap, IMAGEN_DIR, IMAGEN_COMPRES, appContext)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            //Si hemos conseguido entrar a la camara
        } else if (requestCode == CAMARA) {

            Log.i("FOTO", "Hemos abierto la camara")

            try {
                val foto: Bitmap
                //Comprobamos la version del SDK
                if (Build.VERSION.SDK_INT < 28) {
                    foto = MediaStore.Images.Media.getBitmap(appResolver, IMAGEN_URI)
                } else {
                    val source: ImageDecoder.Source = ImageDecoder.createSource(appResolver, IMAGEN_URI)
                    foto = ImageDecoder.decodeBitmap(source)
                }

                // Comprimimos foto
                IMAGEN_COMPRES = 10 * 10
                Utilidades.comprimirImagen(IMAGEN_URI.toFile(), foto, IMAGEN_COMPRES)

                //Cambiamos la foto a la nueva que hayamos tomado
                imaPerfilFoto.setImageBitmap(foto)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}