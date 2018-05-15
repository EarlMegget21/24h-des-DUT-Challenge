package concours;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import concours.ClientAstar.Noeud;


public class ClientAstar {
//	public static boolean arret=false;
	public static int PORT=1337; //port par default (indiqué dans l'énoncé)
	
	public static String[][] lab;
	public static int largeur, hauteur;
	
	//V1: Version objectif le plus proche:
	public static ArrayList<int[]> listeObjectifs=new ArrayList<int[]>(); //stock les x et y de tous les objectifs
	//Fin version objectif le plus proche
	
	public static Noeud position;
	public static Noeud objectif;
	
	public static int frites=0;
	public static int bieres=0;
	
	public static HashMap<String, Noeud> listeOuverte=new HashMap<String, Noeud>();
	public static HashMap<String, Noeud> listeFermee=new HashMap<String, Noeud>();
	public static ArrayList<Noeud> chemin=new ArrayList<Noeud>();
	
	public static void main(String[] args) throws Exception {
		Socket s = new Socket(args[0], PORT);
		System.out.println("STARTClient");
		BufferedReader ins = new BufferedReader(
				new InputStreamReader(s.getInputStream()) );
		PrintWriter outs = new PrintWriter( new BufferedWriter(
				new OutputStreamWriter(s.getOutputStream())), true);
		outs.println("L'equipe du sale"); //envoi du nom de l'equipe
		ins.readLine(); //lecture du code de retour (0) qui indique que la connexion est établie
		//Tout ce qui est commenté correspond à des améliorations possibles du client mais pas
		// utilisé ici (traitement parallèle)
//		Ecrire w=new ClientAstar.Ecrire(outs,arret);
//		Thread t=new Thread(w);
//		t.start();
		boolean premierTour=true;
		String data;
		String[] infos;
		String[] cases;
		String[] ligne;
		data=ins.readLine();
		while(!data.equals("FIN")){ //le serveur nous envoie ça à la fin du jeu
			/* Récupération des infos envoyées par le serveur */
			infos=data.split("/");
			//dimensions
			largeur=Integer.parseInt(infos[0].split("x")[0]);
			hauteur=Integer.parseInt(infos[0].split("x")[1]);
			//position du joueur
			int x=Integer.parseInt(infos[2].split("-")[1].split(",")[0]);
			int y=Integer.parseInt(infos[2].split("-")[1].split(",")[1]);
			//convertion du String en tableau de String à deux dimensions pour le plateau de jeu (on aurait pu faire à une seule dimension avec String.toArray())
			cases=infos[1].split("-");
			if(premierTour){ //pour n'allouer un nouveau tableau qu'au premier tour car les dimensions ne changent pas
				lab=new String[hauteur][];
				premierTour=false;
			}
			//on créer le tableau de cases
			for(int j=0; j<hauteur; j++){
				ligne=new String[largeur];
				for(int i=0; i<largeur; i++){
					ligne[i]=cases[i+(j*largeur)];
					//version objectif le plus proche
					try{
						Integer.parseInt(ligne[i]); //si ça ne lève pas d'exception alors c'est un objectif
						listeObjectifs.add(new int[]{i, j}); //on ajoute ses coordonnés à la liste des objectifs
					}catch(NumberFormatException e){
						//ce n'est pas un objectif
					}
					//fin version objectif le plus proche
				}
				lab[j]=ligne;
			}

			position=new Noeud();
			position.setX(x);
			position.setY(y);
			objectif=new Noeud();
			
	/* V1: Version qui trouve le chemin le plus proche */
			//TODO: calculer tous les chemins dans des threads différents si trop long
			Noeud temp;
			for(int[] obj:listeObjectifs){
				temp=new Noeud(null, 0, 0, 0, obj[0], obj[1]);
				jouerTourProche(temp); //pour chaque objectif on dessine le chemin jusqu'à lui, la méthode assignera le bon chemin dans l'ArrayList chemin
				
				//on réinitialise les listes pour le prochain calcul de chemin
			    listeOuverte=new HashMap<String, Noeud>();
				listeFermee=new HashMap<String, Noeud>();
			}
	/* Fin version qui trouve le chemin le plus proche */
			
	/* V2: Version qui rapporte le plus de points */
			//jouerTour();
	/* Fin version qui rapporte le plus de points */
			
			/* Début de l'envoi du move à faire */
			//on pourrait mettre tous ces if dans une fonction pour aérer	
			boolean dejaJoue=false;
			//ici on test si il y a une case du chemin qui se trouve à la portée d'une biere
			//si c'est le cas et qu'il reste au moins 3 cases jusqu'au prochain objectif
			if(bieres>0 && chemin.size()>=3 && !dejaJoue){
				String moves="B";
				int X=x;
				int Y=y;
				for(int i=0;i<3;i++){
					if(chemin.get(i).getX()==X+1){
						moves+="-E";
						X+=1; //on doit accumuler parce qu'il faut prendre en compte qu'après ce move on sera une case pls loin
					}else if(chemin.get(i).getX()==X-1){
						moves+="-O";
						X-=1;
					}else{
						if(chemin.get(i).getY()==Y+1){
							moves+="-S";
							Y+=1;
						}else if(chemin.get(i).getY()==Y-1){
							moves+="-N";
							Y-=1;
						}else{
							moves+="-C";
							System.err.println("Bug Biere");
						}
					}
				}
				outs.println(moves);
				bieres-=1;
				dejaJoue=true;
				System.out.println("Utilisation biere:"+bieres);
			}
			//ici on test si il y a une case du chemin qui se trouve à la portée d'une frite
			//on parcours toutes les cases du chemin, si il y a des cases qui sont à portée d'une frites alors on joue la dernière trouvée
			if(frites>0 && !dejaJoue){
				int index=0;
				String direction="";
				for(Noeud element:chemin){
					if(element.getX()==x+2 && element.getY()==y && lab[y][x+1].equals("D")){ //on est obligé de tester les deux parce qu'on parcours des cases lointaines, on test aussi si la case entre est ne dune pour pouvoir sauter les murs
						direction="-E";
						index=chemin.indexOf(element);
					}else if(element.getX()==x-2 && element.getY()==y && lab[y][x-1].equals("D")){
						direction="-O";
						index=chemin.indexOf(element);
					}else{
						if(element.getY()==y+2 && element.getX()==x && lab[y+1][x].equals("D")){
							direction="-S";
							index=chemin.indexOf(element);
						}else if(element.getY()==y-2 && element.getX()==x && lab[y-1][x].equals("D")){
							direction="-N";
							index=chemin.indexOf(element);
						}else{
							continue;
						}
					}
				}
				if(index>=1){
					outs.println("F"+direction);
					frites-=1;
					dejaJoue=true;
					System.out.println("Utilisation frite:"+frites);
				}
			}
			//ici on fait un move normal
			if(!dejaJoue){
				if(chemin.get(0).getX()==x+1){ //on peut tester qu'une coordonnée parce que la prochaine case est forcément collée à nous
					outs.println("E");
				}else if(chemin.get(0).getX()==x-1){
					outs.println("O");
				}else{
					if(chemin.get(0).getY()==y+1){
						outs.println("S");
					}else if(chemin.get(0).getY()==y-1){
						outs.println("N");
					}else{
						System.err.println("Bug");
					}
				}
			}
			/* Fin de l'envoi du move à faire */
			
			//on regarde si on gagne un bonus à ce tour
			if(lab[chemin.get(0).getY()][chemin.get(0).getX()].equals("F")){
				frites+=1;

				System.out.println("frites:"+frites);
			}
			if(lab[chemin.get(0).getY()][chemin.get(0).getX()].equals("B"))
			{
				bieres+=1;

				System.out.println("bieres:"+bieres);
			}
		    
			//on réinitialise les listes pour le prochain tour
		    listeOuverte=new HashMap<String, Noeud>();
			listeFermee=new HashMap<String, Noeud>();
			chemin=new ArrayList<Noeud>();
			
	/* V1: Version objectif le plus proche */
			listeObjectifs=new ArrayList<int[]>();
	/* Fin version objectif le plus proche */
			
			//on lit la prochaine situation du jeu renvoyé par le serveur
			data=ins.readLine();
		}
//		t.join();
		try {
			ins.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		arret=true;
		outs.close();
		s.close();
		System.out.println("Fin du jeu!");
	}
	
	/** V1: algorithme a* qui joue un tour en prenant la cible la plus proche**/
	public static void jouerTourProche(Noeud objectifTemporaire){
		//TODO: on peut mettre un compteur qui compare au fur et à mesure la construction du chemin si il dépasse pas un déjà existant, dans ce cas là on stopperait la recherche car ça ne servirait à rien (parcours partiel) 
	    /* initialisation de la case courante */
	    String courant=position.getX()+":"+position.getY();
	 
	    /* ajout de courant dans la liste ouverte */
	    listeOuverte.put(courant, position);
	    ajouter_liste_fermee(courant);
	    ajouter_cases_adjacentes(position);
	 
	    /* tant que la destination n'a pas été atteinte et qu'il reste des noeuds à explorer dans la liste ouverte */
	    while( !(courant.equals(objectifTemporaire.getX()+":"+objectifTemporaire.getY()))
	            &&
	           !listeOuverte.isEmpty()
	         ){
	    	
	    	/* on cherche le meilleur noeud de la liste ouverte, on sait qu'elle n'est pas vide donc il existe */
	        courant = meilleur_noeud(listeOuverte);
	 
	        /* on le passe dans la liste fermee, il ne peut pas déjà y être */
	        ajouter_liste_fermee(courant);
	        
	        /* on recommence la recherche des noeuds adjacents */
	        ajouter_cases_adjacentes(listeFermee.get(courant));
	    }

	    /* si la destination est atteinte, on remonte le chemin */
	    if (courant.equals(objectifTemporaire.getX()+":"+objectifTemporaire.getY())){
	        retrouver_chemin_proche(objectifTemporaire);
	        //Fin
	    }else{
	        System.err.println("Pas de solution");
	    }
	}
	
	/** V2: algorithme a* qui joue un tour en prenant le chemin vers l'objectif qui rapportera le plus de points**/
	public static void jouerTour(){
		//TODO: tester le compromis entre proximité et points rapportés
	    trouverCible(); //j'ai choisi celle qui rapporte le plus de points, même si elle est à l'opposé de la map
	 
	    /* initialisation de la case courante */
	    String courant=position.getX()+":"+position.getY();
	 
	    /* ajout de courant dans la liste ouverte */
	    listeOuverte.put(courant, position);
	    ajouter_liste_fermee(courant);
	    ajouter_cases_adjacentes(position);
	 
	    /* tant que la destination n'a pas été atteinte et qu'il reste des noeuds à explorer dans la liste ouverte */
	    while( !(courant.equals(objectif.getX()+":"+objectif.getY()))
	            &&
	           !listeOuverte.isEmpty()
	         ){
	    	
	    	/* on cherche le meilleur noeud de la liste ouverte, on sait qu'elle n'est pas vide donc il existe */
	        courant = meilleur_noeud(listeOuverte);
	 
	        /* on le passe dans la liste fermee, il ne peut pas déjà y être */
	        ajouter_liste_fermee(courant);
	        
	        /* on recommence la recherche des noeuds adjacents */
	        ajouter_cases_adjacentes(listeFermee.get(courant));
	    }

	    /* si la destination est atteinte, on remonte le chemin */
	    if (courant.equals(objectif.getX()+":"+objectif.getY())){
	        retrouver_chemin();
	        //Fin
	    }else{
	        System.err.println("Pas de solution");
	    }
	}
	
	/** modifie les coordonnées de l'objectif (ici on prend la cible qui rapporte le plus de points) **/
	public static void trouverCible(){
		int x=1;
		int y=1;
		int points=0;
		for(int j=1; j<hauteur-1; j++){ //on part de 1 et on enlève 1 car les bords sont des murs
			for(int i=1; i<largeur-1; i++){
				try {
					if(Integer.parseInt(lab[j][i])>points){
						x=i;
						y=j;
						points=Integer.parseInt(lab[j][i]);
					}
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		objectif.setX(x);
		objectif.setY(y);
	}
	
	/** parcours les cases adjacentes pour ajouter la bonne **/
	public static void ajouter_cases_adjacentes(Noeud current){
	    Noeud noeud;
	    /* on met tous les noeud adjacents dans la liste ouverte (+vérif) */
	    for (int i=current.getX()-1; i<=current.getX()+1; i++){
	        if ((i<0) || (i>=largeur))  /* en dehors de l'image, on oublie */
	            continue;
	        for (int j=current.getY()-1; j<=current.getY()+1; j++){
	            if ((j<0) || (j>=hauteur))   /* en dehors de l'image, on oublie */
	                continue;
	            if ((i==current.getX()) && (j==current.getY()))  /* case actuelle current, on oublie */
	                continue;
	            if( (i==current.getX()+1 && (j==current.getY()+1 || j==current.getY()-1))
	            		||
	               (i==current.getX()-1 && (j==current.getY()+1 || j==current.getY()-1)) )
	            	continue;
	 
	            if (lab[j][i].equals("D"))
	                /* obstace, terrain non franchissable, on oublie */
	                continue;
	 
	            String voisin=i+":"+j; //on a un voisin valide
	            
	            if (!deja_present_dans_liste(voisin, listeFermee)){
	                /* le noeud n'est pas déjà présent dans la liste fermée */
	 
	            	noeud=new Noeud();
	            	
	                /* calcul du cout G du noeud en cours d'étude : cout du parent + distance jusqu'au parent */
	            	noeud.setCoutG(listeFermee.get(current.getX()+":"+current.getY()).getCoutG() + distance(i,j,current.getX(),current.getY()));  
	 
	            	/* calcul du cout H du noeud à la destination */
	            	noeud.setCoutH(distance(i,j,objectif.getX(),objectif.getY()));
	            	
	            	noeud.setCoutF(noeud.getCoutG() + noeud.getCoutH());
	                noeud.setParent(current);
	                noeud.setX(i);
	                noeud.setY(j);
	 
	                if (deja_present_dans_liste(voisin, listeOuverte)){
	                    /* le noeud est déjà présent dans la liste ouverte, il faut comparer les couts */
	                    if (noeud.getCoutF() < listeOuverte.get(voisin).getCoutF()){
	                        /* si le nouveau chemin est meilleur, on met à jour */
	                    	listeOuverte.put(voisin, noeud);
	                    }
	 
	                    /* sinon le noeud courant a un moins bon chemin, on ne change rien */
	 
	                }else{
	                    /* le noeud n'est pas présent dans la liste ouverte, on l'y ajoute */
	                	listeOuverte.put(voisin, noeud);
	                }
	            }
	        }
	    }
	}
	
	/** V1: fonction qui remonte de parents en parents pour retranscrire le chemin inverse de l'objectif jusqu'à nous
	 * et assigne le meilleur chemin (plus proche) **/
	public static void retrouver_chemin_proche(Noeud objectifTemporaire){
		//création du chemin à tester
		ArrayList<Noeud> cheminTemporaire=new ArrayList<Noeud>();
		
	    /* l'arrivée est le dernier élément de la liste fermée */
	    Noeud tmp = listeFermee.get(objectifTemporaire.getX()+":"+objectifTemporaire.getY());
	    cheminTemporaire.add(0, tmp); //on empile au début pour avoir le chemin dans l'ordre
	    Noeud prec=tmp.getParent();
	 
	    while (!(prec.getX()+":"+prec.getY()).equals(position.getX()+":"+position.getY())){
	        tmp = listeFermee.get(prec.getX()+":"+prec.getY());
	        prec=tmp.getParent();
	        cheminTemporaire.add(0, tmp);
	    }
	    //on n'ajoute pas le départ car on y est déjà
	    
	    if(chemin.isEmpty() || cheminTemporaire.size()<chemin.size()){ //si le chemin est mieux ou si aucun chemin n'a encore été trouvé, on l'assigne
	    	chemin=cheminTemporaire;
	    }
	}
	
	/** V2: fonction qui remonte de parents en parents pour retranscrire le chemin inverse **/
	public static void retrouver_chemin(){
	    /* l'arrivée est le dernier élément de la liste fermée */
	    Noeud tmp = listeFermee.get(objectif.getX()+":"+objectif.getY());
	    chemin.add(0, tmp); //on empile au début pour avoir le chemin dans l'ordre
	    Noeud prec=tmp.getParent();
	 
	    while (!(prec.getX()+":"+prec.getY()).equals(position.getX()+":"+position.getY())){
	        tmp = listeFermee.get(prec.getX()+":"+prec.getY());
	        prec=tmp.getParent();
	        chemin.add(0, tmp);
	    }
	    //on n'ajoute pas le départ car on y est déjà
	}
	
	/** passe la case et son noeud de la liste ouverte à la liste fermée **/
	public static void ajouter_liste_fermee(String p){
	    Noeud n = listeOuverte.get(p);
	    listeFermee.put(p, n);
	 
	    /* il faut le supprimer de la liste ouverte, ce n'est plus une solution explorable */
	    listeOuverte.remove(p);
	}
	
	/** recupère le meilleur noeud de la liste ouverte **/
	public static String meilleur_noeud(HashMap<String, Noeud> liste){
		//TODO: passer à des collections ordonnés et triées : accélère la recherche (les HashMap sont des collections qui ne garantissent pas l'ordre
		HashMap.Entry<String, Noeud> entry=liste.entrySet().iterator().next(); //premier élément
	    double curCoutf = entry.getValue().getCoutF();
	    String cleNoeud = entry.getKey();
	 
	    for (HashMap.Entry<String, Noeud> entry2 : liste.entrySet()){ //on parcours pour récupérer le meilleur
	        if (entry2.getValue().getCoutF() < curCoutf){
	        	curCoutf = entry2.getValue().getCoutF();
	            cleNoeud = entry2.getKey();
	        }
	    }
	 
	    return cleNoeud;
	}
	
	/** calcule la distance entre les points (x1,y1) et (x2,y2) **/
	public static double distance(int x1, int y1, int x2, int y2){
	    /* distance euclidienne */
	    return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
	 
	    /* carré de la distance euclidienne pour avoir un nombre rond(passer à des int), les nombres ronds sont moins lourds à manipuler */
	    /* return (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2); */
	}
	
	/** test si la case fait déjà partie de la liste **/
	public static boolean deja_present_dans_liste(String c, HashMap<String, Noeud> l){
	    return l.containsKey(c);
	}
	
	/*** Classe Noeud ***/
	public static class Noeud{
		Noeud parent;
		int x, y;
		double coutG, coutH, coutF;
		public Noeud() {
			super();
			parent=null;
			coutG=0;
			coutH=0;
			coutF=0;
		}
		public Noeud(Noeud parent, double coutG, double coutH, double coutF, int x, int y) {
			super();
			this.parent = parent;
			this.coutG = coutG;
			this.coutH = coutH;
			this.coutF = coutF;
			this.x=x;
			this.y=y;
		}
		public int getX() {
			return x;
		}
		public void setX(int x) {
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
		public Noeud getParent() {
			return parent;
		}
		public void setParent(Noeud parent) {
			this.parent = parent;
		}
		public double getCoutG() {
			return coutG;
		}
		public void setCoutG(double coutG) {
			this.coutG = coutG;
		}
		public double getCoutH() {
			return coutH;
		}
		public void setCoutH(double coutH) {
			this.coutH = coutH;
		}
		public double getCoutF() {
			return coutF;
		}
		public void setCoutF(double coutF) {
			this.coutF = coutF;
		}
		
	}
	
	/*** Classe Ecrire (runnable pour écrire en asynchrone) ***/
//	public static class Ecrire implements Runnable{
//		private PrintWriter r;
//		private boolean arret;
//		
//		Ecrire(PrintWriter re, boolean a){
//			r=re;
//			arret=a;
//		}
//		
//		public void run(){
//			Scanner sc = new Scanner(System.in);
//			String mes=sc.nextLine();
//			while(!mes.equals("fin")){ // on peut remplacer par !mes==null car avec Ctrl+D on peut stopper l'entrÃ©e au clavier ce qui lance une exception, on a juste Ã  la catch et Ã  faire un break;
//				r.println(mes);
//				mes=sc.nextLine();
//			}
//			ClientAstar.arret=true;
//		}
//	}
}
