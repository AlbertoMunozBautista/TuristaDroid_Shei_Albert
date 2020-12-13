package com.example.turistadroid_shei_albert.Sitios

import io.realm.Realm
import io.realm.kotlin.where

object SitiosController {

    //Devuelve todos los sistios de la bbdd
    fun selectDatos(): MutableList<Sitio>? {
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Sitio>().findAll()
        )
    }

    //Insertamos un sitio en la bbdd
    fun insertDato(sitio: Sitio) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(sitio); // Copia, inserta
        }
    }

    //Actualizamos un sitio
    fun updateDato(sitio: Sitio) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealmOrUpdate(sitio)
        }
    }

    //Borra un sitio
    fun delete(sitio: Sitio) {
        Realm.getDefaultInstance().executeTransaction {
            it.where<Sitio>().equalTo("idSitio", sitio.idSitio).findFirst()?.deleteFromRealm()
        }
    }

    //Devuelve una lista de sitios de un usuario
    fun listaUsuario(sitio: String) : MutableList<Sitio>?{
        return Realm.getDefaultInstance().copyFromRealm(
            Realm.getDefaultInstance().where<Sitio>().equalTo("usuarioLugar", sitio).findAll())
    }

}