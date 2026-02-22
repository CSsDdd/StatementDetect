package com.example.statement_detect.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.statement_detect.camera.RequestCameraPermission
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.statement_detect.timer.TimerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(vm: TimerViewModel , navController: NavController){
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var needPhoto by remember { mutableStateOf(false) }
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(56.dp),
                drawerContainerColor = Color(0xFF222846),
                drawerContentColor = Color.White,
            ){
                AppDrawer(drawerState, scope)
            }
        },
        drawerState = drawerState,
        //modifier = TODO(),
        //gesturesEnabled = TODO(),
        //scrimColor = TODO(),
        //content = TODO(),
    ){
        Scaffold(
            modifier = Modifier,
            topBar = {
                TopAppBar(
                    title = { Text("不知道叫什么") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "打开菜单")
                        }
                    }
                )
            },
        ) {innerPadding ->
            ClockGUI(
                modifier = Modifier.padding(innerPadding),
                vm = vm
            ) {
                needPhoto = true
            }
            RequestCameraPermission(
                modifier = Modifier.padding(innerPadding),
                shouldTakePhoto = needPhoto,
                onCaptureTriggered = { needPhoto = false }
            )
        }
    }
}

@Composable
fun AppDrawer(drawerState:DrawerState, scope: CoroutineScope) {
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
                onClick = { scope.launch {  } }){
                Icon(imageVector = Icons.Default.Settings, contentDescription = null)
            }
            //Text("      ")
            //Text("设置")
        }
    }
}