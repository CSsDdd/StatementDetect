package com.example.statement_detect.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.statement_detect.timer.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(vm: TimerViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            MainScaffold(vm = vm, navController = navController)
        }
        composable(Routes.SETTINGS) {
            SettingsScaffold(vm = vm,navController = navController)
        }
    }
}

@Composable
fun AppDrawer(drawerState:DrawerState, scope: CoroutineScope,navController: NavController) {
    Column(modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,){
            IconButton(modifier = Modifier.wrapContentSize(),
                onClick = { scope.launch { drawerState.close() } }){
                Icon(imageVector = Icons.Default.KeyboardBackspace, contentDescription = null)
            }
            //Text("      ")
            //Text("返回")
        }
        Row(modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,){
            IconButton(modifier = Modifier.wrapContentSize(),
                onClick = { navController.navigate(Routes.HOME) }
            ){
                Icon(imageVector = Icons.Default.Home, contentDescription = null)
            }
            //Text("      ")
            //Text("设置")
        }
        Row(modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,){
            IconButton(modifier = Modifier.wrapContentSize(),
                onClick = { navController.navigate(Routes.SETTINGS) }
            ){
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            }
            //Text("      ")
            //Text("设置")
        }
    }
}