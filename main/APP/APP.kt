package main.APP

import isel.leic.utils.Time
import main.CoinAcceptor
import main.DATABASE.CoinDeposit
import main.DATABASE.Products
import main.UI.Dispenser
import main.UI.TUI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.system.exitProcess

fun main(){
    var app: APP = APP()
}

class APP {
    /**
     * Mode 0 - Manual
     * Mode 1 - Arrow
     */
    var mode = 0

    /**
     * Desired timeout for all application processes
     */
    val timeout: Long = 1000

    /**
     * First {Product} of the list of {Products}, obtained after building the list
     */
    var firstProd: Products.Product? = null

    /**
     * Last {Product} visualized by the user
     */
    var currentProd: Products.Product? = null


    /**
     * Get the first Product found in the list of products
     */
    private fun getFirstProduct(): Products.Product? {
        var notFinished = true
        var i = 0
        var firstItem: Products.Product? = null
        while (notFinished) {
            if(i==Products.products.size) emptyMachineStock()
            if (Products.products[i] != null) {
                firstItem = Products.products[i]!!
                notFinished = false
            }
            i++
        }
        return firstItem
    }

    /**
     * Get the first Product found in the list of products
     */
    private fun getLastProduct(): Products.Product? {
        var notFinished = true
        var i = Products.products.size-1
        var lastItem: Products.Product? = null
        while (notFinished) {
            if (Products.products[i] != null) {
                lastItem = Products.products[i]!!
                notFinished = false
            }
            --i
        }
        return lastItem
    }

    /**
     * Function that displays the text shown in case of Machine stock is empty
     */
    private fun emptyMachineStock() {
        TUI.clear()
        /*TUI.cursor(0,0)
        TUI.writeS(" Empty machine  ")
        TUI.cursor(1,0)
        TUI.writeS("     Stock     ")*/
        writeCentered ("Empty machine",0)
        writeCentered ("Stock",1)
    }


    /**
     * Home screen of the application, updates time constantly and
     * date if the time is passed due midnight
     */
    fun home() {
        TUI
        TUI.clear()
        //TUI.cursor(0, 0)
        //TUI.writeS("Vending Machine")
        writeCentered ("Vending Machine",0)
        TUI.cursor(1, 0)
        val currentDateTime = LocalDateTime.now()
        var date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        var time = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        TUI.writeS(date + "   ")
        TUI.writeS(time)
        hideCursor()
        var datechanges: Boolean
        var changed: String?
        var key: Char
        do {
            changed = timechanges(time)
            if (changed != null) {
                TUI.cursor(1, 11)
                TUI.writeS(changed)
                time = changed
                datechanges = (time == "00:01")
                if (datechanges) {
                    date = currentDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    TUI.cursor(1, 0)
                    TUI.writeS(date)
                }
                hideCursor()
            }
            key = TUI.getKey()
            if (M.isMaintenance()) MaintenanceMode()
        } while (key != '#' && key != '*')
        if(key == '#') {
            if (mode == 0) sellManual()
            else SellArrow()
        }
        if(key=='*') cycleProductsDisplay()
    }

    fun writeCentered (text:String, line:Int){
        val Ntext = text.length
        var col = 0
        if (Ntext < 15){
            col = (16 - Ntext)
            if(col%2!=0) col+=1
        }
        TUI.cursor (line, col/2)
        TUI.writeS(text)
    }

    fun hideCursor(){
        TUI.cursor (3, 16)
    }

    fun clearLine(line:Int){
        TUI.cursor (line,0)
        TUI.writeS("                ")
    }


    /**
     * Feature that cycles through all available products (quantity above zero)
     * Press * to return to Home Screen
     */
    private fun cycleProductsDisplay() {
        TUI.clear()
        writeCentered("All Available", 0)
        writeCentered("Products:", 1)
        //TUI.cursor(0,0)
        //TUI.writeS(" All Available  ")
        //TUI.cursor(1,0)
        //TUI.writeS("   Products:    ")
        TUI.cursor(3,16)
        Time.sleep(2500)
        var idx = 0
        var key :String? = null
        while(true){
            if(idx==Products.products.size) home()
            TUI.clear()
            if(Products.products[idx]!=null) {
                if(Products.products[idx]!!.quantity > 0){
                    writeProductManual(Products.products[idx]!!)
                    key = TUI.pressedKeyManual(3000)
                    if(key=="*") home()
                }
            }
            idx++
        }
    }

    /**
     * Returns the current time (if changed), null if not.
     */
    private fun timechanges(time: String?): String? {
        val currentDateTime = LocalDateTime.now()
        val currenttime = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        if (currenttime == time) return null
        return currenttime
    }

