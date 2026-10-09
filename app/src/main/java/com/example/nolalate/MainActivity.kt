package com.example.nolalate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nolalate.ui.theme.NolaLateTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NolaLateTheme {
                var albumIdSeleccionado by remember { mutableStateOf<Int?>(null) }
                var propietarioSeleccionado by remember { mutableStateOf("") }

                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    if (albumIdSeleccionado == null) {
                        MainScreen(onAbrirAlbum = { id, nombre ->
                            albumIdSeleccionado = id
                            propietarioSeleccionado = nombre
                        })
                    } else {
                        AlbumScreen(
                            albumId = albumIdSeleccionado!!,
                            propietario = propietarioSeleccionado,
                            onVolver = { albumIdSeleccionado = null }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onAbrirAlbum: (Int, String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val albumDao = db.albumDao()
    val coroutineScope = rememberCoroutineScope()

    val albumes = remember { mutableStateListOf<AlbumEntity>() }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val listaDb = albumDao.obtenerTodosLosAlbumes()
            withContext(Dispatchers.Main) {
                albumes.clear()
                albumes.addAll(listaDb)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.fondo_app),
            contentDescription = "Fondo Oficial FIFA Nola Late",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                OutlinedIconButton(
                    onClick = { mostrarDialogo = true },
                    modifier = Modifier.size(75.dp).padding(bottom = 16.dp, end = 16.dp),
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color.Black),
                    colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Agregar", tint = Color.Black, modifier = Modifier.size(38.dp))
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ÁLBUM DE",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(top = 65.dp, bottom = 45.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(albumes) { album ->
                        var menuExpandido by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { onAbrirAlbum(album.id, album.propietario) },
                                modifier = Modifier.fillMaxWidth().height(65.dp),
                                shape = RoundedCornerShape(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C6FF)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Spacer(modifier = Modifier.size(48.dp))
                                    Text(text = album.propietario, fontSize = 26.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { menuExpandido = true }, modifier = Modifier.size(48.dp)) {
                                        Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Opciones", tint = Color.White)
                                    }
                                }
                            }

                            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                                DropdownMenuItem(text = { Text("Intercambiar (Próximamente)", color = Color.Gray) }, onClick = { menuExpandido = false }, enabled = false)
                                DropdownMenuItem(
                                    text = { Text("Borrar álbum", color = Color.Red) },
                                    onClick = {
                                        coroutineScope.launch(Dispatchers.IO) {
                                            albumDao.borrarAlbumPorId(album.id)
                                            withContext(Dispatchers.Main) {
                                                albumes.remove(album)
                                            }
                                        }
                                        menuExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false; nuevoNombre = "" },
                title = { Text("Nuevo Álbum", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Nombre del propietario:", modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(value = nuevoNombre, onValueChange = { nuevoNombre = it }, placeholder = { Text("Ej: Fermin") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (nuevoNombre.isNotBlank()) {
                            coroutineScope.launch(Dispatchers.IO) {
                                val nuevoId = albumDao.insertarAlbum(AlbumEntity(propietario = nuevoNombre))

                                val nombresGrupos = listOf(
                                    "FWC", "MEX", "RSA", "KOR", "CZE", "CAN", "BIH", "QAT", "SUI", "BRA",
                                    "MAR", "HAI", "SCO", "USA", "PAR", "AUS", "TUR", "GER", "CUW", "CIV",
                                    "ECU", "NED", "JPN", "SWE", "TUN", "BEL", "EGY", "IRN", "NZL", "ESP",
                                    "CPV", "KSA", "URU", "FRA", "SEN", "IRQ", "NOR", "ARG", "ALG", "AUT",
                                    "JOR", "POR", "COD", "UZB", "COL", "ENG", "CRO", "GHA", "PAN", "CC"
                                )
                                val listaInicialEntities = mutableListOf<FiguritaEntity>()
                                var globalCont = 1
                                nombresGrupos.forEach { grupo ->
                                    val maxFiguritas = if (grupo == "CC") 14 else 20
                                    for (i in 1..maxFiguritas) {
                                        listaInicialEntities.add(
                                            FiguritaEntity(albumId = nuevoId.toInt(), numero = i, numeroGlobal = globalCont, grupo = grupo)
                                        )
                                        globalCont++
                                    }
                                }
                                albumDao.insertarFiguritas(listaInicialEntities)

                                val listaDb = albumDao.obtenerTodosLosAlbumes()
                                withContext(Dispatchers.Main) {
                                    albumes.clear()
                                    albumes.addAll(listaDb)
                                    nuevoNombre = ""
                                    mostrarDialogo = false
                                }
                            }
                        }
                    }) {
                        Text("Guardar", color = Color(0xFF00C6FF), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { mostrarDialogo = false; nuevoNombre = "" }) { Text("Cancelar", color = Color.Gray) } }
            )
        }
    }
}
