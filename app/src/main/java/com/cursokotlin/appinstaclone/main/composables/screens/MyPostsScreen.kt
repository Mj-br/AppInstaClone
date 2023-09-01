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
import androidx.compose.foundation.layout.aspectRatio
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
    var myPost: PostData? = null,
//    var post2: PostData? = null,
//    var post3: PostData? = null,
)
//{
//    fun isFull() = myPost != null && post2 != null && post3 != null
//    fun add(post: PostData) {
//        if (myPost == null) {
//            myPost = post
//        } else if (post2 == null) {
//            post2 = post
//        } else if (post3 == null) {
//            post3 = post
//        }
//    }
//}

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
                modifier = Modifier.weight(1f).padding(1.dp).fillMaxSize()
            ) {
               // On Post click
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

@Composable
fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(top = 16.dp)
        .clickable { onClick.invoke() }) {

        UserImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )
        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = null,
                modifier = Modifier
                    .background(Color.Blue)
            )

        }
    }

}


@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostData>,
    modifier: Modifier,
    onPostClick: (PostData) -> Unit
) {

    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) Text(text = "No posts available")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = modifier.scale(1.01f)
        ) {
            items(posts.size) { postIndex ->
                val post = posts[postIndex]
                val postBoxData = PostBoxData(myPost = post)
                PostBox(item = postBoxData, onPostClick = onPostClick)
            }
        }


    }
}

@Composable
fun PostBox(item: PostBoxData, onPostClick: (PostData) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        PostImage(
            imageUrl = item.myPost?.postImage,
            modifier = Modifier
                .aspectRatio(1f)
                .border(
                    width = 1.dp,
                    color = Color.White
                )
                .clickable { item.myPost?.let { post -> onPostClick(post) } }
        )

    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var modifier = Modifier
            .padding(1.dp)
            .fillMaxSize()
        if (imageUrl == null) {
            modifier = modifier.clickable(enabled = false) { }
        }
        CommonImage(data = imageUrl, modifier = modifier, contentScale = ContentScale.Crop)
    }
}
