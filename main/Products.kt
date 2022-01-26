package main

fun main() {
    Products.buildProducts()
    println(Products.products)
}

object Products {
    /**
     * Lista de produtos com capacidade máxima de 16 tipos de produto
     */
    var products = mutableListOf<Product?>(
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


    /**
     * Objeto Produto, com id, nome, preço e quantidade
     */
    class Product(
        var id: Int, var name: String,
        var quantity: Int, var price: Int
    ) {

        override fun toString(): String {
            return "$id;$name;$quantity;$price"
        }
    }

    fun buildProducts() {
        val list = FileAccess.makeProductList()
        for (i in 0..list.size - 1) {
            if (list[i] != null) {
                val word = list[i]!!.split(';')
                products[i] = Product(i, word[1], word[2].toInt(), word[3].toInt())
            }
        }

    }


    /*
    fun getProduct(id:Int):Product{
        return products[id]
    }

     */

    fun editProduct() {

    }

    fun addProduct(id: Int, name: String, quantity: Int, price: Int): String {
        val prod = Product(id, name, quantity, price)
        products.add(id, prod)
        FileAccess.addProduct(id, name, quantity, price)
        return prod.toString()
    }

    fun removeProduct(id: Int) {
        products.removeAt(id)
    }

}