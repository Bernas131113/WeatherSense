package pt.ipt.weathersense

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pt.ipt.weathersense.adapters.FavoritesAdapter
import pt.ipt.weathersense.models.AddFavoriteRequest
import pt.ipt.weathersense.network.RetrofitClient
import android.app.AlertDialog
import android.widget.EditText
class MainActivity : AppCompatActivity() {
    private lateinit var button: Button

    private lateinit var tvTemperature: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvWind: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvTemperature = findViewById(R.id.tvTemperature)
        tvDescription = findViewById(R.id.tvDescription)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvWind = findViewById(R.id.tvWind)

        button = findViewById(R.id.button)

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        button.setOnClickListener {
            checkLocationPermission()
        }
        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)


        btnLogin.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
        }

        setupFavoritesGrid()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, fetch location
            fetchLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch location
                fetchLocation()
            } else {
                // Permission denied, show message
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"

                    // Debugging: Log URL
                    println("Weather API URL: $weatherUrl")

                    fetchWeatherData(weatherUrl)
                } else {
                    tvTemperature.text = "Could not get location."
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchWeatherData(url: String) {
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    val main = jsonResponse.getJSONObject("main")
                    val temp = main.getString("temp")
                    val feelsLike = main.getString("feels_like")

                    val city = jsonResponse.getString("name")

                    val windObj = jsonResponse.getJSONObject("wind")
                    val windSpeed = windObj.getString("speed")

                    val weatherArray = jsonResponse.getJSONArray("weather")
                    val weatherObj = weatherArray.getJSONObject(0)
                    val description = weatherObj.getString("description")

                    // --- USAR AS VARIÁVEIS GLOBAIS ---
                    tvTemperature.text = "${temp}°C em $city"

                    // Colocar primeira letra maiúscula
                    tvDescription.text = description.replaceFirstChar { it.uppercase() }

                    tvFeelsLike.text = "Sensação: ${feelsLike}°C"
                    tvWind.text = "Vento: ${windSpeed} m/s"

                } catch (e: Exception) {
                    // Podes usar o tvDescription para mostrar erro se quiseres
                    tvDescription.text = "Erro ao ler dados"
                    e.printStackTrace()
                }
            },
            { error ->
                tvDescription.text = "Erro de ligação!"
                error.printStackTrace()
            })

        queue.add(request)
    }
    companion object {
        const val API_KEY = "c6a05c4e496df1f1ec3336054d1dbe28"
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onResume() {
        super.onResume()

        //checking if user is logged in
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userEmail = sharedPref.getString("USER_EMAIL", "User")

        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)

        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddCity)
        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)
        fab.setOnClickListener {
            showAddCityDialog()
        }
        //showing or hiding button based on login state
        if (isLoggedIn) {
            btnLogin.text = "Logout: $userEmail"
            btnLogin.setOnClickListener {
                //clear the session
                sharedPref.edit().clear().apply()

                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()

                // run it back
                onResume()
            }
            fab.visibility = View.VISIBLE
            rvFavorites.visibility = View.VISIBLE

            // Carregar os dados da grelha
            setupFavoritesGrid()


        } else {
            btnLogin.text = "Login"
            btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            fab.visibility = View.GONE
            rvFavorites.visibility = View.GONE
        }



    }

    private fun setupFavoritesGrid() {
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            // User is logged in, fetch data
            fetchFavorites(userId)
        } else {
            // User is logged out, clear the grid (optional)
        }
    }

    private fun fetchFavorites(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getFavorites(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val cities = response.body()!!.favorites
                        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)

                        rvFavorites.layoutManager = GridLayoutManager(this@MainActivity, 1)


                        rvFavorites.adapter = FavoritesAdapter(
                            cities,
                            onCityClick = { clickedCityName ->
                                // 1. Ver Tempo
                                Toast.makeText(this@MainActivity, "Loading $clickedCityName...", Toast.LENGTH_SHORT).show()
                                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$clickedCityName&units=metric&appid=$API_KEY"
                                fetchWeatherData(weatherUrl)
                            },
                            onDeleteClick = { cityToDelete ->
                                // 2. Apagar Cidade (Abre um popup de confirmação opcional ou apaga direto)
                                deleteFavoriteCity(userId, cityToDelete)
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }
    private fun testAddFavorite(userId: String, city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            RetrofitClient.instance.addFavorite(AddFavoriteRequest(userId, city))
            // After adding, refresh the list
            fetchFavorites(userId)
        }
    }

    private fun showAddCityDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Favorite City")

        // Set up the input
        val input = EditText(this)
        input.hint = "Enter city name (e.g. Tokyo)"
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Add") { dialog, which ->
            val cityName = input.text.toString()
            if (cityName.isNotEmpty()) {
                // Get User ID
                val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
                val userId = sharedPref.getString("USER_ID", null)

                if (userId != null) {
                    saveCityToBackend(userId, cityName)
                } else {
                    Toast.makeText(this, "Please login first!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    private fun saveCityToBackend(userId: String, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.addFavorite(AddFavoriteRequest(userId, cityName))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$cityName added!", Toast.LENGTH_SHORT).show()
                        // refresh grid
                        fetchFavorites(userId)
                    } else {
                        Toast.makeText(this@MainActivity, "Error adding city", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun deleteFavoriteCity(userId: String, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Reutilizamos AddFavoriteRequest pq os dados são os mesmos (ID e Cidade)
                val response = RetrofitClient.instance.removeFavorite(AddFavoriteRequest(userId, cityName))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$cityName removed!", Toast.LENGTH_SHORT).show()
                        // Atualiza a grelha
                        fetchFavorites(userId)
                    } else {
                        Toast.makeText(this@MainActivity, "Error removing city", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}