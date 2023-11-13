package program;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class Strassen {

    // Classe représentant une tâche Strassen
    static class TacheStrassen extends RecursiveTask<int[][]> {
        private final int[][] A, B;
        private final int debutLigneA, debutColonneA, debutLigneB, debutColonneB, taille;

        public TacheStrassen(int[][] A, int[][] B, int debutLigneA, int debutColonneA, int debutLigneB, int debutColonneB, int taille) {
            this.A = A;
            this.B = B;
            this.debutLigneA = debutLigneA;
            this.debutColonneA = debutColonneA;
            this.debutLigneB = debutLigneB;
            this.debutColonneB = debutColonneB;
            this.taille = taille;
        }

        @Override
        protected int[][] compute() {
            if (taille <= 64) { // Taille limite pour laquelle nous effectuons la multiplication standard
                return multiplierStandard(A, B, debutLigneA, debutColonneA, debutLigneB, debutColonneB, taille);
            }

            int nouvelleTaille = taille / 2;

            // Diviser les matrices en sous-matrices
            int[][] a11 = sousMatrice(A, debutLigneA, debutColonneA, nouvelleTaille);
            int[][] a12 = sousMatrice(A, debutLigneA, debutColonneA + nouvelleTaille, nouvelleTaille);
            int[][] a21 = sousMatrice(A, debutLigneA + nouvelleTaille, debutColonneA, nouvelleTaille);
            int[][] a22 = sousMatrice(A, debutLigneA + nouvelleTaille, debutColonneA + nouvelleTaille, nouvelleTaille);

            int[][] b11 = sousMatrice(B, debutLigneB, debutColonneB, nouvelleTaille);
            int[][] b12 = sousMatrice(B, debutLigneB, debutColonneB + nouvelleTaille, nouvelleTaille);
            int[][] b21 = sousMatrice(B, debutLigneB + nouvelleTaille, debutColonneB, nouvelleTaille);
            int[][] b22 = sousMatrice(B, debutLigneB + nouvelleTaille, debutColonneB + nouvelleTaille, nouvelleTaille);

            // Calculer les produits intermédiaires
            TacheStrassen p1 = new TacheStrassen(additionner(a11, a22), additionner(b11, b22), 0, 0, 0, 0, nouvelleTaille);
            TacheStrassen p2 = new TacheStrassen(additionner(a21, a22), b11, 0, 0, 0, 0, nouvelleTaille);
            TacheStrassen p3 = new TacheStrassen(a11, soustraire(b12, b22), 0, 0, 0, 0, nouvelleTaille);
            TacheStrassen p4 = new TacheStrassen(a22, soustraire(b21, b11), 0, 0, 0, 0, nouvelleTaille);
            TacheStrassen p5 = new TacheStrassen(additionner(a11, a12), b22, 0, 0, 0, 0, nouvelleTaille);
            TacheStrassen p6 = new TacheStrassen(soustraire(a21, a11), additionner(b11, b12), 0, 0, 0, 0, nouvelleTaille);
            TacheStrassen p7 = new TacheStrassen(soustraire(a12, a22), additionner(b21, b22), 0, 0, 0, 0, nouvelleTaille);

            // Fork les tâches
            invokeAll(p1, p2, p3, p4, p5, p6, p7);

            // Calculer les sous-résultats
            int[][] c11 = soustraire(additionner(additionner(p1.join(), p4.join()), p7.join()), p5.join());
            int[][] c12 = additionner(p3.join(), p5.join());
            int[][] c21 = additionner(p2.join(), p4.join());
            int[][] c22 = soustraire(additionner(additionner(p1.join(), p3.join()), p6.join()), p2.join());

            // Combiner les sous-résultats pour obtenir le résultat final
            return combiner(c11, c12, c21, c22, nouvelleTaille);
        }
    }

    // Fonction d'addition de matrices
    private static int[][] additionner(int[][] A, int[][] B) {
        int n = A.length;
        int[][] resultat = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                resultat[i][j] = A[i][j] + B[i][j];
            }
        }
        return resultat;
    }

    // Fonction de soustraction de matrices
    private static int[][] soustraire(int[][] A, int[][] B) {
        int n = A.length;
        int[][] resultat = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                resultat[i][j] = A[i][j] - B[i][j];
            }
        }
        return resultat;
    }

    // Fonction de multiplication de matrices standard
    private static int[][] multiplierStandard(int[][] A, int[][] B, int debutLigneA, int debutColonneA, int debutLigneB, int debutColonneB, int taille) {
        int[][] resultat = new int[taille][taille];
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                for (int k = 0; k < taille; k++) {
                    resultat[i][j] += A[debutLigneA + i][debutColonneA + k] * B[debutLigneB + k][debutColonneB + j];
                }
            }
        }
        return resultat;
    }

    // Fonction pour extraire une sous-matrice à partir d'une matrice donnée
    private static int[][] sousMatrice(int[][] matrice, int debutLigne, int debutColonne, int taille) {
        int[][] resultat = new int[taille][taille];
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                resultat[i][j] = matrice[debutLigne + i][debutColonne + j];
            }
        }
        return resultat;
    }

    // Fonction pour combiner les sous-résultats en une seule matrice résultante
    private static int[][] combiner(int[][] c11, int[][] c12, int[][] c21, int[][] c22, int taille) {
        int nouvelleTaille = taille * 2;
        int[][] resultat = new int[nouvelleTaille][nouvelleTaille];

        // Copier les sous-matrices dans la matrice résultante
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                resultat[i][j] = c11[i][j];
                resultat[i][j + taille] = c12[i][j];
                resultat[i + taille][j] = c21[i][j];
                resultat[i + taille][j + taille] = c22[i][j];
            }
        }
        return resultat;
    }

    public static void main(String[] args) {
        // Exemple d'utilisation
        ForkJoinPool pool = new ForkJoinPool();
        int[][] matriceA = {{1, 2}, {3, 4}};
        int[][] matriceB = {{5, 6}, {7, 8}};
        int[][] resultat = pool.invoke(new TacheStrassen(matriceA, matriceB, 0, 0, 0, 0, 2));

        // Affichage du résultat
        for (int i = 0; i < resultat.length; i++) {
            for (int j = 0; j < resultat[0].length; j++) {
                System.out.print(resultat[i][j] + " ");
            }
            System.out.println();
        }
    }
}
