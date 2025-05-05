package co.nilin.opex.auth.exception

class UserAlreadyExistsException(username: String) : RuntimeException("User with email ${username} already exists.")