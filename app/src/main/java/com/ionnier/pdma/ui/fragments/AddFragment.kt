package com.ionnier.pdma.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.ionnier.pdma.ui.colors.MyApplicationTheme

class AddFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply { 
        setContent {
            BackHandler {
                findNavController().navigate(com.ionnier.pdma.R.id.openMain)
            }
            MyApplicationTheme {
                Text(text = "Hey!")
            }
        }
    }
}