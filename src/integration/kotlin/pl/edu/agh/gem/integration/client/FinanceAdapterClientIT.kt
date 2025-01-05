package pl.edu.agh.gem.integration.client

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_ACCEPTABLE
import pl.edu.agh.gem.external.client.RestFinanceAdapterClient
import pl.edu.agh.gem.external.dto.reconciliation.GenerateReconciliationRequest
import pl.edu.agh.gem.helper.group.DummyGroup.GROUP_ID
import pl.edu.agh.gem.integration.BaseIntegrationSpec
import pl.edu.agh.gem.integration.ability.stubFinanceAdapterGenerate
import pl.edu.agh.gem.internal.client.FinanceAdapterClientException
import pl.edu.agh.gem.internal.client.RetryableFinanceAdapterClientException
import pl.edu.agh.gem.internal.model.currency.Currency
import pl.edu.agh.gem.util.DummyData.CURRENCY_1

class FinanceAdapterClientIT(
    private val financeAdapterClient: RestFinanceAdapterClient,
) : BaseIntegrationSpec({
        should("generate") {
            // given
            stubFinanceAdapterGenerate(requestBody = GenerateReconciliationRequest(currency = CURRENCY_1), groupId = GROUP_ID)

            // when & then
            shouldNotThrowAny {
                financeAdapterClient.generate(GROUP_ID, Currency(code = CURRENCY_1))
            }
        }

        should("throw FinanceAdapterClientException when we send bad request") {
            // given
            stubFinanceAdapterGenerate(requestBody = GenerateReconciliationRequest(currency = CURRENCY_1), groupId = GROUP_ID, NOT_ACCEPTABLE)

            // when & then
            shouldThrow<FinanceAdapterClientException> {
                financeAdapterClient.generate(GROUP_ID, Currency(code = CURRENCY_1))
            }
        }

        should("throw RetryableFinanceAdapterClientException when client has internal error") {
            // given
            stubFinanceAdapterGenerate(requestBody = GenerateReconciliationRequest(currency = CURRENCY_1), groupId = GROUP_ID, INTERNAL_SERVER_ERROR)

            // when & then
            shouldThrow<RetryableFinanceAdapterClientException> {
                financeAdapterClient.generate(GROUP_ID, Currency(code = CURRENCY_1))
            }
        }
    })
