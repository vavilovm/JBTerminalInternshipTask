package org.jetbrains.vavilov

import org.jetbrains.vavilov.CallChainToFilterAndMapImpl.callChainToFilterAndMap

fun main() {
    val s = readLine() ?: ""
    try {
        println(callChainToFilterAndMap(s))
    } catch (e: CallChainToFilterAndMap.TypeException) {
        println("TYPE ERROR")
    } catch (e: CallChainToFilterAndMap.SyntaxException) {
        println("SYNTAX ERROR")
    }
}