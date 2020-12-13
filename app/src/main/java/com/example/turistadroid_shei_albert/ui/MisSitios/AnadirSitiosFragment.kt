package com.example.turistadroid_shei_albert.ui.MisSitios

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.location.Location
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
import androidx.core.net.toFile
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Sitios.Sitio
import com.example.turistadroid_shei_albert.Sitios.SitiosController
import com.example.turistadroid_shei_albert.Utilidades
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_anadir_sitios.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AnadirSitiosFragment(

    private val fab: FloatingActionButton

) : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //Variables Mapa
    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var marcadorTouch: Marker? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = false


    //Variables cámara
    private val GALERIA = 1
    private val CAMARA = 2
    private val IMAGEN_DIR = "/TuristaDroid"
    private lateinit var IMAGEN_URI: Uri
    private lateinit var IMAGEN_MEDIA_URI: Uri
    private val PROPORCION = 600
    private var IMAGEN_NOMBRE = ""
    private var IMAGEN_COMPRES = 30

    private lateinit var appContext : Context
    private lateinit var appResolver: ContentResolver

    private var imagenSitio: String = ""

    var correo: String = ""
    var fecha : String = ""
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

        val root = inflater.inflate(R.layout.fragment_anadir_sitios, container, false)

        //Creamos variables con los campos del layout
        val botonAnadirAnadir: Button = root.findViewById(R.id.btnAnadirAnadir)
        val botonAnadirCancelar: Button = root.findViewById(R.id.btnAnadirCancelar)
        val textViewAnadirFecha: TextView = root.findViewById(R.id.tvAnadirFecha)
        val imageAnadirFecha : ImageView = root.findViewById(R.id.btAnadirFecha)
        val imagenAnadirFoto: ImageView = root.findViewById(R.id.imaAnadirFoto)
        val editTextAnadirNombre: EditText = root.findViewById(R.id.etAnadirNombre)
        val editTextAnadirLugar: EditText = root.findViewById(R.id.etAnadirLugar)
        val editTextAnadirComentario: EditText = root.findViewById(R.id.etAnadirComentario)
        val spinnerAnadirEstrella: Spinner = root.findViewById(R.id.spiAnadirEstrella)
        var textMainCorreo : TextView = activity!!.findViewById(R.id.tvMainCorreo)

        //Ponemos una foto por defecto al image View
        imagenAnadirFoto.setImageResource(R.drawable.lugar4)

        //Rescatamos el contexto y el contentResolver
        appContext = context!!.applicationContext
        appResolver = activity!!.contentResolver

        var dialog = Dialog(activity!!)

        //Cogemos la fecha actual del sistema y la ponemos por defecto
        val date = LocalDateTime.now()
        textViewAnadirFecha.text = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date)

        //Cuando pulsamos la imagen del calendario abrimos un datepickerdialog para cambiar la fecha
        imageAnadirFecha.setOnClickListener(){
            val date = LocalDateTime.now()
            val datePickerDialog = DatePickerDialog(
                context!!,
                { _, mYear, mMonth, mDay ->
                    textViewAnadirFecha.text = (mDay.toString() + "/" + (mMonth + 1) + "/" + mYear)
                }, date.year, date.monthValue - 1, date.dayOfMonth
            )
            datePickerDialog.show()
        }

        //Botón que nos permite elegir una foto de la galeria o tomar una foto con la cámara
        imagenAnadirFoto.setOnClickListener(){

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

        //Botón cancelar
        botonAnadirCancelar.setOnClickListener(){
            volver()
        }

        //Botón añadir, recogemos todos los campos introducidos y llamamos al
        //método para comprobarCampos
        botonAnadirAnadir.setOnClickListener(){
            correo = textMainCorreo.text.toString()
            fecha = textViewAnadirFecha.text.toString()
            nombre = editTextAnadirNombre.text.toString()
            lugar = editTextAnadirLugar.text.toString()
            comentario = editTextAnadirComentario.text.toString()
            estrellas = spinnerAnadirEstrella.selectedItem.toString().toInt()
            Log.i("Estrellas", "Has cogido : "+estrellas)
            latitud = posicion?.latitude.toString()
            longitud = posicion?.longitude.toString()

            comprobarCampos(correo, fecha, nombre, lugar, comentario, estrellas, latitud, longitud)
        }

        //Iniciamos los permisos, obtenemos la posición actual e inicializamos el mapa
        initPermisos()
        leerPoscionGPSActual()
        initMapa()

        return root
    }

    /**
     * Comprobamos los campos para que no esten vacíos e insertamos el nuevo sitio en la base de datos
     */
    private fun comprobarCampos(correo : String, fecha : String, nombre : String, lugar :String, comentario:String, estrellas: Int, latitud: String, longitud: String) {

        if(fecha.equals("") || imagenSitio.equals("") || nombre.equals("") || lugar.equals("")
            || comentario.equals("") || estrellas == 0 || latitud.equals("") || longitud.equals("")){
            Toast.makeText(context, "Rellene todos los campos", Toast.LENGTH_SHORT).show()
        }else{
            val sitioNuevo = Sitio(correo, nombre,
                lugar,  fecha,
                estrellas,  posicion?.latitude.toString(), posicion?.longitude.toString(), comentario, imagenSitio)

            SitiosController.insertDato(sitioNuevo)
            volver()
        }
    }


    /**
     * Iniciamos permisos
     */
    private fun initPermisos() {
        this.PERMISOS = true
    }


    /**
     * Leemos la posición actual del GPS
     */
    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(activity!!)
    }

    //Inicialiazamos el mapa
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleLugarMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        cargarMapa()
    }

    /**
     * Configuración del mapa
     */
    private fun configurarIUMapa() {
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
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
        mMap.setOnMarkerClickListener(this)
    }

    /**
     * Cuando pulsemos en mapa se nos crea un marcador
     */
    private fun activarEventosMarcadores() {
        mMap.setOnMapClickListener { point -> // Creamos el marcador
            // Borramos el marcador Touch si está puesto
            marcadorTouch?.remove()
            marcadorTouch = mMap.addMarker(
                MarkerOptions() // Posición
                    .position(point) // Título
                    .title("Posición Actual") // título
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(point))
            posicion = point
        }
    }

    /**
     * Cargamos el mapa
     */
    private fun cargarMapa() {
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        activarEventosMarcadores()
        obtenerPosicion()
    }



    /**
     * Obtiene la posición actual para pasarsela al mapa y que se cargue en nuestra posición
     */
    private fun obtenerPosicion() {
        try {
            //Si tenemos permiso cogemos la localización
            if (this.PERMISOS) {
                val local: Task<Location> = mPosicion!!.lastLocation
                local.addOnCompleteListener(
                    activity!!
                ) { task ->
                    if (task.isSuccessful) {
                        localizacion = task.result
                        posicion = LatLng(
                            localizacion!!.latitude,
                            localizacion!!.longitude
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                    } else {
                        Log.i("GPS", "No se encuetra la última posición.")
                    }
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                view!!,
                "No se ha encontrado su posoción actual o el GPS está desactivado",
                Snackbar.LENGTH_LONG
            ).show();
            Log.e("Exception: %s", e.message.toString())
        }
    }

    /**
     * Método que llamamos cuando queremos cerrar el fragment
     */
    private fun volver() {
        fab.show()
        activity?.onBackPressed();
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
                    imaAnadirFoto.setImageBitmap(bitmap)
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

                var bm: ByteArray? = Utilidades.toBiteArray(foto)
                imagenSitio = Base64.encodeToString(bm, Base64.DEFAULT)

                imaAnadirFoto.setImageBitmap(foto)

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        return false
    }

}