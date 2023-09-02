package com.cursokotlin.appinstaclone.main.composables

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.cursokotlin.appinstaclone.DestinationScreen
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.R

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
fun CommonProgressSpinner(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .alpha(0.5f)
            .background(Color.LightGray)
            .clickable(enabled = false) { }
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()
    }
}


/**
 * Navigates to a specified destination screen with optional [parameter].
 *
 * @param navController The NavController used for navigation.
 * @param dest The destination screen to navigate to.
 * @param parameter The optional post ID to append to the route.
 */
fun navigateTo(navController: NavController, dest: DestinationScreen, parameter: String? = null) {
    // Use the NavController to navigate to the specified destination screen.
    // The route for navigation is constructed using the buildRoute function.
    navController.navigate(buildRoute(dest, parameter)) {
        /* Specify navigation behavior */

        // Pop up to the destination screen to remove intermediate destinations from the back stack
        popUpTo(dest.route)

        // Launch the destination screen as a single top-level destination
        launchSingleTop = true
    }
}

/**
 * Builds a route for navigation to a destination screen with an optional [parameter].
 *
 * @param dest The destination screen to navigate to.
 * @param parameter The optional post ID to append to the route.
 * @return The constructed route.
 */
private fun buildRoute(dest: DestinationScreen, parameter: String? = null): String {
    return if (parameter != null) {
        // If a postId is provided, construct the route by appending it to the destination's route.
        "${dest.route}/$parameter"
    } else {
        // If no postId is provided, use only the destination's route.
        dest.route
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


/**
 * Displays an image with optional loading spinner.
 *
 * This composable displays an image fetched from the provided [data] URL or resource,
 * and it supports showing a loading spinner while the image is loading.
 *
 * @param data The URL or resource path of the image to display.
 * @param modifier The modifier to apply to this composable.
 * @param contentScale The content scale for the displayed image.
 */
@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {
    // Create a painter to load and display the image.
    val painter = rememberImagePainter(data = data)

    // Create a Box composable to overlay the image with a spinner when loading.
    Box(modifier = modifier) {
        // Display the image with optional content scale.
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale
        )

        // Show a spinner in the center if the image is still loading.
        if (painter.state is ImagePainter.State.Loading) {
            CommonProgressSpinner(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

/**
 * Displays a user image card.
 *
 * This composable displays a circular card that contains a user image.
 * If [userImage] is null or empty, it displays a default user image icon.
 *
 * @param userImage The URL or resource path of the user's image.
 * @param modifier The modifier to apply to this composable.
 */
@Composable
fun UserImageCard(
    userImage: String?,
    modifier: Modifier = Modifier
        .padding(8.dp)
        .size(64.dp)
) {
    // Create a circular card with the provided modifier.
    Card(shape = CircleShape, modifier = modifier) {
        if (userImage.isNullOrEmpty()) {
            // Display a default user image icon if [userImage] is null or empty.
            Image(
                painter = painterResource(id = R.drawable.ic_user),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.Gray),
                modifier = Modifier
                    .fillMaxSize() // Make the Image occupy the entire Card
                    .padding(4.dp), // Optional padding to adjust the image position
                alignment = Alignment.Center // Center the image within the Card
            )
        } else {
            // Display the user's image using the CommonImage composable.
            CommonImage(data = userImage)
        }
    }
}


/**
 * Displays a common divider.
 *
 * This composable displays a horizontal divider line with a light gray color.
 * It is typically used to separate content within a layout.
 */
@Composable
fun CommonDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )
}
