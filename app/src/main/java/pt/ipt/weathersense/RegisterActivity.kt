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

        // Inicialização dos campos de texto e botões
        val usernameInput = findViewById<EditText>(R.id.etUsername)
        val passInput = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        // Configuração do botão de Registo
        btnRegister.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passInput.text.toString()

            // Validação simples para garantir que os campos não estão vazios
            if(username.isNotEmpty() && password.isNotEmpty()) {
                registerUser(username, password)
            } else {
                Toast.makeText(this, "Por favor introduzir Nome de Utilizador e Palavra-Passe", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão para voltar ao ecrã de Login sem fazer nada
        btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    // Função que envia os dados para o servidor (Backend)
    private fun registerUser(username: String, pass: String) {
        // Usa Coroutines (Dispatchers.IO) para não bloquear a interface durante o pedido de rede
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Chama a API de Registo definida no RetrofitClient
                // Envia um objeto AuthRequest com o user e a pass
                val response = RetrofitClient.instance.register(AuthRequest(username, pass))
                // Volta à Thread Principal (Main) para atualizar a interface
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registro com Sucesso. Realize o Login", Toast.LENGTH_LONG).show()
                        // Fecha esta atividade e volta automaticamente para o Login
                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registro falhado.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // Tratamento de erros de ligação (ex: sem internet ou servidor em baixo)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}