package com.example.pogodynka

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SeniorView.newInstance] factory method to
 * create an instance of this fragment.
 */
class SeniorView : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_senior_view, container, false)
    }

    val api:String="7996a2dfb0fa23e7e1b9037242e66e72"
    val city:String="gliwice,pl"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireActivity().application,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                , SeniorView.REQUEST_PERMISSION_REQUEST_CODE
            )
        }else {
            getCurrentLocation()
            forecast().execute()
        }

        view.findViewById<Switch>(R.id.seniortonormal).apply {
            setOnClickListener {
                view.findNavController().navigate(R.id.action_seniorView_to_normalView)
            }
        }
    }

    //localisation
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SeniorView.REQUEST_PERMISSION_REQUEST_CODE && grantResults.size > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }else{
                Toast.makeText(requireActivity().application,"Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getCurrentLocation() {

        var locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //now getting address from latitude and longitude

        val geocoder = Geocoder(requireActivity().application, Locale.getDefault())
        var addresses:List<Address>

        if (ActivityCompat.checkSelfPermission(
                requireActivity().application,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity().application,
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
        LocationServices.getFusedLocationProviderClient(requireActivity().application)
            .requestLocationUpdates(locationRequest,object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult?) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(requireActivity().application)
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

    inner class forecast() : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$api").readText(
                    Charsets.UTF_8
                )
            }catch (e: Exception){
                response = null
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
                val pressure = main.getString("pressure")+"hPa"

                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name")+", "+sys.getString("country")

                when(weatherDescription){
                    "clear sky" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.slonce)
                    }
                    "few clouds" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.chmurkaslonce)
                    }
                    "scattered clouds" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.chmurki)
                    }
                    "broken clouds" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.ciemnechmurki)
                    }
                    "shower rain" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.deszczyk)
                    }
                    "rain" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.sloncedeszcz)
                    }
                    "thunderstorm" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.burza)
                    }
                    "snow" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.snieg)
                    }
                    "mist" -> {
                        view?.findViewById<ImageView>(R.id.ikonkapogody)?.setImageResource(R.drawable.mgla)
                    }

                }

                /* Populating extracted data into our views */
                view?.findViewById<TextView>(R.id.miasto)?.text = address
                view?.findViewById<TextView>(R.id.data)?.text =  updatedAtText
                view?.findViewById<TextView>(R.id.opis)?.text = weatherDescription.capitalize()
                view?.findViewById<TextView>(R.id.temperatura)?.text = temp
                view?.findViewById<TextView>(R.id.wschod)?.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunrise*1000)
                )
                view?.findViewById<TextView>(R.id.zachod)?.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(
                    Date(sunset*1000)
                )
                view?.findViewById<TextView>(R.id.cisnienie)?.text = pressure

                /* Views populated, Hiding the loader, Showing the main design */
                view?.findViewById<View>(R.id.view)?.visibility = View.GONE
                view?.findViewById<ImageView>(R.id.imageView6)?.visibility = View.GONE

            } catch (e: Exception) {
                view?.findViewById<ImageView>(R.id.imageView6)?.visibility = View.GONE
            }

        }}

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SeniorView.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SeniorView().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
        private val REQUEST_PERMISSION_REQUEST_CODE = 2020
    }

}