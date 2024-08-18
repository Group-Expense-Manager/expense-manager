package pl.edu.agh.gem.internal.validation.cost

import pl.edu.agh.gem.validator.DataWrapper

interface CostDataWrapper : DataWrapper {
    val costData: CostData
}
