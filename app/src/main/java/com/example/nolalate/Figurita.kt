package com.example.nolalate

data class Figurita(
    val numero: Int,
    val numeroGlobal: Int, // Del 1 al 994
    val grupo: String,     // Ej: "ARG", "FWC"
    var cantidad: Int = 0  // 0 = Faltante (Rojo), 1 = Pegada (Verde), >1 = Pegada + Repetidas (Rosa)
)