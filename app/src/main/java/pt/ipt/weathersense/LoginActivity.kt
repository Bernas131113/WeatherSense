package pt.ipt.weathersense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()


        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)


        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            } else {

                lifecycleScope.launch {

                    val user = userDao.checkLogin(email, pass)

                    if (user != null) {

                        Toast.makeText(this@LoginActivity, "Bem-vindo de volta!", Toast.LENGTH_SHORT).show()
                        goToMainActivity()
                    } else {

                        Toast.makeText(this@LoginActivity, "Email ou Password errados.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Para registar, preencha email e password.", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {

                    val existingUser = userDao.checkLogin(email, pass)

                    if (existingUser == null) {

                        val newUser = User(
                            username = email,
                            email = email,
                            pass = pass
                        )


                        userDao.insert(newUser)

                        Toast.makeText(this@LoginActivity, "Conta criada com sucesso!", Toast.LENGTH_LONG).show()


                        etEmail.text.clear()
                        etPassword.text.clear()
                    } else {
                        Toast.makeText(this@LoginActivity, "Este utilizador já existe.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}