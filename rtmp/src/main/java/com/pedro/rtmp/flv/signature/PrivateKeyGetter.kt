package com.pedro.rtmp.flv.signature

import java.security.PrivateKey

/**
 * Created by Maxime on 22/06/21
 */
interface PrivateKeyGetter {
    fun getPrivateKey(): PrivateKey
}