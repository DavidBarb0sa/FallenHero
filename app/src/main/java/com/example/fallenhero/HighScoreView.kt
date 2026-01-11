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


data class ScoreEntry(val name: String = "Player", val score: Int = 0)

@OptIn(ExperimentalMaterial3Api::class)
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

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("High Scores") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )

            }
        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(scores) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(entry.name, color = Color.White)
                        Text(entry.score.toString(), color = Color.White)
                    }
                }
            }
        }
    }
}
