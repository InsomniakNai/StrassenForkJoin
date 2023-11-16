package program;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;


public class Strassen {

    // Classe représentant une tâche Strassen
    static class TacheStrassen extends RecursiveTask<int[][]> {

        /**
         * Première Matrice a traiter
         */
        private final int[][] A;
        /**
         * Deuxième Matrice a traiter
         */
        private final int[][] B;
        /**
         * Ligne de début de la matrice A
         */
        private final int debutLigneA;
        /**
         * Colonne de début de la matrice A
         */
        private final int debutColonneA;
        /**
         * Ligne de début de la matrice B
         */
        private final int debutLigneB;
        /**
         * Colonne de début de la matrice B
         */
        private final int debutColonneB;
        /**
         * Représente la taille des matrices
         */
        private final int taille;
        /**
         * Représente la taille que doit avoir les matrices avant de pouvoir être traitées
         */
        private final int tailleDecoupageMatrice;

        /**
         * Constructeur de la classe TacheStrassen
         * @param A Première matrice
         * @param B Deuxième Matrice
         * @param debutLigneA Ligne de début de la matrice A
         * @param debutColonneA Colonne de début de la matrice A
         * @param debutLigneB Ligne de début de la matrice B
         * @param debutColonneB Colonne de début de la matrice B
         * @param taille Représente la taille des matrices
         * @param tailleDecoupageMatrice Représente la taille que la matrice doit atteindre pour pouvoir être traitée
         */
        public TacheStrassen(int[][] A, int[][] B, int debutLigneA, int debutColonneA, int debutLigneB, int debutColonneB, int taille, int tailleDecoupageMatrice) {
            this.A = A;
            this.B = B;
            this.debutLigneA = debutLigneA;
            this.debutColonneA = debutColonneA;
            this.debutLigneB = debutLigneB;
            this.debutColonneB = debutColonneB;
            this.taille = taille;
            this.tailleDecoupageMatrice = tailleDecoupageMatrice;
        }

        @Override
        protected int[][] compute() {
            //System.out.println(Thread.currentThread().getName());
            if (taille <= tailleDecoupageMatrice) { // Taille limite pour laquelle nous effectuons la multiplication standard
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

            /*  Affichage des différentes matrices

            System.out.println("-------------------- Matrice a11 --------------------");
            printMatrice(a11);
            System.out.println("-------------------- Matrice a12 --------------------");
            printMatrice(a12);
            System.out.println("-------------------- Matrice a21 --------------------");
            printMatrice(a21);
            System.out.println("-------------------- Matrice a22 --------------------");
            printMatrice(a22);

            System.out.println("-------------------- Matrice b11 --------------------");
            printMatrice(b11);
            System.out.println("-------------------- Matrice b12 --------------------");
            printMatrice(b12);
            System.out.println("-------------------- Matrice b21 --------------------");
            printMatrice(b21);
            System.out.println("-------------------- Matrice b22 --------------------");
            printMatrice(b22);
            System.out.println("-----------------------------------------------------");
             */

            // Calculer les produits intermédiaires
            TacheStrassen p1 = new TacheStrassen(additionner(a11, a22), additionner(b11, b22), 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);
            TacheStrassen p2 = new TacheStrassen(additionner(a21, a22), b11, 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);
            TacheStrassen p3 = new TacheStrassen(a11, soustraire(b12, b22), 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);
            TacheStrassen p4 = new TacheStrassen(a22, soustraire(b21, b11), 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);
            TacheStrassen p5 = new TacheStrassen(additionner(a11, a12), b22, 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);
            TacheStrassen p6 = new TacheStrassen(soustraire(a21, a11), additionner(b11, b12), 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);
            TacheStrassen p7 = new TacheStrassen(soustraire(a12, a22), additionner(b21, b22), 0, 0, 0, 0, nouvelleTaille, tailleDecoupageMatrice);

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

    /**
     * Additionne deux matrices entre elles
     * @param A Première Matrice
     * @param B Seconde Matrice
     * @return Retourne une sous-matrice résultante de l'addition
     */
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

    /**
     * Soustrait deux matrices entre elles
     * @param A Première Matrice
     * @param B Seconde Matrice
     * @return Retourne une sous-matrice résultante de la soustraction
     */
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
    /**
     * Multiplie deux matrice entre-elle
     * @param A Première matrice
     * @param B Deuxième matrice
     * @param debutLigneA Ligne de début de la multiplication
     * @param debutColonneA Colonne de début de la  multiplication
     * @param debutLigneB Ligne de début de la multiplication
     * @param debutColonneB Colonne de début de la  multiplication
     * @param taille Taille de la matrice
     * @return Retourne une sous-matrice résultante de la multiplication
     */
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
    /**
     * Permet de créer une sous-matrice en fonction d'une matrice mère
     * @param matrice La matrice mère
     * @param debutLigne La ligne de début de la matrice mère
     * @param debutColonne La colone de début de la matrice mère
     * @param taille La taille de la matrice fille
     * @return Une matrice contenant la partie de la matrice mère correspondant au debutLigne et debutColonne
     */
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
    /**
     * Rassemble les 4 parties de la matrice mère après les différents calcules
     * @param c11 Matrice fille
     * @param c12 Matrice fille
     * @param c21 Matrice fille
     * @param c22 Matrice fille
     * @param taille Taille des matrices filles
     * @return Retourne la matrice mère
     */
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

    /**
     * Initialise une matrice de taille *tailleMatrice* avec un chiffre aléatoire entre 1 et 3
     * @param tailleMatrice La taille de la matrice
     * @return Retourne une matrice de taille : *tailleMatrice* x *tailleMatrice*
     */
    private static int[][] initMatrice(int tailleMatrice){
        int[][] matrice = new int[tailleMatrice][tailleMatrice];
        int nb = (int) (Math.random() * 3) +1;
        for(int i = 0; i < tailleMatrice; i++){
            for (int j = 0; j < tailleMatrice; j++){
                matrice[i][j] = nb;
                nb = (int) (Math.random() * 3) + 1;     //      Stocke un chiffre entre 1 et 3 dans nb
            }
        }
        System.out.println(" --------------- Matrice générée : ---------------------");
        printMatrice(matrice);
        return matrice;
    }


    /**
     * Affiche une matrice de int[][]
     * @param resultat Matrice à afficher
     */
    private static void printMatrice(int[][] resultat) {
        // Affichage du résultat
        for (int i = 0; i < resultat.length; i++) {
            for (int j = 0; j < resultat[0].length; j++) {
                System.out.print(resultat[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        System.out.println("Début du programme");
        // Exemple d'utilisation
        /*
        ForkJoinPool pool = new ForkJoinPool();
        int[][] matriceA = {{1, 2}, {3, 4}};
        int[][] matriceB = {{5, 6}, {7, 8}};
        int[][] resultat = pool.invoke(new TacheStrassen(matriceA, matriceB, 0, 0, 0, 0, 2));
        */

        //Initialisation de la taille de la matrice
        int tailleMatrice = 16;

        //Initialise la pool de Thread
        ForkJoinPool pool = new ForkJoinPool();
        int[][] resultat = pool.invoke(new TacheStrassen(initMatrice( tailleMatrice), initMatrice( tailleMatrice),
                0, 0, 0, 0, tailleMatrice, 2));

        System.out.println("------------- Résultat final -------------");
        printMatrice(resultat);
    }
}
