package com.example.fallenhero

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fallenhero.ui.theme.FallenHeroTheme


@Composable
fun GameScreenView(modifier: Modifier = Modifier, navController: NavController){

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val density = configuration.densityDpi / 160f
    val screenHeightPx = screenHeight.value * density
    val screenWidthPx = screenWidth.value * density

    AndroidView(
        modifier = modifier,
        factory = {
            GameView(it, screenWidthPx.toInt(), screenHeightPx.toInt()).apply {
                this.onGameOver = { score ->
                    navController.navigate("gameover/$score")
                }
            }
        }
    ) {
        it.resume()
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenViewPreview(){
    FallenHeroTheme {
        GameScreenView(navController = rememberNavController())
    }
}
