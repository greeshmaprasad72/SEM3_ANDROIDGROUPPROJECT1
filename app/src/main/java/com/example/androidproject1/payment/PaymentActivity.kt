package com.example.androidproject1.payment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.androidproject1.R
import com.example.androidproject1.databinding.ActivityPaymentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private var billingDetails: CheckoutActivity.BillingDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("PaymentActivity", "onCreate called")

        // Get billing details from CheckoutActivity with null check
        try {
            billingDetails = intent.getSerializableExtra("BILLING_DETAILS") as? CheckoutActivity.BillingDetails
            Log.d("PaymentActivity", "Billing details received: $billingDetails")

            if (billingDetails == null) {
                Log.e("PaymentActivity", "Billing details is null!")
                Toast.makeText(this, "Error: Missing billing details", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Log.e("PaymentActivity", "Error getting billing details: ${e.message}")
            Toast.makeText(this, "Error loading payment page", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        binding.imageviewBackArrow.setOnClickListener {
            Toast.makeText(this, "Back clicked", Toast.LENGTH_SHORT).show()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.clPaymentCard.setOnClickListener {
            Log.d("PaymentActivity", "Card payment clicked")
            navigateToCardPayment()
        }

        binding.clPaymentPaypal.setOnClickListener {
            Log.d("PaymentActivity", "PayPal payment clicked")
            Toast.makeText(this, "PayPal payment selected", Toast.LENGTH_SHORT).show()
            navigateToOrderConfirmation("paypal")
        }

        binding.clPaymentCash.setOnClickListener {
            Log.d("PaymentActivity", "Cash payment clicked")
            Toast.makeText(this, "Cash on delivery selected", Toast.LENGTH_SHORT).show()
            navigateToOrderConfirmation("cash")
        }
    }

    private fun navigateToCardPayment() {
        billingDetails?.let { details ->
            Log.d("PaymentActivity", "Navigating to card payment with details: $details")
            val intent = Intent(this, CardDetailsActivity::class.java).apply {
                putExtra("BILLING_DETAILS", details)
            }
            startActivityForResult(intent, CARD_PAYMENT_REQUEST_CODE)
        } ?: run {
            Log.e("PaymentActivity", "Cannot navigate to card payment - billing details is null")
            Toast.makeText(this, "Error: Missing billing details", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CARD_PAYMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Card payment was successful, finish this activity
            finish()
        }
    }

    private fun navigateToOrderConfirmation(paymentMethod: String) {
        billingDetails?.let { details ->
            Log.d("PaymentActivity", "Navigating to order confirmation with method: $paymentMethod")

            // Create order in database for non-card payments
            createOrderInDatabase(paymentMethod)

            val orderNumber = generateOrderNumber()
            Log.d("PaymentActivity", "Generated order number: $orderNumber")

            try {
                val intent = Intent(this, OrderConfirmationActivity::class.java).apply {
                    putExtra("BILLING_DETAILS", details)
                    putExtra("ORDER_NUMBER", orderNumber)
                    putExtra("ORDER_TOTAL", details.totalAmount)
                    putExtra("PAYMENT_METHOD", paymentMethod)
                    // Use proper flags
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish() // Just finish this activity
            } catch (e: Exception) {
                Log.e("PaymentActivity", "Error navigating to order confirmation: ${e.message}")
                Toast.makeText(this, "Error completing payment", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Log.e("PaymentActivity", "Cannot navigate to order confirmation - billing details is null")
            Toast.makeText(this, "Error: Missing billing details", Toast.LENGTH_LONG).show()
        }
    }

    private fun createOrderInDatabase(paymentMethod: String) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.let { user ->
                val database = FirebaseDatabase.getInstance()
                val orderRef = database.getReference("orders").child(user.uid).push()

                val orderData = hashMapOf(
                    "orderNumber" to generateOrderNumber(),
                    "totalAmount" to billingDetails?.totalAmount,
                    "status" to if (paymentMethod == "cash") "pending" else "completed",
                    "paymentMethod" to paymentMethod,
                    "timestamp" to System.currentTimeMillis(),
                    "customerName" to "${billingDetails?.firstName} ${billingDetails?.lastName}",
                    "deliveryAddress" to billingDetails?.address
                )

                orderRef.setValue(orderData)
                Log.d("PaymentActivity", "Order saved to database: $orderData")
            }
        } catch (e: Exception) {
            Log.e("PaymentActivity", "Error saving order to database: ${e.message}")
        }
    }

    private fun generateOrderNumber(): String {
        return "ORD-${System.currentTimeMillis()}"
    }

    companion object {
        private const val CARD_PAYMENT_REQUEST_CODE = 1001
    }
}