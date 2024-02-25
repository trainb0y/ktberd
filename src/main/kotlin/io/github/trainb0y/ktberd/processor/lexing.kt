package io.github.trainb0y.ktberd.processor

import io.github.trainb0y.ktberd.Token
import io.github.trainb0y.ktberd.TokenType
import io.github.trainb0y.ktberd.alphNums
import io.github.trainb0y.ktberd.raiseErrorAtLine

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

fun getStringToken(code: String, c: Int, filename: String, errorLine: Int): Pair<Int, String> {
	var curr = c
	var quoteValue = ""
	while (""""'"""".contains(code[curr])) {
		quoteValue += code[curr]
		if (isMatchingPair(quoteValue)) {
			return Pair(curr, "")
		}
		curr += 1
	}
	var quoteCount = getQuoteCount(quoteValue)

	var value = ""
	while (curr < code.length) {
		var runningCount = 0
		var quoteStart = curr
		while (code[curr] in """"'""") {
			runningCount += if (code[curr] == '"') 2 else 1
			if (runningCount == quoteCount) {
				return Pair(curr, value)
			}
			curr += 1
		}
		value += code.subSequence(quoteStart, curr + 1)
		curr += 1
	}
	raiseErrorAtLine(filename, code, errorLine, "Invalid string. Starting quotes do not match opening quotes.")
	TODO() // will never be reached (python return never type, is there a kt equivalent?)
}

fun tokenize(filename: String, c: String): List<Token> {
	val code = "$c   " // avoids some out of bounds issues
	var lineCount = 1
	var tokens = mutableListOf<Token>()
	var curr = 0
	var start = 0

	while (curr < code.length) {
		when (code[curr]) {
			'\n' -> {
				lineCount += 1
				start = curr
				addToTokens(tokens, lineCount, curr - start, TokenType.NEWLINE)
			}
			'}' -> addToTokens(tokens, lineCount, curr - start, TokenType.R_CURLY)
			'{' -> addToTokens(tokens, lineCount, curr - start, TokenType.L_CURLY)
			'[' -> addToTokens(tokens, lineCount, curr - start, TokenType.L_SQUARE)
			']' -> addToTokens(tokens, lineCount, curr - start, TokenType.R_SQUARE)
			'.' -> addToTokens(tokens, lineCount, curr - start, TokenType.DOT)
			':' -> addToTokens(tokens, lineCount, curr - start, TokenType.COLON)
			'|' -> addToTokens(tokens, lineCount, curr - start, TokenType.PIPE)
			'&' -> addToTokens(tokens, lineCount, curr - start, TokenType.AND)
			';' -> {
				var value = ";"
				while (code[curr + 1] == '=') {
					value += "="
					curr += 1
				}
				if (value.length == 1) addToTokens(tokens, lineCount, curr - start, TokenType.SEMICOLON)
				else addToTokens(tokens, lineCount, curr - start, TokenType.NOT_EQUAL, value)
			}
			',' -> addToTokens(tokens, lineCount, curr - start, TokenType.COMMA)
			'+' -> {
				if (code[curr+1] == '+') {
					addToTokens(tokens, lineCount, curr - start, TokenType.INCREMENT)
					curr += 1
				} else {
					// rip +=
					addToTokens(tokens, lineCount, curr - start, TokenType.ADD)
				}
			}
			'*' -> addToTokens(tokens, lineCount, curr - start, TokenType.MULTIPLY)
			'/' -> addToTokens(tokens, lineCount, curr - start, TokenType.DIVIDE)
			'^' -> addToTokens(tokens, lineCount, curr - start, TokenType.CARROT)
			'>' -> {
				if (code[curr+1] == '=') {
					addToTokens(tokens, lineCount, curr - start, TokenType.GREATER_EQUAL)
					curr += 1
				} else {
					addToTokens(tokens, lineCount, curr - start, TokenType.GREATER_THAN)
				}
			}
			'<' -> {
				if (code[curr+1] == '=') {
					addToTokens(tokens, lineCount, curr - start, TokenType.LESS_EQUAL)
					curr += 1
				} else {
					addToTokens(tokens, lineCount, curr - start, TokenType.LESS_THAN)
				}
			}
			'!' -> {
				var value = "!"
				while (code[curr + 1] == '!') {
					value += "!"
					curr += 1
				}
				addToTokens(tokens, lineCount, curr - start, TokenType.BANG, value)
			}
			'?' -> {
				var value = "?"
				while (code[curr + 1] == '?') {
					value += "?"
					curr += 1
				}
				if (value.length > 4) {
					raiseErrorAtLine(filename, code, lineCount, "User is too confused. Aborting due to trust issues.")
				}
				addToTokens(tokens, lineCount, curr - start, TokenType.QUESTION, value)
			}
			'=' -> {
				var value = "="
				if (code[curr+1] == '>') {
					curr += 1 // todo: proper order? seems conflicted but ok
					addToTokens(tokens, lineCount, curr - start, TokenType.FUNC_POINT)
				}
				else {
					while (code[curr + 1] == '=') {
						value += "="
						curr += 1
					}
					addToTokens(tokens, lineCount, curr - start, TokenType.EQUAL, value)
				}
			}
			'"', '\'' -> {
				val (newCurr, value) = getStringToken(code, curr, filename, lineCount)
				curr = newCurr
				addToTokens(tokens, lineCount, curr - start, TokenType.STRING, value)
			}
			' ', '\t', '(', ')' -> {
				if (code[curr] == '(' && curr + 1 < code.length && code[curr + 1] == ')') {
					// jank alert
					addToTokens(tokens, lineCount, curr - start, TokenType.WHITESPACE, "")
					addToTokens(tokens, lineCount, curr - start, TokenType.NAME, "")
					addToTokens(tokens, lineCount, curr - start, TokenType.WHITESPACE, "")
					curr += 1
				} else {
					var value = getEffectiveWhitespaceValue(code[curr].toString())
					while (curr + 1 < code.length && code[curr + 1] in " ()\t") {
						value += getEffectiveWhitespaceValue(code[curr + 1].toString())
						curr += 1
					}
					addToTokens(tokens, lineCount, curr - start, TokenType.WHITESPACE, value)
				}
			}
			else -> {
				var value = code[curr].toString()
				while (code[curr+1] in alphNums) {
					curr += 1
					value += code[curr]
				}
				addToTokens(tokens, lineCount, curr - start, TokenType.NAME, value)
			}
		}
	}
	return tokens
}