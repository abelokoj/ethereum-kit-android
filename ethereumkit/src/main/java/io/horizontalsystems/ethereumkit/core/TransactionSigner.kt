package io.horizontalsystems.ethereumkit.core

import io.horizontalsystems.ethereumkit.crypto.CryptoUtils
import io.horizontalsystems.ethereumkit.models.GasPrice
import io.horizontalsystems.ethereumkit.models.RawTransaction
import io.horizontalsystems.ethereumkit.models.Signature
import io.horizontalsystems.ethereumkit.spv.rlp.RLP
import java.math.BigInteger

class TransactionSigner(
        private val privateKey: BigInteger,
        private val chainId: Int
) {

    fun signature(rawTransaction: RawTransaction): Signature {
        return when (val gasPrice = rawTransaction.gasPrice) {
            is GasPrice.Eip1559 -> {
                val signatureData = if (rawTransaction.isSetCode) {
                    signEip7702(rawTransaction, gasPrice.maxFeePerGas, gasPrice.maxPriorityFeePerGas)
                } else {
                    signEip1559(rawTransaction, gasPrice.maxFeePerGas, gasPrice.maxPriorityFeePerGas)
                }
                signatureEip1559(signatureData)
            }
            is GasPrice.Legacy -> {
                require(!rawTransaction.isSetCode) {
                    "EIP-7702 transactions require EIP-1559 gas; there is no legacy-gas form"
                }
                val signatureData = signEip155(rawTransaction, gasPrice.legacyGasPrice)
                signatureLegacy(signatureData)
            }
        }
    }

    private fun signatureLegacy(signatureData: ByteArray): Signature {
        return Signature(
                v = signatureData[64] + if (chainId == 0) 27 else (35 + 2 * chainId),
                r = signatureData.copyOfRange(0, 32),
                s = signatureData.copyOfRange(32, 64)
        )
    }

    private fun signatureEip1559(signatureData: ByteArray): Signature {
        return Signature(
                v = signatureData[64].toInt(),
                r = signatureData.copyOfRange(0, 32),
                s = signatureData.copyOfRange(32, 64)
        )
    }

    private fun signEip155(rawTransaction: RawTransaction, legacyGasPrice: Long): ByteArray {
        val encodedTransaction = RLP.encodeList(
                RLP.encodeLong(rawTransaction.nonce),
                RLP.encodeLong(legacyGasPrice),
                RLP.encodeLong(rawTransaction.gasLimit),
                RLP.encodeElement(rawTransaction.to.raw),
                RLP.encodeBigInteger(rawTransaction.value),
                RLP.encodeElement(rawTransaction.data),
                RLP.encodeInt(chainId),
                RLP.encodeElement(ByteArray(0)),
                RLP.encodeElement(ByteArray(0)))

        val rawTransactionHash = CryptoUtils.sha3(encodedTransaction)

        return CryptoUtils.ellipticSign(rawTransactionHash, privateKey)
    }

    private fun signEip1559(rawTransaction: RawTransaction, maxFeePerGas: Long, maxPriorityFeePerGas: Long): ByteArray {
        val encodedTransaction = RLP.encodeList(
                RLP.encodeInt(chainId),
                RLP.encodeLong(rawTransaction.nonce),
                RLP.encodeLong(maxPriorityFeePerGas),
                RLP.encodeLong(maxFeePerGas),
                RLP.encodeLong(rawTransaction.gasLimit),
                RLP.encodeElement(rawTransaction.to.raw),
                RLP.encodeBigInteger(rawTransaction.value),
                RLP.encodeElement(rawTransaction.data),
                RLP.encode(arrayOf<Any>())
        )
        val rawTransactionHash = CryptoUtils.sha3("0x02".hexStringToByteArray() + encodedTransaction)

        return CryptoUtils.ellipticSign(rawTransactionHash, privateKey)
    }

    private fun signEip7702(rawTransaction: RawTransaction, maxFeePerGas: Long, maxPriorityFeePerGas: Long): ByteArray {
        val encodedTransaction = RLP.encodeList(
                RLP.encodeInt(chainId),
                RLP.encodeLong(rawTransaction.nonce),
                RLP.encodeLong(maxPriorityFeePerGas),
                RLP.encodeLong(maxFeePerGas),
                RLP.encodeLong(rawTransaction.gasLimit),
                RLP.encodeElement(rawTransaction.to.raw),
                RLP.encodeBigInteger(rawTransaction.value),
                RLP.encodeElement(rawTransaction.data),
                RLP.encode(arrayOf<Any>()),
                AuthorizationSigner.encodeList(rawTransaction.authorizationList)
        )
        val rawTransactionHash = CryptoUtils.sha3("0x04".hexStringToByteArray() + encodedTransaction)

        return CryptoUtils.ellipticSign(rawTransactionHash, privateKey)
    }

}
