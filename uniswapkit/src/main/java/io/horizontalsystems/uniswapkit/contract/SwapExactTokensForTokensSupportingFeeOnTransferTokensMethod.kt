package io.horizontalsystems.uniswapkit.contract

import io.horizontalsystems.ethereumkit.models.Address
import java.math.BigInteger

/**
 * Router call for selling a token that skims a fee on transfer.
 *
 * Argument layout is identical to [SwapExactTokensForTokensMethod]; only the
 * function selector differs (0x5c11d795 vs 0x38ed1739). The router variant
 * recomputes intermediate amounts from actual pair balance deltas instead of
 * the nominal path amounts, which is what avoids the `UniswapV2: K` /
 * `PancakeSwap: K` invariant failure on tax tokens.
 *
 * Subclasses the plain method so existing `is SwapExactTokensForTokensMethod`
 * checks in SwapTransactionDecorator continue to decorate these transactions.
 */
class SwapExactTokensForTokensSupportingFeeOnTransferTokensMethod(
        amountIn: BigInteger,
        amountOutMin: BigInteger,
        path: List<Address>,
        to: Address,
        deadline: BigInteger
) : SwapExactTokensForTokensMethod(amountIn, amountOutMin, path, to, deadline) {

    override val methodSignature = Companion.methodSignature

    companion object {
        const val methodSignature =
                "swapExactTokensForTokensSupportingFeeOnTransferTokens(uint256,uint256,address[],address,uint256)"
    }

}
