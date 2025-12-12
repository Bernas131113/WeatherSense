package pt.ipt.weathersense

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var selectedLocation: LatLng? = null
    private lateinit var btnConfirm: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        btnConfirm = findViewById(R.id.btnConfirmLocation)

        // Inicializar o Mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar botão de confirmação
        btnConfirm.setOnClickListener {
            selectedLocation?.let { loc ->
                // Devolver os dados à MainActivity
                val resultIntent = Intent()
                resultIntent.putExtra("lat", loc.latitude)
                resultIntent.putExtra("lon", loc.longitude)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Mover a câmara para Portugal inicialmente (Opcional)
        val portugal = LatLng(39.5, -8.0)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(portugal, 6f))

        // Quando o utilizador clica no mapa
        mMap.setOnMapClickListener { latLng ->
            //Guardar localizacao
            selectedLocation = latLng

            //Limpar marcadores antigos e por um novo
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Local Selecionado"))

            //Mostrar botão de confirmar
            btnConfirm.visibility = View.VISIBLE
        }
    }
}