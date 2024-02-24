package io.github.trainb0y.ktberd

fun debugPrint(filename: String, code: String, message: String, token: Token) {
	if (code.isEmpty()) { // or maybe that means nullability? it's been a while since python
		print(message) // todo: there was ansi formatting
		return
	}

	val carats = token.value.length
	val spaces = token.column - token.value.length + 1

	println("""
		$filename, line ${token.line}\n\n" + 
		${code.split("\n")[token.line - 1]}\n" + \
		${" ".repeat(spaces)}${"^".repeat(carats)}
		$message
		""".trimIndent()
	)
}

fun debugPrintNoToken(filename: String, message: String) {
	println("$filename\n\n$message") // todo: formatting
}

fun raiseErrorAtToken(filename: String, code: String, message: String, token: Token) {
	if (code.isEmpty()) {
		throw InterpretationError(message)
	}
	val carats = token.value.length
	val spaces = token.column - token.value.length + 1

	throw InterpretationError("""
		$filename, line ${token.line}\n\n" + 
		${code.split("\n")[token.line - 1]}\n" + \
		${" ".repeat(spaces)}${"^".repeat(carats)}
		$message
		""".trimIndent()
	)
}