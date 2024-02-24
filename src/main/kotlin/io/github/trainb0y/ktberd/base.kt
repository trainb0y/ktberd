package io.github.trainb0y.ktberd

class NonFormattedError(str: String?=null): Exception(str)
class InterpretationError(str: String?=null): Exception(str)

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

enum class TokenType(val value: String) {
	R_CURLY("}"),
	L_CURLY("{"),
	R_SQUARE("]"),
	L_SQUARE("["),

	DOT("."),
	ADD("+"),
	INCREMENT("++"),
	DECREMENT("--"),
	EQUAL("="),
	DIVIDE ("/"),
	MULTIPLY("*"),
	SUBTRACT("-"),

	COMMA(","),
	COLON(":"),
	SEMICOLON(";"),
	BANG("!"),
	QUESTION("?"),
	CARROT("^"),
	FUNC_POINT("=>"),

	LESS_THAN("<"),
	GREATER_THAN(">"),
	LESS_EQUAL("<="),
	GREATER_EQUAL(">="),
	NOT_EQUAL(";="),
	PIPE("|"),
	AND("&"),

	WHITESPACE("       "),
	NAME("abcaosdijawef"),
	STRING("""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""),

	NEWLINE("\n"),
	SINGLE_QUOTE("'"),
	DOUBLE_QUOTE("\'");

	companion object {
		fun of(value: String): TokenType? = entries.find { it.value == value }
	}
}

enum class OperatorType(val value: String) {
	ADD("+"),
	SUB("-"),
	MUL("*"),
	DIV("/"),
	EXP("^"),
	GT(">"),
	GE(">="),
	LT("<"),
	LE("<="),
	OR("|"),
	AND("&"),
	COM(","),
	E("="),
	EE("=="),
	EEE("==="),
	EEEE("===="),
	NE(";="),
	NEE(";=="),
	NEEE(";===");

	companion object {
		val stringToOperator: Map<String, OperatorType> = entries.associateBy { it.value }
		fun of(value: String): OperatorType? = stringToOperator[value]
	}
}

data class Token(
	val type: TokenType,
	val value: String,
	val line: Int, // todo: these dont get hashed in the python one
	val column: Int // ^
)