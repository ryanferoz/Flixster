package com.example.flixsterplus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

data class Movie(
    val title: String,
    val poster_path: String,
    val overview: String
)

class MovieAdapter(private var movies: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val posterImageView: ImageView = view.findViewById(R.id.poster_image_view)
        val titleTextView: TextView = view.findViewById(R.id.title_text_view)
        val descriptionTextView = view.findViewById<TextView>(R.id.description_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.movie_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.titleTextView.text = movie.title
        holder.descriptionTextView.text = movie.overview
        // Load the poster image using Glide or any other image loading library
        Glide.with(holder.itemView)
            .load("https://image.tmdb.org/t/p/w500/${movie.poster_path}")
            .into(holder.posterImageView)
    }

    override fun getItemCount(): Int = movies.size

    fun setMovies(movies: List<Movie>) {
        this.movies = movies
        notifyDataSetChanged()
    }
}

interface MovieApiService {
    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): Response<NowPlayingResponse>

    data class NowPlayingResponse(
        val results: List<Movie>
    )
}

class MainActivity : AppCompatActivity() {

    private lateinit var movieApiService: MovieApiService
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var movieRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movieRecyclerView = findViewById(R.id.movie_recycler_view)
        movieRecyclerView.layoutManager = LinearLayoutManager(this)
        movieAdapter = MovieAdapter(emptyList())
        movieRecyclerView.adapter = movieAdapter

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        movieApiService = retrofit.create(MovieApiService::class.java)
        fetchNowPlayingMovies()
    }

    private fun fetchNowPlayingMovies() {
        GlobalScope.launch(Dispatchers.Main) {
            val response = movieApiService.getNowPlayingMovies(
                apiKey = "a07e22bc18f5cb106bfe4cc1f83ad8ed",
                language = "en-US"
            )
            if (response.isSuccessful) {
                val movies = response.body()?.results ?: emptyList()
                movieAdapter.setMovies(movies)
            } else {
                print("problem fetching API")
            }
        }
    }
}
