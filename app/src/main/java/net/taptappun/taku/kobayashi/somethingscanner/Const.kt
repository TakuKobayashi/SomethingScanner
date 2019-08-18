package net.taptappun.taku.kobayashi.somethingscanner

object Const {
    val TAG = "SomethingScanner"
    val MOVE_TO_SCAN_INITENT_KEY = "moveToScan"

    enum class ScanMode(val modeName: String){
        Face("face"),
        Text("text"),
        Barcode("barcode"),
    }
}
