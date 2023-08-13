package com.cursokotlin.appinstaclone.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.IgViewModel

@Composable
fun SingUpScreen(navController: NavController, vm: IgViewModel){
    Box(modifier = Modifier.fillMaxWidth()){
        Text(text = "SignUp Screen")
    }
    

}