    /**
     * Initializes all basic components and the product list from the PRODUCTS.txt file
     */
    fun init() {
        TUI.init()
        Products.buildProducts()
        firstProd = getFirstProduct()
        currentProd = firstProd
    }


    /**
     * Primary Initializer of the Application
     * Initializes all basic components (init function) and enters Home Screen
     */
    init {
        init()
        val home = home()
        val sale = sellManual()
        // if(Sale) SaleMode()
        //else MaintenanceMode()
    }

    /**
     * Sell Mode through Arrow Interaction (8 Down or 2 Up)
     */
    private fun SellArrow() {
        TUI.clear()
        writeProductArrow(currentProd!!)
        var current = currentProd!!.id
        var counter = 0
        while (counter <= 5) {
            val idx = TUI.arrowInteraction(Products.products,current)
            if(idx=='*'.code){
                mode = 0
                sellManual()
            }
            when(idx){
                -1 -> {
                    val last = getLastProduct()!!
                    writeProductArrow(last)
                    current = last.id
                    counter = 0
                    currentProd = last
                }

                Products.products.size ->{
                    val first = getFirstProduct()!!
                    writeProductArrow(first)
                    current = first.id
                    counter = 0
                    currentProd = first
                }

                null -> if(currentProd!!.quantity>0) sellProduct(currentProd!!)
                        else noStock()

                else -> {
                    current = idx
                    counter = 0
                    val prod = Products.products[idx]
                    writeProductArrow(prod!!)
                    currentProd = prod
                }
            }
        counter++
        }
        home()
    }


