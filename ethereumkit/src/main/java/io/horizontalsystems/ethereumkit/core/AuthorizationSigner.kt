package io.horizontalsystems.ethereumkit.core

import io.horizontalsystems.ethereumkit.crypto.CryptoUtils
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.ethereumkit.models.Authorization
import io.horizontalsystems.ethereumkit.spv.rlp.RLP
import java.math.BigInteger

/**
 * Builds and signs EIP-7702 authorization tuples.
 *
 * The signing payload is `keccak(MAGIC || rlp([chain_id, address, nonce]))` where
 * MAGIC is 0x05. The MAGIC prefix is what keeps an authorization from ever being
 * mistaken for an RLP-encoded transaction.
 */
object AuthorizationSigner {

    /** `rlp([chain_id, address, nonce])` — the unsigned tuple. */
    fun encodeUnsigned(chainId: Long, address: Address, nonce: Long): ByteArray =
        RLP.encodeList(
            RLP.encodeLong(chainId),
            RLP.encodeElement(address.raw),
            RLP.encodeLong(nonce)
        )

    /** The 32-byte digest an authority signs to delegate its code to [address]. */
    fun signingHash(chainId: Long, address: Address, nonce: Long): ByteArray =
        CryptoUtils.sha3(byteArrayOf(Authorization.MAGIC) + encodeUnsigned(chainId, address, nonce))

    fun sign(chainId: Long, address: Address, nonce: Long, privateKey: BigInteger): Authorization {
        val signatureData = CryptoUtils.ellipticSign(signingHash(chainId, address, nonce), privateKey)

        return Authorization(
            chainId = chainId,
            address = address,
            nonce = nonce,
            yParity = signatureData[64].toInt(),
            r = signatureData.copyOfRange(0, 32),
            s = signatureData.copyOfRange(32, 64)
        )
    }

    /**
     * Signs an authorization for the case where the authority *is* the transaction
     * sender, which is the shape needed to batch approve+swap from a user's own EOA.
     *
     * The transaction's own nonce is consumed before the authorization list is applied,
     * so the authorization must carry [transactionNonce] + 1. Getting this off by one is
     * the single most common way a self-delegating 7702 transaction silently no-ops: the
     * authorization is skipped as invalid, the batch call hits an EOA with no code, and
     * the transaction succeeds having done nothing.
     */
    fun selfSign(
        chainId: Long,
        delegate: Address,
        transactionNonce: Long,
        privateKey: BigInteger
    ): Authorization = sign(chainId, delegate, transactionNonce + 1, privateKey)

    /** `rlp([[chain_id, address, nonce, y_parity, r, s], ...])` for the transaction payload. */
    fun encodeList(authorizations: List<Authorization>): ByteArray =
        RLP.encodeList(
            *authorizations.map { authorization ->
                RLP.encodeList(
                    RLP.encodeLong(authorization.chainId),
                    RLP.encodeElement(authorization.address.raw),
                    RLP.encodeLong(authorization.nonce),
                    RLP.encodeInt(authorization.yParity),
                    RLP.encodeBigInteger(authorization.rBigInteger),
                    RLP.encodeBigInteger(authorization.sBigInteger)
                )
            }.toTypedArray()
        )
}
