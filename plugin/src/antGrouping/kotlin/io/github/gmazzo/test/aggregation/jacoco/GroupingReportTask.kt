package io.github.gmazzo.test.aggregation.jacoco

import org.apache.tools.ant.types.resources.Union
import org.jacoco.ant.ReportTask
import org.jacoco.core.data.ExecutionDataStore
import org.jacoco.core.tools.ExecFileLoader

/**
 * A horrible hack class aiming to introduce support for mapping coverage files correctly to each group
 *
 * It relies on in two internals **likely to fail** in the future:
 * - The [ReportTask.executionDataStore] can it be how swapped while generating the report
 * - A group is about to be rendered when [GroupElement.children.isEmpty] is called
 */
class GroupingReportTask : ReportTask() {

    private val structure = StructureGroup(isRoot = true)

    private val executionDataStorePerGroup = mutableMapOf<String, ExecutionDataStore>()

    init {
        FIELD_STRUCTURE.set(this, structure)
    }

    override fun execute() {
        for (group in structure.children) {
            super.createExecutiondata().addAll(group.createExecutiondata().resourceCollections)
        }

        super.execute()
    }

    override fun createExecutiondata(): Union {
        error("Define execution data in the group structure instead")
    }

    inner class StructureGroup(val isRoot: Boolean) : GroupElement() {

        @Suppress("UNCHECKED_CAST")
        val name get() = FIELD_NAME.get(this) as String

        @Suppress("UNCHECKED_CAST")
        val children = Children(FIELD_CHILDREN.get(this) as MutableList<StructureGroup>)
            .also { FIELD_CHILDREN.set(this, it) }

        val executiondataElement = Union()

        fun createExecutiondata() = executiondataElement

        override fun createGroup(): GroupElement =
            if (isRoot) StructureGroup(isRoot = false).also(children::add)
            else super.createGroup()

        @Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
        inner class Children(val delegate: MutableList<StructureGroup>) : MutableList<StructureGroup> by delegate {

            override fun isEmpty(): Boolean {
                setGroupDataStore()
                return delegate.isEmpty()
            }

            private fun setGroupDataStore() {
                val dataStore = executionDataStorePerGroup.getOrPut(this@StructureGroup.name) {
                    with(ExecFileLoader()) {
                        for (resource in executiondataElement) {
                            runCatching { resource.inputStream.use(::load) }
                        }
                        executionDataStore
                    }
                }

                FIELD_EXECUTION_DATA_STORE.set(this@GroupingReportTask, dataStore)
            }

        }

    }

    private companion object {

        val FIELD_STRUCTURE = ReportTask::class.java
            .getDeclaredField("structure")
            .apply { isAccessible = true }

        val FIELD_EXECUTION_DATA_STORE = ReportTask::class.java
            .getDeclaredField("executionDataStore")
            .apply { isAccessible = true }

        val FIELD_NAME = GroupElement::class.java
            .getDeclaredField("name")
            .apply { isAccessible = true }

        val FIELD_CHILDREN = GroupElement::class.java
            .getDeclaredField("children")
            .apply { isAccessible = true }

    }

}
