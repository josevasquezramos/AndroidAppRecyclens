package com.episi.recyclens.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class FraseMotivadora(
    val texto: String,
    @ServerTimestamp val fecha: Date? = null
) {
    constructor() : this("", null)
}
