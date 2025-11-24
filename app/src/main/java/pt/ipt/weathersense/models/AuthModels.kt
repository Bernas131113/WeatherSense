package pt.ipt.weathersense.models

data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String,
    val userId: String?
)