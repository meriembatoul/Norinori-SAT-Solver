import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

// Interface graphique Swing pour le solveur Norinori
public class NoriGUI extends JFrame {

    private static final int CELL_SIZE = 60;
    private static final Color COLOR_BLACK_CELL = new Color(60, 60, 60);
    private static final Color COLOR_WHITE_CELL = Color.WHITE;
    private static final Color COLOR_GRID_LINE  = Color.DARK_GRAY; 

    private int rows, cols, numRegions;
    private int[][] regionGrid;   // regionGrid[i][j] = numero de region (0 = non attribue)
    private boolean[][] solution; // solution[i][j] = true si case noire
    private boolean showSolution = false;

    private JPanel gridPanel;
    private JLabel statusLabel;
    private int currentRegion = 1; // region selectionnee dans la palette

    //Lance l'interface graphique 
    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            NoriGUI gui = new NoriGUI();
            gui.showSetupDialog();
        });
    }

    // Constructeur

    public NoriGUI() {
        setTitle("Solveur Norinori");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    // Dialogue de configuration (nombre de lignes/colonnes/regions)
    //Affiche une boite de dialogue pour saisir les dimensions de la grille

    private void showSetupDialog() {
        JTextField rowsField    = new JTextField("6", 5);
        JTextField colsField    = new JTextField("6", 5);
        JTextField regionsField = new JTextField("7", 5);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Nombre de lignes :"));
        panel.add(rowsField);
        panel.add(new JLabel("Nombre de colonnes :"));
        panel.add(colsField);
        panel.add(new JLabel("Nombre de regions :"));
        panel.add(regionsField);

        int result = JOptionPane.showConfirmDialog(
            null, panel, "Nouvelle grille Norinori",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        try {
            rows       = Integer.parseInt(rowsField.getText().trim());
            cols       = Integer.parseInt(colsField.getText().trim());
            numRegions = Integer.parseInt(regionsField.getText().trim());
            if (rows < 1 || cols < 1 || numRegions < 1)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Valeurs invalides.", "Erreur", JOptionPane.ERROR_MESSAGE);
            showSetupDialog();
            return;
        }

        regionGrid   = new int[rows][cols];
        solution     = new boolean[rows][cols];
        showSolution = false;
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Construction de l'interface

    //Construit tous les composants Swing de la fenetre principale
    private void buildUI() {
        getContentPane().removeAll();

        //  Panneau du haut : palette de regions
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Region selectionnee : "));
        ButtonGroup group = new ButtonGroup();
        for (int r = 1; r <= numRegions; r++) {
            final int rid = r;
            JToggleButton btn = new JToggleButton(String.valueOf(r));
            btn.setBackground(Color.LIGHT_GRAY);
            btn.setPreferredSize(new Dimension(55, 30));
            btn.addActionListener(e -> currentRegion = rid);
            if (r == 1) btn.setSelected(true);
            group.add(btn);
            topPanel.add(btn);
        }
        add(topPanel, BorderLayout.NORTH);

        //Panneau central : grille
        gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        gridPanel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE));
        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleCellClick(e.getX(), e.getY());
            }
        });
        add(gridPanel, BorderLayout.CENTER);

        //Panneau du bas : boutons + statut
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel btnPanel = new JPanel(new FlowLayout());

        JButton solveBtn = new JButton("Resoudre");
solveBtn.addActionListener(e -> solve());
btnPanel.add(solveBtn);

JButton newBtn = new JButton("Nouvelle grille");
newBtn.addActionListener(e -> showSetupDialog());
btnPanel.add(newBtn);

        statusLabel = new JLabel("Attribuez un numero de region a chaque case, puis cliquez sur Resoudre.");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        bottomPanel.add(btnPanel, BorderLayout.NORTH);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Dessin de la grille avec la solution si disponible.

    private void drawGrid(Graphics g) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;

                // Couleur de fond
                if (showSolution && solution[i][j]) {
                    g.setColor(COLOR_BLACK_CELL);
                } else {
                    g.setColor(COLOR_WHITE_CELL);
                }
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Bordure
                g.setColor(COLOR_GRID_LINE);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                // Texte : numero de region
                if (regionGrid[i][j] > 0) {
                    if (showSolution && solution[i][j])
                        g.setColor(Color.WHITE);
                    else
                        g.setColor(Color.BLACK);
                    String txt = String.valueOf(regionGrid[i][j]);
                    FontMetrics fm = g.getFontMetrics();
                    int tx = x + (CELL_SIZE - fm.stringWidth(txt)) / 2;
                    int ty = y + (CELL_SIZE + fm.getAscent()) / 2 - 2;
                    g.drawString(txt, tx, ty);
                }
            }
        }
    }
    // Interaction utilisateur

    // Gere le clic sur une case de la grille,Attribue la region selectionnee a la case cliquee

    private void handleCellClick(int x, int y) {
        if (showSolution) return; // pas de modification apres resolution
        int j = x / CELL_SIZE;
        int i = y / CELL_SIZE;
        if (i < 0 || i >= rows || j < 0 || j >= cols) return;
        regionGrid[i][j] = currentRegion;
        gridPanel.repaint();
    }

    // Resolution SAT
 //Verifie que toutes les cases ont une region, genere le fichier DIMACS, appelle SAT4J et affiche la solution dans la grille.
    private void solve() {
        // Verification : toutes les cases ont une region
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                if (regionGrid[i][j] == 0) {
                    statusLabel.setText("Erreur : il en reste des cases sans regions !");
                    return;
                }

        statusLabel.setText("Generation CNF");

        try {
            // Creer le dossier output si besoin
            new File("output").mkdirs();
            String dimacsFile = "output/problem.cnf";

            NoriGrid grid = new NoriGrid(rows, cols, numRegions, regionGrid);
            DimacsGenerator gen = new DimacsGenerator(grid);
            gen.generate(dimacsFile);

            statusLabel.setText("SAT-solveur");

            // Lancer le solveur dans un thread separe pour ne pas bloquer l'interface
            new Thread(() -> {
                int[] model = SatSolver.solve(dimacsFile);
                SwingUtilities.invokeLater(() -> {
                    if (model == null) {
                        statusLabel.setText("UNSAT : aucune solution pour cette grille.");
                        return;
                    }
                    // Remplir le tableau solution
                    for (int i2 = 0; i2 < rows; i2++)
                        for (int j2 = 0; j2 < cols; j2++)
                            solution[i2][j2] = false;
                    for (int lit : model) {
                        if (lit > 0) {
                            int idx = lit - 1;
                            int si  = idx / cols;
                            int sj  = idx % cols;
                            if (si < rows && sj < cols)
                                solution[si][sj] = true;
                        }
                    }
                    showSolution = true;
                    gridPanel.repaint();
                    statusLabel.setText("Solution trouvée !");
                });
            }).start();

        } catch (Exception ex) {
            statusLabel.setText("Erreur : " + ex.getMessage());
        }
    }

}