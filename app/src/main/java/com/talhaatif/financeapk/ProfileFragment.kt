package com.talhaatif.financeapk


import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.talhaatif.financeapk.databinding.FragmentProfileBinding
import com.talhaatif.financeapk.viewmodel.ProfileViewModel

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
//    private lateinit var progressDialog: ProgressDialog
    private lateinit var profileViewModel: ProfileViewModel
    private var imgChange = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel = ViewModelProvider(
            this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ProfileViewModel::class.java]

//        progressDialog = ProgressDialog(requireContext()).apply {
//            setMessage("Fetching profile...")
//            show()
//        }


        profileViewModel.fetchUserProfile()
        observeViewModel()

        binding.update.setOnClickListener {
            val name = binding.name.text.toString()
            val currency = binding.currencySelector.text.toString()
            val bitmap = binding.imageView.drawable?.toBitmap()
//            progressDialog.setMessage("Updating...")
//            progressDialog.show()
            profileViewModel.updateUserProfile(name, currency, bitmap, imgChange)
        }
    }

    private fun observeViewModel() {
        profileViewModel.profileData.observe(viewLifecycleOwner, Observer { data ->
            binding.name.setText(data["name"])
            val currency = data["currency"] ?: ""
            val currencies = listOf("USD", "EUR", "PKR", "INR", "GBP").toMutableList()
            if (currencies.contains(currency)) currencies.remove(currency)
            currencies.add(0, currency)
            binding.currencySelector.setAdapter(
                ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, currencies)
            )
            Glide.with(this).load(data["image"]).into(binding.imageView)
        })

        profileViewModel.updateSuccess.observe(viewLifecycleOwner, Observer { success ->
//            progressDialog.dismiss()
            Toast.makeText(requireContext(), if (success) "Profile updated" else "Update failed", Toast.LENGTH_SHORT).show()
        })
    }
}
