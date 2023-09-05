package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            Text(text = "Back", modifier = Modifier.clickable { navController.popBackStack() })

            CommonDivider()

            SinglePostDisplay(navController = navController, vm = vm, post = post)


        }
    }

}

@Composable
fun SinglePostDisplay(navController: NavController, vm: IgViewModel, post: PostData) {
    val userData = vm.userData.value

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            UserImageCard(
                userImage = userData?.imageUrl,
                modifier = Modifier
                    .padding(8.dp)
                    .size(32.dp)
            )
            Text(text = post.username ?: "")
            Text(text = ".", modifier = Modifier.padding(8.dp))

            if (userData?.userId == post.userId) {
                // Current user's post. Don't show anything
            } else {
                Text(text = "Follow", color = Color.Blue, modifier = Modifier.clickable {
                    // Follow a user
                })
            }
        }

    }
    Box {
        val modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .scale(1.04f)
            .size(100.dp)
        CommonImage(
            data = post.postImage,
            modifier = modifier,
            contentScale = ContentScale.FillWidth
        )
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_like), contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Red)
        )
        Text(text = "${post.likes?.size ?: 0} likes", modifier = Modifier.padding(start = 0.dp))

    }

}