package hai913i.tp2.application;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import hai913i.tp2.utils.CouplingScanner;
import hai913i.tp2.utils.Pair;


public class Main
{
    static String graphFilePath = "src/main/resources/graph.dot";
    private static String projectSourcePath;
    
    public static void main(String[] args) {
    	
    	
//    	Exercice n°1 - AVEC SPOON
    	
//    	RECUPERATION DU PATH DANS LES PROPERTIES
    	Properties properties = new Properties();
		String projectPath = null;
		
		try (InputStream fis = new FileInputStream("src/main/resources/projectPath.properties"))
		{
			properties.load(fis);
            projectPath = properties.getProperty("path");

        } catch (IOException io) {
            io.printStackTrace();
        }
        
		projectSourcePath = projectPath + "/src/main/java";
    	
		// INITIALISATION SPOON
    	Launcher spoon = new Launcher();
        spoon.addInputResource(projectSourcePath);
        spoon.buildModel();
        CtModel model = spoon.getModel();
        
        // Affiche le nombre de types chargés dans le modèle
        System.out.println("Nombre de types chargés: " + model.getAllTypes().size());

        Factory factory = model.getRootPackage().getFactory();
        for (CtPackage ctPackage : factory.Package().getAll()) {
            System.out.println("Package: " + ctPackage.getQualifiedName());
            for (CtType<?> ctType : ctPackage.getTypes()) {
                if (ctType instanceof CtClass) {
                    System.out.println("  Classe: " + ctType.getQualifiedName());
                }
            }
        }
        
        // Exo-1 Q1:
        
        CouplingScanner scanner = new CouplingScanner();
        model.getRootPackage().accept(scanner);
        
        CtClass<?> classA = findClassByName(model, "Counter");
		CtClass<?> classB = findClassByName(model, "CounterController");
        		
//        System.out.println("Exemple du couplage entre Counter et CounterController : " + calculateCouplingByVisitor(classA, classB, scanner.getTypeToMethodCalls()));
        System.out.println("Exemple du couplage entre Counter et CounterController : " + calculateCouplingBetweenClasses("Counter", "CounterController", model));
    	
        
        // Exo-1 Q2:
        
        Map<Pair<String, String>, Double> couplingGraph = new HashMap<>();
        
        System.out.println("Creation du graphe de couplage ...");
        fillCouplingGraph(couplingGraph, model);
        System.out.println("Creation du graphe termine !");
        exportGraph(couplingGraph);
        System.out.println("Graphe sauvegarder !");
        
        // Exo-2 Q1:
        // L'algorithme suivra une approche ascendante (agglomérative), où chaque 
        // classe commence comme son propre cluster et, à chaque étape, les deux 
        // clusters les plus couplés sont fusionnés jusqu'à ce que tous les clusters 
        // soient regroupés en un seul.
    }
    
