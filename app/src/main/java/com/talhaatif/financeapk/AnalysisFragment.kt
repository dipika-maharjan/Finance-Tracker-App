package com.talhaatif.financeapk

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
        setupDatePicker()
        fetchTransactionsAndSetupPieChart()
        return binding.root
    }

    private fun fetchTransactionsAndSetupPieChart() {
        val userId = Util().getLocalData(requireContext(), "uid")

        db.collection("budget")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { documents ->
                val entries = mutableListOf<PieEntry>()
                for (document in documents) {
                    val income = document.getDouble("income")?.toFloat() ?: 0f
                    val expense = document.getDouble("expense")?.toFloat() ?: 0f
                    if (income > 0) {
                        entries.add(PieEntry(income, "Income"))
                    }
                    if (expense > 0) {
                        entries.add(PieEntry(expense, "Expense"))
                    }
                }
                setupPieChart(entries)
            }
            .addOnFailureListener { exception ->
                Log.w("AnalysisFragment", "Error getting budget: ", exception)
            }
    }

    private fun setupPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "\nBudget Analysis")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

        val data = PieData(dataSet)
        data.setValueTextSize(16f)
        data.setValueTextColor(resources.getColor(android.R.color.black, null))

        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString() + "%"
            }

            override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                return "${value.toInt()}%"
            }
        })
        binding.pieChart.data = data
        binding.pieChart.setUsePercentValues(true)
        binding.pieChart.invalidate() // refresh
    }

    private fun setupDatePicker() {
        binding.datePickerLayout.setOnClickListener {
            val datePicker = DatePickerFragment { year, month, day ->

                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
                binding.tvSelectDate.text = selectedDate
            }
            datePicker.show(childFragmentManager, "datePicker")
        }
    }
}
