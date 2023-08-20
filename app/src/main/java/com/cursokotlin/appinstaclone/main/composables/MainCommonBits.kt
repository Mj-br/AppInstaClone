package com.cursokotlin.appinstaclone.main.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.DestinationScreen
import com.cursokotlin.appinstaclone.IgViewModel

/**
 * This composable function displays a notification message as a Toast in the user interface.
 *
 * @param vm The view model from which to retrieve the notification message state.
 */
@Composable
fun NotificationMessage(vm: IgViewModel) {
    // Get the notification state and its content from the view model
    val notificationState = vm.popupNotification.value
    val notificationMessage = notificationState?.getContentOrNull()

    // Check if a notification message is available
    if (notificationMessage != null) {
        // Display the notification message as a Toast with a long duration
        Toast.makeText(LocalContext.current, notificationMessage, Toast.LENGTH_LONG).show()
    }
}

@Composable
fun CommonProgressSpinner(){
    Row (
        modifier = Modifier
            .alpha(0.5f)
            .background((Color.LightGray))
            .clickable(enabled = false) { }
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        CircularProgressIndicator()
    }
}

/**
 * Navigates to a specified destination screen using a NavController.
 *
 * @param navController The NavController responsible for navigation.
 * @param dest The destination screen to navigate to.
 */
fun navigateTo(navController: NavController, dest: DestinationScreen) {
    navController.navigate(dest.route) {
        /* Specify navigation behavior */

        // Pop up to the destination screen to remove intermediate destinations from the back stack
        popUpTo(dest.route)

        // Launch the destination screen as a single top-level destination
        launchSingleTop = true
    }
}

/**
 * Checks if a user is signed in and, if so, navigates to the Feed screen.
 *
 * @param vm The view model containing signed-in state information.
 * @param navController The NavController responsible for navigation.
 */
@Composable
fun CheckSignedIn(vm: IgViewModel, navController: NavController) {
    // Create a state variable to track if the navigation has already occurred
    val alreadyLoggedIn = remember { mutableStateOf(false) }

    // Get the signed-in state from the view model
    val signedIn = vm.signedIn.value

    // Check if the user is signed in and the navigation hasn't already occurred
    if (signedIn && !alreadyLoggedIn.value) {
        // Mark that the navigation has occurred
        alreadyLoggedIn.value = true

        // Navigate to the Feed screen and clear the back stack
        navController.navigate(DestinationScreen.Feed.route) {
            popUpTo(0)
        }
    }
}