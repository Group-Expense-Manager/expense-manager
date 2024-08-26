package pl.edu.agh.gem.internal.validation.update

import pl.edu.agh.gem.internal.validation.cost.CostData
import pl.edu.agh.gem.internal.validation.cost.CostDataWrapper
import pl.edu.agh.gem.internal.validation.currency.CurrencyData
import pl.edu.agh.gem.internal.validation.currency.CurrencyDataWrapper
import pl.edu.agh.gem.internal.validation.participant.ParticipantData
import pl.edu.agh.gem.internal.validation.participant.ParticipantDataWrapper

data class ExpenseUpdateDataWrapper(
    override val currencyData: CurrencyData,
    override val costData: CostData,
    override val participantData: ParticipantData,
) : CurrencyDataWrapper, CostDataWrapper, ParticipantDataWrapper
