package hai913i.tp2.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;

public class CouplingScanner extends CtScanner
{
	// Structure pour stocker les informations sur les appels de méthode
    // Par exemple, une Map<CtClass<?>, Map<CtMethod<?>, Set<CtMethod<?>>>>
    private Map<CtType<?>, Map<CtMethod<?>, Set<CtExecutableReference<?>>>> typeToMethodCalls = new HashMap<>();

    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {
        // Initialiser ou mettre à jour la structure pour cette classe
    	typeToMethodCalls.putIfAbsent(ctClass, new HashMap<>());
        super.visitCtClass(ctClass);
    }

    @Override
    public <T> void visitCtMethod(CtMethod<T> method) {
        CtType<?> declaringType = method.getDeclaringType();
        if (declaringType != null) {
            typeToMethodCalls.putIfAbsent(declaringType, new HashMap<>());
            typeToMethodCalls.get(declaringType).putIfAbsent(method, new HashSet<>());
            
            method.getElements(e -> e instanceof CtInvocation<?>).forEach(invocation -> {
                CtExecutableReference<?> invokedMethod = ((CtInvocation<?>) invocation).getExecutable();
                typeToMethodCalls.get(declaringType).get(method).add(invokedMethod);
            });
        }
        
        super.visitCtMethod(method);
    }


    // Méthodes pour accéder aux données collectées
    public Map<CtType<?>, Map<CtMethod<?>, Set<CtExecutableReference<?>>>> getTypeToMethodCalls() {
        return typeToMethodCalls;
    }
}
