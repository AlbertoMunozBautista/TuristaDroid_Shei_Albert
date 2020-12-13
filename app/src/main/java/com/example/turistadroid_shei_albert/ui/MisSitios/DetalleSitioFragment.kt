package com.example.turistadroid_shei_albert.ui.MisSitios

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Sitios.Sitio
import com.example.turistadroid_shei_albert.Utilidades
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DetalleSitioFragment(

    private val fab: FloatingActionButton,
    private var sitio : Sitio?  = null

) : Fragment(), OnMapReadyCallback {


    // Variables mapa
    private lateinit var mMap: GoogleMap
    private var posicion: LatLng? = null
    private var PERMISOS: Boolean = false
    private lateinit var appContext : Context


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root =  inflater.inflate(R.layout.fragment_detalle_sitio, container, false)
        //Rescatamos el contexto
        appContext = context!!.applicationContext

        //Creamos variables con los campos del layout
        val botonDetalleCerrar: Button = root.findViewById(R.id.btnDetalleCerrar)
        val imagenDetalleFoto: ImageView = root.findViewById(R.id.imaDetalleFoto)
        val textViewDetalleFecha: TextView = root.findViewById(R.id.tvDetalleFecha)
        val textViewDetalleNombre: TextView = root.findViewById(R.id.tvDetalleNombre)
        val textViewDetalleLugar: TextView = root.findViewById(R.id.tvDetalleLugar)
        val textViewDetalleComentario: TextView = root.findViewById(R.id.tvDetalleComentario)
        val textViewDetalleEstrella: TextView = root.findViewById(R.id.tvDetalleEstrella)

        //Iniciamos permisos y mapa
        initPermisos()
        initMapa()

        //Volvemos atras si pinchamos en el boton cerrar
        botonDetalleCerrar.setOnClickListener(){
            volver()
        }

        //Cogemos los datos de ese sitio y los ponemos en los campos del layout detalle
        textViewDetalleFecha.text = sitio?.fecha.toString()
        textViewDetalleNombre.text = sitio?.nombreLugar.toString()
        textViewDetalleLugar.text = sitio?.lugar.toString()
        textViewDetalleComentario.text = sitio?.comentario.toString()
        textViewDetalleEstrella.text = sitio?.estrellas.toString()
        var imagen: Bitmap? = Utilidades.base64ToBitmap(sitio?.foto)
        imagenDetalleFoto.setImageBitmap(imagen)

        return root
    }
    //Metodo que hace que se cierre el fragment
    private fun volver() {
        fab.show()
        activity?.onBackPressed();
    }

    //Inicializamos el mapa
    private fun initMapa() {
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.detalleLugarMapaDetalle) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    //Cargamos el mapa
    private fun cargarMapa() {
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


    //Se rescata la posicion del sitio y se carga en el mapa con un marcador
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

    //Configuracion del mapa
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

    //Obtener la posicion del sitio
    private fun obtenerPosicion() {
        posicion = LatLng(sitio!!.latitud.toDouble(), sitio!!.longitud.toDouble())

        mMap.moveCamera(CameraUpdateFactory.newLatLng(posicion));

    }


    private fun initPermisos() {
        this.PERMISOS = true
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