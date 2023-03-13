package com.ionnier.pdma.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.ionnier.pdma.ui.colors.MyApplicationTheme
import com.ionnier.pdma.ui.screens.DrawSettingsScreen
import kotlinx.coroutines.launch

private const val HOME_ROUTE = "Home"
private const val SETTINGS_ROUTE = "Settings"

class MainFragment : Fragment() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MyApplicationTheme {

                    val currentSelectedRoute = rememberSaveable {
                        mutableStateOf(HOME_ROUTE)
                    }
                    val scope = rememberCoroutineScope()
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    fun closeDrawer() {
                        scope.launch {
                            drawerState.close()
                        }
                    }
                    BackHandler {
                        if (drawerState.isOpen) {
                            closeDrawer()
                        } else {
                            activity?.finishAndRemoveTask()
                        }
                    }
                    ModalNavigationDrawer(
                        drawerContent = {
                            Column {
                                val drawerItems = listOf(
                                    Pair(HOME_ROUTE, Icons.Outlined.Home),
                                    Pair(SETTINGS_ROUTE, Icons.Outlined.Settings)
                                )

                                TopAppBar(
                                    title = {},
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(0.0f)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            closeDrawer()
                                        },
                                )
                                for (item in drawerItems) {
                                    NavigationDrawerItem(
                                        label = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = item.second,
                                                    contentDescription = null,
                                                )
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Text(item.first)
                                            }
                                        },
                                        selected = currentSelectedRoute.value == item.first,
                                        onClick = {
                                            currentSelectedRoute.value = item.first
                                            closeDrawer()
                                        },
                                        shape = RectangleShape
                                    )

                                }
                            }
                        },
                        drawerState = drawerState,
                    ) {
                        Column {
                            TopAppBar(
                                title = {
                                    Text(currentSelectedRoute.value)
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                drawerState.open()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Menu,
                                            tint = Color.White,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            )
                            when (currentSelectedRoute.value) {
                                SETTINGS_ROUTE -> DrawSettingsScreen()
                                else -> {

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}