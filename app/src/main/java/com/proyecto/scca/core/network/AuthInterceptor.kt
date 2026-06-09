package com.proyecto.scca.core.network

import com.proyecto.scca.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor
    @Inject
    constructor(
        private val sessionManager: SessionManager,
    ) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val token = sessionManager.tokenActual

            val request =
                if (token != null) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    original
                }

            val response = chain.proceed(request)

            // Handle 401: trigger logout
            if (response.code == 401) {
                sessionManager.logout()
            }

            return response
        }
    }
