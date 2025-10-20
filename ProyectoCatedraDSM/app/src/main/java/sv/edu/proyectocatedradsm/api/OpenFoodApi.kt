package sv.edu.proyectocatedradsm.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class ProductResponse(
    val products: List<Product>
)

data class Product(
    val product_name: String?,
    val nutriments: Nutriments?,
    val image_url: String?
)

data class Nutriments(
    val energy_kcal_100g: Float?,
    val fat_100g: Float?,
    val sugars_100g: Float?,
    val proteins_100g: Float?
)

interface OpenFoodApi {


    @GET("cgi/search.pl")
    fun searchFood(
        @Query("search_terms") search: String,
        @Query("page_size") size: Int = 10,
        @Query("json") json: Int = 1
    ): Call<ProductResponse>
}
