@file:Suppress("DEPRECATION")

package com.talhaatif.financeapk

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.talhaatif.financeapk.databinding.ActivityLoginBinding
import com.talhaatif.financeapk.firebase.Util
import com.talhaatif.financeapk.firebase.Variables.Companion.auth
import com.talhaatif.financeapk.firebase.Variables.Companion.displayErrorMessage
import com.talhaatif.financeapk.firebase.Variables.Companion.isEmailValid
import com.talhaatif.financeapk.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")

        binding.login.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            progressDialog.show()
            viewModel.login(email, password, this)
        }

        viewModel.authState.observe(this, Observer {
            progressDialog.dismiss()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        })

        binding.signup.setOnClickListener{
            startActivity(Intent(this, SignUpScreen::class.java))
        }
    }
}
