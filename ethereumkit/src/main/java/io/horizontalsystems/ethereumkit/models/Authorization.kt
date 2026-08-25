package io.horizontalsystems.ethereumkit.models

import io.horizontalsystems.ethereumkit.core.toHexString
import java.math.BigInteger

/**
 * A signed EIP-7702 authorization tuple: `[chain_id, address, nonce, y_parity, r, s]`.
 *
 * Signing this delegates the *authority's* EOA code to [address] for as long as the
 * delegation stands, which is what makes a single transaction able to run
 * `approve` and `swap` back to back from the user's own address.
 *
 * [chainId] of zero is valid and means the authorization applies on any chain. That is
 * deliberately not the default here: a chain-agnostic delegation signed for a swap is
 * replayable everywhere, so callers must opt into it explicitly.
 *
 * [nonce] is the authority account's nonce at the time the authorization is *applied*.
 * When the authority is also the transaction sender — the self-delegation case, which is
 * what batching approve+swap needs — the transaction's own nonce is consumed first, so
 * this must be the transaction nonce plus one. See [AuthorizationSigner.selfSign].
 */
data class Authorization(
    val chainId: Long,
    val address: Address,
    val nonce: Long,
    val yParity: Int,
    val r: ByteArray,
    val s: ByteArray
) {
    val rBigInteger: BigInteger get() = BigInteger(1, r)
    val sBigInteger: BigInteger get() = BigInteger(1, s)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Authorization) return false
        return chainId == other.chainId &&
            address == other.address &&
            nonce == other.nonce &&
            yParity == other.yParity &&
            r.contentEquals(other.r) &&
            s.contentEquals(other.s)
    }

    override fun hashCode(): Int {
        var result = chainId.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + nonce.hashCode()
        result = 31 * result + yParity
        result = 31 * result + r.contentHashCode()
        result = 31 * result + s.contentHashCode()
        return result
    }

    override fun toString() =
        "Authorization [chainId: $chainId; address: $address; nonce: $nonce; " +
            "yParity: $yParity; r: ${r.toHexString()}; s: ${s.toHexString()}]"

    companion object {
        /** Prefix for the authorization signing payload, per EIP-7702. */
        const val MAGIC: Byte = 0x05

        /** Delegation indicator prefix written to the authority's code slot: 0xef0100. */
        val DELEGATION_PREFIX = byteArrayOf(0xef.toByte(), 0x01, 0x00)
    }
}
