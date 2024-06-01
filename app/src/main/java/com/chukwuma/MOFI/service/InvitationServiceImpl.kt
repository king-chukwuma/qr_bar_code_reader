package com.chukwuma.MOFI.service

import com.chukwuma.MOFI.dto.CheckInResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.url

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