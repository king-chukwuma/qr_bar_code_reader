package com.chukwuma.scanner2.service

import com.chukwuma.scanner2.dto.CheckInResponse
import io.ktor.client.HttpClient
import io.ktor.client.features.json.defaultSerializer
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import kotlinx.serialization.serializer

class InvitationServiceImpl (private val client: HttpClient) : InvitationService {

    override suspend fun checkIn(id: String?): CheckInResponse {
        return try {
            client.post<Unit> {
                url(BackendRoutes.CHECK_IN + id)
            }
            CheckInResponse(true, "Successful!!");
        } catch (e :Exception) {
            println(e.message)
            CheckInResponse(false, e.message);
        }
    }

    override suspend fun checkOut(id: String?): CheckInResponse {
        return try {
            client.post<Unit> {
                url(BackendRoutes.CHECK_OUT + id)
            }
            CheckInResponse(true, "Successful!!");
        } catch (e :Exception) {
            println(e.message)
            CheckInResponse(false, e.message);
        }
    }
}