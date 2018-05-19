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

import concours.ClientDijkstra.Box;
import concours.ClientDijkstra.Edge;
import concours.ClientDijkstra.Graph;


public class ClientDijkstra {
	//	public static boolean arret=false;
	public static int PORT=1337; //port par default (indiqué dans l'énoncé)

	public static String[][] lab;
	public static int largeur, hauteur;

	public static Box position;
	
	public static Vertex start;
	public static ArrayList<Vertex> targets=new ArrayList<Vertex>();

	public static int frites=0;
	public static int bieres=0;

	public static ArrayList<Edge> listAretes;
	public static ArrayList<Box> listCases;

	public static ArrayList<Box> chemin=new ArrayList<Box>();

	public static void main(String[] args) throws Exception {
		Socket s = new Socket(args[0], PORT);
		System.out.println("STARTClient");
		BufferedReader ins = new BufferedReader(
				new InputStreamReader(s.getInputStream()) );
		PrintWriter outs = new PrintWriter( new BufferedWriter(
				new OutputStreamWriter(s.getOutputStream())), true);
		outs.println("L'equipe du sale"); //envoi du nom de l'equipe
		int num=Integer.parseInt(ins.readLine())+1; //lecture du code de retour qui indique le numéro du joueur
		//Tout ce qui est commenté correspond à des améliorations possibles du client mais pas
		// utilisé ici (traitement parallèle)
		//		Ecrire w=new ClientDijkstra.Ecrire(outs,arret);
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
			int x=Integer.parseInt(infos[2].split("-")[num].split(",")[0]);
			int y=Integer.parseInt(infos[2].split("-")[num].split(",")[1]);
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
					}catch(NumberFormatException e){
						//ce n'est pas un objectif
					}
					//fin version objectif le plus proche
				}
				lab[j]=ligne;
			}
			
			position=new Box(x, y);

			/* V1: Version qui trouve le chemin le plus proche */
			//TODO: calculer tous les chemins dans des threads différents si trop long
			jouerTourProche(); //pour chaque objectif on dessine le chemin jusqu'à lui, la méthode assignera le bon chemin dans l'ArrayList chemin
			/* Fin version qui trouve le chemin le plus proche */
			
			/* V2: Version qui rapporte le plus de points */
			//jouerTour();
			/* Fin version qui rapporte le plus de points */
			
			int k=0; //box index
			Box target=chemin.get(chemin.size()-1);
			while(target.getX()!=x || target.getY()!=y){ //while we're not at the target
				
				try{ //we test if the target is still a number (still there)
					Integer.parseInt(lab[target.getY()][target.getX()]);
					
					/* Début de l'envoi du move à faire */
					//on pourrait mettre tous ces if dans une fonction pour aérer	
					boolean dejaJoue=false;
					//ici on test si il y a une case du chemin qui se trouve à la portée d'une biere
					//si c'est le cas et qu'il reste au moins 3 cases jusqu'au prochain objectif
					if(bieres>0 //on a des bières
							&& chemin.size()-k>=3 //le chemin est assez long
							&& !dejaJoue //on n'a pas déjà joué
							&& !(lab[chemin.get(k+1).getY()][chemin.get(k+1).getX()].equals("F")||lab[chemin.get(k+1).getY()][chemin.get(k+1).getX()].equals("B")) //on ne va pas sauter un bonus
							&& !(lab[chemin.get(k+2).getY()][chemin.get(k+2).getX()].equals("F")||lab[chemin.get(k+2).getY()][chemin.get(k+2).getX()].equals("B")) //on ne va pas sauter un bonus
					){
						String moves="B";
						int X=x;
						int Y=y;
						for(int i=k;i<k+3;i++){
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
						k+=2;
						System.out.println("Utilisation biere:"+bieres);
					}
					//ici on test si il y a une case du chemin qui se trouve à la portée d'une frite
					//on parcours toutes les cases du chemin, si il y a des cases qui sont à portée d'une frites alors on joue la dernière trouvée
					if(frites>0 && !dejaJoue){
						int index=0;
						String direction="";
						for(Box element:chemin){
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
						if(index>k){ //if the box is between the player and the target
							outs.println("F"+direction);
							frites-=1;
							dejaJoue=true;
							k=index;
							System.out.println("Utilisation frite:"+frites);
						}
					}
					//ici on fait un move normal
					if(!dejaJoue){
						if(chemin.get(k).getX()==x+1){ //on peut tester qu'une coordonnée parce que la prochaine case est forcément collée à nous
							outs.println("E");
						}else if(chemin.get(k).getX()==x-1){
							outs.println("O");
						}else{
							if(chemin.get(k).getY()==y+1){
								outs.println("S");
							}else if(chemin.get(k).getY()==y-1){
								outs.println("N");
							}else{
								System.err.println("Bug");
							}
						}
					}
					/* Fin de l'envoi du move à faire */
		
					//on regarde si on gagne un bonus à ce tour
					if(lab[chemin.get(k).getY()][chemin.get(k).getX()].equals("F")){
						frites+=1;
		
						System.out.println("frites:"+frites);
					}
					if(lab[chemin.get(k).getY()][chemin.get(k).getX()].equals("B"))
					{
						bieres+=1;
		
						System.out.println("bieres:"+bieres);
					}

					x=chemin.get(k).getX();
					y=chemin.get(k).getY();
					k+=1; //incremment the box index
				}catch(NumberFormatException e){
					System.out.println("L'objectif a disparu, on change de chemin");
					break; //if the target have been taken by somebody else, we calculate the next path
				}finally{
					//on lit la prochaine situation du jeu renvoyé par le serveur
					data=ins.readLine();
				}
			}
			//on réinitialise les listes
			chemin=new ArrayList<Box>();
			listCases=new ArrayList<Box>();
			listAretes=new ArrayList<Edge>();
			targets=new ArrayList<Vertex>();
			Box.uniqueIndex=0;
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

	/** build every edges of the graph **/
	public static void construireGraphe(){
		listAretes=new ArrayList<Edge>();
		listCases=new ArrayList<Box>();
		for(int j=1; j<ClientDijkstra.hauteur-1; j++){
			for(int i=1; i<ClientDijkstra.largeur-1; i++){
				Box box=new Box(i, j);
				if(!ClientDijkstra.lab[j][i].equals("D")){
					boolean haut=false, bas=false, gauche=false, droite=false;
					int lastIndexHor=listCases.size()-1;
					int lastIndexVert=listCases.size()-(ClientDijkstra.largeur-2);
					if(!ClientDijkstra.lab[j][i-1].equals("D")){ //case gauche pas une dune
						gauche=true;
					}
					if(!ClientDijkstra.lab[j][i+1].equals("D")){ //case droite pas une dune
						droite=true;
					}
					if(!ClientDijkstra.lab[j-1][i].equals("D")){ //case haut pas une dune
						haut=true;
					}
					if(!ClientDijkstra.lab[j+1][i].equals("D")){ //case bas pas une dune
						bas=true;
					}
					try{ //if it doesn't throw an exception then it's a number so we treats it like a vertex
						box.setScore(Integer.parseInt(ClientDijkstra.lab[j][i]));
						box.setIndex();
						if(haut){ //end of vertical edge
							listCases.get(lastIndexVert).getEdgeVerticalStart().addBox(true, false, box); //verticale alors on prend l'edge de la derniere case-le nombre de cases sur une ligne pour avoir celle juste au dessus
						}
						if(bas){ //start a vertical edge
							Edge areteVert=new Edge();
							areteVert.addBox(true, true, box);
							listAretes.add(areteVert);
						}
						if(gauche){ //end of horizontal edge
							listCases.get(lastIndexHor).getEdgeHorizontalStart().addBox(false, false, box); //horizontale alors on prend l'edge de la derniere case
						}
						if(droite){ //starts an horizontal edge
							Edge areteHor=new Edge();
							areteHor.addBox(false, true, box);
							listAretes.add(areteHor);
						}
					}catch(NumberFormatException e){ //it's not a number
						//it's the player so we treat it as a vertex
						if(i==ClientDijkstra.position.getX() && j==ClientDijkstra.position.getY()){
							box.setIndex();
							if(haut){ //end of vertical edge
								listCases.get(lastIndexVert).getEdgeVerticalStart().addBox(true, false, box); //verticale alors on prend l'edge de la derniere case-le nombre de cases sur une ligne pour avoir celle juste au dessus
							}
							if(bas){ //start a vertical edge
								Edge areteVert=new Edge();
								areteVert.addBox(true, true, box);
								listAretes.add(areteVert);
							}
							if(gauche){ //end of horizontal edge
								listCases.get(lastIndexHor).getEdgeHorizontalStart().addBox(false, false, box); //horizontale alors on prend l'edge de la derniere case
							}
							if(droite){ //starts an horizontal edge
								Edge areteHor=new Edge();
								areteHor.addBox(false, true, box);
								listAretes.add(areteHor);
							}
						}else{
							if(gauche){ //ligne horizontale ou fin edge horizontale
								//on ajoute à l'edge horizontale
								listCases.get(lastIndexHor).getEdgeHorizontalStart().addBox(false, false, box); //horizontale alors on prend l'edge de la derniere case
								if(haut){ //fin edge verticale (Noeud)
									//on ajoute à edge verticale
									listCases.get(lastIndexVert).getEdgeVerticalStart().addBox(true, false, box); //verticale alors on prend l'edge de la derniere case-le nombre de cases sur une ligne pour avoir celle juste au dessus
									box.setIndex();
								}
								if(bas){ //début edge verticale (Noeud)
									//on créer une edge verticale
									Edge areteVert=new Edge();
									areteVert.addBox(true, true, box);
									box.setIndex();
									listAretes.add(areteVert);
								}
								if((haut || bas) && droite){ //croisement ou debut/fin edge verticale (Noeud)
									//on créer une edge horizontale
									Edge areteHor=new Edge();
									areteHor.addBox(false, true, box);
									box.setIndex();
									listAretes.add(areteHor);
								}else if(droite){ //c'est une ligne horizontale
									box.setEdgeHorizontalStart(listCases.get(lastIndexHor).getEdgeHorizontalStart()); //horizontale alors on prend l'edge de la derniere case
								}else{//si !droite alors fin edge horizontale (Noeud)
									box.setIndex();
								}
							}else{
								if(haut){ //fin edge verticale (Noeud)
									//on ajoute à edge verticale
									listCases.get(lastIndexVert).getEdgeVerticalStart().addBox(true, false, box); //verticale alors on prend l'edge de la derniere case-le nombre de cases sur une ligne pour avoir celle juste au dessus
									if(!bas){ //c'est pas une ligne verticale
										box.setIndex();
									}
								}
								if(droite){ //debut edge horizontale (Noeud)
									//on créer une edge horizontale
									Edge areteHor=new Edge();
									areteHor.addBox(false, true, box);
									box.setIndex();
									listAretes.add(areteHor);
									if(bas){ //debut edge verticale (Noeud)
										//on créer une edge verticale
										Edge areteVert=new Edge();
										areteVert.addBox(true, true, box);
										box.setIndex();
										listAretes.add(areteVert);
									}
								}else{
									if(haut && bas){ //ligne verticale
										box.setEdgeVerticalStart(listCases.get(lastIndexVert).getEdgeVerticalStart()); //verticale alors on prend l'edge de la derniere case-le nombre de cases sur une ligne pour avoir celle juste au dessus
									}else if(bas){ //debut edge verticale (Noeud)
										//on créer une edge verticale
										Edge areteVert=new Edge();
										areteVert.addBox(true, true, box);
										box.setIndex();
										listAretes.add(areteVert);
									}
								}
							}
						}
					}
				}
				listCases.add(box); //on ajoute la case à la liste des cases
			}
		}
	}

	/** V1: Dijkstra algorithm that play a round by taking the closest target **/
	public static void jouerTourProche(){
		construireGraphe(); //build the boxes and the edges with the game grid
		
		Edge[] edges=listAretes.toArray(new Edge[1]); //list to array
	    Graph g = new Graph(edges); //build the graph

		g.calculateShortestDistances(); //calculate the shortest distances and set all paths from the player position to every other vertices
		g.printResult(); //get the best path to the closest target
	}

	/** V2: algorithme a* qui joue un tour en prenant le chemin vers l'objectif qui rapportera le plus de points**/
	public static void jouerTour(){
		//TODO: tester le compromis entre proximité et points rapportés

	}

	/*** class that represents a graph ***/
	public static class Graph {

		private ArrayList<Vertex> nodes;
		private int noOfNodes;
		private Edge[] edges;
		private int noOfEdges;

		/** build the graph by creating all of his vertices based on his edges **/
		public Graph(Edge[] edges) {
			this.edges = edges;

			// create all nodes
			this.noOfNodes = calculateNoOfNodes(edges);
			this.nodes = new ArrayList<Vertex>();

			for (int n = 0; n < this.noOfNodes; n++) {
				this.nodes.add(new Vertex());
			}

			this.noOfEdges = edges.length;

			Box firstBox;
			Box lastBox;
			int firstIndex;
			int lastIndex;
			// add all the edges to the vertices, each edge added to two vertices (origin and destination)
			for (int edgeToAdd = 0; edgeToAdd < this.noOfEdges; edgeToAdd++) {
				firstBox=edges[edgeToAdd].getListBox().get(0);
				lastBox=edges[edgeToAdd].getListBox().get(edges[edgeToAdd].getListBox().size()-1);
				firstIndex=firstBox.getIndex();
				lastIndex=lastBox.getIndex();
				this.nodes.get(firstIndex).getEdges().add(edges[edgeToAdd]);
				this.nodes.get(lastIndex).getEdges().add(edges[edgeToAdd]);
				if(firstBox.getX()==position.getX() && firstBox.getY()==position.getY()){
					start=this.nodes.get(firstIndex); //we set the start vertex
				}
				if(lastBox.getX()==position.getX() && lastBox.getY()==position.getY()){
					start=this.nodes.get(lastIndex); //we set the start vertex
				}
				if(firstBox.getScore()>0 && !targets.contains(this.nodes.get(firstIndex))){ //if the vertex is a target and the list doesn't already contain it
					targets.add(this.nodes.get(firstIndex)); //we add it
				}
				if(lastBox.getScore()>0 && !targets.contains(this.nodes.get(lastIndex))){ //if the vertex is a target and the list doesn't already contain it
					targets.add(this.nodes.get(lastIndex)); //we add it
				}
			}
		}

		/** get the number of vertices by taking the highest index of all vertices (box that is one of the ends of an edge) **/
		private int calculateNoOfNodes(Edge[] edges) {
			int noOfNodes = 0;

			for (Edge e : edges) {
				int premierIndex=e.getListBox().get(0).getIndex();
				int dernierIndex=e.getListBox().get(e.getListBox().size()-1).getIndex();
				if (premierIndex > noOfNodes)
					noOfNodes = premierIndex;
				if (dernierIndex > noOfNodes)
					noOfNodes = dernierIndex;
			}

			noOfNodes++;

			return noOfNodes;
		}
		
		/** calculates the shortest distance by taking the player position as source **/
		public void calculateShortestDistances() {
			// player position as source
			start.setDistanceFromSource(0);
			int nextNode = this.nodes.indexOf(start); //set the index of the first vertex to be evaluate

			// visit every vertex
			for (int i = 0; i < this.nodes.size(); i++) {
				// loop around the edges of current vertex
				ArrayList<Edge> currentNodeEdges = this.nodes.get(nextNode).getEdges();

				for (int joinedEdge = 0; joinedEdge < currentNodeEdges.size(); joinedEdge++) {
					//we get neighbors index
					int neighbourIndex = currentNodeEdges.get(joinedEdge).getNeighbourIndex(nextNode);

					// only if not visited
					if (!this.nodes.get(neighbourIndex).isVisited()) {
						int tentative = this.nodes.get(nextNode).getDistanceFromSource() + currentNodeEdges.get(joinedEdge).getListBox().size();
						//we set the distance if it's the shortest and we redefine the previous vertex
						if (tentative < nodes.get(neighbourIndex).getDistanceFromSource()) {
							nodes.get(neighbourIndex).setDistanceFromSource(tentative);
							nodes.get(neighbourIndex).setPrevious(currentNodeEdges.get(joinedEdge));
						}
					}
				}

				// all neighbors checked so the vertex is visited
				nodes.get(nextNode).setVisited(true);

				// next vertex must be with shortest distance
				nextNode = getNodeShortestDistanced();
			}
		}

		/** get the vertex with shortest distance which hasn't been yet visited **/
		private int getNodeShortestDistanced() {
			int storedNodeIndex = 0;
			int storedDist = Integer.MAX_VALUE;

			//loop through every vertices
			for (int i = 0; i < this.nodes.size(); i++) {
				int currentDist = this.nodes.get(i).getDistanceFromSource();
				//if the vertex is not visited and the distance is lower, we eventually get this vertex
				if (!this.nodes.get(i).isVisited() && currentDist < storedDist) {
					storedDist = currentDist;
					storedNodeIndex = i;
				}
			}

			return storedNodeIndex;
		}

		/** set the final path (best target) and display the shortest distance from origin to each target **/
		public void printResult() {
//			String output = "Number of nodes = " + this.noOfNodes;
//			output += "\nNumber of edges = " + this.noOfEdges;

			ArrayList<Box> tempPath=new ArrayList<Box>();
			//TODO: V2: stocker un chemin temporaire et comparer le score de la target pour avoir le chemin final
			for (Vertex target:targets) {
				retrouver_chemin(tempPath, target);
//				output += "\nThe shortest distance is " + target.getDistanceFromSource()+"; Chemin:\n";
//				for(Box b:tempPath){
//					output += b.getX()+":"+b.getY()+"|";
//				}
//				output += "\n";
				
				if(chemin.isEmpty() || tempPath.size() < chemin.size()){ //if the final path is not set or if it is set and its size is greater than the temporary Path, then the temporary is better
					chemin=tempPath;
				}
				tempPath=new ArrayList<Box>();
			}
			
			chemin.remove(0);
//			System.out.println(output);
		}
		
		/** build the path to the vertex passed in parameter from edge to edge **/
		public void retrouver_chemin(ArrayList<Box> tempPath, Vertex goal){
			/* the vertex is the end */
			Vertex tmp=goal; //temporary vertex
			Edge prec=tmp.getPrevious(); //the edge of the vertex that leads to the previous vertex in the path

			boolean reverse=false;
			int lastIndex;
			int firstIndex;
		
			while (tmp!=start){
				
				if(prec.getNeighbourIndex(nodes.indexOf(tmp))==prec.getListBox().get(0).getIndex()){ //boxes are listed in a fixed order in edges, in fact we have to know if the boxes will be picked in this order or the reverse to build the path
					reverse=true;
				}else{
					reverse=false;
				}
				lastIndex=prec.getListBox().size()-1; //by default, the last index is the last of the list
				firstIndex=0; //by default, the first index is the first of the list
				if(reverse){
					if(tempPath.size()>0 && tempPath.get(0)==prec.getListBox().get(lastIndex)){
						lastIndex-=1; //if the last box added was the same as the next to be added, we don't put it in the path by incrementing the index (remove doubles)
					}
					for(int i=lastIndex; i>=0; i--){ //reverse loop
						tempPath.add(0, prec.getListBox().get(i)); //we push at the beginning of the list to get the path in the correct order
					}
				}else{
					if(tempPath.size()>0 && tempPath.get(0)==prec.getListBox().get(0)){
						firstIndex+=1; //if the last box added was the same as the next to be added, we don't put it in the path by incrementing the index (remove doubles)
					}
					for(int i=firstIndex; i<=lastIndex; i++){ //normal loop
						tempPath.add(0, prec.getListBox().get(i)); //we push at the beginning of the list to get the path in the correct order
					}
				}
				
				tmp=nodes.get(prec.getNeighbourIndex(nodes.indexOf(tmp))); //we get the previous vertex by taking the neighbour of the current one linked by this edge
				prec=tmp.getPrevious();
			}
		}

		public ArrayList<Vertex> getNodes() {
			return nodes;
		}
		public int getNoOfNodes() {
			return noOfNodes;
		}
		public Edge[] getEdges() {
			return edges;
		}
		public int getNoOfEdges() {
			return noOfEdges;
		}
	}

	/*** class that represents an edge ***/
	public static class Edge {

		private ArrayList<Box> listBox; //list of boxes, the size is the length of the edge

		public Edge(){
			listBox=new ArrayList<Box>();
		}
		public Edge(ArrayList<Box> listBox) {
			this.listBox=listBox;
		}
		public ArrayList<Box> getListBox() {
			return listBox;
		}
		public void setListBox(ArrayList<Box> listBox) {
			this.listBox = listBox;
		}

		/** add a box to the box list of this edge **/
		public void addBox(boolean vertical, boolean start, Box box){
			listBox.add(box);
			//set this edge as the edge that starts/stops/contains the box based on the direction
			//an edge that starts and stops at the same box means that the edge just contains the box (box!=vertex)
			if(vertical){
				if(start){
					box.setEdgeVerticalStart(this);
				}else{
					box.setEdgeVerticalEnd(this);
				}
			}else{
				if(start){
					box.setEdgeHorizontalStart(this);
				}else{
					box.setEdgeHorizontalEnd(this);
				}
			}
		}

		/** determines the neighbouring node of a supplied node, based on the two nodes connected by this edge **/
		public int getNeighbourIndex(int nodeIndex) {
			if (this.listBox.get(0).getIndex() == nodeIndex) {
				return this.listBox.get(listBox.size()-1).getIndex();
			} else {
				return this.listBox.get(0).getIndex();
			}
		}
	}

	/*** class that represents a box in the game grid ***/
	public static class Box{

		private static int uniqueIndex=0;
		private int index=-1; //if the index>0 then the box is a vertex
		private int x;
		private int y;
		private int score=0;
		private Edge edgeVerticalStart;
		private Edge edgeVerticalEnd;
		private Edge edgeHorizontalStart;
		private Edge edgeHorizontalEnd;

		public Box(int x, int y) {
			super();
			this.x = x;
			this.y = y;
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
		public Edge getEdgeVerticalStart() {
			return edgeVerticalStart;
		}
		public void setEdgeVerticalStart(Edge edgeVerticalStart) {
			this.edgeVerticalStart = edgeVerticalStart;
		}
		public Edge getEdgeVerticalEnd() {
			return edgeVerticalEnd;
		}
		public void setEdgeVerticalEnd(Edge edgeVerticalEnd) {
			this.edgeVerticalEnd = edgeVerticalEnd;
		}
		public Edge getEdgeHorizontalStart() {
			return edgeHorizontalStart;
		}
		public void setEdgeHorizontalStart(Edge edgeHorizontalStart) {
			this.edgeHorizontalStart = edgeHorizontalStart;
		}
		public Edge getEdgeHorizontalEnd() {
			return edgeHorizontalEnd;
		}
		public void setEdgeHorizontalEnd(Edge edgeHorizontalEnd) {
			this.edgeHorizontalEnd = edgeHorizontalEnd;
		}
		public int getIndex() {
			return index;
		}

		/** set the vertex's index if not already done **/
		public void setIndex() {
			if(index<0){ //check if the index hasn't been yet initialized
				index=uniqueIndex;
				uniqueIndex+=1;
			}
		}
		public int getScore() {
			return score;
		}
		public void setScore(int score) {
			this.score = score;
		}
	}

	/*** class that represents a vertex ***/
	public static class Vertex {

		private int distanceFromSource = Integer.MAX_VALUE; //distance from the origin
		private boolean visited; //already visited?
		private ArrayList<Edge> edges = new ArrayList<Edge>(); // list of edges that start/stop on this vertex
		private Edge previous;
		
		public int getDistanceFromSource() {
			return distanceFromSource;
		}
		public void setDistanceFromSource(int distanceFromSource) {
			this.distanceFromSource = distanceFromSource;
		}
		public boolean isVisited() {
			return visited;
		}
		public void setVisited(boolean visited) {
			this.visited = visited;
		}
		public ArrayList<Edge> getEdges() {
			return edges;
		}
		public void setEdges(ArrayList<Edge> edges) {
			this.edges = edges;
		}
		public Edge getPrevious() {
			return previous;
		}
		public void setPrevious(Edge previous) {
			this.previous = previous;
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
	//			ClientDijkstra.arret=true;
	//		}
	//	}
}
