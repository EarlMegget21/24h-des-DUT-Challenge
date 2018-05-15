package test;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import concours.ClientDijkstra;
import concours.ClientDijkstra.Box;
import concours.ClientDijkstra.Edge;
import concours.ClientDijkstra.Graph;
import concours.ClientDijkstra.Vertex;

public class TestClientDijkstra {
	
	public static ArrayList<Edge> listAretes;
	public static ArrayList<Box> listCases;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ClientDijkstra.largeur=25;
		ClientDijkstra.hauteur=17;
		
		String chaine =  "D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-"
					   + "D-S-D-S-S-S-S-S-D-S-D-S-D-S-S-S-D-S-S-S-S-S-S-S-D-"
					   + "D-S-D-D-D-S-D-S-D-S-D-S-D-D-D-S-D-D-D-D-D-S-D-D-D-"
					   + "D-S-S-S-S-S-D-F-S-S-S-F-S-S-D-S-D-8-D-S-D-S-S-S-D-"
					   + "D-D-D-D-D-D-D-S-D-D-D-D-D-F-D-S-D-S-D-S-D-D-D-S-D-"
					   + "D-S-D-S-D-4-S-6-D-B-D-S-S-S-S-S-B-8-D-S-S-S-D-S-D-"
					   + "D-S-D-F-D-D-D-D-D-S-D-D-D-D-D-S-D-D-D-D-D-S-D-S-D-"
					   + "D-S-S-S-S-S-B-S-S-S-D-S-S-S-D-B-D-F-D-S-S-S-S-S-D-"
					   + "D-S-D-S-D-D-D-S-D-D-D-S-D-D-D-S-D-S-D-F-D-S-D-D-D-"
					   + "D-S-D-S-D-S-D-S-D-6-S-9-S-S-S-S-D-S-D-S-D-S-D-S-D-"
					   + "D-D-D-S-D-S-D-D-D-D-D-S-D-D-D-F-D-S-D-D-D-9-D-S-D-"
					   + "D-S-S-S-S-S-9-S-S-S-S-F-S-S-D-S-D-S-D-S-S-S-S-S-D-"
					   + "D-S-D-S-D-S-D-D-D-D-D-S-D-D-D-S-D-S-D-S-D-S-D-S-D-"
					   + "D-S-D-S-D-9-S-S-D-S-S-B-D-S-S-S-S-S-S-4-D-S-D-S-D-"
					   + "D-S-D-D-D-D-D-D-D-D-D-S-D-S-D-D-D-D-D-S-D-D-D-S-D-"
					   + "D-S-S-S-S-S-S-S-S-S-D-S-D-S-S-S-S-S-D-S-D-S-S-S-D-"
					   + "D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D";
		String[] cases=chaine.split("-");
		ClientDijkstra.lab=new String[ClientDijkstra.hauteur][];
		for(int j=0; j<ClientDijkstra.hauteur; j++){
			String[] ligne=new String[ClientDijkstra.largeur];
			for(int i=0; i<ClientDijkstra.largeur; i++){
				ligne[i]=cases[i+(j*ClientDijkstra.largeur)];
			}
			ClientDijkstra.lab[j]=ligne;
		}
		
		ClientDijkstra.position=new Box(1,1);
		
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

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testAlgo() {
		Edge[] edges=listAretes.toArray(new Edge[1]);
	    Graph g = new Graph(edges);

		g.calculateShortestDistances();
		g.printResult();
	}
	
	@Test
	@Ignore
	public void testGraph() {
		Edge[] edges=listAretes.toArray(new Edge[1]);
	    Graph g = new Graph(edges);
	    int nombre=0;
	    for(Vertex sommet:g.getNodes()){
	    	nombre+=1;
	    	System.out.println(nombre+" List:");
	    	for(Edge e:sommet.getEdges()){
	    		int taille=e.getListBox().size();
	    		System.out.println("Depart="+e.getListBox().get(0).getX()+":"+e.getListBox().get(0).getY()+"|Fin="+e.getListBox().get(taille-1).getX()+":"+e.getListBox().get(taille-1).getY()+"|Taille="+taille);
	    	}
	    }
	}
	
	@Test
	@Ignore
	public void testConstruct(){
		for(Edge arete:listAretes){
			int taille=arete.getListBox().size();
			System.out.println("Depart="+arete.getListBox().get(0).getX()+":"+arete.getListBox().get(0).getY()+"|Fin="+arete.getListBox().get(taille-1).getX()+":"+arete.getListBox().get(taille-1).getY()+"|Taille="+taille);
		}
	}

}
