package com.talhaatif.financeapk

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.talhaatif.financeapk.databinding.FragmentHomeBinding
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.models.BudgetModel
import com.talhaatif.financeapk.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var pieChart: PieChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        pieChart = binding.pieChart

        setupObservers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(checkAllParameters()){

            binding.fab.setOnClickListener {

                val intent = Intent(requireActivity(), AddTransactions::class.java)
                startActivity(intent)

            }
        }
    }

    private fun checkAllParameters(): Boolean {
        return isAdded && context != null
    }

    private fun setupObservers() {
        homeViewModel.budgetData.observe(viewLifecycleOwner) { budget ->
            updateUI(budget)
            setupPieChart(budget)
        }

        homeViewModel.currencyType.observe(viewLifecycleOwner) { currency ->
            updateCurrencySymbol(currency)
        }
    }

    private fun updateUI(budget: BudgetModel?) {
        if (budget == null) return

        binding.tvBalanceAmount.text = budget.balance.toString()
        binding.tvIncomeAmount.text =  budget.income.toString()
        binding.tvExpenseAmount.text = budget.expense.toString()
    }

    private fun setupPieChart(budget: BudgetModel?) {
        if (budget == null) return

        // Create entries for the pie chart
        val entries = mutableListOf<PieEntry>()
        if (budget.income > 0) entries.add(PieEntry(budget.income.toFloat(), "Income"))
        if (budget.expense > 0) entries.add(PieEntry(budget.expense.toFloat(), "Expense"))

        // Configure the dataset
        val dataSet = PieDataSet(entries, "Budget Breakdown").apply {
            colors = listOf(Color.parseColor("#6A66FF"), Color.parseColor("#554FF6")) // Updated colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE // Place values inside slices
            xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE // Place labels outside slices
            valueLinePart1Length = 0.5f // Length of the first part of the line connecting the label to the slice
            valueLinePart2Length = 0.4f // Length of the second part of the line
            valueLineColor = Color.WHITE // Color of the line connecting the label to the slice
            sliceSpace = 3f // Add space between slices for better visibility
        }

        // Configure the pie data
        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart)) // Display values as percentages
        }

        // Configure the pie chart
        pieChart.apply {
            data = pieData
            description.isEnabled = false // Disable description
            setUsePercentValues(true) // Display values as percentages
            isDrawHoleEnabled = true // Enable the hole in the center
            holeRadius = 50f // Adjust the size of the hole
            transparentCircleRadius = 55f // Add a transparent circle around the hole
            setHoleColor(Color.BLACK) // Match the theme background
            setEntryLabelColor(Color.WHITE) // Label text color
            setEntryLabelTextSize(12f) // Increase label text size
            setEntryLabelTypeface(Typeface.DEFAULT_BOLD) // Bold labels
            legend.textColor = Color.WHITE // Legend text color
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM // Position legend at the bottom
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // Center the legend
            legend.orientation = Legend.LegendOrientation.HORIZONTAL // Display legend horizontally
            legend.yEntrySpace = 10f // Add vertical spacing between legend entries
            legend.xEntrySpace = 20f // Add horizontal spacing between legend entries
            legend.formSize = 12f // Adjust the size of the legend icons
            animateY(1000, Easing.EaseInOutQuad) // Add animation
            invalidate() // Refresh the chart
        }
    }

    private fun updateCurrencySymbol(currency: String) {
        binding.tvBalanceAmount.text = "$currency ${binding.tvBalanceAmount.text}"
        binding.tvIncomeAmount.text = "$currency ${binding.tvIncomeAmount.text}"
        binding.tvExpenseAmount.text = "$currency ${binding.tvExpenseAmount.text}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
