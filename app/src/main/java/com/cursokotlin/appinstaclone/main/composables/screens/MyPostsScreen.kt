package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationItem
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationMenu

@Composable
fun MyPostsScreen(navController: NavController, vm: IgViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "MyPosts Screen")
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )

    }
}