package com.talhaatif.financeapk

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.talhaatif.financeapk.databinding.FragmentAnalysisBinding
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.firebase.Variables.Companion.db
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AnalysisFragment : Fragment() {

    private lateinit var binding: FragmentAnalysisBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun checkAllParameters(): Boolean {
        return isAdded && context != null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (checkAllParameters()) {
            setupDatePicker()
            fetchTransactionsAndSetupPieChart()
        }
    }

    private fun fetchTransactionsAndSetupPieChart() {
        val userId = Util().getLocalData(requireContext(), "uid")

        db.collection("budget")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { documents: QuerySnapshot -> // Fix: Explicit type added
                val entries = mutableListOf<PieEntry>()
                for (document: DocumentSnapshot in documents) { // Fix: Explicit type added
                    Log.d("FirestoreData", "Document: ${document.data}")

                    val income: Float = when (val incomeValue = document.get("income")) {
                        is Number -> incomeValue.toFloat()
                        is String -> incomeValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }

                    val expense: Float = when (val expenseValue = document.get("expense")) {
                        is Number -> expenseValue.toFloat()
                        is String -> expenseValue.toFloatOrNull() ?: 0f
                        else -> 0f
                    }

                    Log.d("FirestoreParsed", "Income: $income, Expense: $expense")

                    if (income > 0) {
                        entries.add(PieEntry(income, "Income"))
                    }
                    if (expense > 0) {
                        entries.add(PieEntry(expense, "Expense"))
                    }
                }
                if (isAdded) {
                    setupPieChart(entries)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AnalysisFragment", "Error getting budget: ", exception)
            }
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        if (!isAdded) return
        val dataSet = PieDataSet(entries, "\nBudget Analysis")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        dataSet.valueTypeface = Typeface.DEFAULT_BOLD
        val data = PieData(dataSet)
        data.setValueTextSize(16f)
        data.setValueTextColor(resources.getColor(android.R.color.black, null))

        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "${value.toInt()}%"
            }

            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return "${value.toInt()}%"
            }
        })
        binding.pieChart.data = data
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.invalidate() // refresh chart
    }

    private fun setupDatePicker() {
        if (!isAdded) return
        binding.datePickerLayout.setOnClickListener {
            val datePicker = DatePickerFragment { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                binding.tvSelectDate.text = selectedDate

                fetchTransactionsAndSetupPieChart(selectedDate)
            }
            datePicker.show(childFragmentManager, "datePicker")
        }
    }

    private fun fetchTransactionsAndSetupPieChart(selectedDate: String) {
        val userId = Util().getLocalData(requireContext(), "uid")

        db.collection("transactions")
            .whereEqualTo("uid", userId)
            .whereEqualTo("transDate", selectedDate)
            .get()
            .addOnSuccessListener { documents: QuerySnapshot -> // Fix: Explicit type added
                val entries = mutableListOf<PieEntry>()
                var totalIncome = 0f
                var totalExpense = 0f
                for (document: DocumentSnapshot in documents) { // Fix: Explicit type added
                    val transType = document.getString("transType")
                    val transAmount = document.getString("transAmount")?.split(" ")?.first()?.toFloatOrNull() ?: 0f

                    if (transType == "Income") {
                        totalIncome += transAmount
                    } else if (transType == "Expense") {
                        totalExpense += transAmount
                    }
                }
                if (totalIncome > 0) {
                    entries.add(PieEntry(totalIncome, "Income"))
                }
                if (totalExpense > 0) {
                    entries.add(PieEntry(totalExpense, "Expense"))
                }
                setupPieChart(entries)
            }
            .addOnFailureListener { exception ->
                Log.e("AnalysisFragment", "Error getting transactions: ", exception)
            }
    }
}
