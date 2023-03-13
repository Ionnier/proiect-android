package com.ionnier.pdma.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ionnier.pdma.Settings
import com.ionnier.pdma.ui.colors.MyApplicationTheme

@Composable
fun DrawSettingsScreen(
    modifier: Modifier = Modifier,
) {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = modifier.padding(horizontal = 16.dp),
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val dynamicColors = rememberSaveable {
                        mutableStateOf(Settings.dynamic_colors)
                    }
                    Text(
                        text = "Enable dynamic colors",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = dynamicColors.value,
                        onCheckedChange = {
                            Settings.dynamic_colors = !Settings.dynamic_colors
                            dynamicColors.value = Settings.dynamic_colors
                        }
                    )

                }

            }
        }
    }
}