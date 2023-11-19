package hai913i.tp2.application;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
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
    	
    	Scanner scannerUtil = new Scanner(System.in);
    	
//    	Exercice n°1
    	
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
        
        CtClass<?> classA = null;
        CtClass<?> classB = null;
        String classAname, classBname;
        
        System.out.println("Exercice 1.1 : Couplage entre les classes");
        CouplingScanner scanner = new CouplingScanner();
        model.getRootPackage().accept(scanner);
        
        do
        {
        	System.out.println("Rentrez le nom de la classe A (ex : Counter)");
            classAname = scannerUtil.nextLine();
            System.out.println("Rentrez le nom de la classe B (ex : CounterController)");
            classBname = scannerUtil.nextLine();
            
            classA = findClassByName(model, classAname);
    		classB = findClassByName(model, classBname);
    		
    		if (classA == null)
    		{
    			System.out.println("La classe " + classAname + "est introuvable, rentrer une classe présente dans le projet");
    		}
    		
    		if (classB == null)
    		{
    			System.out.println("La classe " + classBname + "est introuvable, rentrer une classe présente dans le projet");
    		}
    		
        } while (classA == null || classB == null);
        
        // Version visitor
        System.out.println("Couplage entre " + classAname + " et " + classBname + " : " + calculateCouplingByVisitor(classA, classB, scanner.getTypeToMethodCalls()));

//        // Version base
//        System.out.println("Couplage entre " + classAname + " et " + classBname + " : " + calculateCouplingBetweenClasses(classAname, classBname, model));
    	
        
        // Exo-1 Q2:
        System.out.println("Exercice 1.2 : Graphe de couplage");
        System.out.println("Voulez-vous générer le coupling graph du projet cible (y/n) ?");
        String userInput = scannerUtil.nextLine();
        
        if (userInput.equals("y"))
        {
        	Map<Pair<String, String>, Double> couplingGraph = new HashMap<>();
            
            System.out.println("Creation du graphe de couplage ...");
            fillCouplingGraph(couplingGraph, model);
            System.out.println("Creation du graphe termine !");
            exportGraph(couplingGraph);
            System.out.println("Graphe sauvegarder !");
        }
        
        System.out.println("Merci, au revoir !");
        
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
	
	private static void fillCouplingGraphSpoon(Map<Pair<String, String>, Double> couplingGraph, CouplingScanner scanner) {
	    Map<CtType<?>, Map<CtMethod<?>, Set<CtExecutableReference<?>>>> typeToMethodCalls = scanner.getTypeToMethodCalls();

	    for (Map.Entry<CtType<?>, Map<CtMethod<?>, Set<CtExecutableReference<?>>>> classEntry : typeToMethodCalls.entrySet()) {
	        CtType<?> classA = classEntry.getKey();
	        for (Map.Entry<CtMethod<?>, Set<CtExecutableReference<?>>> methodEntry : classEntry.getValue().entrySet()) {
	            for (CtExecutableReference<?> calledMethod : methodEntry.getValue()) {
	                CtTypeReference<?> classBTypeRef = calledMethod.getDeclaringType();
	                if (classBTypeRef != null) {
	                    CtType<?> classB = classBTypeRef.getDeclaration();
	                    if (classB != null && !classA.equals(classB)) {
	                        Pair<String, String> classPair = new Pair<>(classA.getQualifiedName(), classB.getQualifiedName());
	                        couplingGraph.merge(classPair, 1.0, Double::sum);
	                    }
	                }
	            }
	        }
	    }

	    // Normaliser les valeurs de couplage
	    normalizeCouplingValues(couplingGraph);
	}

	private static void normalizeCouplingValues(Map<Pair<String, String>, Double> couplingGraph) {
	    double maxCouplingValue = couplingGraph.values().stream().max(Double::compare).orElse(1.0);
	    for (Pair<String, String> key : couplingGraph.keySet()) {
	        double normalizedValue = couplingGraph.get(key) / maxCouplingValue;
	        couplingGraph.put(key, normalizedValue);
	    }
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
