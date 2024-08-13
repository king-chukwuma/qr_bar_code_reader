package com.chukwuma.MOFI.service

import kotlinx.serialization.Serializable

@Serializable
data class ExceptionResponse(val code: Int, val reason: String)
