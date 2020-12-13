package com.example.turistadroid_shei_albert.Usuarios

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.io.Serializable

//Clase usuarios
open class Usuario (@PrimaryKey var correo: String = "",
                    var nombre: String = "",
                    var usuario: String = "",
                    var contraseña: String = "",
                    var twitter: String = "",
                    var github: String = "",
                    var fotoUsuario: String = "") : RealmObject()  {

}