package cz.cvut.arfittingroom.activity

interface ResourceListener {
    fun applyImage(type: String, ref: String)
    fun applyModel(type: String, ref: String)
    fun removeImage(type: String)
    fun removeModel(type: String)
}