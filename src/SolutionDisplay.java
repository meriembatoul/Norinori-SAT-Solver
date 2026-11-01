import java.io.*;
import java.util.*;

//Cette classe sert à afficher la solution du solveur SAT sous forme de grille Norinori lisible
public class SolutionDisplay {
     
    //entree: @param grid  la grille de départ avec régions
    //@param model la solution
            

    public static void display(NoriGrid grid, int[] model, DimacsGenerator gen) {

        // tableau 2D qui représente les cases noires de la grille
        // true = case noire, false = case blanche
        boolean[][] black = new boolean[grid.rows][grid.cols];    

        // on parcour toutes les variables retournées par le SAT solver
        for (int lit : model) {

            // si lit > 0, ça veut dire que la variable est vraie
            if (lit > 0) {
                
                int idx = lit - 1;    // on transforme la variable SAT en index 0-based
                int i = idx / grid.cols;      // calcul de la ligne dans la grille
                int j = idx % grid.cols;      // calcul de la colonne dans la grille

                if (i < grid.rows && j < grid.cols)  // on vérifie qu'on reste dans la grille
                    black[i][j] = true;     // on marque la case comme noire
            }
        }

        System.out.println("\n    SOLUTION NORINORI    \n"); 

     
    for (int i = 0; i < grid.rows; i++) {
    for (int j = 0; j < grid.cols; j++) {

        if (black[i][j])         // si la case est noire, on la met entre crochets 
            System.out.printf("[%d] ", grid.region[i][j]);

        else             // sinon on affiche normalement la région      
            System.out.printf(" %d  ", grid.region[i][j]);
    }
    System.out.println();   // retour à la ligne après chaque ligne de la grille
}
        System.out.println();

        // Verification
        boolean valid = verify(grid, black);   // on vérifie si la solution respecte toutes les règles

        // affichage résultat final
        if (valid)     
            System.out.println("La solution est VALIDE, toute les contraintes sont satisfiables \n");
        else
            System.out.println("La solution est INVALIDE, verifie les contraintes \n");
    }

    // Vérifie si la solution respecte les règles du jeu Norinori
    public static boolean verify(NoriGrid grid, boolean[][] black) {

        boolean ok = true;       // variable globale qui dit si tout est correct

        // Regle 1 : chaque région doit avoir exactement 2 cases noires
        for (int r = 1; r <= grid.numRegions; r++) {

            List<int[]> cells = grid.getCellsOfRegion(r);      // récupère toutes les cellules appartenant à la région r
            int count = 0;   // compteur de cases noires dans la région

            for (int[] c : cells)      // on parcourt toutes les cellules de la région
                if (black[c[0]][c[1]]) 
                    count++;

            // si on n'a pas exactement 2 cases noires: erreur
            if (count != 2) {
                System.out.println("la Region \" + r + \" a \" + count + \" cases noires (il faut exactement 2)");
                ok = false;
            }
        }

        //  Regle 2  : chaque case noire doit avoir exactement 1 voisin noir
        for (int i = 0; i < grid.rows; i++) {

            for (int j = 0; j < grid.cols; j++) {

                if (black[i][j]) {     // on ne traite que les cases noires
                    
                    List<int[]> nb = grid.getNeighbours(i, j);   //on recupere les voisins de la case:haut, bas, gauche, droite
                    int blackNb = 0;      // compteur de voisins noirs

                    for (int[] n : nb)       // on parcourt les voisins
                        if (black[n[0]][n[1]]) blackNb++;       // si voisin noir alors on incrémente

                    if (blackNb != 1) {       // il doit y avoir exactement 1 voisin noir
                        System.out.println("la case  (\" + i + \",\" + j + \") a \"\n" +
"                                + blackNb + \" voisin noirs (il faut exactement 1)");
                        ok = false;
                    }
                }
            }
        }
        return ok;    // retourne vrai si aucune erreur détectée
    }


    /**
     Lit un fichier de sortie d’un SAT solver (format DIMACS)
     et reconstruit le modèle sous forme de tableau d’entiers.
     */
    public static int[] readModelFromFile(String solverOutputFile) throws IOException {
        List<Integer> lits = new ArrayList<>();// liste temporaire pour stocker les littéraux

        // ouverture du fichier
        try (BufferedReader br = new BufferedReader(new FileReader(solverOutputFile))) {
            String line;

            while ((line = br.readLine()) != null) {   // lecture ligne par ligne
                line = line.trim();    // nettoyage espaces

                if (line.startsWith("v ") || line.startsWith("V ")) {  // seules les lignes "v" contiennent le modèle
                    String[] parts = line.substring(2).trim().split("\\s+");  // on enlève "v " puis on découpe la ligne

                    for (String p : parts) {
                        int val = Integer.parseInt(p);  // conversion en entier
                        if (val != 0) lits.add(val);   // 0 = fin de ligne SAT du coup ignoré
                    }
                }
            }
        }
        return lits.stream().mapToInt(Integer::intValue).toArray();   // conversion List<Integer> = int[]
    }
}
