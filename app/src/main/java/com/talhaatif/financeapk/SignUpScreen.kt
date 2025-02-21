package com.talhaatif.financeapk

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.talhaatif.financeapk.databinding.ActivitySignUpScreenBinding
import com.talhaatif.financeapk.viewmodel.AuthViewModel
import java.io.ByteArrayOutputStream

class SignUpScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpScreenBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var progressDialog: ProgressDialog
    private var imgChange = true
    private lateinit var imageUri: Uri
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")

        binding.register.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val name = binding.name.text.toString()
            val currency = binding.currencySelector.text.toString()

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || currency.isEmpty()) {
                return@setOnClickListener
            }

            if (imgChange) {
                return@setOnClickListener
            }

            progressDialog.show()
            viewModel.signUp(this, email, password, name, currency, imageUri, bitmap)
        }

        binding.imageView.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, 1)
        }

        viewModel.authState.observe(this, Observer {
            progressDialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        })

        viewModel.errorMessage.observe(this, Observer { message ->
            progressDialog.dismiss()
        })



        binding.login.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.data!!
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            binding.imageView.setImageBitmap(bitmap)
            imgChange = false
        }
    }
}
