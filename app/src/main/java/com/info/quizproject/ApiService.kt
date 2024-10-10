package com.info.quizproject

import com.info.quizproject.dataclass.QuestionData
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("your-endpoint-url")  // Replace with actual API endpoint
    fun getQuestions(): Call<QuestionData>
}
