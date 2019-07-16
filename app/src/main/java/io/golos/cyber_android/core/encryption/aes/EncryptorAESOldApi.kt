package io.golos.cyber_android.core.encryption.aes

import io.golos.domain.Encryptor
import io.golos.domain.KeyValueStorageFacade
import java.lang.UnsupportedOperationException
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/** Encryption/Decryption via AES for an old API*/
class EncryptorAESOldApi
constructor(
    private val keyValueStorage: KeyValueStorageFacade,
    private val encryptor: Encryptor
) : EncryptorAESBase() {

    companion object {
        private const val KEY_SIZE_BYTES = KEY_SIZE / 8         // in bytes
    }

    private var key: Key? = null  // It's a hole in security but RSA alg (used in encryptor) is quite slow...

    init {
        if (!isKeyExists()) {
            createKey()
        }
    }

    override fun getKey(): Key {
        if(key == null) {
            key = keyValueStorage.getAESCryptoKey()!!
                .let { encryptor.decrypt(it) }
                .let { SecretKeySpec(it, "AES") }
        }

        return key!!
    }

    override fun getCipher(): Cipher {
        throw UnsupportedOperationException("This operation is not supported for an old API")
    }

    private fun isKeyExists() = keyValueStorage.getAESCryptoKey() != null

    private fun createKey() {
        val generatedKey = ByteArray(KEY_SIZE_BYTES)
        secureRandom.nextBytes(generatedKey)

        val encryptedKey = encryptor.encrypt(generatedKey)
        keyValueStorage.saveAESCryptoKey(encryptedKey!!)

        key = SecretKeySpec(generatedKey, "AES")
    }
}