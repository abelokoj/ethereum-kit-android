package io.horizontalsystems.uniswapkit.contract

import io.horizontalsystems.ethereumkit.models.Address
import java.math.BigInteger

/**
 * Router call for buying a token that skims a fee on transfer.
 *
 * Same argument layout as [SwapExactETHForTokensMethod]; selector 0xb6f9de95
 * instead of 0x7ff36ab5. Payable, so the ETH value is still carried on the
 * transaction rather than in the calldata.
 */
class SwapExactETHForTokensSupportingFeeOnTransferTokensMethod(
        amountOutMin: BigInteger,
        path: List<Address>,
        to: Address,
        deadline: BigInteger
) : SwapExactETHForTokensMethod(amountOutMin, path, to, deadline) {

    override val methodSignature = Companion.methodSignature

    companion object {
        const val methodSignature =
                "swapExactETHForTokensSupportingFeeOnTransferTokens(uint256,address[],address,uint256)"
    }

}
