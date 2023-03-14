package com.ionnier.pdma.ui.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.ionnier.pdma.IntentReceiver
import com.ionnier.pdma.ui.colors.MyApplicationTheme
import com.ionnier.pdma.ui.screens.DrawSettingsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*


private const val HOME_ROUTE = "Home"
private const val TRACK_ROUTE = "Track"
private const val LOG_ROUTE = "History"
private const val SETTINGS_ROUTE = "Settings"

class MainFragment : Fragment() {
    private val mainViewModel: MainViewModel by viewModels()

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
                    if (mainViewModel.showDialog.collectAsState().value) {
                        Surface {
                            val dismiss = {
                                mainViewModel.showDialog.update {
                                    false
                                }
                            }
                            Dialog(
                                onDismissRequest = {
                                    dismiss()
                                }
                            ) {
                                val time = Calendar.getInstance().time

                                var hours =  remember {
                                    mutableStateOf(time.hours)
                                }
                                var minutes =  remember {
                                    mutableStateOf(time.minutes)
                                }
                                var seconds =  remember {
                                    mutableStateOf(time.seconds)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Choose time", modifier = Modifier.fillMaxWidth())
                                        Row {
                                            GenerateRow(value = hours)
                                            GenerateRow(value = minutes, minValue = 0, maxValue = 60)
                                            GenerateRow(value = seconds, minValue = 0, maxValue = 60)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    val alarmManager =
                                                        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                                                    // Set the alarm to start at 8:30 a.m.
                                                    val calendar: Calendar =
                                                        Calendar.getInstance().apply {
                                                            timeInMillis = System.currentTimeMillis()
                                                            set(Calendar.HOUR_OF_DAY, hours.value)
                                                            set(Calendar.MINUTE, minutes.value)
                                                            set(Calendar.SECOND, seconds.value)
                                                        }

                                                    val intent =
                                                        Intent(context, IntentReceiver::class.java)
                                                    intent.putExtra("myAction", "notify")
                                                    val pendingIntent =
                                                        PendingIntent.getBroadcast(
                                                            context,
                                                            0,
                                                            intent,
                                                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                                                        )

                                                    alarmManager?.let {
                                                        it.setRepeating(
                                                            AlarmManager.RTC_WAKEUP,
                                                            calendar.timeInMillis,
                                                            0,
                                                            pendingIntent
                                                        )
                                                    }
                                                    dismiss()
                                                }
                                            ) {
                                                Text(text = "Set reminder")
                                            }

                                        }
                                    }

                                }

                            }
                        }
                    }
                    ModalNavigationDrawer(
                        drawerContent = {
                            Column(
                                modifier = Modifier.alpha(
                                    if (drawerState.isOpen || drawerState.isAnimationRunning) 1.0f else 0.0f
                                )
                            ) {
                                val drawerItems = listOf(
                                    Pair(HOME_ROUTE, Icons.Outlined.Home),
                                    Pair(TRACK_ROUTE, Icons.Outlined.Lock),
                                    Pair(LOG_ROUTE, Icons.Outlined.List),
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
                                            Surface {
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
                        Surface(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column {
                                TopAppBar(
                                    title = {
                                        Text(currentSelectedRoute.value)
                                    },
                                    actions = {
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
                                    SETTINGS_ROUTE -> DrawSettingsScreen(
                                        recreate = {
                                            activity?.recreate()
                                        },
                                        scheduleReminder = {
                                            mainViewModel.showDialog.update {
                                                true
                                            }
                                        }
                                    )
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

    @Composable
    private fun GenerateRow(
        value: MutableState<Int>,
        maxValue: Int = 23,
        minValue: Int = 0,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = {
                    value.value += 1
                },
                enabled = value.value < maxValue
            ) {
                Icon (
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null
                )
            }
            Text(
                text = value.value.toString()
            )
            IconButton(
                onClick = {
                    value.value -= 1
                },
                enabled = value.value > minValue
            ) {
                Icon (
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null
                )
            }

        }
    }
}

class MainViewModel: ViewModel() {
    val showDialog =  MutableStateFlow(false)
}