package com.example.fallenhero

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment


data class ScoreEntry(val name: String = "Player", val score: Int = 0)

@Composable
fun HighScoresView(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    var scores by remember { mutableStateOf(listOf<ScoreEntry>()) }

    LaunchedEffect(Unit) {
        db.collection("highscores")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                scores = result.map { doc ->
                    ScoreEntry(
                        name = doc.getString("name") ?: "Player",
                        score = doc.getLong("score")?.toInt() ?: 0
                    )
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Background igual à HomeView
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        // --- Highscore Title Added ---
        Image(
            painter = painterResource(id = R.drawable.highscore),
            contentDescription = "Highscore Title",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .height(120.dp)
        )

        // --- Standalone Back Button ---
        IconButton(
            onClick = {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 110.dp) // Increased to make space for the larger title
        ) { 
            items(scores) {
                entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(entry.name, color = Color.White)
                    Text(entry.score.toString(), color = Color.White)
                }
            }
        }
    }
}
