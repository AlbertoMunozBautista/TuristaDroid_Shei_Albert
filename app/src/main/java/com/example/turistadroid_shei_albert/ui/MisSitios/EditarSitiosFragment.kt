package com.example.turistadroid_shei_albert.ui.MisSitios

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Sitios.Sitio
import com.example.turistadroid_shei_albert.Sitios.SitiosController
import com.example.turistadroid_shei_albert.Utilidades
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_editar_sitios.*
import java.io.IOException
import java.time.LocalDateTime

class EditarSitiosFragment(
    private val fab: FloatingActionButton,
    private var sitio : Sitio?  = null
) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {

    //Variables Cámara
    private val GALERIA = 1
    private val CAMARA = 2
    private lateinit var appContext : Context
    private lateinit var appResolver: ContentResolver
    private val IMAGEN_DIR = "/TuristaDroid"
    private lateinit var IMAGEN_URI: Uri
    private var IMAGEN_NOMBRE = ""
    private var IMAGEN_COMPRES = 30

    private var imagenMeter: String = ""
    private var imagenSitio: String = ""

    //Variables Mapa
    private lateinit var mMap: GoogleMap
    private var marcadorTouch: Marker? = null
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = false

    var correo: String = ""
    var fecha : String = ""
    var foto : String = ""
    var nombre : String = ""
    var lugar : String = ""
    var comentario : String = ""
    var estrellas : Int = 0
    var latitud : String =""
    var longitud : String =""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root =  inflater.inflate(R.layout.fragment_editar_sitios, container, false)

        //Creamos variables con los campos del layout
        var botonEditarGuardar : Button = root.findViewById(R.id.btnEditarAceptar)
        var botonEditarCancelar : Button = root.findViewById(R.id.btnEditarCancelar)
        var editTextEditarLugar : EditText = root.findViewById(R.id.etEditarLugar)
        var editTextEditarNombre : EditText = root.findViewById(R.id.etEditarNombre)
        var editTextEditarComentario : EditText = root.findViewById(R.id.etEditarComentario)
        var imagenEditarFoto : ImageView = root.findViewById(R.id.imaEditarFoto)
        var imagenEditarFecha : ImageView = root.findViewById(R.id.imaEditarFecha)
        var spinnerEditarEstrella : Spinner = root.findViewById(R.id.spiEditarEstrella)
        var textViewEditarFecha : TextView = root.findViewById(R.id.tvEditarFecha)
        var textMainCorreo : TextView = activity!!.findViewById(R.id.tvMainCorreo)

        //Rescatamos el contexto y el contentResolver
        appContext = context!!.applicationContext
        appResolver = activity!!.contentResolver

        var dialog = Dialog(activity!!)

        //Iniciamos permisos y mapa
        initPermisos()
        initMapa()

        //Botón cancelar
        botonEditarCancelar.setOnClickListener(){
            volver()
        }

        //Cuando pulsamos la imagen del calendario abrimos un datepickerdialog para cambiar la fecha
        imagenEditarFecha.setOnClickListener(){
            val date = LocalDateTime.now()
            val datePickerDialog = DatePickerDialog(
                context!!,
                { _, mYear, mMonth, mDay ->
                    textViewEditarFecha.text = (mDay.toString() + "/" + (mMonth + 1) + "/" + mYear)
                }, date.year, date.monthValue - 1, date.dayOfMonth
            )
            datePickerDialog.show()
        }

        //Botón que nos permite elegir una foto de la galeria o tomar una foto con la cámara
        imagenEditarFoto.setOnClickListener(){

            //Se nos abre un dialog que nos da a elegir entre 2 opciones
            dialog.setContentView(R.layout.camara_layout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            //Se rescatan las imágenes del layout de la cámara (si no se rescatan no funciona)
            var imagenCamaraCamara: ImageView = dialog.findViewById(R.id.imaCamaraCamara)
            var imagenCamaraGaleria: ImageView = dialog.findViewById(R.id.imaCamaraGaleria)

            //si elegimos la cámara
            imagenCamaraCamara.setOnClickListener(){
                fotoCamara()
                dialog.dismiss()
            }

            //si elegimos la galería
            imagenCamaraGaleria.setOnClickListener(){
                fotoGaleria()
                dialog.dismiss()
            }

            dialog.show()
        }

        //Botón guardar, recogemos todos los campos introducidos y llamamos al
        //método para comprobarCampos
        botonEditarGuardar.setOnClickListener(){
            correo = textMainCorreo.text.toString()
            fecha = textViewEditarFecha.text.toString()
            foto = imagenEditarFoto.toString()
            nombre = editTextEditarNombre.text.toString()
            lugar = editTextEditarLugar.text.toString()
            comentario = editTextEditarComentario.text.toString()
            estrellas = spinnerEditarEstrella.selectedItem.toString().toInt()
            Log.i("Estrellas", "Has cogido : "+estrellas)
            latitud = posicion?.latitude.toString()
            longitud = posicion?.longitude.toString()

            comprobarCampos(correo, fecha, foto, nombre, lugar, comentario, estrellas, latitud, longitud)
        }

        //Cogemos los datos de ese sitio y los ponemos en los campos del layout editar
        textViewEditarFecha.text = sitio?.fecha.toString()
        editTextEditarNombre.setText(sitio?.nombreLugar.toString())
        editTextEditarLugar.setText(sitio?.lugar.toString())
        editTextEditarComentario.setText(sitio?.comentario.toString())

        when(sitio?.estrellas){
            1 -> spinnerEditarEstrella.setSelection(0)
            2 -> spinnerEditarEstrella.setSelection(1)
            3 -> spinnerEditarEstrella.setSelection(2)
            4 -> spinnerEditarEstrella.setSelection(3)
            5 -> spinnerEditarEstrella.setSelection(4)
        }

        var imagen: Bitmap? = Utilidades.base64ToBitmap(sitio?.foto)
        imagenEditarFoto.setImageBitmap(imagen)

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
        if (requestCode == GALERIA) {
            Log.d("FOTO", "Hemos abierto la galeria")
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



                    //La imagen obtenida en bitmap la pasamos a 1ºbyteArray y 2ºbase64 para guardarla en la bbdd
                    var bm: ByteArray? = Utilidades.toBiteArray(bitmap)
                    imagenSitio = Base64.encodeToString(bm, Base64.DEFAULT)

                    // Cambiamos a la imagen que hemos elegido
                    imaEditarFoto.setImageBitmap(bitmap)
                    Utilidades.copiarImagen(bitmap, IMAGEN_DIR, IMAGEN_COMPRES, appContext)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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


                //La imagen obtenida en bitmap la pasamos a 1ºbyteArray y 2ºbase64 para guardarla en la bbdd
                var bm: ByteArray? = Utilidades.toBiteArray(foto)
                imagenSitio = Base64.encodeToString(bm, Base64.DEFAULT)

                // Cambiamos a la imagen que hemos elegido
                imaEditarFoto.setImageBitmap(foto)


            } catch (e: Exception) {
                e.printStackTrace()
            }



        }
    }

    /**
     * Método que llamamos cuando queremos cerrar el fragment
     */
    private fun volver() {
        fab.show()
        activity?.onBackPressed();
    }

    /**
     * Comprobamos los campos para que no esten vacíos e insertamos el sitio editado en la base de datos
     */
    private fun comprobarCampos(correo : String, fecha : String, foto: String, nombre : String, lugar :String, comentario:String, estrellas: Int, latitud: String, longitud: String) {

        if(fecha.equals("") || foto.equals("") || nombre.equals("") || lugar.equals("")
            || comentario.equals("") || latitud.equals("") || longitud.equals("")){
            Toast.makeText(context, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
        }else{

            var bm: ByteArray? = Utilidades.toBiteArray(imaEditarFoto.drawable.toBitmap())
            imagenMeter = Base64.encodeToString(bm, Base64.DEFAULT)

            val sitioEditar = Sitio(sitio!!.idSitio, correo, nombre, lugar,  fecha, estrellas,
                posicion?.latitude.toString(), posicion?.longitude.toString(), comentario, imagenMeter)

            SitiosController.updateDato(sitioEditar)
            volver()
        }
    }

    /**
     * Inicializamos el mapa
     */
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleLugarMapaEditar) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    //Cargamos el mapa
    private fun cargarMapa() {
        Log.i("Mapa", "Configurando Modo Insertar")
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        cargarMarcadores()
        activarEventosMarcadores()
        obtenerPosicion()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        cargarMapa()
    }

    //Se rescata la posicion del sitio nuevo y se carga en el mapa con un marcador
    private fun cargarMarcadores() {

        var foto : Bitmap? = Utilidades.base64ToBitmap(sitio!!.foto)
        val estacion = LatLng(sitio!!.latitud.toDouble(), sitio!!.longitud.toDouble())
        mMap.addMarker(
            MarkerOptions() // Posición
                .position(estacion) // Título
                .title(sitio!!.nombreLugar)
                .snippet(sitio!!.estrellas.toString() + " Estrellas") // Subtitulo
                .icon(BitmapDescriptorFactory.fromBitmap(crearPin(foto!!)))
        )

    }

    /**
     * Configuracion del mapa
     */
    private fun configurarIUMapa() {
        Log.i("Mapa", "Configurando IU Mapa")
        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        val uiSettings: UiSettings = mMap.uiSettings
        // Activamos los gestos
        uiSettings.isScrollGesturesEnabled = true
        uiSettings.isTiltGesturesEnabled = true
        // Activamos la brújula
        uiSettings.isCompassEnabled = true
        // Activamos los controles de zoom
        uiSettings.isZoomControlsEnabled = true
        // Activamos la barra de herramientas
        uiSettings.isMapToolbarEnabled = true
        // Hacemos el zoom por defecto mínimo
        mMap.setMinZoomPreference(12.0f)

    }

    /**
     * Obtener la posicion del sitio guardado en la base de datos
     */
    private fun obtenerPosicion() {
        posicion = LatLng(sitio!!.latitud.toDouble(), sitio!!.longitud.toDouble())

        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));

    }

    /**
     * Cuando pulsemos en mapa se nos crea un marcador
     */
    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si está puesto
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions()
                    .position(point) // Posición
                    .title("Posición Actual") // Título
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            posicion = point
        }
    }

    /**
     * Iniciamos permisos
     */
    private fun initPermisos() {
        this.PERMISOS = true
    }

    /**
     * Creamos pin con la foto de la base de datos, este método redondea la foto y la inserta en una imagen
     */
    private fun crearPin(imagenID: Bitmap): Bitmap? {
        val fotografia = imagenID
        var result: Bitmap? = null
        try {
            result = Bitmap.createBitmap(dp(62f), dp(76f), Bitmap.Config.ARGB_8888)
            result.eraseColor(Color.TRANSPARENT)
            val canvas = Canvas(result)
            val drawable = ContextCompat.getDrawable(context!!, R.drawable.map_pin)
            drawable?.setBounds(0, 0, dp(62f), dp(76f))
            drawable?.draw(canvas)
            val roundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val bitmapRect = RectF()
            canvas.save()
            val bitmap = imagenID
            //Bitmap bitmap = BitmapFactory.decodeFile(path.toString()); /*generate bitmap here if your image comes from any url*/
            if (bitmap != null) {
                val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                val matrix = Matrix()
                val scale = dp(52f) / bitmap.width.toFloat()
                matrix.postTranslate(dp(5f).toFloat(), dp(5f).toFloat())
                matrix.postScale(scale, scale)
                roundPaint.shader = shader
                shader.setLocalMatrix(matrix)
                bitmapRect[dp(5f).toFloat(), dp(5f).toFloat(), dp(52f + 5).toFloat()] = dp(52f + 5).toFloat()
                canvas.drawRoundRect(bitmapRect, dp(26f).toFloat(), dp(26f).toFloat(), roundPaint)
            }
            canvas.restore()
            try {
                canvas.setBitmap(null)
            } catch (e: Exception) {
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        return result
    }


    // Densidad de pantalla
    fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else
            Math.ceil((resources.displayMetrics.density * value).toDouble()).toInt()
    }


    override fun onMarkerClick(p0: Marker?): Boolean {
        TODO("Not yet implemented")
    }


}