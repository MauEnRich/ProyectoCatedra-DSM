package sv.edu.proyectocatedradsm.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Cambiado a la API pública
    private const val BASE_URL = "https://world.openfoodfacts.org/"

    val api: OpenFoodApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenFoodApi::class.java)
    }
}
