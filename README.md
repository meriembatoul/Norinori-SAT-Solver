# 🧩 Norinori SAT Solver

Solveur automatique de grilles de **Norinori**, basé sur la **logique propositionnelle** : la grille est traduite en formule **CNF** (format DIMACS), puis résolue par le solveur **SAT4J**.
Projet universitaire réalisé en Java.


## Le jeu

On dispose d'une grille découpée en **régions**. Il faut noircir des cases de façon que :

1. **chaque région contienne exactement 2 cases noires** ;
2. **chaque case noire ait exactement 1 voisine noire** (haut, bas, gauche, droite) : les cases noires forment des dominos.

## Comment ça marche

Chaque case `(i, j)` devient une variable booléenne `x_ij` (vraie si la case est noire).

| Règle | Encodage en clauses |
|---|---|
| Au moins 2 cases noires par région | pour chaque case `a` de la région : une clause avec toutes les *autres* cases |
| Au plus 2 cases noires par région | pour chaque triplet de cases : `¬x_a ∨ ¬x_b ∨ ¬x_c` |
| Au moins 1 voisine noire | `¬x ∨ (voisines de x)` |
| Au plus 1 voisine noire | pour chaque paire de voisines : `¬x ∨ ¬v1 ∨ ¬v2` |

Le programme écrit ces clauses dans un fichier `output/problem.cnf` (format DIMACS standard), le donne à SAT4J, puis réaffiche la solution sous forme de grille et vérifie qu'elle respecte les contraintes.

## Utilisation

### Prérequis

- JDK 21 ou plus
- SAT4J (`lib/org.ow2.sat4j.core-2.3.6.jar`, déjà fourni)

### Compilation (dans le dossier `src/`)

```bash
# Linux / Mac / Git Bash
javac -cp .:../lib/org.ow2.sat4j.core-2.3.6.jar *.java

# Windows (cmd / PowerShell)
javac -cp .;../lib/org.ow2.sat4j.core-2.3.6.jar *.java
```

### Mode graphique

```bash
java -cp .:../lib/org.ow2.sat4j.core-2.3.6.jar Main
```

Saisir le nombre de lignes, de colonnes et de régions, attribuer une région à chaque case avec la souris, puis cliquer sur **Résoudre**.

### Mode terminal

```bash
java -cp .:../lib/org.ow2.sat4j.core-2.3.6.jar Main ../grids/grid6.txt
```

Résultat (les cases noires sont entre crochets, avec leur numéro de région) :

```
Variables: 36  Clauses: 347
SAT - solution trouvee

 1   1   1  [1] [2]  2
 1  [3] [3]  2   2   2
[1]  4   4  [4]  4  [2]
[6]  6   6  [6]  4  [4]
 6   6  [5]  5   5   5
 6   6  [5]  7  [7] [7]
```

### Format d'une grille

```
n m r          (n lignes, m colonnes, r régions)
matrice n x m  (numéro de région de chaque case, à partir de 1)
```

## Grilles fournies

| Fichier | Contenu |
|---|---|
| `grid6.txt` | grille valide de 6x6 avec 7 régions |
| `grid2.txt` | cas minimal (1x2, une seule région) |
| `grid1.txt` | fichier vide : test de détection d'erreur |
| `grid3.txt` | valeurs invalides (0 et négatives) |
| `grid4.txt` | dimensions déclarées incorrectes |
| `grid5.txt` | nombre de régions incohérent |

Le programme détecte ces erreurs et affiche un message clair au lieu de planter.

## Pistes d'amélioration

- Vérifier l'unicité de la solution.
- Générer automatiquement des grilles.

