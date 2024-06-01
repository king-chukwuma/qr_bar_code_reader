package com.chukwuma.MOFI.service

import com.chukwuma.MOFI.dto.CheckInResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging

interface InvitationService {

    suspend fun checkIn(id: String?) : CheckInResponse
    suspend fun checkOut(id: String?): CheckInResponse

    companion object {
        fun create() : InvitationService {
            return InvitationServiceImpl (
                client = HttpClient(Android) {
                    install(Logging) {
                        level = LogLevel.BODY
                    }
                    install (JsonFeature) {
                        serializer = KotlinxSerializer()
                    }
                }
            )
        }
    }
}