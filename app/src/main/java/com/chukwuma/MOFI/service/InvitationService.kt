package com.chukwuma.MOFI.service

import com.chukwuma.MOFI.dto.CheckInResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface InvitationService {

    suspend fun checkIn(id: String?) : CheckInResponse
    suspend fun checkOut(id: String?): CheckInResponse

    companion object {
        fun create() : InvitationService {
            return InvitationServiceImpl (
                client = HttpClient(Android) {
                    install(Logging) {
                        level = LogLevel.ALL
                    }
                    install(ContentNegotiation) {
                        json(json = Json {
                            prettyPrint = true
                            isLenient = true
                            ignoreUnknownKeys = true
                        })
                    }
                }
            )
        }
    }
}