package com.example.turistadroid_shei_albert.ui.MisSitios

import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Sitios.Sitio
import com.example.turistadroid_shei_albert.Sitios.SitiosController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_mis_sitios.*

class MisSitiosFragment : Fragment() {

    //Variables

    private var SITIOS = mutableListOf<Sitio>() //Lista de sitios
    private lateinit var sitiosAdapter: SitiosListAdapter //Adaptador de sitios
    private lateinit var tareaSitios: TareaCargarSitios // Tarea hilo para cargar sitios
    private var paintSweep = Paint()
    private lateinit var appContext : Context
    private lateinit var fabButton : FloatingActionButton
    private var correo : String = ""
    private var FILTRO = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_mis_sitios, container, false)
        fabButton= root.findViewById(R.id.FabSitiosNuevo)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Iniciamos los swipe, el spinner y cargamos los sitios
        iniciarSwipeRecarga()
        iniciarSwipeHorizontal()
        cargarSitios()
        iniciarSpinner()

        appContext = context!!.applicationContext
        sitiosRecycler.layoutManager = LinearLayoutManager(context)

        //Cuando pulsamos el botón flotante de crear sitios
        FabSitiosNuevo.setOnClickListener {

            //Si nos oculta el botón flotante
            fabButton.hide()

            //Cargamos el fragment de añadir sitios
            val nuevoFragment = AnadirSitiosFragment(fabButton)
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.replace(R.id.SitiosRelative, nuevoFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }


    }

    /**
     * Obtenemos el correo de la persona que ha iniciado sesión
     */
    private fun iniciarCorreo(){
        var textMainCorreo : TextView = activity!!.findViewById(R.id.tvMainCorreo)
        correo = textMainCorreo.text.toString()
    }

    /**
     * Inicializamos el spinner del filtro con el array de búsqueda (fecha, lugar, puntuación)
     */
    private fun iniciarSpinner() {
        val tipoBusqueda = resources.getStringArray(R.array.tipos_busqueda)
        val adapter = ArrayAdapter(context!!,
            android.R.layout.simple_spinner_item, tipoBusqueda)
        sitiosSpinnerFiltro.adapter = adapter
        sitiosSpinnerFiltro.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                FILTRO = sitiosSpinnerFiltro.selectedItemPosition
                Toast.makeText(context!!, "Ordenando por: " + tipoBusqueda[position], Toast.LENGTH_SHORT).show()
                visualizarListaItems()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    /**
     * Swipe recarga de sitios
     */
    private fun iniciarSwipeRecarga() {
        sitiosSwipeRefresh.setColorSchemeResources(R.color.colorPrimaryDark)
        sitiosSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.colorAccent)
        sitiosSwipeRefresh.setOnRefreshListener {

            //cargamos los sitios al refrescar
            cargarSitios()
        }
    }

    /**
     * Swipe horizontal que nos servirá para eliminar o editar
     */
    private fun iniciarSwipeHorizontal() {
        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or
                    ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            //Según donde deslizemos
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                //izquierda -> borramos
                //derecha -> editamos
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        borrarElemento(position)
                    }
                    else -> {
                        editarElemento(position)
                    }
                }
            }

            /**
             * Cuando hemos deslizado a la derecha, nos lleva a editar elemento
             * donde abrimos el fragment editar elemento
             */
            private fun editarElemento(position: Int) {

                //Ocultamos el floating button
                fabButton.hide()

                //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
                //instantaneamente para que desaparezca el color del fondo
                val editedModel: Sitio = SITIOS[position]
                sitiosAdapter.removeItem(position)
                sitiosAdapter.restoreItem(editedModel, position)

                //llamamos al fragment editar
                val editarFragment = EditarSitiosFragment(fabButton, SITIOS[position])
                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction.replace(R.id.SitiosRelative, editarFragment)
                transaction.addToBackStack(null)
                transaction.commit()


            }


            /**
             * Se crea el dibujo cuando deslizamos
             */
            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3
                    // Si es dirección a la derecha: izquierda->derecta
                    // Pintamos de azul y ponemos el icono
                    if (dX > 0) {
                        // Pintamos el botón izquierdo
                        botonIzquierdo(canvas, dX, itemView, width)
                    } else {
                        // Caso contrario
                        botonDerecho(canvas, dX, itemView, width)
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        // Añadimos los eventos al RV
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(sitiosRecycler)
    }

    /**
     * Se pinta cuando deslizamos a la derecha
     */
    private fun botonDerecho(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de rojo y ponemos el icono
        paintSweep.color = Color.RED
        val background = RectF(
            itemView.right.toFloat() + dX,
            itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_seep_eliminar)
        val iconDest = RectF(
            itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right
                .toFloat() - width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * función que dependiendo el valor del spinner ordena la lista por una cosa u otra
     */
    private fun ordenarLugares() {
        when(FILTRO){
            0 -> this.SITIOS.sortBy { it.fecha }
            1 -> this.SITIOS.sortBy { it.nombreLugar }
            2 -> this.SITIOS.sortBy { it.estrellas }

        }
    }

    /**
     * Se pinta cuando deslizamos a la izquierda
     */
    private fun botonIzquierdo(canvas: Canvas, dX: Float, itemView: View, width: Float) {
        // Pintamos de azul y ponemos el icono
        paintSweep.setColor(Color.BLUE)
        val background = RectF(
            itemView.left.toFloat(), itemView.top.toFloat(), dX,
            itemView.bottom.toFloat()
        )
        canvas.drawRect(background, paintSweep)
        val icon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_sweep_editar)
        val iconDest = RectF(
            itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left
                .toFloat() + 2 * width, itemView.bottom.toFloat() - width
        )
        canvas.drawBitmap(icon, null, iconDest, paintSweep)
    }

    /**
     * LLamamos al hilo para cargar los sitios
     */
    private fun cargarSitios() {
        tareaSitios = TareaCargarSitios()
        tareaSitios.execute()
    }

    /**
     * Cuando hemos deslizado a la izquierda, nos lleva a borrar elemento
     * donde abrimos el fragment borrar elemento
     */
    private fun borrarElemento(position: Int) {
        //Cuando hemos deslizado, quitamos el elemento del swipe y lo ponemos
        //instantaneamente para que desaparezca el color del fondo
        val deleteModel: Sitio = SITIOS[position]
        sitiosAdapter.removeItem(position)
        sitiosAdapter.restoreItem(deleteModel, position)


        //Alert dialog para confirmar si desea eliminar el sitio deslizado
        Log.i("Elimar", "Eliminando...")
        AlertDialog.Builder(this.context)
            .setIcon(R.drawable.logo1)
            .setTitle("Eliminar lugar")
            .setMessage("¿Desea eliminar el sitio seleccionado?")
            .setPositiveButton(getString(R.string.si)){ dialog, which -> eliminarSitioConfirmado(position)}
            .setNegativeButton(getString(R.string.no),null)
            .show()

    }

    /**
     * Si en el alert dialog hemos confirmado que sí queremos eliminar un sito
     * lo borramos de la base de datos
     */
    private fun eliminarSitioConfirmado(position:Int) {
        SitiosController.delete(SITIOS[position])
        val snackbar = Snackbar.make(view!!, "Sitio eliminado", Snackbar.LENGTH_LONG)
        sitiosAdapter.removeItem(position)
        snackbar.show()
    }

    /**
     * Se llama cuando hacemos clic en un item
     */
    private fun eventoClicFila(sitio: Sitio) {
        abrirSitio(sitio)
    }

    /**
     * Se llama cuando hemos pulsado un sitio, abrimos el fragment detalle
     */
    private fun abrirSitio(sitio : Sitio){
        //Se oculta el floating button
        fabButton.hide()
        Log.i("Click", "Hemos pulsado abrir sitio")

        //Se llama al detalle fragment
        val lugarDetalle = DetalleSitioFragment(fabButton, sitio)
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.SitiosRelative, lugarDetalle)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /**
     * Tarea asíncrona para la carga de sitios
     */
    inner class TareaCargarSitios : AsyncTask<Void?, Void?, Void?>() {
        // antes de ejecutar
        override fun onPreExecute() {

            //comprobamos el swipe
            if (sitiosSwipeRefresh.isRefreshing) {
                sitiosSwipeRefresh.isRefreshing = false

            }
        }
        // Tarea
        override fun doInBackground(vararg args: Void?): Void? {
            try {
                //Obtenemos el correo de la persona que ha iniciado sesión
                iniciarCorreo()
                //a través de una consulta cargamos la lista de sitios de ese usuario
                SITIOS = SitiosController.listaUsuario(correo)!!
            } catch (e: Exception) {
            }
            return null
        }
        //después de ejecutar
        override fun onPostExecute(args: Void?) {
            //llamamos al filtro
            ordenarLugares()

            //detecta cuando pulsamos en un item
            sitiosAdapter = SitiosListAdapter(SITIOS) {
                eventoClicFila(it)
            }

            sitiosRecycler.adapter = sitiosAdapter
            // Avismos que ha cambiado
            sitiosAdapter.notifyDataSetChanged()
            sitiosRecycler.setHasFixedSize(true)
            sitiosSwipeRefresh.isRefreshing = false
            Toast.makeText(context, "sitios cargados", Toast.LENGTH_LONG).show()
        }

    }

    /**
     * Si paramos cancelamos la tarea
     */
    override fun onStop() {
        super.onStop()
        tareaSitios.cancel(true)
    }


    /**
     * Visualiza la lista de items
     */
    private fun visualizarListaItems() {
        ordenarLugares()
        sitiosRecycler.adapter = sitiosAdapter

    }

}