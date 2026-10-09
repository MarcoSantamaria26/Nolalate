package com.example.nolalate

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlbumDao {

    // --- COMANDOS PARA LA TABLA DE ÁLBUMES ---
    @Query("SELECT * FROM table_albumes ORDER BY id DESC")
    suspend fun obtenerTodosLosAlbumes(): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlbum(album: AlbumEntity): Long

    @Query("DELETE FROM table_albumes WHERE id = :albumId")
    suspend fun borrarAlbumPorId(albumId: Int)

    // --- COMANDOS PARA LA TABLA DE FIGURITAS ---
    @Query("SELECT * FROM table_figuritas WHERE albumId = :albumId ORDER BY numeroGlobal ASC")
    suspend fun obtenerFiguritasDelAlbum(albumId: Int): List<FiguritaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFiguritas(figuritas: List<FiguritaEntity>)

    @Query("UPDATE table_figuritas SET cantidad = :nuevaCantidad WHERE albumId = :albumId AND numeroGlobal = :numGlobal")
    suspend fun actualizarCantidadFigurita(albumId: Int, numGlobal: Int, nuevaCantidad: Int)
}
