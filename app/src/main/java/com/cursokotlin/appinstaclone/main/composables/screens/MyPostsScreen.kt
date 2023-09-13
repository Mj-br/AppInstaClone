package com.cursokotlin.appinstaclone.main.composables.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.DestinationScreen
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.R
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationItem
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationMenu
import com.cursokotlin.appinstaclone.main.composables.CommonImage
import com.cursokotlin.appinstaclone.main.composables.CommonProgressSpinner
import com.cursokotlin.appinstaclone.main.composables.UserImageCard
import com.cursokotlin.appinstaclone.main.composables.navigateTo

data class PostBoxData(
    var myPost: PostData? = null
)

@Composable
fun MyPostsScreen(navController: NavController, vm: IgViewModel) {

    /**
     * Launcher for selecting an image for a new post.
     *
     * This launcher is used to launch the image picker activity and handle the result.
     * When an image is selected, it encodes the image URI and navigates to the new post screen
     * with the encoded URI as a parameter.
     */
    val newPostImageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Encode the selected image URI as a string
                val encoded = Uri.encode(it.toString())

                // Create a route to the new post screen with the encoded URI as a parameter
                val route = DestinationScreen.NewPost.createRoute(encoded)

                // Navigate to the new post screen
                navController.navigate(route)
            }
        }


    val userData = vm.userData.value
    val isLoading = vm.inProgress.value

    val postsLoading = vm.refreshPostsProgress.value
    val posts = vm.posts.value


    Column {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                ProfileImage(userData?.imageUrl) {
                    newPostImageLauncher.launch("image/*")
                }
                Row(modifier = Modifier.padding(top = 48.dp)) {

                    Text(
                        text = "15\nposts", modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "54\nfollowers", modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "93\nfollowing", modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                val usernameDisplay =
                    if (userData?.username == null) "" else "@${userData?.username}"
                Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = usernameDisplay)
                Text(text = userData?.bio ?: "")

            }
            OutlinedButton(
                onClick = { navigateTo(navController, DestinationScreen.Profile) },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.LightGray
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(10),

                ) {
                Text(text = "Edit Profile", color = Color.Black)
            }
            PostList(
                isContextLoading = isLoading,
                postsLoading = postsLoading,
                posts = posts,
                modifier = Modifier
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize()
            ) { postId ->
                navigateTo(navController, dest = DestinationScreen.SinglePost, parameter = postId)
            }
        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )

    }

    if (isLoading) {
        CommonProgressSpinner()
    }
}

/**
 * Displays a user's profile image in a clickable box.
 *
 * @param imageUrl The URL of the user's profile image.
 * @param onClick A lambda function to handle click events on the profile image.
 */
@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 16.dp)
            .clickable { onClick.invoke() } // Invoke the provided lambda function on click
    ) {
        // Display the user's profile image in a UserImageCard
        UserImageCard(
            userImage = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )
        // Display a circular "Add" button in a Card overlay
        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            // Display the "Add" icon with a blue background
            Image(
                painter = painterResource(id = R.drawable.ic_add), // Replace with your "Add" icon resource
                contentDescription = null,
                modifier = Modifier
                    .background(Color.Blue)
            )
        }
    }
}


/**
 * Displays a list of posts in a grid layout.
 *
 * @param isContextLoading A boolean indicating whether the context (e.g., user data) is loading.
 * @param postsLoading A boolean indicating whether the posts are currently loading.
 * @param posts The list of [PostData] objects to display.
 * @param modifier Modifier for customizing the appearance and behavior of the PostList.
 * @param onPostClick A lambda function to handle post click events. It receives the postId as a parameter.
 */
@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (String?) -> Unit
) {
    // Check if posts are currently loading
    if (postsLoading) {
        CommonProgressSpinner() // Display a loading spinner
    } else if (posts.isEmpty()) {
        // If there are no posts and context is not loading, show a message
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) {
                Text(text = "No posts available")
            }
        }
    } else {
        // Display the posts in a LazyVerticalGrid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Display 3 columns in the grid
            modifier = modifier.scale(1.02f) // Apply a scaling modifier to the grid
        ) {
            items(posts.size) { postIndex ->
                // Get the post at the current index
                val post = posts[postIndex]
                // Create PostBoxData for the current post
                val postBoxData = PostBoxData(myPost = post)
                // Display the post using the PostBox composable
                PostBox(item = postBoxData, onPostClick = onPostClick)
            }
        }
    }
}


/**
 * Displays a post in a Box container with optional click behavior.
 *
 * @param item The [PostBoxData] representing the post to display.
 * @param onPostClick A lambda function to handle post click events. It receives the postId as a parameter.
 */
@Composable
fun PostBox(item: PostBoxData, onPostClick: (String?) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth() // Make the box take the maximum available width
            .height(135.dp) // Set the height of the box
    ) {
        // Inside the Box, there is a PostImage composable
        PostImage(
            imageUrl = item.myPost?.postImage, // Get the URL of the post's image from the data
            modifier = Modifier
                .border(
                    width = 1.dp, // Set a border around the image
                    color = Color.White // Border color is white
                )
                .clickable { // Add a clickable behavior to the image
                    item.myPost?.let { post ->
                        // When clicked, invoke the onPostClick lambda function with the postId
                        onPostClick(post.postId) // Pass the postId to the click handler
                    }
                }
        )
    }
}

/**
 * Displays an image in a Box container with optional click behavior.
 *
 * @param imageUrl The URL of the image to display.
 * @param modifier Modifier for customizing the image container.
 */
@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    // Create a Box composable to display an image
    Box(modifier = modifier) {
        // Define a modifier variable for further customization
        var modifier = Modifier
            .padding(1.dp) // Apply padding around the image
            .fillMaxSize() // Make the image fill the available space
        if (imageUrl == null) {
            // If imageUrl is null, make the image unclickable
            modifier = modifier.clickable(enabled = false) { }
        }
        // Use the CommonImage composable to display the image with the given data and modifiers
        CommonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)
    }
}
