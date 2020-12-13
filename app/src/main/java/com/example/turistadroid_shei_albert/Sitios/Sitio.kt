package com.example.turistadroid_shei_albert.Sitios

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

//Clase sitio
open class Sitio (
    @PrimaryKey var idSitio: String = "",
    var usuarioLugar: String = "",
    var nombreLugar: String = "",
    var lugar: String = "",
    var fecha: String = "",
    var estrellas : Int = 0,
    var latitud: String = "",
    var longitud: String = "",
    var comentario: String = "",
    var foto: String = ""

): RealmObject() {
    constructor(
        usuarioLugar: String,
        nombreLugar: String,
        lugar: String,
        fecha: String,
        estrellas: Int,
        latitud: String,
        longitud: String,
        comentario: String,
        foto: String
    ) : this (
        (UUID.randomUUID().toString()),
        usuarioLugar,
        nombreLugar,
        lugar,
        fecha,
        estrellas,
        latitud,
        longitud,
        comentario,
        foto
    )

}