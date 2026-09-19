package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.AppRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TailoringApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    val repository: AppRepository by lazy {
        AppRepository(
            orderDao = database.orderDao(),
            categoryDao = database.categoryDao(),
            cutterDao = database.cutterDao(),
            tailorDao = database.tailorDao(),
            cutterPriceDao = database.cutterPriceDao(),
            tailorPriceDao = database.tailorPriceDao(),
            userDao = database.userDao(),
            userPermissionsDao = database.userPermissionsDao(),
            cutterReportExpenseDao = database.cutterReportExpenseDao(),
            tailorReportExpenseDao = database.tailorReportExpenseDao()
        )
    }

    val sessionManager: SessionManager by lazy {
        SessionManager(
            context = this,
            repository = repository,
            scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        )
    }
}
