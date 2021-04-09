package org.jetbrains.vavilov

import org.jetbrains.vavilov.CallChainToFilterAndMap.SyntaxException
import org.jetbrains.vavilov.CallChainToFilterAndMap.TypeException

object CallChainToFilterAndMapImpl : CallChainToFilterAndMap {
    private class Call(val isFilter: Boolean, val expr: String, var rest: String)
    private class Expr(val arithmetic: Boolean, val expr: String, val rest: String)

    override fun callChainToFilterAndMap(callChain: String): String {
        val filterString = StringBuilder()
        var mapString = "element"

        var call = Call(false, mapString, callChain)
        while (call.rest.isNotEmpty()) {
            call = parseCall(call.rest, mapString)
            if (call.isFilter) {
                if (filterString.isNotEmpty()) {
                    filterString.append('&')
                }
                filterString.append(call.expr)
            } else {
                mapString = call.expr
            }

            val rest = call.rest
            if (rest.isNotEmpty()) {
                if (!rest.startsWith("%>%")) {
                    throw SyntaxException()
                } else {
                    call.rest = rest.substringAfter("%>%")
                    if (call.rest.isEmpty()) {
                        throw SyntaxException()
                    }
                }
            }
        }

        if (filterString.isEmpty()) {
            // nothing to filter
            filterString.append("(1=1)")
        }
        return "filter{$filterString}%>%map{$mapString}"
    }

    private fun parseCall(call: String, mapString: String): Call {
        val isFilter: Boolean
        val expr: String

        when {
            call.startsWith("filter{") -> {
                expr = call.substringAfter("filter{")
                isFilter = true
            }
            call.startsWith("map{") -> {
                expr = call.substringAfter("map{")
                isFilter = false
            }
            else -> {
                throw SyntaxException()
            }
        }

        val res = parseExpr(expr, mapString)
        val rest = res.rest

        if (rest.isEmpty() || rest[0] != '}') {
            throw SyntaxException()
        }

        if (isFilter && res.arithmetic || !isFilter && !res.arithmetic) {
            throw TypeException()
        }

        return Call(isFilter, res.expr, rest.substringAfter('}'))
    }

    private fun parseExpr(expr: String, mapString: String): Expr {
        if (expr.startsWith("element")) {
            return Expr(true, mapString, expr.substringAfter("element"))
        }

        val sb = StringBuilder()
        if (expr.isEmpty()) {
            throw SyntaxException()
        }

        // parse binary expression
        if (expr[0] == '(') {
            val expr1 = parseExpr(expr.substringAfter('('), mapString)
            if (expr1.rest.isEmpty()) {
                // there must be an operator
                throw SyntaxException()
            }
            val op = expr1.rest[0]
            val expr2 = parseExpr(expr1.rest.substringAfter(op), mapString)
            val arithmetic: Boolean

            // check types
            when (op) {
                '+', '-', '*' -> {
                    arithmetic = true
                    if (!(expr1.arithmetic && expr2.arithmetic)) {
                        throw TypeException()
                    }
                }
                '>', '<', '=' -> {
                    arithmetic = false
                    if (!(expr1.arithmetic && expr2.arithmetic)) {
                        throw TypeException()
                    }
                }
                '&', '|' -> {
                    arithmetic = false
                    if (expr1.arithmetic || expr2.arithmetic) {
                        throw TypeException()
                    }
                }
                else -> throw SyntaxException() // bad operator
            }

            if (expr2.rest.isEmpty() || expr2.rest[0] != ')') {
                throw SyntaxException()
            }

            return Expr(
                arithmetic, '(' + expr1.expr + op + expr2.expr + ')',
                expr2.rest.substringAfter(')')
            )
        }

        // parse constant expression
        var currentPosition = 0
        if (expr[currentPosition] == '-') {
            sb.append('-')
            currentPosition++
            if (expr.length <= currentPosition) {
                throw SyntaxException()
            }
        }

        while (expr.length > currentPosition && expr[currentPosition].isDigit()) {
            sb.append(expr[currentPosition++])
        }


        return Expr(true, sb.toString(), expr.substring(currentPosition))
    }

}
 

