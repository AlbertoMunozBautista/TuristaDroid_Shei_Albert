package com.example.turistadroid_shei_albert.Usuarios

import android.content.Context
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where

object UsuarioController {

    // Variables de la base de datos
    private const val DATOS_BD = "TURISTA_DROID_AS"
    private const val DATOS_BD_VERSION = 1L

    /**
     * función para iniciar realm
     */
    fun initRealm(context: Context?) {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .name(DATOS_BD)
            //Versión del esquema
            .schemaVersion(DATOS_BD_VERSION)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)

    }

    /**
     * Inserta un usuario en la base de datos
     */
    fun insertDato(usuario: Usuario) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealm(usuario);
        }
    }

    /**
     * Actualiza un usuario en la base de datos
     */
    fun updateDato(usuario: Usuario) {
        Realm.getDefaultInstance().executeTransaction {
            it.copyToRealmOrUpdate(usuario)
        }
    }

    /**
     * Devuelve el objeto usuario que contiene esa id
     */
    fun consultaPorID(correo: String): Usuario? {
        return Realm.getDefaultInstance().where<Usuario>().equalTo("correo", correo).findFirst()
    }
}