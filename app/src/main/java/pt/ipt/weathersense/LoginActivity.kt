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

        val usernameInput = findViewById<EditText>(R.id.etUsername)
        val passInput = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passInput.text.toString()

            // Validação simples dos campos
            if(username.isNotEmpty() && password.isNotEmpty()) {

                Toast.makeText(this, "A conectar ao servidor...", Toast.LENGTH_SHORT).show()

                // Validação simples dos campos
                btnLogin.isEnabled = false
                btnLogin.text = "A carregar..."

                loginUser(username, password)
            } else {
                Toast.makeText(this, "Preenche os campos todos!", Toast.LENGTH_SHORT).show()
            }
        }

        // Navegação para o ecrã de registo
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Função para efetuar login usando a API
    private fun loginUser(username: String, pass: String) {
        // Inicia Coroutine na Thread de IO (Input/Output) para não bloquear a UI
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Chamada de rede pelo Retrofit
                val response = RetrofitClient.instance.login(AuthRequest(username, pass))

                // Volta à Thread Principal para atualizar a UI
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Login com sucesso: Guarda sessão nas SharedPreferences
                        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        val userId = response.body()?.userId
                        editor.putString("USER_NAME", username)
                        editor.putBoolean("IS_LOGGED_IN", true)
                        editor.putString("USER_ID", userId)
                        editor.apply() // Commit das alterações
                        Toast.makeText(this@LoginActivity, "Login com sucesso!", Toast.LENGTH_SHORT).show()

                        finish() // Fecha a atividade de login e volta à Main
                    } else {
                        Toast.makeText(this@LoginActivity, "Login falhado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}