package com.example.androidproject1.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject1.R
import com.example.androidproject1.databinding.ActivityOrderConfirmationBinding
import com.example.androidproject1.product.ProductActivity
import com.example.androidproject1.LoginActivity
import com.example.androidproject1.MainActivity
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.*

class OrderConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderConfirmationBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("OrderConfirmation", "Activity started")

        binding = ActivityOrderConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            val billingDetails = intent.getSerializableExtra("BILLING_DETAILS") as? CheckoutActivity.BillingDetails
                ?: throw IllegalStateException("Missing billing details")

            val orderNumber = intent.getStringExtra("ORDER_NUMBER")
                ?: throw IllegalStateException("Missing order number")

            val orderTotal = intent.getDoubleExtra("ORDER_TOTAL", -1.0).takeIf { it > 0 }
                ?: throw IllegalStateException("Invalid order total")

            val paymentMethod = intent.getStringExtra("PAYMENT_METHOD")
                ?: throw IllegalStateException("Missing payment method")

            Log.d("OrderConfirmation", "Valid data received - Order: $orderNumber, Total: $orderTotal")
            setupViews(billingDetails, orderNumber, orderTotal, paymentMethod)
            setupBackPressedCallback()

        } catch (e: Exception) {
            Log.e("OrderConfirmation", "Error in order data: ${e.message}")
            Toast.makeText(this, getString(R.string.error_invalid_order_info), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupViews(
        billingDetails: CheckoutActivity.BillingDetails,
        orderNumber: String,
        orderTotal: Double,
        paymentMethod: String
    ) {
        try {
            Log.d("OrderConfirmation", "Setting up views...")

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

            binding.tvOrderNumber.text = getString(R.string.order_number, orderNumber)
            binding.tvOrderTotal.text = currencyFormat.format(orderTotal)

            when (paymentMethod.trim().lowercase(Locale.ROOT)) {
                "cash" -> {
                    binding.tvPaymentMethod.text = getString(R.string.payment_method_cash)
                    binding.tvPaymentNote.text = getString(R.string.payment_note_cash)
                }
                "paypal" -> {
                    binding.tvPaymentMethod.text = getString(R.string.payment_method_paypal)
                    binding.tvPaymentNote.text = getString(R.string.payment_note_paypal)
                }
                "card" -> {
                    binding.tvPaymentMethod.text = getString(R.string.payment_method_card)
                    binding.tvPaymentNote.text = getString(R.string.payment_note_card)
                }
                else -> {
                    binding.tvPaymentMethod.text = "Payment Method: $paymentMethod"
                    binding.tvPaymentNote.text = "Payment processed successfully"
                }
            }

            binding.tvDeliveryAddress.text = getString(
                R.string.delivery_address_format,
                billingDetails.firstName,
                billingDetails.lastName,
                billingDetails.address,
                billingDetails.city,
                billingDetails.zipCode,
                billingDetails.country
            )

            binding.btnContinueShopping.setOnClickListener {
                Log.d("OrderConfirmation", "Continue shopping clicked")
                navigateToProducts()
            }

            Log.d("OrderConfirmation", "Views setup completed")

        } catch (e: Exception) {
            Log.e("OrderConfirmation", "Error setting up views: ${e.message}")
            Toast.makeText(this, getString(R.string.error_displaying_order_details), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("OrderConfirmation", "Back pressed - navigating to products")
                navigateToProducts()
            }
        })
    }

    private fun navigateToProducts() {
        try {
            Log.d("OrderConfirmation", "Navigating to products page...")

            val user = auth.currentUser
            if (user == null) {
                Log.w("OrderConfirmation", "User not logged in, redirecting to login")
                navigateToLogin()
                return
            }

            val intent = Intent(this, ProductActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("USER_ID", user.uid)
            }
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            Log.e("OrderConfirmation", "Navigation error", e)
            Toast.makeText(this, "Could not open products page", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }
}