    /**
     * Second Version of the Sell Mode, implements a slower but more accurate key reader display
     */
    private fun sellManualV2() {
        TUI.clear()
        if (firstProd != null) {
            if(currentProd==firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            TUI.writeS("No Products")
            home()
        }
        var prod = 0
        var counter = 0
        var item = currentProd
        while (counter <= 8) {
            val key = TUI.pressedKeyManual(300)
            if (key == "*"|| key == "**") {
                mode = 1
                SellArrow()
            }
            if (key != null) {
                if (key != "#"){
                    counter = 0
                    prod = key.toInt()
                    if (key.length == 1) {
                        item = Products.products[key[0].digitToInt()]
                        if (item != null){
                            currentProd = item
                            writeProductManual(item)
                        }
                        else productNotAvailableS(key.toString())
                    } else {
                        println(key.toInt())
                        if (key.toInt() in 10..15) {
                            item = Products.products[key.toInt()]
                            if (item != null){
                                currentProd = item
                                writeProductManual(item)
                            }
                            else productNotAvailableS(key.toString())
                        } else productNotAvailableS(key.toString())
                    }
                };
                if(key== "#"){
                    if(currentProd!!.quantity>0) sellProduct(currentProd!!)
                    else noStock()
                }
               // if (key.toString() == "#" && Products.products[prod]!=null && Products.products[prod]!!.quantity > 0) sellProduct(prod)
            }
            counter++
        }
        home()
    }

    /**
     * The Sell Mode prints the last visualized item (first Product if application restarted),
     * and constantly waits for a key to be pressed.
     * If the key pressed is a first digit, it display's the Product immediatly and waits for the second key
     * if the second key isn't the digit '1', it prints the Product immediatly similarly to what was previously mentioned,
     * if the second key is the digit '1' it displays the full 2 number digit Product, if it exists in the product list
     */
    private fun sellManual() {
        TUI.clear()
        if (firstProd != null) {
            if (currentProd == firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            TUI.writeS("No Products")
            home()
        }
        val prod = selectProduct()
        if(prod!=null){
            if(prod.quantity>0) sellProduct(currentProd!!) else noStock()
        }
        home()
    }

    /**
     * Enters the Selection Screen to select a certain product, if the the user takes more than 5 seconds to press any key
     * timeout is given and the Application returns to the Home Screen.
     */
    private fun selectProduct(): Products.Product? { //Passar timeout como parametro
        var counter = 0
        var prod: Products.Product?
        var fullNumber:Int
        var key: Char?
        currentProd = firstProd
        var prevKey: String = currentProd!!.id.toString()
        while (counter <= 5) {
            key = TUI.pressedQuickKeyManual(1000)
            if(key!=null){
                if (key == '*') {
                    mode = 1
                    SellArrow()
                }
                if (key != '#') {
                    counter = 0
                    if(TUI.isSecondDigit&&TUI.firstPressedKey=='1'){
                        prevKey+= key.toString()
                        fullNumber = ("${TUI.firstPressedKey!!}$key").toInt()
                        if(fullNumber<Products.products.size) {
                            prod = Products.products[fullNumber]
                            if (prod != null){
                                TUI.isSecondDigit = false
                                currentProd = prod
                                writeProductManual(prod)
                            }
                            else{
                                prod = Products.products[key.digitToInt()]
                                if(prod!=null){
                                    TUI.isSecondDigit = false
                                    writeProductManual(prod)
                                    currentProd=prod
                                }else productNotAvailableC(key)
                            }
                        }
                        else {
                            TUI.isSecondDigit = false
                            prevKey = key.toString()
                            prod = Products.products[key.digitToInt()]
                            if(prod!=null) writeProductManual(prod) else productNotAvailableC(key)
                        }
                    }else{
                        prod = Products.products[key.digitToInt()]
                        prevKey = key.toString()
                        if (prod != null) {
                            currentProd = prod
                            writeProductManual(prod)
                            TUI.firstPressedKey = key
                            TUI.isSecondDigit = true
                        }else productNotAvailableC(key)
                    }
                }
                if(key== '#'&& (currentProd!!.id.toString() == prevKey)){
                    return currentProd!!
                }
            }
            counter++
        };return null
    }

    /**
     * If the Machine presents no products at all (no stock, no data in PRODUCTS.txt),
     * an "Out Of Stock" message is displayed
     */

    private fun noStock() {
        TUI.clear()
        /*TUI.cursor(0,0)
        TUI.writeS("  Out Of Stock  ")
        TUI.cursor(1,0)
        TUI.writeS("       :(       ")*/
        writeCentered ("Out Of Stock", 0)
        writeCentered (":(", 1)
        Time.sleep(2300)
    }

    /**
     * Function that sells a product passed as function parameter. Interacts directly with the CoinAcceptor and Products class functions that check
     * if a coin was inserted, and therefore collected, and modify the product quantity, respectivelly. If the coin value inserted matches the item price, the function proceeds to dispense
     * said product and returns to Home Screen after a farewell message.
     */
    private fun sellProduct(product: Products.Product) {
        println("Sell Product $product")
        val price = product.price
        var priceleft = price
        //TUI.cursor(1, 0)
        //TUI.writeS("      ${getCoinValue(price)}$      ")
        clearLine(1)
        writeCentered ("${getCoinValue(price)}$",1)
        hideCursor()
        var key: String? = null
        CoinAcceptor.init()
        while (true) {
            key = TUI.pressedKeyManual(500)
            if(key=="#"){
                abort()
                CoinAcceptor.ejectCoins()
                home()
            }
            if(CoinAcceptor.counter==price){
                CoinAcceptor.collectCoins()
                //TUI.cursor(1,0)
                //TUI.writeS("Collect Product")
                writeCentered ("Collect Product", 1)
                hideCursor()
                Dispenser.dispense(product.id)
                /*TUI.cursor(0,0)
                TUI.writeS("  Thank You!")
                TUI.cursor(1,0)
                TUI.writeS("  See You Soon!    ")*/
                TUI.clear()
                writeCentered ("Thank You!", 0)
                writeCentered ("See You Soon!",1)
                hideCursor()
                Time.sleep(2000)
                CoinDeposit.addCoins(CoinAcceptor.counter)
                Products.removeProduct(product.id)
                home()
            }
            if(CoinAcceptor.hasCoin()){
                CoinAcceptor.acceptCoin()
                priceleft-=5
                //TUI.cursor(1, 0)
                //TUI.writeS("      ${getCoinValue(priceleft)}$      ")
                writeCentered("${getCoinValue(priceleft)}$", 1)
                hideCursor()
                while(CoinAcceptor.hasCoin()){
                    if(key=="#"){
                        abort()
                        CoinAcceptor.ejectCoins()
                        home()
                    }
                }
            }
        }
    }

    /**
     * Aborts the current Sell Mode
     */
    private fun abort() {
        TUI.clear()
        TUI.writeS("Vending Aborted")
        hideCursor()
        if(CoinAcceptor.counter>0){
            //TUI.cursor(1,0)
            //TUI.writeS("  Return ${getCoinValue(CoinAcceptor.counter)}$")
            writeCentered ("Return ${getCoinValue(CoinAcceptor.counter)}$",1)
            hideCursor()
        }
    }

    /**
     * In-case the product selected has no information (no data in the PRODUCTS.txt),
     * a "Product Not Available" message is displayed
     * @param key - a String number of 2 digits
     */
    private fun productNotAvailableS(key: String) {
        TUI.clear()
        /*TUI.cursor(0, 3)
        TUI.writeS("Product $key")
        TUI.cursor(1, 1)
        TUI.writeS("Not Available")*/
        writeCentered ("Product $key", 0)
        writeCentered ("Not Available",1)
        hideCursor()
    }

    /**
     * In-case the product selected has no information (no data in the PRODUCTS.txt),
     * a "Product Not Available" message is displayed
     * @param key - a char number of 1 digit
     */
    private fun productNotAvailableC(key: Char) {
        TUI.clear()
        /*TUI.cursor(0, 3)
        TUI.writeS("Product $key")
        TUI.cursor(1, 1)
        TUI.writeS("Not Available")*/
        writeCentered ("Product $key", 0)
        writeCentered ("Not Available",1)
        hideCursor()
    }

    /**
     * Writes Product in Arrow Sell Mode
     */
    private fun writeProductArrow(prod: Products.Product) {
        TUI.clear()
        //TUI.cursor(0, 5)
        //TUI.writeS(prod.name)
        writeCentered(prod.name,0)
        TUI.cursor(1, 0)
        val prodId = prod.id
        if (prodId in 10..16) TUI.writeS("$prodId*")
        else TUI.writeS("0${prodId}*")
        TUI.cursor(1, 6)
        val prodQuant = prod.quantity
        if (prodQuant in 10..16) TUI.writeS("#${prodQuant}")
        else TUI.writeS("#0$prodQuant")
        TUI.cursor(1, 12)
        TUI.writeS("${getCoinValue(prod.price)}$")
    }
    /**
     * Writes Product in Manual Sell Mode
     */
    private fun writeProductManual(prod: Products.Product) {
        TUI.clear()
        writeCentered(prod.name, 0)
        //TUI.cursor(0, 5)
        //TUI.writeS(prod.name)
        TUI.cursor(1, 0)
        val prodId = prod.id
        if (prodId in 10..16) TUI.writeS(prodId.toString())
        else TUI.writeS("0$prodId")
        TUI.cursor(1, 6)
        val prodQuant = prod.quantity
        if (prodQuant in 10..16) TUI.writeS("#${prodQuant}")
        else TUI.writeS("#0$prodQuant")
        TUI.cursor(1, 12)
        TUI.writeS("${getCoinValue(prod.price)}$")
    }

    /**
     * Gets the respective Double value of the Coin, that represents a real value in a certain currency.
     */
    private fun getCoinValue(value: Int): Double {
        return value.toDouble() / 10
    }

    /**
     *
     *  -------------------------------------   Modo de Manutenção  -------------------------------------------
     */

    /**
     * Primary function of Maintenance Mode that is called when the M button is pressed and remains pressed.
     */

    private fun MaintenanceMode() {
        TUI.clear()
        //TUI.cursor(0, 0)
        //TUI.writeS("Maintenance Mode")
        writeCentered("Maintenance Mode",0)
        var key: Char
        do {
            TUI.cursor(1, 0)
            TUI.writeS("1-Dispense test")
            hideCursor()
            key = TUI.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !M.isMaintenance()) break
            TUI.cursor(1, 0)
            TUI.writeS("2-Update Prod. ")
            hideCursor()
            key = TUI.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !M.isMaintenance()) break
            TUI.cursor(1, 0)
            TUI.writeS("3-Remove Prod. ")
            hideCursor()
            key = TUI.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !M.isMaintenance()) break
            TUI.cursor(1, 0)
            TUI.writeS("4-Shutdown     ")
            hideCursor()
            key = TUI.waitKey(2000)
            if ((key == '1') || (key == '2') || (key == '3') || (key == '4') || !M.isMaintenance()) break
        } while (true)
        if (!M.isMaintenance()) home()
        when (key) {
            '1' -> dispenseTest()  //Dispense Test
            '2' -> updateProduct()  //Update Prod
            '3' -> removeProduct() //Remove Prod
            '4' -> shutdown() //Shutdown

        }
    }

    /**
     * Dispense Test that simulates the dispensing of a product.
     */

    private fun dispenseTest(){
        TUI.clear()
        if (firstProd != null) {
            if (currentProd == firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            TUI.writeS("No Products")
            home()
        }
        val prod = selectProduct()
        if(prod==null) MaintenanceMode()
        //TUI.cursor(1,0)
        //TUI.writeS("  *- to Print   ")
        clearLine(1)
        writeCentered ("*- to Print",1)
        var key: Char? = null
        do{
            key = TUI.pressedQuickKeyManual(1000)
            if(key=='#'){
                abort()
                MaintenanceMode()
            }
        }while(key!='*')
        //TUI.cursor(1,0)
        //TUI.writeS("Collect Product")
        clearLine(1)
        writeCentered("Collect Product",1)
        TUI.cursor(2,16)
        Dispenser.dispense(prod!!.id)
        MaintenanceMode()
    }

    /**
     * Product quantity update according to user's input
     */
    private fun updateProduct(){
        TUI.clear()
        if (firstProd != null) {
            if (currentProd == firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            TUI.writeS("No Products")
            home()
        }
        var firstKey: Char? = null
        var secondKey: Char? = null
        var key: Char?
        val prod = selectProduct()
        var ifPressed = false
        if(prod==null) MaintenanceMode()
        clearLine(1)
        TUI.cursor(1,0)
        TUI.writeS("Qty:??")
        TUI.cursor(1,4)
        var counter = 0
        do{
            if(ifPressed){
                TUI.cursor(0,0)
                TUI.writeS("Update "+prod!!.name)
                TUI.cursor(0,13)
                TUI.writeS("="+firstKey+secondKey)
                //TUI.cursor(1,0)
                //TUI.writeS("5-Yes  other-No")
                clearLine(1)
                writeCentered ("5-Yes  other-No",1)
                TUI.cursor(2,16)
                counter = 0
                while(counter<=10) {
                    key = TUI.waitKey(1000)
                    if(key=='5'){
                        val value:Int  = (""+firstKey+secondKey).toInt()
                        updateProductQuantity(prod.id,value)
                    }
                    if(key!=TUI.NONE) MaintenanceMode()
                    counter++
                };
            }
            else {
                key = TUI.waitKey(1000)
                if(key != TUI.NONE) {
                    if (firstKey == null && key != '*' && key != '#') {
                        firstKey = key
                        TUI.writeC(firstKey)
                    } else {
                        if (key == '*') {
                            firstKey = null
                            TUI.cursor(1, 4)
                            TUI.writeC('?')
                            TUI.cursor(1,4)
                        }
                        if(key != '#' && key != '*') {
                            secondKey = key
                            ifPressed = true
                        }
                    }
                }
            }
            counter++
        }while(counter<=10)
        MaintenanceMode()
    }

    /**
     * Product Quantity Update function, aditional function to "updateProduct" function
     */
    private fun updateProductQuantity(prodId:Int ,c: Int) {
        Products.updateProduct(prodId,c)
        MaintenanceMode()
    }

    /**
     * Product Removal Function, that removes a certain introduced product from the respective hard-coded List of Products,
     * and the PRODUCTS.txt file, simulating the full removal of a product from the Machine's available Stock.
     */
    private fun removeProduct(){
        TUI.clear()
        if (firstProd != null) {
            if (currentProd == firstProd) writeProductManual(firstProd!!)
            else writeProductManual(currentProd!!)
        } else {
            TUI.writeS("No Products")
            home()
        }
        val prod = selectProduct()
        if(prod==null) MaintenanceMode()
        var counter = 0
        var key:Char?
        TUI.clear()
        do{
            TUI.cursor(0,0)
            TUI.writeS("Remove "+prod!!.name)
            //TUI.cursor(1,0)
            //TUI.writeS("5-Yes  other-No ")
            writeCentered ("5-Yes  other-No",1)
            TUI.cursor(2,16)

            key = TUI.waitKey(1000)
            if(key=='5'){
                Products.deleteProduct(prod.id)
                currentProd=firstProd
            }
            if(key!=TUI.NONE) MaintenanceMode()
            counter++
        }while(counter<=10)
        MaintenanceMode()
    }

    /**
     * Procedure for Application Shutdown.
     */
    private fun shutdown(){
        /*TUI.cursor(0,0)
        TUI.writeS("    Shutdown    ")
        TUI.cursor(1,0)
        TUI.writeS("5-Yes   other-No")*/
        TUI.clear()
        writeCentered("Shutdown",0)
        writeCentered("5-Yes   other-No",1)
        TUI.cursor(2,16)
        var counter = 0;
        var key:Char?
        while(counter<=5) {
            key = TUI.waitKey(1000)
            if(key=='5'){
                TUI.clear()
                //TUI.cursor(0,0)
                //TUI.writeS("  See you soon  ")
                writeCentered("See You Soon",0)
                hideCursor()
                Time.sleep(2000)
                TUI.clear()
                exitProcess(1)
            }
            if (key!=TUI.NONE) MaintenanceMode()
            counter++
        };home()
    }
}