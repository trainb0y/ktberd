package io.github.trainb0y.ktberd.processor

import io.github.trainb0y.ktberd.NonFormattedError
import io.github.trainb0y.ktberd.OperatorType
import io.github.trainb0y.ktberd.Token
import io.github.trainb0y.ktberd.TokenType
import io.github.trainb0y.ktberd.raiseErrorAtToken

abstract class ExpressionTreeNode {
	abstract fun toStr(tabs: Int = 0): String // todo: kinda un-kotliny

}

class SingleOperatorNode(val expression: ExpressionTreeNode, val operator: Token): ExpressionTreeNode() {
	override fun toStr(tabs: Int): String {
		val tabby = "\t".repeat(tabs)
		return """
			${tabby}Single Operator:
			${tabby}Operator: ${operator.value}
			${tabby}Expression:
			${expression.toStr(tabs + 2)}
		""".trimIndent()
	}
}

class ListNode(val values: List<ExpressionTreeNode>): ExpressionTreeNode() {
	override fun toStr(tabs: Int): String {
		val tabby = "\t".repeat(tabs)
		val valuesStr = values.joinToString("\n") { it.toStr(tabs + 2) }
		return """
			${tabby}List:
			${tabby + "\t"}Values:
			$valuesStr
		""".trimIndent()
	}
}

class ExpressionNode(val left: ExpressionTreeNode, val right: ExpressionTreeNode, val operator: OperatorType, val operatorToken: Token):
	ExpressionTreeNode() {
	override fun toStr(tabs: Int): String {
		val tabby = "\t".repeat(tabs)
		return """
			${tabby}Expression:
			${tabby + "\t"}Operator: ${operator}:
			${tabby + "\t"}Left:
			${left.toStr(tabs + 2)}
			${tabby + "\t"}Right:
			${right.toStr(tabs + 2)}
		""".trimIndent()
	}
}

class FunctionNode(val name: Token, val args: List<ExpressionTreeNode>): ExpressionTreeNode() {
	override fun toStr(tabs: Int): String {
		val tabby = "\t".repeat(tabs)
		val argsStr = args.joinToString("\n") { it.toStr(tabs + 2) }
		return """
			${tabby}Function:
			${tabby + "\t"}Name: ${name.value}
			${tabby + "\t"}Argsuments:
			$argsStr
		""".trimIndent()
	}
}

class IndexNode(val value: ExpressionTreeNode, val index: ExpressionTreeNode): ExpressionTreeNode() {
	override fun toStr(tabs: Int): String {
		val tabby = "\t".repeat(tabs)
		return """
			${tabby}Index:
			${tabby + "\t"}Of:
			${value.toStr(tabs + 2)}
			${tabby + "\t"}At:
			${index.toStr(tabs + 2)}
		""".trimIndent()
	}
}

class ValueNode(val nameOrValue: Token): ExpressionTreeNode() {
	override fun toStr(tabs: Int): String {
		val tabby = "\t".repeat(tabs)
		return "${tabby}Value: ${nameOrValue.value}"
	}
}

fun getExprFirstToken(expr: ExpressionTreeNode): Token? {
	return when (expr) {
		is SingleOperatorNode -> expr.operator
		is ExpressionNode -> getExprFirstToken(expr.left) ?: getExprFirstToken(expr.right)
		is FunctionNode -> expr.name
		is ListNode -> expr.values.firstOrNull()?.let { getExprFirstToken(it) }
		is ValueNode -> expr.nameOrValue
		is IndexNode -> getExprFirstToken(expr.value) ?: getExprFirstToken(expr.index)
		else -> null
	}
}

