package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ServiceItem
import com.example.data.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    val allServices: StateFlow<List<ServiceItem>>
    val allTransactions: StateFlow<List<TransactionItem>>

    // Filters and Queries
    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow("ALL") // ALL, PAID, DUE, PARTIAL
    val selectedServiceType = MutableStateFlow<ServiceItem?>(null)

    // UI Active States
    val activeMemoTransaction = MutableStateFlow<TransactionItem?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)

        allServices = repository.allServices
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        allTransactions = repository.allTransactions
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Populate default services asynchronously if empty
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDefaultServices()
        }
    }

    // Filtered transaction list based on search and status
    val filteredTransactions: StateFlow<List<TransactionItem>> = combine(
        allTransactions,
        searchQuery,
        filterStatus
    ) { transactions, query, status ->
        transactions.filter { transaction ->
            val matchesQuery = query.isEmpty() ||
                    transaction.citizenName.contains(query, ignoreCase = true) ||
                    transaction.citizenPhone.contains(query) ||
                    transaction.serviceName.contains(query, ignoreCase = true)

            val matchesStatus = when (status) {
                "ALL" -> true
                "PAID" -> transaction.dueAmount <= 0.0
                "DUE" -> transaction.dueAmount > 0.0 && transaction.paidAmount == 0.0
                "PARTIAL" -> transaction.dueAmount > 0.0 && transaction.paidAmount > 0.0
                else -> true
            }

            matchesQuery && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Statistics Calculations for Dashboard
    val stats: StateFlow<StatsSummary> = allTransactions.map { transactions ->
        val todayStart = getStartOfToday()
        val todayTx = transactions.filter { it.timestamp >= todayStart }

        val totalCollected = transactions.sumOf { it.paidAmount }
        val totalDue = transactions.sumOf { it.dueAmount }
        val totalGovtFee = transactions.sumOf { it.govtFee }
        val totalEntrepreneurFee = transactions.sumOf { it.entrepreneurFee }

        val todayCollected = todayTx.sumOf { it.paidAmount }
        val todayDue = todayTx.sumOf { it.dueAmount }
        val todayCount = todayTx.size
        val totalCount = transactions.size

        StatsSummary(
            totalCollected = totalCollected,
            totalDue = totalDue,
            totalGovtFee = totalGovtFee,
            totalEntrepreneurFee = totalEntrepreneurFee,
            todayCollected = todayCollected,
            todayDue = todayDue,
            todayCount = todayCount,
            totalCount = totalCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsSummary()
    )

    fun addTransaction(
        citizenName: String,
        citizenPhone: String,
        serviceName: String,
        govtFee: Double,
        entrepreneurFee: Double,
        paidAmount: Double,
        note: String
    ) {
        val total = govtFee + entrepreneurFee
        val due = total - paidAmount

        viewModelScope.launch(Dispatchers.IO) {
            val transaction = TransactionItem(
                citizenName = citizenName.trim(),
                citizenPhone = citizenPhone.trim(),
                serviceName = serviceName,
                govtFee = govtFee,
                entrepreneurFee = entrepreneurFee,
                paidAmount = paidAmount,
                dueAmount = due,
                note = note.trim()
            )
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction.id)
        }
    }

    fun addService(name: String, govtFee: Double, entrepreneurFee: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val service = ServiceItem(
                name = name.trim(),
                govtFee = govtFee,
                entrepreneurFee = entrepreneurFee,
                isDefault = false
            )
            repository.insertService(service)
        }
    }

    fun deleteService(service: ServiceItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteService(service.id)
        }
    }

    private fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

data class StatsSummary(
    val totalCollected: Double = 0.0,
    val totalDue: Double = 0.0,
    val totalGovtFee: Double = 0.0,
    val totalEntrepreneurFee: Double = 0.0,
    val todayCollected: Double = 0.0,
    val todayDue: Double = 0.0,
    val todayCount: Int = 0,
    val totalCount: Int = 0
)
