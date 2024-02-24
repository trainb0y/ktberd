package io.github.trainb0y.ktberd

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