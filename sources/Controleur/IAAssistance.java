/*
 * Sokoban - Encore une nouvelle version (à but pédagogique) du célèbre jeu
 * Copyright (C) 2018 Guillaume Huard
 *
 * Ce programme est libre, vous pouvez le redistribuer et/ou le
 * modifier selon les termes de la Licence Publique Générale GNU publiée par la
 * Free Software Foundation (version 2 ou bien toute autre version ultérieure
 * choisie par vous).
 *
 * Ce programme est distribué car potentiellement utile, mais SANS
 * AUCUNE GARANTIE, ni explicite ni implicite, y compris les garanties de
 * commercialisation ou d'adaptation dans un but spécifique. Reportez-vous à la
 * Licence Publique Générale GNU pour plus de détails.
 *
 * Vous devez avoir reçu une copie de la Licence Publique Générale
 * GNU en même temps que ce programme ; si ce n'est pas le cas, écrivez à la Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307,
 * États-Unis.
 *
 * Contact:
 *          Guillaume.Huard@imag.fr
 *          Laboratoire LIG
 *          700 avenue centrale
 *          Domaine universitaire
 *          38401 Saint Martin d'Hères
 */
package Controleur;

import Global.Configuration;
import Modele.Coup;
import Structures.Sequence;

import java.util.Random;
import java.util.logging.Logger;

import static java.lang.Math.abs; //Pour valeur absolue (distance de Manhattan)

abstract class IAAssistance extends IA {
	Random r;
	Logger logger;
	int[][][][] array;
	int maL, maC;

	public IAAssistance() {
		r = new Random();
	}

	@Override
	public void initialise() {
		logger = Configuration.instance().logger();
		logger.info("Essai de la résolution automatique");
	}

	// Calcule la distance de Manhattan
	@Override
	public int heuristique(int depL, int depC, int arrL, int arrC) {
		return(abs(depL-arrL)+abs(depC-arrC));
	}

	@Override
	public void min_Du_Hu(int[][] arr, int origineL, int origineC, boolean[][] app){
		int minL=100000;
		int minC=100000;
		int min=100000;
		int l=arr.length;
		int c=arr[0].length;
		for(int i=0; i<l; i++){
			for(int j=0; j<c; j++){
				if(!niveau.aMur(origineL, origineC) && !niveau.aMur(i, j) && !app[i][j]){
					if((arr[i][j]+heuristique(origineL, origineC, i, j))<min){
						minL=i;
						minC=j;
					}
				}
			}
		}
		maL=minL;
		maC=minC;
	}

	// bool correspond au Q dans le TD
	@Override
	public boolean vide(boolean[][] bool){
		int l=bool.length;
		int c=bool[0].length;
		for(int i=0; i<l; i++) {
			for (int j = 0; j < c; j++) {
				if(bool[i][j]){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void Astar(int[][] arr, int origineL, int origineC){
		int l=arr.length;
		int c=arr[0].length;
		boolean init=false;
		int i, j;
		boolean[][] bool=new boolean[l][c]; // si true alors la case est dans l'ensemble à visiter (utile pour min_Du_Hu)
		for(i=0; i<l; i++) {
			for (j = 0; j < c; j++) {
				bool[i][j]=false;
			}
		}
		while(!vide(bool) || !init){
			for(i=0; i<l; i++) {
				for (j = 0; j < c; j++) {
					if(!init) {
						if (!niveau.aMur(i, j)) {
							arr[i][j] = 0;
							bool[i][j] = true; // Ajout
							init = true;
						}
					}else{
						if (!niveau.aMur(i, j)){
							min_Du_Hu(arr, i, j, bool);
							if(maL==origineL && maC==origineC){
								// return Dd dans le td d'algo, quoi mettre ici ???
								return;
							}
							bool[i][j]=false; // Retrait
							// Tous les voisins de [i][j] (priorité H/G/D/B)
							int valeur=arr[i][j];
							if(!niveau.aMur(i+1, j)){
								if(valeur<arr[i+1][j]){
									arr[i+1][j]=valeur;
									bool[i+1][j]=true;
								}
							}
							if(!niveau.aMur(i, j-1)){
								if(valeur<arr[i][j-1]){
									arr[i][j-1]=valeur;
									bool[i][j-1]=true;
								}
							}
							if(!niveau.aMur(i, j+1)){
								if(valeur<arr[i][j+1]){
									arr[i][j+1]=valeur;
									bool[i][j+1]=true;
								}
							}
							if(!niveau.aMur(i-1, j)){
								if(valeur<arr[i-1][j]){
									arr[i-1][j]=valeur;
									bool[i-1][j]=true;
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public Sequence<Coup> joue() {
		Sequence<Coup> resultat = Configuration.instance().nouvelleSequence();
		int pousseurL = niveau.lignePousseur();
		int pousseurC = niveau.colonnePousseur();

		logger.info("Constrution d'une solution avec A*");
		int l=niveau.lignes();
		int c=niveau.colonnes();
		array = new int[l][c][l][c];


		for(int i=0; i<l; i++){
			for(int j=0; j<c; j++){
				for(int k=0; k<l; k++){
					for(int m=0; m<c; m++){
						if(i==k && j==m){
							array[i][j][k][m]=0;
						}
						array[i][j][k][m]=100000;
					}
				}
				if(!niveau.aMur(i, j)){
					Astar(array[i][j], i, j);
				}
			}
		}

		for(int i=0; i<l; i++){
			for(int j=0; j<c; j++){
				for(int k=0; k<l; k++){
					for(int m=0; m<c; m++){
						System.out.println("Valeur de array["+i+"]"+"["+j+"]"+"["+k+"]"+"["+m+"] = "+array[i][j][k][m]);
					}
				}
			}
		}


//
//		while(!niveau.niveauTermine()){
//			// Mouvement du pousseur
//			Coup coup = new Coup();
//			boolean libre = false;
			/*while (!libre) {



				if (niveau.estOccupable(nouveauL, nouveauC)) {
					logger.info("Téléportation en (" + nouveauL + ", " + nouveauC + ") !");
					coup.deplace(pousseurL, pousseurC, nouveauL, nouveauC);
					resultat.insereQueue(coup);
					pousseurL = nouveauL;
					pousseurC = nouveauC;
					libre = true;
				}
			}*/
//		}
		return resultat;
	}

	@Override
	public void finalise() {
		logger.info("Fin de la solution avec A*");
	}
}
