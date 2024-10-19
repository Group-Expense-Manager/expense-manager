package pl.edu.agh.gem.internal.client

import pl.edu.agh.gem.internal.model.currency.Currency

interface FinanceAdapterClient {
    fun generate(groupId: String, currency: Currency)
}

class FinanceAdapterClientException(override val message: String?) : RuntimeException()

class RetryableFinanceAdapterClientException(override val message: String?) : RuntimeException()
