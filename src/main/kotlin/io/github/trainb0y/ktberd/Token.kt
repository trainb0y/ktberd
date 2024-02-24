package io.github.trainb0y.ktberd

data class Token(
	val type: TokenType,
	val value: String,
	val line: Int, // todo: these dont get hashed in the python one
	val column: Int // ^
)