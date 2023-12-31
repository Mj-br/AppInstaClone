package com.cursokotlin.appinstaclone.main.composables.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.DestinationScreen
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.data.PostData
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationItem
import com.cursokotlin.appinstaclone.main.composables.BottomNavigationMenu
import com.cursokotlin.appinstaclone.main.composables.navigateTo

/**
 * Display the search screen.
 *
 * This composable function displays a search bar, a list of searched posts, and a bottom navigation menu.
 *
 * @param navController The NavController for navigation.
 * @param vm The view model containing relevant data.
 *
 */
@Composable
fun SearchScreen(navController: NavController, vm: IgViewModel) {
    // Get data from the view model
    val searchedPostsLoading = vm.searchedPostsProgress.value
    val searchedPosts = vm.searchedPosts.value
    var searchTerm by rememberSaveable { mutableStateOf("") }

    Column {
        // Display the search bar
        SearchBar(
            searchTerm = searchTerm,
            onSearchChange = { searchTerm = it },
            onSearch = { vm.searchPosts(searchTerm) }
        )

        // Display the list of searched posts
        PostList(
            isContextLoading = false,
            postsLoading = searchedPostsLoading,
            posts = searchedPosts,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp)
        ) { post ->
            // Navigate to the SinglePostScreen when a post is clicked
            navigateTo(
                navController = navController,
                dest = DestinationScreen.SinglePost,
                parameter = post
            )
        }

        // Display the bottom navigation menu
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.SEARCH,
            navController = navController
        )
    }
}

/**
 * Display the search bar.
 *
 * This composable function displays a search bar where users can enter a search term.
 *
 * @param searchTerm The current search term.
 * @param onSearchChange Callback to handle changes in the search term.
 * @param onSearch Callback to handle the search action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(searchTerm: String, onSearchChange: (String) -> Unit, onSearch: () -> Unit) {
    val focusManager = LocalFocusManager.current

    TextField(
        value = searchTerm,
        onValueChange = onSearchChange,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, CircleShape),
        shape = CircleShape,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                focusManager.clearFocus()
            }
        ),
        maxLines = 1,
        singleLine = true,
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
            textColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        trailingIcon = {
            IconButton(onClick = {
                onSearch()
                focusManager.clearFocus()
            }) {
                // Search icon
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
        }
    )
}
