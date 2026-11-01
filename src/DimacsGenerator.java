import java.io.*;
import java.util.*;

public class DimacsGenerator {

    private final NoriGrid grid;  // La grille du puzzle
    private final List<int[]> clauses;  // Liste de toutes les contraintes logiques (clauses)
    private int numVars;  // Nombre total de variables (une variable par case)
    
    // Constructeur :
    public DimacsGenerator(NoriGrid grid) {
        this.grid = grid;
        this.clauses = new ArrayList<>();
        this.numVars = grid.rows * grid.cols;
    }
    
    // Convertit une case (i,j) en numéro de variable SAT
    public int varId(int i, int j) {
        return i * grid.cols + j + 1;
    }

   
    public void generate(String outputFile) throws IOException {
        clauses.clear();  // vider anciennes clauses
        encodeRegionConstraints();   // règle 1 : régions
        encodeNeighbourConstraints(); // règle 2 : voisins
        writeDimacs(outputFile); // écrit le fichier CNF
        // affichage info
        System.out.println("DIMACS file written: " + outputFile);
        System.out.println("Variables: " + numVars + "  Clauses: " + clauses.size());
    }

   

    private void encodeRegionConstraints() {
    for (int r = 1; r <= grid.numRegions; r++) { // boucle sur chaque région (1, 2, 3, ..., numRegions)
        List<int[]> cells = grid.getCellsOfRegion(r); // chaque case = tableau [i, j]
        int k = cells.size();  // nombre de cases dans cette région
        
        // une région doit contenir au moins 2 cases
        if (k < 2) { 
            throw new IllegalStateException(
                "Erreur : la region " + r + " contient " + k + " cellule(s). Minimum requis : 2.");
        }

            // (a) AU MOINS 2 CASES NOIRES
            for (int a = 0; a < k; a++) {
            int[] clause = new int[k - 1]; // une clause contient (k-1) variables
            int idx = 0;
            // on parcourt toutes les cases
            for (int b = 0; b < k; b++) {
                if (b != a)  // on ignore la case de a 
                    clause[idx++] = varId(cells.get(b)[0], cells.get(b)[1]); // on ajoute la variable correspondant à la case (i,j)
            }
            clauses.add(clause); // on ajoute cette clause dans la liste globale
        }

            // (b) AU PLUS 2 CASES NOIRES
            // on prend toutes les combinaisons de 3 cases
            for (int a = 0; a < k; a++) {
            for (int b = a + 1; b < k; b++) {
                for (int c = b + 1; c < k; c++) {
                    int va = varId(cells.get(a)[0], cells.get(a)[1]);
                    int vb = varId(cells.get(b)[0], cells.get(b)[1]);
                    int vc = varId(cells.get(c)[0], cells.get(c)[1]); // on récupère les variables SAT des 3 cases
                    clauses.add(new int[]{-va, -vb, -vc}); // les 3 ne peuvent pas être vrai en même temps
                }
            }
        }
    }
}

    
    private void encodeNeighbourConstraints() {
        // on parcourt toutes les cases de la grille
        for (int i = 0; i < grid.rows; i++) {
            for (int j = 0; j < grid.cols; j++) {
                int v = varId(i, j); // variable SAT correspondant à la case (i,j)
                List<int[]> nb = grid.getNeighbours(i, j); // liste des voisins de la case (haut, bas, gauche, droite)
                int nbSize = nb.size(); // nombre de voisins

                // (a) AU MOINS 1 VOISIN NOIR
                int[] atLeastOne = new int[nbSize + 1]; // on crée une clause de taille (nbSize + 1)
                 //  si la case est noire :
                atLeastOne[0] = -v;
                // on ajoute tous les voisins 
                for (int k = 0; k < nbSize; k++)
                    atLeastOne[k + 1] = varId(nb.get(k)[0], nb.get(k)[1]); // convertit voisin (i,j) en variable SAT
                clauses.add(atLeastOne); // ajoute la clause

                // (b) AU PLUS 1 VOISIN NOIR
                // on prend toutes les paires de voisins
                for (int a = 0; a < nbSize; a++) {
                    for (int b = a + 1; b < nbSize; b++) {
                        int vn1 = varId(nb.get(a)[0], nb.get(a)[1]); 
                        int vn2 = varId(nb.get(b)[0], nb.get(b)[1]);  // variables des voisins
                        clauses.add(new int[]{-v, -vn1, -vn2}); // interdit que 2 voisins soient noirs
                    }
                }
            }
        }
    }



    private void writeDimacs(String outputFile) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputFile))){ // ouvre le fichier en écriture 
             
            pw.println("c Norinori puzzle - CNF encoding");
            pw.println("c Variables: x_ij = 1 iff cell (i,j) is black");
            pw.println("c varId(i,j) = i*cols + j + 1  (0-indexed i,j)");
            pw.println("c Grid: " + grid.rows + " rows x " + grid.cols + " cols, "
                       + grid.numRegions + " regions");   // lignes de commentaire (commencent par "c")
            pw.println("p cnf " + numVars + " " + clauses.size());   // p cnf <nbVariables> <nbClauses>
            // pour chaque clause
            for (int[] clause : clauses) {
                StringBuilder sb = new StringBuilder(); // construit la ligne texte
                for (int lit : clause) {
                    sb.append(lit).append(' '); // ajoute la variable (ex: 1, -2, 3...)
                }
                sb.append('0'); // 0 = fin de clause en DIMACS
                pw.println(sb); //ecrit la clause dans le fichier
            }
        }
    }
}