    private static void exportGraph(Map<Pair<String, String>, Double> couplingGraph)
	{
    	StringBuilder dotBuilder = new StringBuilder("digraph G {\n");

        for (Map.Entry<Pair<String, String>, Double> entry : couplingGraph.entrySet()) {
            Pair<String, String> classPair = entry.getKey();
            Double couplingValue = entry.getValue();

            // Ajouter chaque arête avec une étiquette de poids
            dotBuilder.append(String.format("  \"%s\" -> \"%s\" [label=\"%.3f\"];\n",
                    classPair.getLeft(), classPair.getRight(), couplingValue));
        }

        dotBuilder.append("}");

        // Écrire le contenu dans un fichier DOT
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/coupling_graph.dot"))) {
            writer.write(dotBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

    // Remplis une collection avec les éléments d'un model
	private static void fillCouplingGraph(Map<Pair<String, String>, Double> couplingGraph, CtModel model)
	{
    	List<CtClass<?>> classes = model.getElements(new TypeFilter<>(CtClass.class));
    	
        for (CtClass<?> classA : classes) {
            for (CtClass<?> classB : classes) {
                if (!classA.equals(classB)) {
                    double coupling = calculateCouplingBetweenClasses(classA.getSimpleName(), classB.getSimpleName(), model);
                    couplingGraph.put(new Pair<>(classA.getQualifiedName(), classB.getQualifiedName()), coupling);
                }
            }
        }
	}
	
	// A FAIRE // Remplis une collection avec les éléments d'un model
	private static void fillCouplingGraphSpoon(Map<Pair<String, String>, Double> couplingGraph, CouplingScanner scanner)
	{
	}

	// Calcule le couplage entre 2 classes
    private static double calculateCouplingBetweenClasses(String classNameA, String classNameB, CtModel model) {
        int totalRelations = 0;
        int relationsBetweenAAndB = 0;

        for (CtType<?> ctType : model.getAllTypes()) {
            if (ctType instanceof CtClass) {
                CtClass<?> ctClass = (CtClass<?>) ctType;
                
	            for (CtMethod<?> method : ctClass.getMethods()) {
	                // Ici, comptez les appels de méthodes dans toutes les classes
	                totalRelations += countMethodCalls(method);
	
	                // Comptez les appels de méthodes spécifiques entre ClassA et ClassB
	                if (ctClass.getSimpleName().equals(classNameA)) {
	                    relationsBetweenAAndB += countCallsToClass(method, classNameB);
	                }
	                if (ctClass.getSimpleName().equals(classNameB)) {
	                    relationsBetweenAAndB += countCallsToClass(method, classNameA);
	                }
	            }
            }
        }
        
        return (double) relationsBetweenAAndB / totalRelations;
    }
    
    // Parcoure le corps d'une méthode donnée et compte tous les appels de méthodes.
    private static int countMethodCalls(CtMethod<?> method) {
        return method
                .getElements(new TypeFilter<>(CtInvocation.class))
                .size();
    }
    
    // Compte combien de fois une méthode donnée appelle des méthodes appartenant à une classe spécifique
    private static int countCallsToClass(CtMethod<?> method, String className) {
        List<CtInvocation<?>> invocations = method.getElements(new TypeFilter<>(CtInvocation.class));
        int count = 0;
        for (CtInvocation<?> invocation : invocations) {
            CtExecutableReference<?> executableReference = invocation.getExecutable();
            if (executableReference != null && executableReference.getDeclaringType() != null) {
                CtTypeReference<?> typeReference = executableReference.getDeclaringType();
                if (typeReference.getSimpleName().equals(className)) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public static double calculateCouplingByVisitor(CtType<?> classA, CtType<?> classB, Map<CtType<?>, Map<CtMethod<?>, Set<CtExecutableReference<?>>>> typeToMethodCalls) {
        double numberOfRelationsBetweenAandB = 0;
        int totalNumberOfRelations = 0;

        for (Map.Entry<CtType<?>, Map<CtMethod<?>, Set<CtExecutableReference<?>>>> classEntry : typeToMethodCalls.entrySet()) {
            for (Map.Entry<CtMethod<?>, Set<CtExecutableReference<?>>> methodEntry : classEntry.getValue().entrySet()) {
                for (CtExecutableReference<?> calledMethod : methodEntry.getValue()) {
                    // Compter le nombre total de relations
                    totalNumberOfRelations++;

                    // Vérifier si l'appel de méthode est entre classA et classB
                    if ((classEntry.getKey().equals(classA) && calledMethod.getDeclaringType().equals(classB.getReference())) ||
                        (classEntry.getKey().equals(classB) && calledMethod.getDeclaringType().equals(classA.getReference()))) {
                        numberOfRelationsBetweenAandB++;
                    }
                }
            }
        }

        return totalNumberOfRelations > 0 ? numberOfRelationsBetweenAandB / totalNumberOfRelations : 0;
    }

    public static CtClass<?> findClassByName(CtModel model, String className) {
        for (CtType<?> type : model.getAllTypes()) {
            if (type instanceof CtClass && type.getSimpleName().equals(className)) {
                return (CtClass<?>) type;
            }
        }
        return null; // ou gérer l'absence de la classe différemment
    }

}
