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





class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.etEmail)
        val passInput = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passInput.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                // 1. AVISAR QUE ESTÁ A TENTAR
                Toast.makeText(this, "A conectar ao servidor...", Toast.LENGTH_SHORT).show()

                // 2. Desativar botão para não clicar 2 vezes
                btnLogin.isEnabled = false
                btnLogin.text = "A carregar..."

                loginUser(email, password)
            } else {
                Toast.makeText(this, "Preenche os campos todos!", Toast.LENGTH_SHORT).show()
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, pass: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.login(AuthRequest(email, pass))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        val userId = response.body()?.userId
                        editor.putString("USER_EMAIL", email)
                        editor.putBoolean("IS_LOGGED_IN", true)
                        editor.putString("USER_ID", userId) //guarda o USER_ID
                        editor.apply()
                        Toast.makeText(this@LoginActivity, "Login Success!", Toast.LENGTH_SHORT).show()

                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}