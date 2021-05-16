package com.master.musicroomclient.model

data class Listener(val name: String, val connectedAt: String?) {

    constructor(name: String) : this(name, null)
}
