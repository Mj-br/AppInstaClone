package com.cursokotlin.appinstaclone.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cursokotlin.appinstaclone.DestinationScreen
import com.cursokotlin.appinstaclone.IgViewModel
import com.cursokotlin.appinstaclone.core.composables.CommonProgressSpinner
import com.cursokotlin.appinstaclone.core.composables.navigateTo

@Composable
fun LoginScreen(navController: NavController, vm: IgViewModel) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(
                rememberScrollState()
            )
    ) {


        Header(Modifier.align(Alignment.TopEnd))
        BodyLogin(Modifier.align(Alignment.Center), vm)
        FooterLogin(Modifier.align(Alignment.BottomCenter), navController)


    }

    //Displays a progress spinner if a loading operation is in progress.
    val isLoading = vm.inProgress.value
    if (isLoading) {
        CommonProgressSpinner()
    }


}

@Composable
fun BodyLogin(modifier: Modifier, vm: IgViewModel) {

    val focus = LocalFocusManager.current

    //Variables to save states with LiveData or Flow
    val emailState = remember { mutableStateOf(TextFieldValue()) }
    val passState = remember { mutableStateOf(TextFieldValue()) }


    //Design of the body login screen
    Column(modifier = modifier) {
        ImageLogo(Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.size(100.dp))
        Text(
            text = "Log In",
            modifier = Modifier.padding(8.dp),
            fontSize = 30.sp,
            fontFamily = FontFamily.Serif,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Email(email = emailState, onTextChanged = {
            TODO("ViewModel.onLoginChanged(it,password)")
        })
        Spacer(modifier = Modifier.size(4.dp))
        Password(passState) {
            TODO("ViewModel.onLoginChanged(email,it)")
        }
        Spacer(modifier = Modifier.size(32.dp))
        Button(
            onClick = {
                focus.clearFocus(force = true)
                vm.onLogin(emailState.value.text, passState.value.text)
            },
            /*loginEnable: Boolean, loginViewModel: LoginViewModel)*/
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF4EA8E9),
                disabledBackgroundColor = Color(0xFF78C8F9),
                contentColor = Color.White,
                disabledContentColor = Color.White

            )
        ) {
            Text(text = "Log In")

        }
        Spacer(modifier = Modifier.size(64.dp))
    }


}

@Composable
fun FooterLogin(modifier: Modifier, navController: NavController) {
    Column(modifier = modifier.fillMaxWidth()) {
        Divider(
            modifier = Modifier
                .background(Color(0xFFF9F9F9))
                .height(1.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(24.dp))
        GoToSignUp(navController = navController)
        Spacer(modifier = Modifier.size(24.dp))

    }

}

@Composable
fun GoToSignUp(navController: NavController) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text = "Don't have an account? Go to ->",
            fontSize = 12.sp,
            color = Color(0xFFB5B5B5)
        )
        Text(
            text = "Sign Up",
            Modifier
                .padding(horizontal = 8.dp)
                .clickable {
                    navigateTo(navController, DestinationScreen.SignUp)
                },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4EA8E9)
        )

    }

}

