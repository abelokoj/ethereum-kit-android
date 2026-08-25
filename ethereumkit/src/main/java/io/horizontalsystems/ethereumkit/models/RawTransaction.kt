package io.horizontalsystems.ethereumkit.models

import io.horizontalsystems.ethereumkit.core.toHexString
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.ethereumkit.models.GasPrice
import java.math.BigInteger

class RawTransaction(
        val gasPrice: GasPrice,
        val gasLimit: Long,
        val to: Address,
        val value: BigInteger,
        val nonce: Long,
        val data: ByteArray = ByteArray(0),
        /**
         * EIP-7702 authorization tuples. When non-empty the transaction is encoded as
         * type 0x04 instead of 0x02, which requires [gasPrice] to be [GasPrice.Eip1559];
         * EIP-7702 has no legacy-gas form.
         */
        val authorizationList: List<Authorization> = emptyList()
) {
    val isSetCode: Boolean
        get() = authorizationList.isNotEmpty()

    override fun toString(): String {
        return "RawTransaction [gasPrice: $gasPrice; gasLimit: $gasLimit; to: $to; value: $value; data: ${data.toHexString()}; nonce: $nonce; authorizations: ${authorizationList.size}]"
    }
}
