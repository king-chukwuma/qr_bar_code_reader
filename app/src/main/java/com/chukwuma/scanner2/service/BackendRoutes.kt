package com.chukwuma.scanner2.service

object BackendRoutes {

    private const val BASE_URL = "https://0aa6-105-112-185-28.ngrok-free.app/invitation"
    const val CHECK_IN = "$BASE_URL/in?id="
    const val CHECK_OUT = "$BASE_URL/out?id="
}