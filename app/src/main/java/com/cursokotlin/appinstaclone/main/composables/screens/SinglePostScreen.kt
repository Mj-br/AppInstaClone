package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.main.composables.CommonImage

@Composable
fun SinglePostScreen(
    navController: NavController,
    vm: IgViewModel,
    post: PostData?
) {
    Text(text = "Single post screen ${post?.postDescription}")
    
    PostImage(imageUrl = post?.postImage, modifier = Modifier.fillMaxWidth().padding(64.dp) )

}