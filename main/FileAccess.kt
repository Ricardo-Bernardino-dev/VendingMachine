package main

import java.io.File

fun main() {
}

object FileAccess {

    fun makeProductList(): List<String?> {
        val list = mutableListOf<String?>(
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        )
        var i = 0
        File("main/PRODUCTS").forEachLine {
            val word = it.substringBefore(';')
            list.add(word.toInt(), it)
        };return list
    }

    fun getCoins(): Int {
        var text = File("main/CoinDeposit.txt").readText()
        text = text.substringBeforeLast(';')
        return text.toInt()

    }

    //products usa .split, lista de strings

    fun addCoins(coinsToAdd: Int): Int {
        if (coinsToAdd <= 0) return 0
        val currentCoins = getCoins()
        val coinsToWrite = currentCoins + coinsToAdd
        val myfile = File("main/CoinDeposit.txt").bufferedWriter().use { out ->
            out.write("$coinsToWrite;")
        }
        return 1
    }


    //in test
    fun addProduct(id: Int, name: String, quantity: Int, price: Int) {
        val myfile = File("main/PRODUCTS.txt").bufferedWriter().use { out ->
            out.write("$id;$name;$quantity$price")
        }
    }

    fun removeProduct(id: Int) {

    }

    //fun getProducts():List<String>
}