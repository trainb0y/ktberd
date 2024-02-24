package io.github.trainb0y.ktberd.processor

import io.github.trainb0y.ktberd.Token
import io.github.trainb0y.ktberd.TokenType

fun addToTokens(tokenList: MutableList<Token>, line: Int, column: Int, token: TokenType, value: String? = null) {
	tokenList.add(Token(token, value ?: token.value, line, column))
}

fun getEffectiveWhitespaceValue(c: String): String = when (c) {
	in arrayOf(" ", '(') -> " "
	"\t" -> c
	else -> ""
}

fun getQuoteCount(quoteValue: String): Int = quoteValue.map { c -> if (c == '"') 2 else 1 }.sum()

fun isMatchingPair(quoteValue: String): Boolean {
	val totalSum = getQuoteCount(quoteValue)
	if (totalSum % 2 != 0) return false

	for (i in quoteValue.indices) {
		if (getQuoteCount(quoteValue.toList().subList(0, i).toString()) == totalSum / 2) return true
	}
	return false
}

fun getStringToken(code: String, curr: Int, filename: String, errorLine: Int): Pair<Int, String> {

}