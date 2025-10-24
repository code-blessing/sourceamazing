package org.codeblessing.sourceamazing.schema.clazzgraph

import org.codeblessing.sourceamazing.schema.api.datacollection.DataCollectionErrorCode
import org.codeblessing.sourceamazing.schema.api.datacollection.exceptions.DataValidationException
import org.codeblessing.sourceamazing.schema.assertExceptionWithErrorCode
import org.codeblessing.sourceamazing.schema.typesafeapi.Clazz
import org.codeblessing.sourceamazing.schema.typesafeapi.ClazzModelId
import org.codeblessing.sourceamazing.schema.typesafeapi.toClazz
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CircularDependencyValidatorTest {

    @Test
    fun `empty clazz graph should be fine`() {
        runValidator()
    }

    @Nested
    inner class OnlyEagerNodes {
        val nodeA = createEagerNode("Node-A")
        val nodeB = createEagerNode("Node-B")
        val nodeC = createEagerNode("Node-C")

        @Test
        fun `clazz graph with two nodes pointing one to another vica-verse should be fine`() {
            nodeA pointsTo nodeB

            runValidator(nodeA, nodeB)
        }

        @Test
        fun `clazz graph with two nodes being independent from each other should be fine`() {
            runValidator(nodeA, nodeB)
        }

        @Test
        fun `clazz graph with one node pointing to itself should throw exception`() {
            nodeA pointsTo nodeA

            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED
            ) {
                runValidator(nodeA)
            }
        }

        @Test
        fun `clazz graph with two nodes pointing one to another and vica-verse should throw exception`() {
            nodeA pointsTo nodeB
            nodeB pointsTo nodeA

            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED
            ) {
                runValidator(nodeA, nodeB)
            }
        }

        @Test
        fun `clazz graph with three nodes pointing in a line should be fine`() {
            nodeA pointsTo nodeB
            nodeB pointsTo nodeC

            runValidator(nodeA, nodeB, nodeC)
        }

        @Test
        fun `clazz graph with three nodes building a reference circle should throw exception`() {
            nodeA pointsTo nodeB
            nodeB pointsTo nodeC
            nodeC pointsTo nodeA

            assertExceptionWithErrorCode<DataValidationException>(
                DataCollectionErrorCode.UNRESOLVABLE_CIRCULAR_DEPENDENCY_DETECTED
            ) {
                runValidator(nodeA, nodeB, nodeC)
            }
        }
    }

    @Nested
    inner class EagerNodesAndOneLazyNode {
        val nodeA = createLazyNode("Node-A")
        val nodeB = createEagerNode("Node-B")
        val nodeC = createEagerNode("Node-C")

        @Test
        fun `clazz graph with two nodes pointing one to another vica-verse should be fine`() {
            nodeA pointsTo nodeB

            runValidator(nodeA, nodeB)
        }

        @Test
        fun `clazz graph with two nodes being independent from each other should be fine`() {
            runValidator(nodeA, nodeB)
        }

        @Test
        fun `clazz graph with one node pointing to itself should be fine`() {
            nodeA pointsTo nodeA

            runValidator(nodeA)
        }

        @Test
        fun `clazz graph with two nodes pointing one to another and vica-verse should throw exception`() {
            nodeA pointsTo nodeB
            nodeB pointsTo nodeA

            runValidator(nodeA, nodeB)
        }

        @Test
        fun `clazz graph with three nodes pointing in a line should be fine`() {
            nodeA pointsTo nodeB
            nodeB pointsTo nodeC

            runValidator(nodeA, nodeB, nodeC)
        }

        @Test
        fun `clazz graph with three nodes building a reference circle should throw exception`() {
            nodeA pointsTo nodeB
            nodeB pointsTo nodeC
            nodeC pointsTo nodeA

            runValidator(nodeA, nodeB, nodeC)
        }
    }

    private fun runValidator(vararg nodes: NeighbourInstance) {
        val clazzNodes = nodes.associateBy { it.clazzModelId }
        CircularDependencyValidator.checkForUnresolvableCircularDependencies(clazzNodes)
    }

    @Suppress("SameParameterValue")
    private fun createLazyNode(key: String): MutableNeighbourInstance {
        return MutableNeighbourInstance(ClazzForInterface::class.toClazz(), ClazzModelId.of(key))
    }

    private fun createEagerNode(key: String): MutableNeighbourInstance {
        return MutableNeighbourInstance(ClazzForClass::class.toClazz(), ClazzModelId.of(key))
    }

    interface ClazzForInterface

    class ClazzForClass

    class MutableNeighbourInstance(
        override val clazz: Clazz,
        override val clazzModelId: ClazzModelId,
        override val neighbours: MutableList<NeighbourInstance> = mutableListOf(),
    ) : NeighbourInstance {

        infix fun pointsTo(neighbour: MutableNeighbourInstance) {
            neighbours.add(neighbour)
        }
    }
}
