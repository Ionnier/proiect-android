package com.ionnier.pdma.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.recreate
import com.ionnier.pdma.Settings
import com.ionnier.pdma.ui.colors.MyApplicationTheme

@Composable
fun DrawSettingsScreen(
    modifier: Modifier = Modifier,
    recreate: () -> Unit,
    scheduleReminder: () -> Unit,
    setCalories: () -> Unit
) {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Set calories",
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = setCalories) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null
                        )

                    }
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val randomColors = rememberSaveable {
                        mutableStateOf(Settings.random_colors)
                    }
                    Text(
                        text = "Enable random colors",
                        modifier = Modifier.weight(1f),
                        color = Color.White
                    )
                    Switch(
                        checked = randomColors.value,
                        onCheckedChange = {
                            Settings.random_colors = !Settings.random_colors
                            randomColors.value = Settings.random_colors
                            recreate()
                        }
                    )
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Schedule reminder",
                        modifier = Modifier.weight(1f),
                        color = Color.White
                    )
                    IconButton(onClick = scheduleReminder) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null
                        )

                    }
                }

            }
        }
    }
}