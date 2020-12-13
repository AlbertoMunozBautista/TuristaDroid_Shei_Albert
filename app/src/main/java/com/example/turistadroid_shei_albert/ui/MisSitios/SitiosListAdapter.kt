package com.example.turistadroid_shei_albert.ui.MisSitios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.turistadroid_shei_albert.R
import com.example.turistadroid_shei_albert.Sitios.Sitio
import com.example.turistadroid_shei_albert.Utilidades
import kotlinx.android.synthetic.main.item_lugar.view.*

class SitiosListAdapter (
    private val listaSitios: MutableList<Sitio>,
    private val accionPrincipal: (Sitio) -> Unit

) : RecyclerView.Adapter<SitiosListAdapter.LugarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LugarViewHolder {
        return LugarViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lugar, parent, false)
        )
    }

    //Rescatamos los datos d un sitio y los ponemos en sus componentes
    override fun onBindViewHolder(holder: LugarViewHolder, position: Int) {

        holder.textViewItemNombre.text = listaSitios[position].nombreLugar
        holder.textViewItemLugar.text = listaSitios[position].lugar
        holder.textViewItemFecha.text = listaSitios[position].fecha
        holder.textViewItemEstrella.text = listaSitios[position].estrellas.toString()
        holder.imagenItemFoto.setImageBitmap(Utilidades.base64ToBitmap(listaSitios[position].foto))
        holder.itemSitios.setOnClickListener(){
            accionPrincipal(listaSitios[position])
        }
    }


    //Eliminamos un item de la lista
    fun removeItem(position: Int) {
        listaSitios.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, listaSitios.size)
    }


    //Recuperamos un item de la lista
    fun restoreItem(item: Sitio, position: Int) {
        listaSitios.add(position, item)
        notifyItemInserted(position)
        notifyItemRangeChanged(position, listaSitios.size)
    }

    //Devolvemos el numero de elementos que tiene la lista
    override fun getItemCount(): Int {
        return listaSitios.size
    }

    //Rescatamos los et y tv del item
    class LugarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenItemFoto = itemView.imaItemFoto
        var textViewItemNombre = itemView.tvItemNombre
        var textViewItemLugar = itemView.tvItemLugar
        var textViewItemFecha = itemView.tvItemFecha
        var textViewItemEstrella = itemView.tvItemEstrella
        var itemSitios = itemView.itemLugar
        var context = itemView.context
    }

}