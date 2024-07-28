package com.talhaatif.financeapk

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.talhaatif.financeapk.adapters.TransactionsAdapter
import com.talhaatif.financeapk.databinding.FragmentHomeBinding
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.models.Transaction

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val db = FirebaseFirestore.getInstance()
    private val utils = Util()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Initializing Account !!!")

        val userId = utils.getLocalData(requireContext(), "uid")

        binding.fab.setOnClickListener {

            val intent = Intent(requireActivity(), AddTransactions::class.java)
            startActivity(intent)

        }
        updateBudgetViews()

        db.collection("transactions")
            .whereEqualTo("uid", userId)
            .get()
            .addOnSuccessListener { result ->
                val transactions = mutableListOf<Transaction>()
                for (document in result) {
                    transactions.add(document.toObject(Transaction::class.java))
                }
                setupRecyclerView(transactions)
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    private fun setupRecyclerView(transactions: List<Transaction>) {
        binding.transactions.layoutManager = LinearLayoutManager(requireContext())
        binding.transactions.adapter = TransactionsAdapter(transactions)
    }
    @SuppressLint("SetTextI18n")
    private fun updateBudgetViews() {
        progressDialog.show()

        val userId = utils.getLocalData(requireContext(), "uid")

        val budgetRef = db.collection("budget").document(userId)

        budgetRef.get().addOnSuccessListener { document ->

            if (document != null) {
                val income = document.getDouble("income") ?: 0.0
                val expense = document.getDouble("expense") ?: 0.0
                val balance = document.getDouble("balance") ?: 0.0
                val currencyType = utils.getLocalData(requireContext(),"currency")

                binding.tvIncomeAmount.text = "${income} ${currencyType}"
                binding.tvExpenseAmount.text = "${expense} ${currencyType}"
                binding.tvBalanceAmount.text = "${balance} ${currencyType}"
                progressDialog.dismiss()

            } else {
                Toast.makeText(requireContext(), "No budget data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(requireContext(), "Error getting budget data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

}
