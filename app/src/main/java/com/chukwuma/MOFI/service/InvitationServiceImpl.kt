package com.chukwuma.MOFI.service

import android.util.Log
import com.chukwuma.MOFI.dto.CheckInResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse

class InvitationServiceImpl (private val client: HttpClient) : InvitationService {

    override suspend fun checkIn(id: String?): CheckInResponse {
        return makeCall(BackendRoutes.CHECK_IN + id)
    }

    override suspend fun checkOut(id: String?): CheckInResponse {
        return makeCall(BackendRoutes.CHECK_OUT + id)
    }

    private suspend fun makeCall(url: String):  CheckInResponse{
        val httpResponse: HttpResponse =  try {
            client.post(url)
        } catch (e :RuntimeException) {
            return CheckInResponse(false, "Failed");
        }

        return when (httpResponse.status.value) {
            in 200..299 -> {
                CheckInResponse(true, "Successful");
            }
            else -> {
                val responseBody = httpResponse.body<ExceptionResponse>()
                CheckInResponse(false, responseBody.reason)
            }
        }
    }
}