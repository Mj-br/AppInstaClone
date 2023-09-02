package com.cursokotlin.appinstaclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cursokotlin.appinstaclone.auth.LoginScreen
import com.cursokotlin.appinstaclone.auth.ProfileScreen
import com.cursokotlin.appinstaclone.auth.SingUpScreen
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.main.composables.screens.FeedScreen
import com.cursokotlin.appinstaclone.main.composables.NotificationMessage
import com.cursokotlin.appinstaclone.main.composables.screens.MyPostsScreen
import com.cursokotlin.appinstaclone.main.composables.screens.NewPostsScreen
import com.cursokotlin.appinstaclone.main.composables.screens.SearchScreen
import com.cursokotlin.appinstaclone.main.composables.screens.SinglePostScreen
import com.cursokotlin.appinstaclone.ui.theme.AppInstaCloneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppInstaCloneTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstagramApp()
                }
            }
        }
    }
}

sealed class DestinationScreen(val route: String) {
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Feed : DestinationScreen("feed")
    object Search : DestinationScreen("search")
    object MyPosts : DestinationScreen("myposts")
    object Profile : DestinationScreen("profile")
    object NewPost : DestinationScreen("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }
    object SinglePost : DestinationScreen("singlepost")
}

@Composable
fun InstagramApp() {
    val vm = hiltViewModel<IgViewModel>()
    val navController = rememberNavController()

    NotificationMessage(vm = vm)

    NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route) {
        composable(DestinationScreen.SignUp.route) {
            SingUpScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Feed.route) {
            FeedScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Search.route) {
            SearchScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.MyPosts.route) {
            MyPostsScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, vm = vm)
        }

        composable(DestinationScreen.NewPost.route) { navBackStackEntry ->
            val imageUri = navBackStackEntry.arguments?.getString("imageUri")
            imageUri?.let { imageUri ->
                NewPostsScreen(navController = navController, vm = vm, encodeUri = imageUri)
            }
        }

        composable(DestinationScreen.SinglePost.route + "/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            postId?.let { id ->
                val post = vm.getPostById(id)
                SinglePostScreen(navController = navController, vm = vm, post = post)
            }
        }


    }

}

@Preview
@Composable
fun GreetingPreview() {
    AppInstaCloneTheme {
        InstagramApp()

    }
}