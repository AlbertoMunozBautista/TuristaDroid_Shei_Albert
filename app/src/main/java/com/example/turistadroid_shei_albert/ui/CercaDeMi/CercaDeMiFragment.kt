package com.example.turistadroid_shei_albert.ui.CercaDeMi

import android.content.Context
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Sitios.Sitio
import com.example.turistadroid_shei_albert.Sitios.SitiosController
import com.example.turistadroid_shei_albert.Usuarios.UsuarioController
import com.example.turistadroid_shei_albert.Utilidades
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar

class CercaDeMiFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener  {

    //Variables del mapa
    private lateinit var mMap: GoogleMap
    private var mPosicion: FusedLocationProviderClient? = null
    private var localizacion: Location? = null
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = false

    private lateinit var appContext : Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cerca_de_mi, container, false)

        //Rescatamos el contexto
        appContext = context!!.applicationContext
        UsuarioController.initRealm(appContext)

        //Iniciamos los permisos, rescatamos la posición e inicializiamos el mapa
        initPermisos()
        leerPoscionGPSActual()
        initMapa()

        return root
    }

    /**
     * Cogemos nuestra posición actual
     */
    private fun leerPoscionGPSActual() {
        mPosicion = LocationServices.getFusedLocationProviderClient(activity!!)
    }

    /**
     * Inicializamos el mapa
     */
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleCercaMapa) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    /**
     * Cargamos el mapa
     */
    private fun cargarMapa() {
        Log.i("Mapa", "Configurando Modo Insertar")
        if (this.PERMISOS) {
            mMap.isMyLocationEnabled = true
        }
        cargarMarcadores()
        obtenerPosicion()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        configurarIUMapa()
        cargarMapa()
    }

    /**
     * Se rescata la posicion del los sitios, se guarda en una lista y se cargan en el mapa
     */
    private fun cargarMarcadores() {

        //listaSitios contiene todos los sitios de la base de datos
        var listaSitios: MutableList<Sitio>
        listaSitios = SitiosController.selectDatos()!!

        //se recorre la lista y se van poniendo los sitios en el mapa
        for(u in 0..listaSitios.size - 1) {
            var foto : Bitmap? = Utilidades.base64ToBitmap(listaSitios[u].foto)
            val estacion = LatLng(listaSitios[u].latitud.toDouble(), listaSitios[u].longitud.toDouble())
            mMap.addMarker(
                MarkerOptions()
                    .position(estacion)  // Posición
                    .title(listaSitios[u].nombreLugar)// Título
                    .snippet(listaSitios[u].estrellas.toString() + " Estrellas") // Subtitulo
                    .icon(BitmapDescriptorFactory.fromBitmap(crearPin(foto!!)))
            )

        }
    }

    /**
     * Configuración del mapa
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
        mMap.setOnMarkerClickListener(this)
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
     * Iniciamos permisos
     */
    private fun initPermisos() {
        this.PERMISOS = true
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    //Creamos un pin con la imagen del sitio que estamos visualizando
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


}