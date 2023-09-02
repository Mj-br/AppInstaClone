package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.data.PostData

@Composable
fun SinglePostScreen(
    navController: NavController,
    vm: IgViewModel,
    post: PostData
) {
    Text(text = "Single post screen ${post.postDescription}")

}