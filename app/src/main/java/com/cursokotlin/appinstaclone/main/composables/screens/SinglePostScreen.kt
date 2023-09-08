package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.main.composables.CommonDivider
import com.cursokotlin.appinstaclone.main.composables.CommonImage
import com.cursokotlin.appinstaclone.main.composables.UserImageCard
import com.cursokotlin.appinstaclone.R

@Composable
fun SinglePostScreen(
    navController: NavController,
    vm: IgViewModel,
    post: PostData?
) {
    post?.userId?.let {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    text = "Back",
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }

            CommonDivider()

            SinglePostDisplay(navController = navController, vm = vm, post = post)
        }
    }
}

@Composable
fun SinglePostDisplay(navController: NavController, vm: IgViewModel, post: PostData) {
    val userData = vm.userData.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserImageCard(
            userImage = post.userImage, //We can change this for userData?.imageUrl
            modifier = Modifier
                .padding(start = 4.dp, top = 0.dp, bottom = 0.dp, end = 4.dp)
                .size(32.dp)
        )

        Text(text = post.username ?: "")
        Text(text = ".", modifier = Modifier.padding(8.dp))

        if (userData?.userId == post.userId) {
            // Current user's post. Don't show anything
        } else if (userData?.following?.contains(post.userId) == true) {
            Text(
                text = "Following",
                color = Color.Gray,
                modifier = Modifier.clickable { vm.onFollowClick(post.userId!!) })
        } else {
            Text(text = "Follow", color = Color.Blue, modifier = Modifier.clickable { vm.onFollowClick(post.userId!!) })
        }
    }

    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .scale(1.04f)
            .padding(top = 8.dp, bottom = 8.dp)
            .height(400.dp)
        CommonImage(
            data = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }

    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_like),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Red)
        )
        Text(text = "${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 4.dp))
    }

    Row(modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 0.dp, end = 4.dp)) {
        Text(text = post.username ?: "", fontWeight = FontWeight.Bold)
        Text(text = post.postDescription ?: "", modifier = Modifier.padding(start = 8.dp))
    }

    Row(modifier = Modifier.padding(4.dp)) {
        Text(text = "10 comments", color = Color.Gray)
    }
}
