package com.example.nolalate

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(albumId: Int, propietario: String, onVolver: () -> Unit) {
    val nombresGrupos = listOf(
        "FWC", "MEX", "RSA", "KOR", "CZE", "CAN", "BIH", "QAT", "SUI", "BRA",
        "MAR", "HAI", "SCO", "USA", "PAR", "AUS", "TUR", "GER", "CUW", "CIV",
        "ECU", "NED", "JPN", "SWE", "TUN", "BEL", "EGY", "IRN", "NZL", "ESP",
        "CPV", "KSA", "URU", "FRA", "SEN", "IRQ", "NOR", "ARG", "ALG", "AUT",
        "JOR", "POR", "COD", "UZB", "COL", "ENG", "CRO", "GHA", "PAN", "CC"
    )

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val albumDao = db.albumDao()
    val coroutineScope = rememberCoroutineScope()

    val listadoInicial = remember { mutableStateListOf<FiguritaEntity>() }

    var filtroSeleccionado by remember { mutableStateOf("TODAS") }
    var menuExpandido by remember { mutableStateOf(false) }
    var figuritaParaEditar by remember { mutableStateOf<FiguritaEntity?>(null) }

    LaunchedEffect(albumId) {
        coroutineScope.launch(Dispatchers.IO) {
            val listaDb = albumDao.obtenerFiguritasDelAlbum(albumId)
            withContext(Dispatchers.Main) {
                listadoInicial.clear()
                listadoInicial.addAll(listaDb)
            }
        }
    }

    val pegadas = listadoInicial.count { it.cantidad >= 1 }
    val faltantes = listadoInicial.count { it.cantidad == 0 }
    val repetidas = listadoInicial.sumOf { if (it.cantidad > 1) it.cantidad - 1 else 0 }

    val gridState = rememberLazyGridState()

    val figuritasFiltradas = when (filtroSeleccionado) {
        "FALTANTES" -> listadoInicial.filter { it.cantidad == 0 }
        "REPETIDAS" -> listadoInicial.filter { it.cantidad > 1 }
        else -> listadoInicial
    }

    val gruposAgrupados = figuritasFiltradas.groupBy { it.grupo }

    val mapeoIndicesGrupo = remember(gruposAgrupados, filtroSeleccionado) {
        val mapa = mutableMapOf<String, Int>()
        var contadorItemsLogicos = 0
        gruposAgrupados.forEach { (nombreGrupo, figsDelGrupo) ->
            mapa[nombreGrupo] = contadorItemsLogicos
            if (filtroSeleccionado == "TODAS" || figsDelGrupo.isNotEmpty()) {
                contadorItemsLogicos += 1
            }
            contadorItemsLogicos += figsDelGrupo.size
        }
        mapa
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = "Fondo Oficial Nola Late",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Álbum de $propietario", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        TextButton(onClick = onVolver) { Text("◀ Volver", color = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.4f)
                    )
                )
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 16.dp, end = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        ContadorCard(titulo = "Pegadas", cantidad = pegadas, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        ContadorCard(titulo = "Faltantes", cantidad = faltantes, color = Color(0xFFF44336), modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        ContadorCard(titulo = "Repetidas", cantidad = repetidas, color = Color(0xFFFF8A80), modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Button(
                            onClick = { compartirPorWhatsApp(context, propietario, listadoInicial, "FALTANTES") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Compartir Faltantes", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { compartirPorWhatsApp(context, propietario, listadoInicial, "REPETIDAS") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                        ) {
                            Text("Compartir Repetidas", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Button(
                            onClick = { menuExpandido = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.85f))
                        ) {
                            Text("VER: $filtroSeleccionado", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                            listOf("TODAS", "FALTANTES", "REPETIDAS").forEach { opcion ->
                                DropdownMenuItem(text = { Text(opcion) }, onClick = { filtroSeleccionado = opcion; menuExpandido = false })
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        state = gridState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        gruposAgrupados.forEach { (nombreGrupo, figsDelGrupo) ->
                            if (filtroSeleccionado == "TODAS" || figsDelGrupo.isNotEmpty()) {
                                item(span = { GridItemSpan(5) }) {
                                    Text(
                                        text = nombreGrupo,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
                                    )
                                }
                            }

                            items(figsDelGrupo, key = { it.numeroGlobal }) { fig ->
                                Box(modifier = Modifier.padding(3.dp)) {
                                    ItemFigurita(
                                        numero = fig.numero,
                                        cantidad = fig.cantidad,
                                        onClick = {
                                            val index = listadoInicial.indexOfFirst { it.numeroGlobal == fig.numeroGlobal }
                                            if (index != -1) {
                                                val nuevaCant = listadoInicial[index].cantidad + 1
                                                listadoInicial[index] = listadoInicial[index].copy(cantidad = nuevaCant)
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    albumDao.actualizarCantidadFigurita(albumId, fig.numeroGlobal, nuevaCant)
                                                }
                                            }
                                        },
                                        onLongClick = { figuritaParaEditar = fig }
                                    )
                                }
                            }
                        }
                    }
                }

                // BARRA LATERAL DINÁMICA: Cambia automáticamente según la vista seleccionada
                if (gruposAgrupados.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .width(45.dp)
                            .fillMaxHeight()
                            .padding(top = 12.dp, bottom = 12.dp, start = 4.dp, end = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val gruposVisibles = gruposAgrupados.keys.toList()
                        items(gruposVisibles.size) { index ->
                            val grupo = gruposVisibles[index]
                            Text(
                                text = grupo,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                mapeoIndicesGrupo[grupo]?.let { indiceDestino ->
                                                    coroutineScope.launch {
                                                        gridState.scrollToItem(indiceDestino)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    .padding(vertical = 2.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (figuritaParaEditar != null) {
            val figActual = figuritaParaEditar!!
            val indexEnLista = listadoInicial.indexOfFirst { it.numeroGlobal == figActual.numeroGlobal }

            AlertDialog(
                onDismissRequest = { figuritaParaEditar = null },
                title = { Text("Figurita ${figActual.grupo} ${figActual.numero}", fontWeight = FontWeight.Bold) },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (indexEnLista != -1 && listadoInicial[indexEnLista].cantidad > 0) {
                                    val nuevaCant = listadoInicial[indexEnLista].cantidad - 1
                                    listadoInicial[indexEnLista] = listadoInicial[indexEnLista].copy(cantidad = nuevaCant)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        albumDao.actualizarCantidadFigurita(albumId, figActual.numeroGlobal, nuevaCant)
                                    }
                                }
                            },
                            shape = CircleShape
                        ) { Text("-", fontSize = 24.sp) }

                        Text(
                            text = if (indexEnLista != -1) "${listadoInicial[indexEnLista].cantidad}" else "0",
                            fontSize = 28.sp,
                            modifier = Modifier.padding(horizontal = 24.dp),
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                if (indexEnLista != -1) {
                                    val nuevaCant = listadoInicial[indexEnLista].cantidad + 1
                                    listadoInicial[indexEnLista] = listadoInicial[indexEnLista].copy(cantidad = nuevaCant)
                                    coroutineScope.launch(Dispatchers.IO) {
                                        albumDao.actualizarCantidadFigurita(albumId, figActual.numeroGlobal, nuevaCant)
                                    }
                                }
                            },
                            shape = CircleShape
                        ) { Text("+", fontSize = 24.sp) }
                    }
                },
                confirmButton = { TextButton(onClick = { figuritaParaEditar = null }) { Text("Listo", fontWeight = FontWeight.Bold) } }
            )
        }
    }
}

@Composable
fun ContadorCard(titulo: String, cantidad: Int, color: Color, modifier: Modifier) {
    Card(modifier = modifier.height(70.dp), colors = CardDefaults.cardColors(containerColor = color), shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(titulo, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Medium)
                    Text("$cantidad", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemFigurita(numero: Int, cantidad: Int, onClick: () -> Unit, onLongClick: () -> Unit) {
    val backgroundColor = when (cantidad) {
        0 -> Color(0xFFF44336)
        else -> Color(0xFF4CAF50)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$numero", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        if (cantidad > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .size(18.dp)
                    .background(Color(0xFFFF8A80), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${cantidad - 1}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 11.sp,
                    style = LocalTextStyle.current.copy(
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = false
                        )
                    )
                )
            }
        }
    }
}

fun compartirPorWhatsApp(context: android.content.Context, propietario: String, listado: List<FiguritaEntity>, tipo: String) {
    val filtradas = if (tipo == "FALTANTES") {
        listado.filter { it.cantidad == 0 }
    } else {
        listado.filter { it.cantidad > 1 }
    }

    if (filtradas.isEmpty()) {
        Toast.makeText(context, "¡No tenés $tipo para compartir!", Toast.LENGTH_SHORT).show()
        return
    }

    // Agrupamos por grupo (ej: ARG, BRA, etc.) para que quede prolijo tipo figurita
    val textoAgrupado = filtradas.groupBy { it.grupo }
        .entries.joinToString(separator = "\n") { (grupo, figs) ->
            "• *$grupo*: " + figs.joinToString(", ") { it.numero.toString() }
        }

    val mensaje = "¡Hola! Te dejo mis figuritas *$tipo* del álbum de $propietario (Nola Late):\n\n$textoAgrupado"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, mensaje)
        setPackage("com.whatsapp")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Por si no tiene WhatsApp oficial o salta algún error, intentamos genérico
        val intentGenerico = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        context.startActivity(Intent.createChooser(intentGenerico, "Compartir con..."))
    }
}