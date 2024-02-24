package io.github.trainb0y.ktberd

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
		fun of(value: String): OperatorType? = OperatorType.entries.find { it.value == value }
	}
}