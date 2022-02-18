package main.DATABASE

import java.io.File

fun main() {
}

object FileAccess {

    var productList: MutableList<String?> = makeProductList()

    private fun makeProductList(): MutableList<String?> {
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
        File("main/DATABASE/Products/PRODUCTS").forEachLine {
            val word = it.substringBefore(';')
            list.add(word.toInt(), it)
        };return list
    }

    private fun getCoins(): Int {
        var text = File("main/DATABASE/Coins/CoinDeposit.txt").readText()
        text = text.substringBeforeLast(';')
        return text.toInt()

    }

    //products usa .split, lista de strings

    fun addCoins(coinsToAdd: Int): Int {
        if (coinsToAdd <= 0) return 0
        val currentCoins = getCoins()
        val coinsToWrite = currentCoins + coinsToAdd
        val myfile = File("main/DATABASE/Coins/CoinDeposit.txt").bufferedWriter().use { out ->
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

    fun removeProduct(id: Int){
        val prod = productList[id]!!.split(';').toMutableList()
        prod[2] = "${prod[2].toInt()-1}"
        productList[id] = prod.joinToString(";")
        updateProducts()
    }

    /**
     * Maintenance Mode Update product with a certain quantity
     */
    fun updateProduct(id: Int, quant:Int){
        val prod = productList[id]!!.split(';').toMutableList()
        prod[2] = "${prod[2].toInt()+quant}"
        productList[id] = prod.joinToString(";")
        updateProducts()
    }

    /**
     * Deletes a certain product from the PRODUCTS file
     */
    fun deleteProduct(id:Int){
        productList[id]=null
        updateProducts()
    }

    /**
     * File Update
     */
    private fun updateProducts(){
        val prodFile = File("main/DATABASE/Products/PRODUCTS")
        prodFile.bufferedWriter().use {
                out ->
            for (i in 0 .. productList.size-1){
                if(productList[i] != null) out.write(productList[i]+System.lineSeparator())
            }
        }
    }
}