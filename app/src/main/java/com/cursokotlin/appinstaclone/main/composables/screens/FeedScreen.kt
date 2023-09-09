package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.DestinationScreen
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationItem
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationMenu
import com.cursokotlin.appinstaclone.main.composables.CommonImage
import com.cursokotlin.appinstaclone.main.composables.CommonProgressSpinner
import com.cursokotlin.appinstaclone.main.composables.UserImageCard
import com.cursokotlin.appinstaclone.main.composables.navigateTo

@Composable
fun FeedScreen(navController: NavController, vm: IgViewModel) {

    val userDataLoading = vm.inProgress.value
    val userData = vm.userData.value
    val personalizedFeed = vm.postsFeed.value
    val personalizedFeedLoading = vm.postsFeedProgress.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
        )
        {
            UserImageCard(userImage = userData?.imageUrl)
        }

        PostsList(
            posts = personalizedFeed,
            modifier = Modifier.weight(1f),
            loading = personalizedFeedLoading or userDataLoading,
            navController = navController,
            vm = vm,
            currentUserId = userData?.userId ?: ""
        )

        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.FEED,
            navController = navController
        )

    }

}

@Composable
fun PostsList(
    posts: List<PostData>,
    modifier: Modifier,
    loading: Boolean,
    navController: NavController,
    vm: IgViewModel,
    currentUserId: String
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(items = posts) { postData ->
                Post(post = postData, currentUserId = currentUserId, vm = vm) {
                    navigateTo(navController, DestinationScreen.SinglePost, it /*TODO:Look out*/)

                }
            }
        }
        if (loading)
            CommonProgressSpinner()
    }
}

@Composable
fun Post(
    post: PostData,
    currentUserId: String,
    vm: IgViewModel,
    onPostClick: (String) /*TODO:Look out*/ -> Unit
) {
    Card(
        shape = RoundedCornerShape(corner = CornerSize(4.dp)),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp, bottom = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(color = Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    CommonImage(
                        data = post.userImage,
                        contentScale = ContentScale.Crop
                    )
                }
                Text(text = post.username ?: "", modifier = Modifier.padding(4.dp))

            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
               /* val modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 150.dp)*/

                val modifier = Modifier
                    .fillMaxWidth()
                    .scale(1.04f)
                    .padding(top = 8.dp, bottom = 8.dp)
                    .height(400.dp)
                CommonImage(
                    data = post.postImage,
                    modifier = modifier,
                    contentScale = ContentScale.Crop //FillWidth
                )
            }

        }
    }

}

