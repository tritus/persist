import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.squareup.kotlinpoet.*

class PersistVisitor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : KSEmptyVisitor<Unit, Unit>() {
    override fun defaultHandler(node: KSNode, data: Unit) {
        val persistDefinition = node as KSClassDeclaration
        createDataHolder(persistDefinition)
        createProvider(persistDefinition)
    }

    private fun KSClassDeclaration.getDataHolderClassName() = "${simpleName.asString()}_Data"

    private fun KSClassDeclaration.getProviderClassName() = "${simpleName.asString()}Provider"

    private fun createDataHolder(persistDefinition: KSClassDeclaration) {
        val fileName = persistDefinition.getDataHolderClassName()
        val packageName = persistDefinition.packageName.asString()
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createDataHolderClass(persistDefinition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, persistDefinition.containingFile!!),
            packageName,
            fileName
        ).use { dataHolderFile ->
            val fileBytes = fileSpec.toString().toByteArray()
            dataHolderFile.write(fileBytes)
        }
    }

    private fun createDataHolderClass(persistDefinition: KSClassDeclaration): TypeSpec {
        val className = persistDefinition.getDataHolderClassName()
        val classBuilder = TypeSpec.classBuilder(className)
        classBuilder.addSuperinterface(
            ClassName(
                persistDefinition.packageName.asString(),
                persistDefinition.simpleName.asString()
            )
        )
        val constructorBuilder = FunSpec.constructorBuilder()
        persistDefinition.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            val parameterTypeDeclaration = property.type.resolve().declaration
            val propertyClassName = ClassName(
                parameterTypeDeclaration.packageName.asString(),
                parameterTypeDeclaration.simpleName.asString()
            )
            constructorBuilder.addParameter(propertyName, propertyClassName)
            classBuilder.addProperty(
                PropertySpec.builder(propertyName, propertyClassName, KModifier.OVERRIDE)
                    .initializer(propertyName)
                    .build()
            )
        }
        classBuilder.primaryConstructor(constructorBuilder.build())
        return classBuilder.build()
    }

    private fun createProvider(persistDefinition: KSClassDeclaration) {
        val fileName = persistDefinition.getProviderClassName()
        val packageName = persistDefinition.packageName.asString()
        val fileSpec = FileSpec.builder(packageName, fileName)
            .addType(createProviderClass(persistDefinition))
            .build()
        codeGenerator.createNewFile(
            Dependencies(true, persistDefinition.containingFile!!),
            packageName,
            fileName
        ).use { dataHolderFile ->
            dataHolderFile.write(fileSpec.toString().toByteArray())
        }
    }

    private fun createProviderClass(persistDefinition: KSClassDeclaration): TypeSpec {
        val className = persistDefinition.getProviderClassName()
        val classBuilder = TypeSpec.objectBuilder(className)
        val newBuilder = FunSpec.builder("new")
        persistDefinition.getAllProperties().forEach { property ->
            val propertyName = property.simpleName.asString()
            val parameterTypeDeclaration = property.type.resolve().declaration
            val propertyClassName = ClassName(
                parameterTypeDeclaration.packageName.asString(),
                parameterTypeDeclaration.simpleName.asString()
            )
            newBuilder.addParameter(propertyName, propertyClassName)
        }
        newBuilder.addCode("""
            return ${persistDefinition.getDataHolderClassName()}(${persistDefinition.getAllProperties().map { it.simpleName.asString() }.joinToString(", ")})
        """.trimIndent())
        newBuilder.returns(ClassName(persistDefinition.packageName.asString(), persistDefinition.simpleName.asString()))
        classBuilder.addFunction(newBuilder.build())
        return classBuilder.build()
    }
}