package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.utils.type.isInterface

object CircularDependencyValidator {
    fun checkForUnresolvableCircularDependencies(clazzInstanceMap: Map<ClazzModelId, NeighbourInstance>) {
        val ctx = CircularDependencyContext(clazzInstanceMap)
        clazzInstanceMap.values.forEach { clazzInstance ->
            if (ctx.needsCheck(clazzInstance.clazzModelId)) {
                checkForUnresolvableCircularDependencies(ctx, clazzInstance)
            }
        }
    }

    private fun checkForUnresolvableCircularDependencies(
        ctx: CircularDependencyContext,
        clazzInstance: NeighbourInstance,
    ) {
        val clazzModelId = clazzInstance.clazzModelId
        if (ctx.isFinished(clazzModelId)) {
            return
        }
        if (ctx.isUnderInspection(clazzModelId)) {
            throw DataValidationException(
                DataCollectionErrorCode.UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED.withFormattedMessage(
                    "Circular dependency for $clazzModelId not found"
                )
            )
        }

        ctx.markUnderInspection(clazzModelId)
        for (neighbourNode in clazzInstance.neighbours) {
            checkForUnresolvableCircularDependencies(ctx, neighbourNode)
        }
        ctx.markFinished(clazzModelId)
    }

    private fun NeighbourInstance.isLazyReference(): Boolean {
        // if the clazz class is an interface, we will create
        // a proxy that does evaluate its references only if called.
        return clazz.clazz.isInterface
    }

    private enum class DepthFirstSearchNodeState {
        UNTOUCHED, // a.k.a. WHITE
        UNDER_INSPECTION, // a.k.a. GREY
        FINISHED, // a.k.a. BLACK
    }

    private data class CircularDependencyContext(val clazzInstanceMap: Map<ClazzModelId, NeighbourInstance>) {
        val depthFirstSearchState: MutableMap<ClazzModelId, DepthFirstSearchNodeState> =
            clazzInstanceMap.mapValues { (_, node) -> initialDfsState(node) }.toMutableMap()

        private fun initialDfsState(clazzInstance: NeighbourInstance): DepthFirstSearchNodeState {
            return if (clazzInstance.isLazyReference()) {
                DepthFirstSearchNodeState.FINISHED
            } else {
                DepthFirstSearchNodeState.UNTOUCHED
            }
        }

        fun needsCheck(clazzModelId: ClazzModelId): Boolean {
            return depthFirstSearchState.getValue(clazzModelId) === DepthFirstSearchNodeState.UNTOUCHED
        }

        fun markUnderInspection(clazzModelId: ClazzModelId) {
            depthFirstSearchState[clazzModelId] = DepthFirstSearchNodeState.UNDER_INSPECTION
        }

        fun isUnderInspection(clazzModelId: ClazzModelId): Boolean {
            return depthFirstSearchState.getValue(clazzModelId) === DepthFirstSearchNodeState.UNDER_INSPECTION
        }

        fun isFinished(clazzModelId: ClazzModelId): Boolean {
            return depthFirstSearchState.getValue(clazzModelId) === DepthFirstSearchNodeState.FINISHED
        }

        fun markFinished(clazzModelId: ClazzModelId) {
            depthFirstSearchState[clazzModelId] = DepthFirstSearchNodeState.FINISHED
        }
    }
}
