package com.example.statement_detect.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.statement_detect.timer.TimerViewModel
import com.example.statement_detect.ui.theme.VarelaroundFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(vm: TimerViewModel,navController: NavController){
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(56.dp),
                drawerContainerColor = Color(0xFF222846),
                drawerContentColor = Color.White,
            ){
                AppDrawer(drawerState, scope , navController)
            }
        },
        drawerState = drawerState,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SETTINGS", fontFamily = VarelaroundFontFamily, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "打开菜单")
                        }
                    })
            }
        ) {innerpadding ->
            SettingsGUI(vm = vm, modifier = Modifier.padding(innerpadding))
        }

    }
}