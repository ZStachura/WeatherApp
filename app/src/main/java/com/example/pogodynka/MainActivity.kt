package com.example.pogodynka


import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(){

    companion object{
        private val REQUEST_PERMISSION_REQUEST_CODE = 2020
    }


    val api:String="7996a2dfb0fa23e7e1b9037242e66e72"
    var city:String="gliwice,pl"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@MainActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                ,REQUEST_PERMISSION_REQUEST_CODE)
        }else {
            getCurrentLocation()
            forecast().execute()
        }
    }

    //localisation
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE && grantResults.size > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }else{
                Toast.makeText(this@MainActivity,"Permission Denied!",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getCurrentLocation() {

        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //now getting address from latitude and longitude

        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
        var addresses:List<Address>

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.getFusedLocationProviderClient(this@MainActivity)
            .requestLocationUpdates(locationRequest,object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)
                    if (locationResult != null && locationResult.locations.size > 0){
                        var locIndex = locationResult.locations.size-1

                        var latitude = locationResult.locations.get(locIndex).latitude
                        var longitude = locationResult.locations.get(locIndex).longitude

                        addresses = geocoder.getFromLocation(latitude,longitude,1)

                        var currentaddress:String = addresses[0].getLocality() + "," + addresses[0].getCountryName()
                        var city:String=addresses[0].getLocality().lowercase() + ",pl"
                        //println(city)
                        //findViewById<TextView>(R.id.miasto).text=city
                    }
                }
            }, Looper.getMainLooper())

    }

    //forecast

    inner class forecast() : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
                println(e.toString())
            }
            return response
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt:Long = jsonObj.getLong("dt")
                val updatedAtText = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(
                    Date(updatedAt*1000)
                )
                val tempe=main.getInt("temp")
                val temp = tempe.toString() +"°C"
                val pressure = main.getString("pressure")+" hPa"

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                when(weatherDescription){
                    "clear sky" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.slonce)
                    }
                    "few clouds" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.chmurkaslonce)
                    }
                    "scattered clouds" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.chmurki)
                    }
                    "broken clouds" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.ciemnechmurki)
                    }
                    "shower rain" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.deszczyk)
                    }
                    "rain" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.sloncedeszcz)
                    }
                    "thunderstorm" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.burza)
                    }
                    "snow" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.snieg)
                    }
                    "mist" -> {
                        findViewById<ImageView>(R.id.ikonkapogody).setImageResource(R.drawable.mgla)
                    }

                }

                /* Populating extracted data into our views */
                findViewById<TextView>(R.id.miasto).text = address
                findViewById<TextView>(R.id.data).text =  updatedAtText
                findViewById<TextView>(R.id.opis).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temperatura).text = temp
                findViewById<TextView>(R.id.wschod).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunrise*1000)
                )
                findViewById<TextView>(R.id.zachod).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunset*1000)
                )
                findViewById<TextView>(R.id.cisnienie).text = pressure

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById<View>(R.id.view).visibility = View.GONE
                findViewById<ImageView>(R.id.imageView6).visibility = View.GONE

            } catch (e: Exception) {
                findViewById<ImageView>(R.id.imageView6).visibility = View.GONE
            }

        }
    }
    }