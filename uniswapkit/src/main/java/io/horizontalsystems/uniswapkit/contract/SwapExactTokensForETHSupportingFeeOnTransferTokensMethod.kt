package io.horizontalsystems.uniswapkit.contract

import io.horizontalsystems.ethereumkit.models.Address
import java.math.BigInteger

/**
 * Router call for selling a fee-on-transfer token into the chain's native coin.
 *
 * Same argument layout as [SwapExactTokensForETHMethod]; selector 0x791ac947
 * instead of 0x18cbafe5. This is the variant that the common "sell my tax
 * token for BNB/ETH" path needs.
 *
 * Note: the router rejects `amountOutMin == 0` for this function
 * (INSUFFICIENT_OUTPUT_AMOUNT), so callers must pass a non-zero minimum.
 */
class SwapExactTokensForETHSupportingFeeOnTransferTokensMethod(
        amountIn: BigInteger,
        amountOutMin: BigInteger,
        path: List<Address>,
        to: Address,
        deadline: BigInteger
) : SwapExactTokensForETHMethod(amountIn, amountOutMin, path, to, deadline) {

    override val methodSignature = Companion.methodSignature

    companion object {
        const val methodSignature =
                "swapExactTokensForETHSupportingFeeOnTransferTokens(uint256,uint256,address[],address,uint256)"
    }

}
