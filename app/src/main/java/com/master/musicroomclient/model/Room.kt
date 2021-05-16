package com.master.musicroomclient.model

data class Room(val id: Long?, val name: String, val code: String?, val listeners: List<Listener>) {
    constructor(name: String) : this(null, name, "555", emptyList())
}
