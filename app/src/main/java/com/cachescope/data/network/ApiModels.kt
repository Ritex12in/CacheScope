package com.cachescope.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class SearchUserResponse(
    @SerialName("total_count") val totalCount: Int = 0,
    val items: List<UserItem> = emptyList()
)

@Serializable
data class UserItem(
    val id: Long,
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String = "",
    val url: String = "",
    val type: String = "User",
    val score: Double = 0.0
)

@Serializable
data class RepoItem(
    val id: Long,
    val name: String,
    @SerialName("full_name") val fullName: String = "",
    val description: String? = null,
    val language: String? = null,
    @SerialName("stargazers_count") val stars: Int = 0,
    @SerialName("forks_count") val forks: Int = 0
)

interface UserApiService {

    @GET("search/users")
    suspend fun searchUsers(@Query("q") query: String): SearchUserResponse

    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Query("per_page") perPage: Int = 10
    ): List<RepoItem>
}
