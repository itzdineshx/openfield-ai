package com.openfield.app.feature.tools.math

import java.util.ArrayDeque
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

/**
 * Result of evaluating a mathematical expression deterministically without LLM inference.
 */
sealed class MathResult {
    data class Success(val value: Double, val formatted: String) : MathResult()
    data class Error(val message: String) : MathResult()
}

/**
 * Robust deterministic expression evaluator using Shunting-yard algorithm and RPN evaluation.
 * Evaluates standard arithmetic, precedence, parentheses, powers, percentages, and basic functions.
 * Guarantees zero hallucination and zero network/model usage.
 */
object DeterministicMathEngine {

    fun evaluate(expression: String): MathResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return MathResult.Error("Empty expression")

        return try {
            val tokens = tokenize(trimmed)
            if (tokens.isEmpty()) return MathResult.Error("Invalid expression syntax")
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn)

            if (result.isNaN()) return MathResult.Error("Undefined result (NaN)")
            if (result.isInfinite()) return MathResult.Error("Division by zero")

            val formatted = formatNumber(result)
            MathResult.Success(result, formatted)
        } catch (e: ArithmeticException) {
            MathResult.Error(e.message ?: "Arithmetic error")
        } catch (e: Exception) {
            MathResult.Error(e.message ?: "Syntax error")
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val s = expr.replace(" ", "")

        while (i < s.length) {
            val c = s[i]

            // Numbers (including decimals)
            if (c.isDigit() || (c == '.' && i + 1 < s.length && s[i + 1].isDigit())) {
                val sb = StringBuilder()
                while (i < s.length && (s[i].isDigit() || s[i] == '.')) {
                    sb.append(s[i])
                    i++
                }
                tokens.add(sb.toString())
                continue
            }

            // Word functions: sqrt, abs, round
            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < s.length && s[i].isLetter()) {
                    sb.append(s[i])
                    i++
                }
                val fn = sb.toString().lowercase(Locale.ROOT)
                if (fn in listOf("sqrt", "abs", "round")) {
                    tokens.add(fn)
                } else {
                    throw IllegalArgumentException("Unknown function '$fn'")
                }
                continue
            }

            // Unary minus handling
            if (c == '-') {
                val isUnary = tokens.isEmpty() || tokens.last() in listOf("(", "+", "-", "*", "/", "^")
                if (isUnary) {
                    tokens.add("neg")
                    i++
                    continue
                }
            }

            // Operators and punctuation
            when (c) {
                '+', '-', '*', '/', '^', '(', ')', '%' -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> throw IllegalArgumentException("Unexpected character '$c'")
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "neg" -> 4
            "^" -> 3
            "*", "/", "%" -> 2
            "+", "-" -> 1
            else -> 0
        }
    }

    private fun isRightAssociative(op: String): Boolean = op == "^" || op == "neg"

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in listOf("sqrt", "abs", "round") -> stack.push(token)
                token == "(" -> stack.push(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isEmpty()) throw IllegalArgumentException("Mismatched parentheses")
                    stack.pop() // remove '('
                    if (stack.isNotEmpty() && stack.peek() in listOf("sqrt", "abs", "round")) {
                        output.add(stack.pop())
                    }
                }
                else -> { // Operator
                    val p = precedence(token)
                    while (stack.isNotEmpty() && stack.peek() != "(" &&
                        (precedence(stack.peek()) > p || (precedence(stack.peek()) == p && !isRightAssociative(token)))
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.pop()
            if (top == "(" || top == ")") throw IllegalArgumentException("Mismatched parentheses")
            output.add(top)
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
                continue
            }

            when (token) {
                "neg" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax for unary minus")
                    stack.push(-stack.pop())
                }
                "sqrt" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for sqrt")
                    val a = stack.pop()
                    if (a < 0) throw ArithmeticException("Cannot take square root of negative number")
                    stack.push(sqrt(a))
                }
                "abs" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for abs")
                    stack.push(abs(stack.pop()))
                }
                "round" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Missing argument for round")
                    stack.push(round(stack.pop()))
                }
                "+" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operands for '+'")
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(a + b)
                }
                "-" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operands for '-'")
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(a - b)
                }
                "*" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operands for '*'")
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(a * b)
                }
                "/" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operands for '/'")
                    val b = stack.pop()
                    val a = stack.pop()
                    if (b == 0.0) throw ArithmeticException("Division by zero")
                    stack.push(a / b)
                }
                "%" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operands for '%'")
                    val b = stack.pop()
                    val a = stack.pop()
                    if (b == 0.0) throw ArithmeticException("Division by zero in modulo")
                    stack.push(a % b)
                }
                "^" -> {
                    if (stack.size < 2) throw IllegalArgumentException("Missing operands for '^'")
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(a.pow(b))
                }
                else -> throw IllegalArgumentException("Unknown token: $token")
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("Invalid expression structure")
        return stack.pop()
    }

    private fun formatNumber(value: Double): String {
        return if (value == round(value) && !value.isInfinite() && abs(value) < 1e12) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }
}
