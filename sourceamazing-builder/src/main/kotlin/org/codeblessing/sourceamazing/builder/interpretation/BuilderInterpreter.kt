package org.codeblessing.sourceamazing.builder.interpretation

import org.codeblessing.sourceamazing.builder.alias.Alias
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.ConceptIdentifierAnnotationData
import org.codeblessing.sourceamazing.builder.interpretation.facetvalue.FacetValueAnnotationContent
import org.codeblessing.sourceamazing.builder.update.DataContext
import org.codeblessing.sourceamazing.schema.ConceptName

interface BuilderInterpreter {
    fun getBuilderInterpreterNewConceptsIncludingDuplicates(): List<Pair<Alias, ConceptName>>
    fun getBuilderInterpreterAliasesToSetRandomConceptIdentifierValueIncludingDuplicates(): List<Alias>
    fun getBuilderInterpreterAliasesToSetConceptIdentifierValueAliasesIncludingDuplicates(): List<Alias>
    fun getBuilderInterpreterManualAssignedConceptIdentifierAnnotationContent(dataContext: DataContext?): List<ConceptIdentifierAnnotationData>
    fun getBuilderInterpreterFacetValueAnnotationContent(dataContext: DataContext?): List<FacetValueAnnotationContent>
}