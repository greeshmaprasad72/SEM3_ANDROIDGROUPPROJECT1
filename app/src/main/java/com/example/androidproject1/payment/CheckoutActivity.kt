package com.example.androidproject1.payment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject1.databinding.ActivityCheckoutBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.Serializable
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    private var totalAmount = 0.0
    private var subTotal = 0.0
    private var deliveryFee = 0.0
    private var taxAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from CartActivity
        totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        subTotal = intent.getDoubleExtra("SUB_TOTAL", 0.0)
        deliveryFee = intent.getDoubleExtra("DELIVERY_FEE", 0.0)
        taxAmount = intent.getDoubleExtra("TAX", 0.0)

        setupViews()
        setupClickListeners()
        validatePostalCode()
    }

    private fun setupViews() {
        binding.imageviewBackArrow.setOnClickListener { finish() }

        // Pre-fill user email if available
        FirebaseAuth.getInstance().currentUser?.email?.let { email ->
            binding.edittextEmail.setText(email)
        }
    }

    private fun setupClickListeners() {
        binding.buttonProceedToPayment.setOnClickListener {
            if (validateForm()) {
                proceedToPayment()
            }
        }
    }

    private fun validateForm(): Boolean {
        with(binding) {
            if (edittextFirstName.text.isNullOrEmpty()) {
                edittextFirstName.error = "First name required"
                return false
            }
            if (edittextLastName.text.isNullOrEmpty()) {
                edittextLastName.error = "Last name required"
                return false
            }
            if (edittextEmail.text.isNullOrEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(edittextEmail.text.toString()).matches()) {
                edittextEmail.error = "Valid email required"
                return false
            }
            if (edittextAddress.text.isNullOrEmpty()) {
                edittextAddress.error = "Address required"
                return false
            }
            if (edittextPostalCode.text.isNullOrEmpty()) {
                edittextPostalCode.error = "Zip code required"
                return false
            }
            if(!isValidCanadianPostalCode(edittextPostalCode.text.toString())){
                edittextPostalCode.error = "Invalid postal code"
                return false
            }
            if (edittextCity.text.isNullOrEmpty()) {
                edittextCity.error = "City required"
                return false
            }

        }
        return true
    }

    private fun proceedToPayment() {
        val billingDetails = BillingDetails(
            firstName = binding.edittextFirstName.text.toString(),
            lastName = binding.edittextLastName.text.toString(),
            email = binding.edittextEmail.text.toString(),
            address = binding.edittextAddress.text.toString(),
            zipCode = binding.edittextAddress.text.toString(),
            city = binding.edittextCity.text.toString(),
            country = binding.edittextCountry.text.toString(),
            totalAmount = totalAmount
        )

        val intent = Intent(this, PaymentActivity::class.java).apply {
            putExtra("BILLING_DETAILS", billingDetails)
        }
        startActivity(intent)
    }
    private fun isValidCanadianPostalCode(postalcode: String): Boolean {
        val pattern = "^[A-Z][0-9][A-Z]\\s[0-9][A-Z][0-9]$"
        return postalcode.matches(pattern.toRegex())
    }
    private fun validatePostalCode() {
        with(binding) {
            edittextPostalCode.addTextChangedListener(object : TextWatcher {
                private var isUpdating = false
                private var previousText = ""

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    previousText = s.toString()
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(editable: Editable) {
                    if (isUpdating) return

                    var input = editable.toString().replace("[^A-Za-z0-9]".toRegex(), "")
                    if (input.length > 6) {
                        input = input.substring(0, 6)
                    }

                    isUpdating = true
                    if (input.isEmpty() || input.length < previousText.replace(
                            "[^A-Za-z0-9]".toRegex(),
                            ""
                        ).length
                    ) {
                        edittextPostalCode.setText(input.uppercase(Locale.getDefault()))
                        edittextPostalCode.setSelection(input.length)
                    } else if (input.length > 3) {
                        val formatted = input.substring(0, 3) + " " + input.substring(3)
                        edittextPostalCode.setText(formatted.uppercase(Locale.getDefault()))
                        edittextPostalCode.setSelection(formatted.length)
                    } else {
                        edittextPostalCode.setText(input.uppercase(Locale.getDefault()))
                        edittextPostalCode.setSelection(input.length)
                    }

                    isUpdating = false
                }
            })
        }
    }

    data class BillingDetails(
        val firstName: String = "",
        val lastName: String = "",
        val email: String ="",
        val address: String = "",
        val city: String = "",
        val zipCode: String = "",
        val country: String = "",
        val totalAmount: Double = 0.0
    ) : Serializable
}