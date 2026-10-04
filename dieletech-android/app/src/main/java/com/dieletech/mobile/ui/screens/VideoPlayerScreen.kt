package com.dieletech.mobile.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dieletech.mobile.data.api.LessonRepository
import com.dieletech.mobile.data.local.ProgressStore
import com.dieletech.mobile.data.model.Lesson
import com.dieletech.mobile.ui.theme.VisualAssets
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * HU-07: Reproductor de video con Media3 ExoPlayer.
 *   - Play/pausa y control de progreso (nativos de PlayerView).
 *   - Recuerda la última posición: al abrir la lección arranca donde quedó.
 *   - Guarda la posición cada 5 s y al pausar/salir.
 *
 * HU-08: Botón "Marcar como completada" al final del video o manualmente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    lessonId: Long,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val store = remember { ProgressStore(ctx) }
    val scope = rememberCoroutineScope()

    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var completed by remember { mutableStateOf(false) }
    var initialPosMs by remember { mutableStateOf(0L) }
    var loaded by remember { mutableStateOf(false) }

    // Carga inicial: lección + estado
    LaunchedEffect(lessonId) {
        lesson = LessonRepository.findLesson(lessonId)
        completed = store.isCompleted(lessonId).first()
        initialPosMs = store.lastPositionMs(lessonId).first()
        loaded = true
    }

    if (!loaded || lesson == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentLesson = lesson!!
    val visual = remember(currentLesson.courseId) { VisualAssets.forCourse(currentLesson.courseId) }

    // ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(currentLesson.videoUrl)))
            playWhenReady = true
            prepare()
            seekTo(initialPosMs) // ← HU-07: resume
        }
    }

    // Detecta fin de video → propone completar
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    scope.launch {
                        store.markCompleted(lessonId, true)
                        completed = true
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            scope.launch { store.saveLastPosition(lessonId, exoPlayer.currentPosition) }
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Autosave cada 5s
    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(5000)
            store.saveLastPosition(lessonId, exoPlayer.currentPosition)
        }
    }

    // Pausar al mandar la app al background
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    scope.launch { store.saveLastPosition(lessonId, exoPlayer.currentPosition) }
                    exoPlayer.pause()
                }
                else -> {}
            }
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentLesson.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { c ->
                        PlayerView(c).apply {
                            player = exoPlayer
                            useController = true
                            controllerShowTimeoutMs = 2000
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Lección ${currentLesson.order}",
                    fontSize = 11.sp,
                    color = visual.accent,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    currentLesson.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    currentLesson.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            if (completed) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            store.markCompleted(lessonId, false)
                            completed = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(Modifier.height(6.dp))
                    Text("  Lección completada — desmarcar")
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            store.markCompleted(lessonId, true)
                            completed = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = visual.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Text("  Marcar lección como completada")
                }
            }
        }
    }
}
