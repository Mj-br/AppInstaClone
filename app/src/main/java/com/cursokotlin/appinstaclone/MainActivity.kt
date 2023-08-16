package com.cursokotlin.appinstaclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cursokotlin.appinstaclone.auth.SingUpScreen
import com.cursokotlin.appinstaclone.core.composables.NotificationMessage
import com.cursokotlin.appinstaclone.ui.theme.AppInstaCloneTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppInstaCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String){
    object SignUp: DestinationScreen("signup")
}

@Composable
fun InstagramApp(){
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route ){
        composable(DestinationScreen.SignUp.route){
            SingUpScreen(navController = navController, vm = vm)
        }

    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppInstaCloneTheme {
        InstagramApp()

    }
}