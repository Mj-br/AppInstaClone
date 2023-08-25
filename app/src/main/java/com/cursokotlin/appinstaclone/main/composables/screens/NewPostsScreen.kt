package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.IgViewModel

@Composable
fun NewPostsScreen(navController: NavController, viewModel: IgViewModel, encodeUri: String) {

    val imageUri by remember{ mutableStateOf(encodeUri)}
    var description by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current


}