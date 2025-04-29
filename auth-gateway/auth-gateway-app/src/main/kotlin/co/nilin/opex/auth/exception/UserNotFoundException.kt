package co.nilin.opex.auth.exception

class UserNotFoundException(username: String) : RuntimeException("User with email ${username} not found.")