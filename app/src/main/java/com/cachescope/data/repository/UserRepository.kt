package com.cachescope.data.repository

import com.cachescope.data.network.SearchUserResponse
import com.cachescope.data.network.UserApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: UserApiService
) {
    suspend fun searchUsers(query: String): Result<SearchUserResponse> = runCatching {
        api.searchUsers(query)
    }
}
