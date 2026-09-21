package com.example

import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.example.ui.viewmodel.DevicesViewModel
import com.example.ui.viewmodel.MessagingViewModel
import com.example.ui.viewmodel.OrdersViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.TailorsViewModel
import com.example.ui.viewmodel.UsersViewModel

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
    private val usersViewModel: UsersViewModel by viewModels { appViewModelFactory }
    private val devicesViewModel: DevicesViewModel by viewModels { appViewModelFactory }
    private val messagingViewModel: MessagingViewModel by viewModels { appViewModelFactory }
    private val settingsViewModel: SettingsViewModel by viewModels { appViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register current device on app startup if not already registered
        val currentDeviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            ?: "device_${Build.BOARD}"
        val currentDeviceModel = Build.MODEL ?: "Android Device"
        devicesViewModel.registerCurrentDeviceIfNeeded(currentDeviceId, currentDeviceModel)

        setContent {
            MyApplicationTheme {
                MainAppContainer(
                    authViewModel = authViewModel,
                    ordersViewModel = ordersViewModel,
                    categoriesViewModel = categoriesViewModel,
                    cuttersViewModel = cuttersViewModel,
                    tailorsViewModel = tailorsViewModel,
                    usersViewModel = usersViewModel,
                    devicesViewModel = devicesViewModel,
                    messagingViewModel = messagingViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
