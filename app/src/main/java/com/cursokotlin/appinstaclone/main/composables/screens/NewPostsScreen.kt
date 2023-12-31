package com.cursokotlin.appinstaclone.main.composables.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.main.composables.CommonDivider
import com.cursokotlin.appinstaclone.main.composables.CommonProgressSpinner

/**
 * Displays the screen for creating a new post with an image and description.
 *
 * @param navController The navigation controller for navigating to other screens.
 * @param vm The view model associated with the new post screen.
 * @param encodeUri The encoded URI of the selected image to be posted.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable

fun NewPostsScreen(navController: NavController, vm: IgViewModel, encodeUri: String) {
    // Create a mutable state for the image URI
    val imageUri by remember { mutableStateOf(encodeUri) }

    // Create a mutable state for the description
    var description by rememberSaveable { mutableStateOf("") }

    // Remember the scroll state
    val scrollState = rememberScrollState()

    // Get the current focus manager
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxWidth()
    ) {
        // Row for Cancel and Post buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Cancel", modifier = Modifier.clickable { navController.popBackStack() })
            Text(text = "Post", modifier = Modifier.clickable {
                // Clear focus to dismiss the keyboard
                focusManager.clearFocus()

                // Call onNewPost from the view model to create the post
                vm.onNewPost(Uri.parse(imageUri), description) {
                    // After successful post creation, navigate back
                    navController.popBackStack()
                }
            })
        }

        // Divider
        CommonDivider()

        // Display the selected image
        Image(
            painter = rememberImagePainter(imageUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 150.dp),
            contentScale = ContentScale.FillWidth
        )

        // Description input field
        Row(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                label = { Text(text = "Description") },
                singleLine = false,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    textColor = Color.Black
                )
            )
        }
    }

    // Check if there's a progress spinner to display
    val inProgress = vm.inProgress.value
    if (inProgress) {
        CommonProgressSpinner()
    }
}

