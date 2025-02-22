package com.talhaatif.financeapk

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.talhaatif.financeapk.databinding.ActivityAddTransactionsBinding
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.viewmodel.AddTransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddTransactions : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionsBinding
    private lateinit var viewModel: AddTransactionViewModel
    private val utils = Util()
    private var selectedDate = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Date picker logic
        binding.btnDatePicker.setOnClickListener {
            showDatePicker()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> { // Expense Tab
                        binding.tvAmount.text = "-$"
                        // Change text color of TextView and EditText to red
                        binding.tvAmount.setTextColor(ContextCompat.getColor(this@AddTransactions, R.color.red))
                        binding.etAmount.setTextColor(ContextCompat.getColor(this@AddTransactions, R.color.red))
                        binding.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@AddTransactions, R.color.red))
                        binding.tabLayout.setTabTextColors(
                            ContextCompat.getColor(this@AddTransactions, R.color.white), // Unselected color
                            ContextCompat.getColor(this@AddTransactions, R.color.red) // Selected color
                        )
                    }
                    1 -> { // Income Tab
                        // Change text color of TextView and EditText to green
                        binding.tvAmount.setTextColor(ContextCompat.getColor(this@AddTransactions, R.color.green))
                        binding.etAmount.setTextColor(ContextCompat.getColor(this@AddTransactions, R.color.green))
                        binding.tvAmount.text = "+$"
                        binding.tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this@AddTransactions, R.color.green))
                        binding.tabLayout.setTabTextColors(
                            ContextCompat.getColor(this@AddTransactions, R.color.white), // Unselected color
                            ContextCompat.getColor(this@AddTransactions, R.color.green) // Selected color
                        )
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        viewModel = ViewModelProvider(this).get(AddTransactionViewModel::class.java)

        binding.btnAddRecord.setOnClickListener {
            val currencyType = utils.getLocalData(this, "currency")
            val amount = binding.etAmount.text.toString().trim() + " " + currencyType
            val notes = binding.etNotes.text.toString().trim()
            val transactionType = if (binding.tabLayout.selectedTabPosition == 0) "Expense" else "Income"

            if (amount.isEmpty() || selectedDate.isEmpty() || transactionType.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (transactionType.isEmpty()) {
                Toast.makeText(this, "Transaction Type Not Selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addTransaction(this, amount, transactionType, selectedDate, notes)
        }

        // Observe transaction status
        viewModel.transactionState.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            result.onFailure { error ->
                Toast.makeText(this, "Failed to add transaction: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Default selection to today
                .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
            }

            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            binding.tvDate.text = selectedDate
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

}
