package com.example.fallenhero

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun GameOverView(score: Int, navController: NavController, context: Context) {

    var showNameDialog by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }

    val db = FirebaseFirestore.getInstance()
    val playerId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    // Verifica se bateu o highscore
    LaunchedEffect(score) {
        db.collection("highscores").document(playerId).get()
            .addOnSuccessListener { document ->
                val oldScore = document.getLong("score")?.toInt() ?: 0
                if (score > oldScore) {
                    showNameDialog = true
                } else {
                    saveHighScore(context, score) // mantém o nome antigo
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE", "Erro ao ler documento", e)
                saveHighScore(context, score) // se não existir, cria novo score
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Adiciona a imagem de fundo
        Image(
            painter = painterResource(id = R.drawable.gameoverbg),
            contentDescription = "Game Over Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // O texto "GAME OVER" foi removido
            Text("Score: $score", fontSize = 32.sp, color = Color.White)
            Spacer(modifier = Modifier.height(50.dp))
            Button(onClick = {
                SoundManager.stopAll() // Stop all sounds
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }) {
                Text("Voltar ao Menu")
            }
        }

        // Dialog para inserir nome se bateu highscore
        if (showNameDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Novo Highscore!") },
                text = {
                    Column {
                        Text("Insere o teu nome:")
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = playerName,
                            onValueChange = { playerName = it },
                            placeholder = { Text("Nome") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        saveHighScore(context, score, playerName.ifBlank { "Jogador" })
                        showNameDialog = false
                        SoundManager.stopAll() // Stop all sounds
                        navController.navigate("home")
                    }) {
                        Text("Guardar")
                    }
                }
            )
        }
    }
}

fun saveHighScore(context: Context, newScore: Int, playerName: String? = null) {
    val db = FirebaseFirestore.getInstance()

    val playerId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )

    val docRef = db.collection("highscores").document(playerId)

    docRef.get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val oldScore = document.getLong("score")?.toInt() ?: 0

                if (newScore > oldScore) { // Só atualiza se for maior
                    val data = hashMapOf(
                        "score" to newScore,
                        "playerId" to playerId,
                        "name" to (playerName ?: document.getString("name") ?: "Jogador")
                    )
                    docRef.set(data)
                        .addOnSuccessListener { Log.d("FIREBASE", "Score atualizado: $newScore") }
                        .addOnFailureListener { Log.e("FIREBASE", "Erro ao atualizar", it) }
                } else {
                    Log.d("FIREBASE", "Score não é maior ($newScore <= $oldScore)")
                }
            } else {
                val data = hashMapOf(
                    "score" to newScore,
                    "playerId" to playerId,
                    "name" to (playerName ?: "Jogador")
                )
                docRef.set(data)
                    .addOnSuccessListener { Log.d("FIREBASE", "Score criado: $newScore") }
                    .addOnFailureListener { Log.e("FIREBASE", "Erro ao criar score", it) }
            }
        }
        // Linha corrigida
        .addOnFailureListener { e -> Log.e("FIREBASE", "Erro ao ler documento", e) }
}
