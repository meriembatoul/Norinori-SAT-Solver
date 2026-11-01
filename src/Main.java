import java.io.IOException;


public class Main {

    public static void main(String[] args) {

        // Si aucun argument on lance l'interface graphique
        if (args.length == 0) {
            // ouvre la fenêtre du jeu
            NoriGUI.launch();

        } else {       // Sinon mode terminal (avec fichier)
  

            // args[1] = fichier CNF (optionnel)
            String gridFile   = args[0]; // args[0] = fichier de la grille donné par l'utilisateur
// si args contient au moins 2 arguments, on utilise args[1] comme fichier CNF
// sinon on utilise "output/problem.cnf" par défaut
            String dimacsFile = args.length >= 2 ? args[1] : "output/problem.cnf";

            try {

                // Lire la grille 
                System.out.println("Lecture de la grille : " + gridFile);
                NoriGrid grid = NoriGrid.fromFile(gridFile);   // charger la grille depuis le fichier

                // Générer le CNF 
                System.out.println("\nGeneration CNF");
                DimacsGenerator gen = new DimacsGenerator(grid);   // objet qui transforme la grille en contraintes
                new java.io.File("output").mkdirs();     // créer le dossier "output" s'il n'existe pas
                gen.generate(dimacsFile);  // générer le fichier .cnf

        
                System.out.println("\nResolution SAT");
                int[] model = SatSolver.solve(dimacsFile); // model de resolution 

        
                if (model != null)
                    SolutionDisplay.display(grid, model, gen); // afficher la solution sous forme de grille
                else
                    System.out.println("Aucune solution trouvée");

            } catch (IOException e) {
    System.err.println("Erreur de lecture : " + e.getMessage());
} catch (IllegalStateException e) {
    System.err.println("Erreur de contrainte : " + e.getMessage());
} catch (Exception e) {
    System.err.println("Erreur inattendue : " + e.getMessage());
}
        }
    }
}