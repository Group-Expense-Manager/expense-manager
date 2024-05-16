package pl.edu.agh.gem

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.IsolationMode.InstancePerTest

object ProjectConfig : AbstractProjectConfig() {
    override val isolationMode: IsolationMode = InstancePerTest
}
