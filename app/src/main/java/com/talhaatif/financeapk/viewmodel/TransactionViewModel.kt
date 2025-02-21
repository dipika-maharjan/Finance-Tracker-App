package com.talhaatif.financeapk.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.models.Transaction

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val utils = Util()

    private val _transactions = MutableLiveData<Map<String, List<Transaction>>>()
    val transactions: LiveData<Map<String, List<Transaction>>> get() = _transactions

    fun fetchTransactions() {
        val userId = utils.getLocalData(getApplication(), "uid") ?: return
        db.collection("transactions")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { result ->
                val transactionsList = mutableListOf<Transaction>()
                for (document in result) {
                    transactionsList.add(document.toObject(Transaction::class.java))
                }
                _transactions.value = groupTransactionsByDate(transactionsList)
            }
            .addOnFailureListener {
                _transactions.value = emptyMap()
            }
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
        return transactions.groupBy { it.transDate }
    }
}