fun buildExpressionTree(filename: String, tokens: List<Token>, code: String): ExpressionTreeNode {
	/**
	 * This language has significant whitespace, so the biggest split happens where there is most space
	 * - func a, b  +  c becomes func(a, b) + c but func a, b+c  becomes func(a, b + c)
	 * - a + func   b  ,  c + d is not legal because it translates to (a + func)(b, c + d)
	 * - 2 * 1+3 becomes 2 * (1 + 3)
	 */
	if (tokens.isEmpty()) throw NonFormattedError("Wow, you broke it. Congrats")


	// tabs at beginning or end don't matter
	for (token in tokens.subList(1, tokens.size - 1)) {
		if (token.type == TokenType.WHITESPACE && '\t' in token.value) {
			raiseErrorAtToken(filename, code, "Tabs are not allowed in expressions.", token)
		} else if (token.type == TokenType.NEWLINE) {
			raiseErrorAtToken(filename, code, "Due to the laws of significant whitespace, no newline characters are permitted in expressions. If your code is so long that it needs newlines, consider rewriting it :)", token)
		}
	}

	val tokensWithoutWhitespace = tokens.filterNot { it.type == TokenType.WHITESPACE }

	if (tokensWithoutWhitespace.size == 2 &&
		tokensWithoutWhitespace.first().type == TokenType.L_SQUARE &&
		tokensWithoutWhitespace.last().type == TokenType.R_SQUARE
		) return ListNode(emptyList())


	val startsWithWhitespace = tokens.first().type == TokenType.WHITESPACE
	val endsWithWhitespace = tokens.last().type == TokenType.WHITESPACE

	val updatedList = tokens.map{ OperatorType.of(it.value) }
	var maxWidth = -1
	var maxIndex = -1
	var bracketLayers = 0

	for (i in updatedList.indices) {
		when (tokens[i].type) {
			TokenType.L_SQUARE -> bracketLayers += 1
			TokenType.R_SQUARE -> bracketLayers -= 1
			else -> {}
		}
		if (updatedList[i] is OperatorType && bracketLayers == 0) {
			try {
				// bad bad bad! (negative sign check)
				if (i == 0 || tokens[i - 1].type == TokenType.WHITESPACE &&
					tokens[i+1].type != TokenType.WHITESPACE &&
					updatedList[i] == OperatorType.SUB
				) continue

				var lLen = 0;
				var rLen = 0;

				if (tokens[i-1].type == TokenType.WHITESPACE)
					lLen = tokens[i-1].value.length
				if (tokens[i+1].type == TokenType.WHITESPACE)
					rLen = tokens[i+1].value.length
				if (lLen != rLen && updatedList[i] != OperatorType.COM)
					raiseErrorAtToken(filename, code, "Whitespace must be equal on either side of an operator.", tokens[i])
				if (rLen > maxWidth) {
					maxWidth = rLen
					maxIndex = i
				}
			}
			catch (e: IndexOutOfBoundsException) {
				raiseErrorAtToken(filename, code, "Operator cannot be at the end of an expression.", tokens[i])
			}
		}
	}

	// single argument function
	val startsWithOperator = tokensWithoutWhitespace[0].type in arrayOf(TokenType.SEMICOLON, TokenType.SUBTRACT))

	val firstNameIndex = startsWithWhitespace.numify + startsWithOperator.numify

	if (tokens.size >= 3 + firstNameIndex &&
		tokens[firstNameIndex].type == TokenType.NAME &&
		tokens[firstNameIndex + 1].type == TokenType.WHITESPACE &&
		tokens[firstNameIndex + 2].type in arrayOf(
			TokenType.NAME,
			TokenType.L_SQUARE,
			TokenType.STRING,
			TokenType.SUBTRACT,
			TokenType.SEMICOLON
		) &&
		tokens[firstNameIndex + 1].value.length > maxWidth) {
			val functionNode = FunctionNode(
				tokens[firstNameIndex],
				listOf(buildExpressionTree(filename, tokens.subList(firstNameIndex + 1, tokens.size), code))
			)
			if (startsWithOperator) {
				return SingleOperatorNode(functionNode, tokensWithoutWhitespace[0])
			} else {
				return functionNode
			}
		}

	// check for operator at beginning // fixme: might have messed this up (conversion)
	if (startsWithOperator && (maxIndex == -1 || run {
			val t = tokens[startsWithWhitespace.numify + 1]
			t.type == TokenType.WHITESPACE && t.value.length > maxWidth || updatedList[maxIndex] == OperatorType.COM
		})) {
		return SingleOperatorNode(buildExpressionTree(filename, tokens.subList(startsWithWhitespace.numify + 1, tokens.size), code), tokensWithoutWhitespace[0])
	}

	// value, like a list
	if (maxIndex == -1) {
		val nameOrValue: Token
		try {
			nameOrValue = tokensWithoutWhitespace[0]
			if (nameOrValue.type !in arrayOf(TokenType.NAME, TokenType.L_SQUARE, TokenType.STRING)) throw Exception()
		} catch (e: Exception) {
			raiseErrorAtToken(filename, code, "Expected name or value.", tokensWithoutWhitespace[0])
			TODO() // will never be reached
		}

		if (nameOrValue.type == TokenType.L_SQUARE) {
			bracketLayers = 1
			for ((i, token) in tokensWithoutWhitespace.subList(1, tokensWithoutWhitespace.size).withIndex() ) {// fixme: unsure
				when (token.type) {
					TokenType.L_SQUARE -> bracketLayers += 1
					TokenType.R_SQUARE -> bracketLayers -= 1
					else -> {}
				}
				if (bracketLayers == 0) {
					// closing end of list
					if (i == tokensWithoutWhitespace.size - 1) {
						// split by most significant comma
						// ambiguity hell btw (see original python)

						// TODO: COME BACK

					}
				}
			}
		}
	}
}


// leftover from python version
private val Boolean.numify: Int
	get() =  if (this) 1 else 0