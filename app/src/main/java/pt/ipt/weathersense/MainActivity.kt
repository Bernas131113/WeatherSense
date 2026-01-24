package pt.ipt.weathersense

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
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
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.ipt.weathersense.BuildConfig
import pt.ipt.weathersense.adapters.ForecastAdapter
import pt.ipt.weathersense.models.ForecastItem

import java.util.Date
class MainActivity : AppCompatActivity() {
    // Declaração de componentes da interface e variáveis de estado
    private lateinit var button: Button
    private lateinit var tvTemperature: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var tvLocalTime: TextView
    private lateinit var tvCity: TextView
    private lateinit var searchLocation: Button
    private var isCardExpanded = true
    private lateinit var groupCardContent: androidx.constraintlayout.widget.Group
    private lateinit var btnToggleCard: ImageButton
    private lateinit var btnFavAction: Button
    private var favoriteCitiesList: MutableList<String> = ArrayList()
    private lateinit var btnAbout: ImageButton



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Ajusta o layout para não ficar tapado pelas barras de sistema (topo e fundo)
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Aplica padding para evitar a barra de estado (topo) e a barra de navegação (fundo)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialização dos componentes da UI
        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)
        tvTemperature = findViewById(R.id.tvTemperature)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvWind = findViewById(R.id.tvWind)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        tvLocalTime = findViewById(R.id.tvLocalTime)
        tvCity = findViewById(R.id.tvCity)
        searchLocation = findViewById(R.id.searchLocation)
        button = findViewById(R.id.button)
        groupCardContent = findViewById(R.id.groupCardContent)
        btnToggleCard = findViewById(R.id.btnToggleCard)
        btnFavAction = findViewById(R.id.btnFavAction)
        btnAbout = findViewById<ImageButton>(R.id.btnAbout)


        // Inicializa o cliente de localização (Google Play Services)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkLocationPermission()

        // Configuração dos Listeners (botões)
        button.setOnClickListener {
            checkLocationPermission() // Tenta obter localização novamente
        }


        searchLocation.setOnClickListener {
            showSearchDialog() // Abre popup de pesquisa
        }


        btnLogin.setOnClickListener {
            // Navega para o ecrã de login
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Lógica para expandir/colapsar o cartão de tempo
        btnToggleCard.setOnClickListener {
            toggleCardVisibility()
        }

        // Navegação para o ecrã "Sobre"
        btnAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        // Carrega a grelha de favoritos ao iniciar
        setupFavoritesGrid()
    }

    // Verifica se a app tem permissão para aceder ao GPS
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Se não tem, pede permissão ao utilizador
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Se já tem, procura a localização atual
            fetchLocation()
        }
    }

    // Callback recebido após o utilizador aceitar/negar permissões
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
    // Obtém a última localização conhecida do dispositivo
    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Constrói URL da API OpenWeather com as coordenadas
                    val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"

                    fetchWeatherData(weatherUrl)
                } else {
                    tvTemperature.text = "Could not get location."
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
    }

    // Faz o pedido HTTP à API OpenWeather (com o Volley)
    private fun fetchWeatherData(url: String) {
        val queue = Volley.newRequestQueue(this)

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    // Parse do JSON recebido
                    val jsonResponse = JSONObject(response)

                    val main = jsonResponse.getJSONObject("main")
                    val temp = main.getDouble("temp").toInt().toString()
                    val feelsLike = main.getDouble("feels_like").toInt().toString()
                    val city = jsonResponse.getString("name")

                    val windObj = jsonResponse.getJSONObject("wind")
                    val windSpeed = windObj.getString("speed")

                    val weatherArray = jsonResponse.getJSONArray("weather")
                    val weatherObj = weatherArray.getJSONObject(0)
                    val iconCode = weatherObj.getString("icon")

                    // Cálculo da hora local da cidade pesquisada (considerando o fuso horário)
                    val timezoneOffset = jsonResponse.getLong("timezone")
                    // Obter a hora atual em UTC
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    // Somar o desvio da cidade
                    calendar.add(Calendar.SECOND, timezoneOffset.toInt())



                    // Construir o URL da imagem (4x para ficar com melhor qualidade)
                    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@4x.png"

                    // Formatação da hora
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    // Garantir que não soma o fuso do telemóvel
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val localTime = sdf.format(calendar.time)


                    // Atualiza a UI com os dados
                    tvCity.text = city
                    tvTemperature.text = "${temp}°C "
                    tvFeelsLike.text = "Sensação: ${feelsLike}°C"
                    tvWind.text = "Vento: ${windSpeed} m/s"
                    tvLocalTime.text = "Hora local: ${localTime}"

                    // Carregar a imagem com o Glide
                    Glide.with(this)
                        .load(iconUrl)
                        .into(ivWeatherIcon)

                    // Atualiza estado do botão de favoritos (Adicionar vs Remover)
                    updateFavoriteButtonState(city)

                    // Previsão futura baseada nestas coordenadas
                    val coord = jsonResponse.getJSONObject("coord")
                    val lat = coord.getDouble("lat")
                    val lon = coord.getDouble("lon")
                    fetchForecast(lat,lon)
                } catch (e: Exception) {
                    tvTemperature.text = "Erro ao ler dados"
                    e.printStackTrace()
                }
            },
            { error ->
                tvTemperature.text = "Erro de ligação!"
                error.printStackTrace()
            })

        queue.add(request)
    }
    companion object {
        val API_KEY = BuildConfig.API_KEY
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    // Previsão para os próximos dias (Forecast 5 days)
    private fun fetchForecast(latitude: Double, longitude: Double) {
        val url = "https://api.openweathermap.org/data/2.5/forecast?lat=$latitude&lon=$longitude&units=metric&appid=$API_KEY"

        val request = StringRequest(Request.Method.GET, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)

                    // Obter o desvio de fuso horário da cidade (em segundos)
                    val cityObj = jsonResponse.getJSONObject("city")
                    val timezoneOffset = cityObj.getLong("timezone")

                    val list = jsonResponse.getJSONArray("list")
                    val forecastItems = ArrayList<ForecastItem>()

                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Ex: "Seg"

                    // Itera sobre a lista de previsões (3 em 3 horas)
                    for (i in 0 until list.length()) {
                        val itemObj = list.getJSONObject(i)

                        // Obter o timestamp UTC da previsão (segundos)
                        val dt = itemObj.getLong("dt")

                        // Calcular a hora LOCAL nessa cidade (0-23)
                        // (dt + timezone) dá-nos a hora local em segundos Unix.
                        // Dividimos por 3600 para ter horas totais, e % 24 para ter a hora do dia.
                        val localHour = ((dt + timezoneOffset) / 3600) % 24


                        // Escolher previsões que sejam entre as 11h e as 13h locais.
                        // Como os dados vêm de 3 em 3 horas (ex: 10, 13, 16 ou 11, 14, 17),
                        // este intervalo garante que apanhamos sempre o "meio-dia" local.
                        if (localHour in 11..13) {

                            val main = itemObj.getJSONObject("main")
                            val temp = main.getDouble("temp").toInt().toString() + "°C"

                            val weatherArray = itemObj.getJSONArray("weather")
                            val icon = weatherArray.getJSONObject(0).getString("icon")

                            // Formatar data para obter o dia da semana
                            val dtTxt = itemObj.getString("dt_txt")
                            val date = inputFormat.parse(dtTxt)
                            val dayName = dayFormat.format(date!!)

                            forecastItems.add(ForecastItem(dayName, temp, icon))
                        }
                    }

                    // Configura a lista horizontal (RecyclerView)
                    val rvForecast = findViewById<RecyclerView>(R.id.rvForecast)
                    rvForecast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    rvForecast.adapter = ForecastAdapter(forecastItems)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            })

        Volley.newRequestQueue(this).add(request)
    }

    // Chamado sempre que o ecrã volta a ficar visível (ex: após login)
    override fun onResume() {
        super.onResume()

        // Verifica estado da sessão nas SharedPreferences
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val username = sharedPref.getString("USER_NAME", "User")

        val btnLogin = findViewById<Button>(R.id.btnGoToLogin)
        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)
        val btnChangePass = findViewById<Button>(R.id.btnChangePass)

        if (isLoggedIn) {
            // UI para utilizador autenticado
            btnLogin.text = "Logout"
            tvUserEmail.text = "Olá, $username"
            tvUserEmail.visibility = View.VISIBLE
            btnChangePass.visibility = View.VISIBLE
            btnChangePass.setOnClickListener {
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivity(intent)
            }

            // Lógica de Logout
            btnLogin.setOnClickListener {
                sharedPref.edit().clear().apply()
                Toast.makeText(this, "Logout feito!", Toast.LENGTH_SHORT).show()
                favoriteCitiesList.clear()
                onResume() // Recarrega UI
            }

            rvFavorites.visibility = View.VISIBLE
            // Carrega favoritos do backend
            setupFavoritesGrid()

        } else {
            // UI para visitante
            btnLogin.text = "Login"
            tvUserEmail.visibility = View.GONE
            btnFavAction.visibility = View.GONE
            btnChangePass.visibility = View.GONE

            btnLogin.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            rvFavorites.visibility = View.GONE
        }
    }

    private fun setupFavoritesGrid() {
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val userId = sharedPref.getString("USER_ID", null)

        if (userId != null) {
            fetchFavorites(userId)
        } else {
        }
    }

    // Carrega a lista de cidades favoritas do utilizador (via API Backend/Retrofit)
    private fun fetchFavorites(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getFavorites(userId)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val cities = response.body()!!.favorites
                        favoriteCitiesList = cities.toMutableList()

                        // Configura a lista vertical de favoritos
                        val rvFavorites = findViewById<RecyclerView>(R.id.rvFavorites)

                        rvFavorites.layoutManager = GridLayoutManager(this@MainActivity, 1)
                        rvFavorites.adapter = FavoritesAdapter(
                            cities,
                            onCityClick = { clickedCityName ->
                                // Ao clicar num favorito, carrega o tempo dessa cidade
                                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$clickedCityName&units=metric&appid=$API_KEY"
                                fetchWeatherData(weatherUrl)
                            },
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    // Mostra diálogo para pesquisar uma cidade manualmente
    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pesquisar Localização")

        // Configurar o input
        val input = EditText(this)
        input.hint = "Introduza o nome da cidade (ex: Porto)"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Pesquisar") { dialog, which ->
            val cityName = input.text.toString().trim()
            if (cityName.isNotEmpty()) {
                // Construir o URL da API para essa cidade
                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&units=metric&appid=$API_KEY"
                fetchWeatherData(weatherUrl)
            }
        }

        // Opção para abrir o Google Maps
        builder.setNeutralButton("Google Maps") { dialog, which ->
            val intent = Intent(this, MapPickerActivity::class.java)
            mapPickerLauncher.launch(intent)
        }
        builder.setNegativeButton("Cancelar") { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    // Guarda cidade nos favoritos (chamada ao Backend)
    private fun saveCityToBackend(userId: String, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.addFavorite(AddFavoriteRequest(userId, cityName))
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$cityName adicionado aos favoritos!", Toast.LENGTH_SHORT).show()
                        // Atualiza lista
                        fetchFavorites(userId)
                        favoriteCitiesList.add(cityName)
                        updateFavoriteButtonState(cityName)
                    } else {
                        Toast.makeText(this@MainActivity, "Erro a adicionar cidade", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Remove uma cidade dos favoritos chamando a API
    private fun deleteFavoriteCity(userId: String, cityName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Reutilizamos o objeto AddFavoriteRequest porque os dados necessários são os mesmos (ID do user e Nome da cidade)
                val response = RetrofitClient.instance.removeFavorite(AddFavoriteRequest(userId, cityName))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "$cityName removido dos favoritos!", Toast.LENGTH_SHORT).show()
                        // Atualiza lista
                        fetchFavorites(userId)
                        favoriteCitiesList.remove(cityName)
                        // Atualiza o botão para permitir adicionar novamente se necessário
                        updateFavoriteButtonState(cityName)
                    } else {
                        Toast.makeText(this@MainActivity, "Erro a remover a cidade", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Expande ou recolhe o cartão com os detalhes do tempo
    private fun toggleCardVisibility() {
        isCardExpanded = !isCardExpanded

        if (isCardExpanded) {
            // Mostra os detalhes e muda o ícone para "fechar"
            groupCardContent.visibility = View.VISIBLE
            btnToggleCard.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            // Só mostra o botão de favoritos se o utilizador estiver logado
            val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
            if (isLoggedIn) {
                btnFavAction.visibility = View.VISIBLE
            } else {
                btnFavAction.visibility = View.GONE
            }
        } else {
            // Esconde os detalhes e o botão de favoritos
            groupCardContent.visibility = View.GONE
            btnFavAction.visibility = View.GONE
            btnToggleCard.setImageResource(android.R.drawable.ic_input_add)
        }
    }

    // Atualiza o estado e comportamento do botão de favoritos (Adicionar vs Remover)
    private fun updateFavoriteButtonState(currentCity: String) {
        val sharedPref = getSharedPreferences("WeatherAppSession", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("IS_LOGGED_IN", false)
        val userId = sharedPref.getString("USER_ID", null)

        if (isLoggedIn && userId != null) {
            btnFavAction.visibility = View.VISIBLE

            // Verifica se a cidade atual já está na lista de favoritos
            if (favoriteCitiesList.contains(currentCity)) {
                // caso já seja favorito -> Botão serve para remover
                btnFavAction.text = "Remover dos Favoritos"

                btnFavAction.setOnClickListener {
                    // Cria um AlertDialog para confirmar a remoção (Requisito de segurança)
                    AlertDialog.Builder(this)
                        .setTitle("Remover Favorito")
                        .setMessage("Tem a certeza que deseja remover $currentCity dos favoritos?")
                        .setPositiveButton("Sim") { dialog, _ ->
                            // Só executa a remoção se o utilizador confirmar
                            deleteFavoriteCity(userId, currentCity)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Não") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } else {
                // caso não seja favorito -> Botão serve para adicionar
                btnFavAction.text = "Adicionar aos Favoritos"
                btnFavAction.setOnClickListener {
                    saveCityToBackend(userId, currentCity)
                }
            }
        } else {
            // Se não tiver login, esconde o botão
            btnFavAction.visibility = View.GONE
        }
    }
    // Recebe o resultado da seleção no Google Maps (Activity MapPicker)
    private val mapPickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                // Obtém as coordenadas enviadas pela MapPickerActivity
                val lat = data.getDoubleExtra("lat", 0.0)
                val lon = data.getDoubleExtra("lon", 0.0)

                // Construir URL com Lat/Lon (igual ao GPS)
                val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&units=metric&appid=$API_KEY"

                Toast.makeText(this, "Localização recebida do mapa!", Toast.LENGTH_SHORT).show()
                fetchWeatherData(weatherUrl)
            }
        }
    }

}