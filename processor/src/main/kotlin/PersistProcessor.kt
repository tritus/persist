import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSEmptyVisitor

class PersistProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val annotatedSymbols = resolver.getSymbolsWithAnnotation("Persist").toList()
        val processableSymbols = annotatedSymbols
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.INTERFACE && it.validate() }
        processableSymbols.forEach { it.accept(PersistVisitor(codeGenerator), Unit) }
        return annotatedSymbols - processableSymbols
    }
}

class PersistVisitor(private val codeGenerator: CodeGenerator): KSEmptyVisitor<Unit, Unit>() {
    override fun defaultHandler(node: KSNode, data: Unit) {
        val persistDefinition = node as KSClassDeclaration
        createPersistantObject(persistDefinition)
        createProvider(persistDefinition)
        persistDefinition.getAllProperties().forEach { handleProperty(it) }
    }

    private fun getPersistantDataClassName(persistDefinition: KSClassDeclaration) =
        "${persistDefinition.simpleName}_Data}"

    private fun getProviderClassName(persistDefinition: KSClassDeclaration) =
        "${persistDefinition.simpleName}Provider}"

    private fun createPersistantObject(persistDefinition: KSClassDeclaration) {
        val className = getPersistantDataClassName(persistDefinition)
        codeGenerator.createNewFile(
            Dependencies(true, persistDefinition.containingFile!!),
            persistDefinition.packageName.asString(),
            className
        )
    }

    private fun createProvider(persistDefinition: KSClassDeclaration) {
        val className = getProviderClassName(persistDefinition)
        val providerFile = codeGenerator.createNewFile(
            Dependencies(true, persistDefinition.containingFile!!),
            persistDefinition.packageName.asString(),
            className
        )
    }

    private fun handleProperty(property: KSPropertyDeclaration) {
        TODO("Not yet implemented")
    }
}