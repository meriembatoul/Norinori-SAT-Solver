import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.Reader;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

import java.io.*;

//Cette classe permet d’utiliser le solveur SAT4J pour résoudre  un problème SAT écrit au format DIMACS
//Si une solution existe on récupère le modèle (les valeurs des variables) sinon on retourne null


public class SatSolver {

     /*
      Cette méthode prend en entrée le chemin vers un fichier DIMACS
      et essaie de résoudre le problème SAT correspondant.
     
      Entree: dimacsFile = chemin vers le fichier .cnf
      Retour :   - un tableau d'entiers (modèle) si SAT
                 - null si UNSAT (pas de solution)
     */

    public static int[] solve(String dimacsFile) {
        try {
            ISolver solver = SolverFactory.newDefault();   // On crée un solveur SAT standard fourni par SAT4J
            solver.setTimeout(3600);      // On fixe un timeout pour éviter que ça tourne à l’infini

            Reader reader = new DimacsReader(solver);    // Le reader lit le fichier DIMACS et construit la formule pour le solveur
            IProblem problem = reader.parseInstance(dimacsFile);    // Ici on charge le fichier .cnf

            if (problem.isSatisfiable())     // On demande au solveur s’il existe une solution
            {
                System.out.println("SAT - solution trouvee");
                return problem.model();
            } else {
                System.out.println("UNSAT - il n'éxiste pas de solution pour cette grille");
                return null;
            }
        } catch (Exception e) {

            // Gestion des erreurs possibles lors de la lecture ou de la résolution :
            // fichier introuvable, format DIMACS incorrect, ou erreur interne du solveur

            System.err.println("Erreur lors de la résolution SAT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
