import java.io.*;
import java.util.*;


public class NoriGrid {

    public final int rows;     // n numero des lignes
    public final int cols;     // m numero de colonnes
    public final int numRegions; // numero de regions
    public final int[][] region; // numero de la region de la case region[i][j] (id de la region de la case)

    // constructeur pour creer la grille
    public NoriGrid(int rows, int cols, int numRegions, int[][] region) {
        this.rows = rows;
        this.cols = cols;
        this.numRegions = numRegions;
        this.region = region;
    }

  
    //fonction pour lire un fichier qui contient la representation de la grille 
    // format du fichier : la premiere ligne: n m numRegions 
    // le reste des lignes sont des entiers representant l'Id de la region de chaque case
    public static NoriGrid fromFile(String filename) throws IOException {
        //ouvrir le fichier
    try (Scanner sc = new Scanner(new File(filename))) {

        // tester si le fichier est vide ou si le header(premiere ligne) est incomplet
        if (!sc.hasNextInt()) throw new IOException("Erreur : fichier vide ou format invalide.");

        //recuperer le header (n m numRegions)
        int rows = sc.nextInt();
        int cols = sc.nextInt();
        int numRegions = sc.nextInt();

        // verifier si les valeurs sont negatives ou nulles
        if (rows <= 0 || cols <= 0)
            throw new IOException("Erreur: dimensions invalides (" + rows + "x" + cols + ")");
        if (numRegions <= 0)
            throw new IOException("Erreur: nombre de regions invalide (" + numRegions + ")");

        // creation du tableau qui represente la grille 
        int[][] region = new int[rows][cols];
        // variable sert a garder et detecter le plus grand id de la region
        // sert a verifier si le nombre des regions declarées dans le header est le méme
        int maxRegionFound = 0;

        // lecture de la grille (on parcourt toutes les cases)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // test si le fichier est incomplet
                if (!sc.hasNextInt())
                    throw new IOException("Erreur: donnees manquantes sur les cellules");
                // sinon on lit la valeur ( region de la case(i,j))
                int val = sc.nextInt();

                // test si les valeurs 0 ou negatives
                if (val <= 0)
                    throw new IOException("Erreur: identifiant de region invalide , Les regions doivent etre >= 1.");

                // si ce nest pas le cas alors on remplis la grille avec cette valeur
                region[i][j] = val;

                // si l'id de la region de la case courante est plus grande que maxRegionFound on le garde
                if (val > maxRegionFound) maxRegionFound = val;
            }
        }

        //verification finale: si le numRegions declare trop grand/petit par rapport au maxRegionFound trouvé
        if (maxRegionFound != numRegions) {
            throw new IOException("Erreur de saisie: le header declaré ne corresponds pas au numero de regions dans la grille");
        }
        //si il yas pas de probléme on crée la grille finale
        return new NoriGrid(rows, cols, numRegions, region);
    }
}

    
    //fonction qui donne toutes les cases d'une region spécefique (de la forme int[]{row, col} )
    public List<int[]> getCellsOfRegion(int regionId) {
        //on cree une liste "cells" vide (liste de int pour stocker la position de chaque case)
        List<int[]> cells = new ArrayList<>();
        //on parcour toutes les lignes et colonnes 
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
        //si  l'id de la region de la case (i, j) est egale à la région donnée
        //alors on ajoute cette position a la liste 
                if (region[i][j] == regionId)
                    cells.add(new int[]{i, j});
        // on retourne toute les cases retrouvés
        return cells;
    }


    //fonction qui donne tout les voisins d'une case (i,j)
    public List<int[]> getNeighbours(int i, int j) {
        //creer une liste vides "nb" des voisins(liste de int pour stocker la position de chaque voisin)
        List<int[]> nb = new ArrayList<>();

        //dirs represente les direction possible 
        //  {-1,0}=haut   {1,0}=bas   {0,1}=droite   {0,-1}gauche

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        // pour chaque direction (d dans dirs)
        for (int[] d : dirs) {
            //nouvelle position du voisin : ligne= i +deplacement / colonne= j+deplacement
            // par ex: si (i, j) = (2, 3) et d = {-1, 0} le voisin = (1, 3) 
            int ni = i + d[0];
            int nj = j + d[1];

            //on verifie que le voisin appartient a la grille, alors on l'ajoute 
            if (ni >= 0 && ni < rows && nj >= 0 && nj < cols)
                nb.add(new int[]{ni, nj});
        }
        // on retourne tout les voisins
        return nb;
    }

}
