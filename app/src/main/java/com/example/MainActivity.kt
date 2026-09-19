package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppViewModelFactory
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.CuttersViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.TailorsViewModel

class MainActivity : ComponentActivity() {

    private val appViewModelFactory by lazy {
        val app = application as TailoringApp
        AppViewModelFactory(app.repository, app.sessionManager)
    }

    private val authViewModel: AuthViewModel by viewModels { appViewModelFactory }
    private val ordersViewModel: OrdersViewModel by viewModels { appViewModelFactory }
    private val categoriesViewModel: CategoriesViewModel by viewModels { appViewModelFactory }
    private val cuttersViewModel: CuttersViewModel by viewModels { appViewModelFactory }
    private val tailorsViewModel: TailorsViewModel by viewModels { appViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer(
                    authViewModel = authViewModel,
                    ordersViewModel = ordersViewModel,
                    categoriesViewModel = categoriesViewModel,
                    cuttersViewModel = cuttersViewModel,
                    tailorsViewModel = tailorsViewModel
                )
            }
        }
    }
}
