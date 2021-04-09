package org.jetbrains.vavilov

interface CallChainToFilterAndMap {
    class SyntaxException : Exception("SYNTAX ERROR")
    class TypeException : Exception("TYPE ERROR")

    fun callChainToFilterAndMap(callChain: String): String
}