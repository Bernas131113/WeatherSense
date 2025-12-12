package pt.ipt.weathersense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipt.weathersense.models.AuthRequest
import pt.ipt.weathersense.network.RetrofitClient

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameInput = findViewById<EditText>(R.id.etUsername)
        val passInput = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passInput.text.toString()
            if(username.isNotEmpty() && password.isNotEmpty()) {
                registerUser(username, password)
            } else {
                Toast.makeText(this, "Por favor introduzir username e password", Toast.LENGTH_SHORT).show()
            }
        }
        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(username: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Chama a API de Registo
                val response = RetrofitClient.instance.register(AuthRequest(username, pass))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please Login.", Toast.LENGTH_LONG).show()
                        // Volta para a tela de Login após o sucesso
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration Failed. Email may already be in use.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}