package com.damo

import com.google.crypto.tink.Aead
import com.google.crypto.tink.BinaryKeysetReader
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AesGcmKeyManager
import com.google.crypto.tink.aead.ChaCha20Poly1305KeyManager
import java.io.ByteArrayOutputStream
import java.util.Base64

fun encryptKey(dek: KeysetHandle, kekAead: Aead): String =
    ByteArrayOutputStream().use {
        dek.write(JsonKeysetWriter.withOutputStream(it), kekAead)
        String(it.toByteArray())
    }


fun main() {

    AeadConfig.register()
    println("Initiated Tink Aead")

    val kek = KeysetHandle.generateNew(AesGcmKeyManager.aes128GcmTemplate())
    val kekJson = keyJson(kek)
    println("kekJson: $kekJson")

    val kekAead = kek.getPrimitive(Aead::class.java)

    var dek = KeysetHandle.generateNew(ChaCha20Poly1305KeyManager.chaCha20Poly1305Template())
    val dekJson = keyJson(dek)
    println("dekJson: $dekJson")

    val encryptedDekJson = encryptKey(dek, kekAead)
    println("DEK encrypted with KEK, will be stored with cipherText $encryptedDekJson ")

    var dekAead = dek.getPrimitive(Aead::class.java)

    val plainText = "Hello, Damo"

    val cypherText = dekAead.encrypt(plainText.toByteArray(), null).base64EncodeToString()

    println("cypherText: $cypherText")

    val envelope = Envelope(encryptedDekJson, cypherText)

    dekAead = null
    dek = null

    val decryptedFromEnvelope = decryptEnvelope(envelope, kekAead)
    println("Decrypted From Envelope $decryptedFromEnvelope")
}

fun decryptEnvelope(envelope: Envelope, kekAead: Aead): String {

    val dek = decryptKey(envelope.encryptedDekJson, kekAead)
    println("DEK decrypted $dek ")
    val dekAead = dek.getPrimitive(Aead::class.java)
    return String(dekAead.decrypt(envelope.cypherText.base64Decode(), null))

}

fun decryptKey(encryptedKeyBase64: String, kekAead: Aead): KeysetHandle {
    return KeysetHandle.read(
        BinaryKeysetReader.withBytes(encryptedKeyBase64.base64Decode()),
        kekAead
    )
}


data class Envelope(val encryptedDekJson: String, val cypherText: String)

fun keyJson(keysetHandle: KeysetHandle): String = ByteArrayOutputStream().use {
    CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withOutputStream(it))
    String(it.toByteArray())
}

fun ByteArray.base64EncodeToString(): String {
    return Base64.getEncoder().encodeToString(this)
}

fun String.base64Decode(): ByteArray {
    return Base64.getDecoder().decode(this)
}


