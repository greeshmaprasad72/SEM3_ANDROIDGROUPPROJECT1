package com.example.androidproject1.payment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject1.R
import com.example.androidproject1.databinding.ActivityCardDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.NumberFormat
import java.util.*

class CardDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var billingDetails: CheckoutActivity.BillingDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("CardDetailsActivity", "Activity started")

        try {
            billingDetails = intent.getSerializableExtra("BILLING_DETAILS") as? CheckoutActivity.BillingDetails
                ?: throw IllegalStateException("Billing details not provided")

            Log.d("CardDetailsActivity", "Received billing details: $billingDetails")
            setupViews()
            setupClickListeners()
            setupTextWatchers()

        } catch (e: Exception) {
            Log.e("CardDetailsActivity", "Error initializing activity: ${e.message}")
            Toast.makeText(this, "Error loading payment page", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupViews() {
        binding.imageviewBackArrow.setOnClickListener {
            Log.d("CardDetailsActivity", "Back button clicked")
            finish()
        }

        // Display total amount
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.textviewLabel.text = "Pay ${currencyFormat.format(billingDetails.totalAmount)}"
    }

    private fun setupClickListeners() {
        binding.buttonConfirmPayment.setOnClickListener {
            Log.d("CardDetailsActivity", "Confirm payment clicked")
            if (validateCardDetails()) {
                processPayment()
            }
        }
    }

    private fun setupTextWatchers() {
        binding.edittextCardNumber.addTextChangedListener(createCardNumberTextWatcher())
        binding.edittextExpiryDate.addTextChangedListener(createExpiryDateTextWatcher())
    }

    private fun createCardNumberTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val cleanString = it.toString().replace(" ", "")
                    if (cleanString.isNotEmpty() && cleanString.length % 4 == 0 && it.length <= 19) {
                        val formatted = StringBuilder()
                        for (i in cleanString.indices) {
                            if (i % 4 == 0 && i != 0) formatted.append(" ")
                            formatted.append(cleanString[i])
                        }
                        binding.edittextCardNumber.removeTextChangedListener(this)
                        binding.edittextCardNumber.setText(formatted.toString())
                        binding.edittextCardNumber.setSelection(formatted.length)
                        binding.edittextCardNumber.addTextChangedListener(this)
                    }
                }
            }
        }
    }

    private fun createExpiryDateTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length == 2 && !it.toString().contains("/")) {
                        binding.edittextExpiryDate.removeTextChangedListener(this)
                        binding.edittextExpiryDate.setText("$it/")
                        binding.edittextExpiryDate.setSelection(3)
                        binding.edittextExpiryDate.addTextChangedListener(this)
                    }
                }
            }
        }
    }

    private fun validateCardDetails(): Boolean {
        with(binding) {
            // Card Name Validation
            if (edittextCardName.text.isNullOrBlank()) {
                edittextCardName.error = "Card holder name required"
                edittextCardName.requestFocus()
                return false
            }

            // Card Number Validation (16 digits)
            val cardNumber = edittextCardNumber.text.toString().replace(" ", "")
            if (cardNumber.length != 16 || !cardNumber.matches("\\d+".toRegex())) {
                edittextCardNumber.error = "Valid 16-digit card number required"
                edittextCardNumber.requestFocus()
                return false
            }

            // Expiry Date Validation (MM/YY format)
            val expiryDate = edittextExpiryDate.text.toString()
            if (!isValidExpiryDate(expiryDate)) {
                edittextExpiryDate.error = "Valid MM/YY required"
                edittextExpiryDate.requestFocus()
                return false
            }

            // CVV Validation (3-4 digits)
            val cvv = edittextCvv.text.toString()
            if (cvv.length !in 3..4 || !cvv.matches("\\d+".toRegex())) {
                edittextCvv.error = "Valid CVV required"
                edittextCvv.requestFocus()
                return false
            }

            return true
        }
    }

    private fun isValidExpiryDate(expiryDate: String): Boolean {
        if (!expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}".toRegex())) {
            return false
        }

        val parts = expiryDate.split("/")
        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        return year > currentYear || (year == currentYear && month >= currentMonth)
    }

    private fun processPayment() {
        Log.d("CardDetailsActivity", "Processing payment...")

        // Generate order details first
        val orderNumber = generateOrderNumber()
        val orderTotal = billingDetails.totalAmount

        binding.buttonConfirmPayment.isEnabled = false
        binding.buttonConfirmPayment.text = getString(R.string.processing)

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                Log.d("CardDetailsActivity", "Saving order to database...")
                saveOrderToDatabase(orderNumber, orderTotal)

                Log.d("CardDetailsActivity", "Navigating to confirmation...")
                navigateToConfirmation(orderNumber, orderTotal)

            } catch (e: Exception) {
                Log.e("CardDetailsActivity", "Payment processing failed", e)
                binding.buttonConfirmPayment.isEnabled = true
                binding.buttonConfirmPayment.text = getString(R.string.confirm_payment)
                Toast.makeText(this, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }, 2000)
    }

    private fun saveOrderToDatabase(orderNumber: String, orderTotal: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")

        val database = FirebaseDatabase.getInstance()
        val orderRef = database.getReference("orders").child(userId).push()

        val orderData = hashMapOf(
            "orderNumber" to orderNumber,
            "totalAmount" to orderTotal,
            "status" to "completed",
            "paymentMethod" to "card",
            "timestamp" to System.currentTimeMillis(),
            "customerName" to "${billingDetails.firstName} ${billingDetails.lastName}",
            "deliveryAddress" to billingDetails.address,
            "city" to billingDetails.city,
            "zipCode" to billingDetails.zipCode,
            "country" to billingDetails.country,
            "cardLast4" to binding.edittextCardNumber.text.toString().takeLast(4)
        )

        orderRef.setValue(orderData).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                throw task.exception ?: Exception("Failed to save order to database")
            }
            Log.d("CardDetailsActivity", "Order saved successfully: $orderData")
        }
    }

    private fun navigateToConfirmation(orderNumber: String, orderTotal: Double) {
        try {
            Log.d("CardDetailsActivity", "Creating confirmation intent...")
            val intent = Intent(this, OrderConfirmationActivity::class.java).apply {
                putExtra("BILLING_DETAILS", billingDetails)
                putExtra("ORDER_NUMBER", orderNumber)
                putExtra("ORDER_TOTAL", orderTotal)
                putExtra("PAYMENT_METHOD", "card")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            Log.d("CardDetailsActivity", "Starting confirmation activity...")
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e("CardDetailsActivity", "Navigation failed", e)
            throw e
        }
    }

    private fun generateOrderNumber(): String {
        return "ORD-${System.currentTimeMillis()}"
    }
}