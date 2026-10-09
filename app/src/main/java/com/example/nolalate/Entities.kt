package com.example.nolalate

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// =========================================================================
// 1. TABLA DE ÁLBUMES (Guarda los propietarios permanentemente)
// =========================================================================
@Entity(tableName = "table_albumes")
data class AlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propietario: String
)

// =========================================================================
// 2. TABLA DE FIGURITAS (Guarda el estado de las 994 estampas de cada dueño)
// =========================================================================
@Entity(
    tableName = "table_figuritas",
    primaryKeys = ["albumId", "numeroGlobal"], // Clave compuesta para no repetir la misma figurita en el mismo álbum
    foreignKeys = [
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["id"],
            childColumns = ["albumId"],
            onDelete = ForeignKey.CASCADE // Si borras un álbum de un dueño, se borran automáticamente todas sus figuritas para no ocupar espacio
        )
    ]
)
data class FiguritaEntity(
    val albumId: Int,         // Conectado al ID del dueño del álbum
    val numero: Int,          // Su número dentro del grupo (ej: del 1 al 20)
    val numeroGlobal: Int,    // Su número absoluto (del 1 al 994)
    val grupo: String,        // El código de la sección (ej: ARG, MEX)
    val cantidad: Int = 0     // 0 = Faltante, 1 = Pegada, >1 = Repetidas
)
