package pt.ipt.weathersense

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipt.weathersense.models.UpdatePasswordRequest
import pt.ipt.weathersense.network.RetrofitClient

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Botão Cancelar apenas fecha a atividade atual
        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val newPass = etNewPassword.text.toString()

            if (newPass.isNotEmpty()) {
                // Recuperar o ID do utilizador logado através das SharedPreferences (Sessão)
                val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
                val userId = sharedPref.getString("USER_ID", null)

                if (userId != null) {
                    updatePassword(userId, newPass)
                } else {
                    Toast.makeText(this, "Erro: Utilizador não identificado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "A password não pode estar vazia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função que comunica com a API para atualizar a password no servidor
    private fun updatePassword(userId: String, newPass: String) {
        // Coroutine na thread de IO para operações de rede
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Chama a rota /update-password definida na API
                val response = RetrofitClient.instance.updatePassword(UpdatePasswordRequest(userId, newPass))

                // Volta à thread principal (Main) para mostrar o resultado ao utilizador
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Password alterada com sucesso!", Toast.LENGTH_LONG).show()
                        finish() // Fecha a janela e volta ao menu
                    } else {
                        Toast.makeText(this@EditProfileActivity, "Erro ao atualizar. Tenta novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Erro de rede: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}