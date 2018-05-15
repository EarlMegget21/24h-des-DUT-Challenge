package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import concours.ClientAstar;
import concours.ClientAstar.Noeud;

public class TestClientAstar {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		ClientAstar.largeur=25;
		ClientAstar.hauteur=17;
		
		String chaine = "D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-"
				   + "D-S-D-S-S-S-S-S-D-S-D-S-D-S-S-S-D-S-S-S-S-S-S-S-D-"
				   + "D-S-D-D-D-S-D-S-D-S-D-S-D-D-D-S-D-D-D-D-D-S-D-D-D-"
				   + "D-S-S-S-S-S-D-F-S-S-S-F-S-S-D-S-D-80-D-S-D-S-S-S-D-"
				   + "D-D-D-D-D-D-D-S-D-D-D-D-D-F-D-S-D-S-D-S-D-D-D-S-D-"
				   + "D-S-D-S-D-40-S-60-D-B-D-S-S-S-S-S-B-80-D-S-S-S-D-S-D-"
				   + "D-S-D-F-D-D-D-D-D-S-D-D-D-D-D-S-D-D-D-D-D-S-D-S-D-"
				   + "D-S-S-S-S-S-B-S-S-S-D-S-S-S-D-B-D-F-D-S-S-S-S-S-D-"
				   + "D-S-D-S-D-D-D-S-D-D-D-S-D-D-D-S-D-S-D-F-D-S-D-D-D-"
				   + "D-S-D-S-D-S-D-S-D-60-S-90-S-S-S-S-D-S-D-S-D-S-D-S-D-"
				   + "D-D-D-S-D-S-D-D-D-D-D-S-D-D-D-F-D-S-D-D-D-90-D-S-D-"
				   + "D-S-S-S-S-S-90-S-S-S-S-F-S-S-D-S-D-S-D-S-S-S-S-S-D-"
				   + "D-S-D-S-D-S-D-D-D-D-D-S-D-D-D-S-D-S-D-S-D-S-D-S-D-"
				   + "D-S-D-S-D-90-S-S-D-S-S-B-D-S-S-S-S-S-S-40-D-S-D-S-D-"
				   + "D-S-D-D-D-D-D-D-D-D-D-S-D-S-D-D-D-D-D-S-D-D-D-S-D-"
				   + "D-S-S-S-S-S-S-S-S-S-D-S-D-S-S-S-S-S-D-S-D-S-S-S-D-"
				   + "D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D-D";
		String[] cases=chaine.split("-");
		ClientAstar.lab=new String[ClientAstar.hauteur][];
		for(int j=0; j<ClientAstar.hauteur; j++){
			String[] ligne=new String[ClientAstar.largeur];
			for(int i=0; i<ClientAstar.largeur; i++){
				ligne[i]=cases[i+(j*ClientAstar.largeur)];
			}
			ClientAstar.lab[j]=ligne;
		}
		
		ClientAstar.position=new Noeud();
		ClientAstar.position.setX(1);
		ClientAstar.position.setY(1);
		ClientAstar.position.setCoutG(0);
		ClientAstar.objectif=new Noeud();
		ClientAstar.objectif.setX(3);
		ClientAstar.objectif.setY(1);

	}
	
	/* pour tester les autres méthodes, il faut décommenter ça, enlver le @Ignore sur les methodes et
	 * mettre @Ignore sur testJouerUnTour()
	 */
//	@Before
//	public void setUp(){
//	
//		ClientAstar.Noeud n1=new Noeud(null, 2, 1, 3, 1, 2);
//		ClientAstar.Noeud n2=new Noeud(null, 0, 1, 1, 1, 3);
//		ClientAstar.Noeud n3=new Noeud(null, 2, 1.5, 3.5, 6, 2);
//		
//		ClientAstar.listeOuverte.put("1:2", n1);
//		ClientAstar.listeOuverte.put("1:3", n2);
//		ClientAstar.listeOuverte.put("6:2", n3);
//		
//		//ClientAstar.Noeud n5=new Noeud(null, 2, 1, 3, 1, 1); //Depart
//		ClientAstar.Noeud n6=new Noeud(ClientAstar.depart, 0, 1, 1, 2, 3);
//		ClientAstar.Noeud n7=new Noeud(n6, 2, 1.5, 3.5, 3, 1); //Objectif
//		
//		ClientAstar.listeFermee.put("1:1", ClientAstar.depart);
//		ClientAstar.listeFermee.put("2:3", n6);
//		ClientAstar.listeFermee.put("3:1", n7);
//	}

	@Test
	public void testJouerTour() {
		ClientAstar.jouerTour();
		for(Noeud elem:ClientAstar.chemin){
			ClientAstar.lab[elem.getY()][elem.getX()]="|"; //on marque le chemin avec des |
		}
		for(int j=0; j<ClientAstar.hauteur; j++){
			for(int i=0; i<ClientAstar.largeur; i++){
				System.out.print(ClientAstar.lab[j][i]);
			}
			System.out.print("\n");
		}
	}
	

	@Test
	public void testTrouverCible(){
		ClientAstar.trouverCible();
		assertEquals(11, ClientAstar.objectif.getX());
		assertEquals(9, ClientAstar.objectif.getY());
	}

	@Test
	@Ignore
	public void testAjouter_cases_adjacentes() {
		ClientAstar.ajouter_cases_adjacentes(ClientAstar.position);
		assertEquals(3,ClientAstar.listeOuverte.get("1:2").getCoutF());
		Noeud test=new Noeud();
		test.setX(5);
		test.setY(1);
		test.setCoutG(3);
		ClientAstar.listeFermee.put("5:1", test);
		ClientAstar.ajouter_cases_adjacentes(test);
		assertTrue(ClientAstar.listeOuverte.get("5:2")!=null);
		assertTrue(ClientAstar.listeOuverte.get("4:1")!=null);
		assertTrue(ClientAstar.listeOuverte.get("6:1")!=null);
	}

	@Test
	@Ignore
	public void testRetrouver_chemin() {
		ClientAstar.retrouver_chemin();
		
	    assertEquals(3, ClientAstar.chemin.get(1).getX());
	    assertEquals(1, ClientAstar.chemin.get(1).getY());
	    assertEquals(2, ClientAstar.chemin.get(0).getX());
	    assertEquals(3, ClientAstar.chemin.get(0).getY());
	}

	@Test
	@Ignore
	public void testAjouter_liste_fermee() {
		int tailleOuverte=ClientAstar.listeOuverte.size();
		int tailleFermee=ClientAstar.listeFermee.size();
		ClientAstar.ajouter_liste_fermee("6:2");
		ClientAstar.ajouter_liste_fermee("1:3");
		ClientAstar.ajouter_liste_fermee("1:2");
		assertEquals(tailleOuverte-3, ClientAstar.listeOuverte.size());
		assertEquals(tailleFermee+3, ClientAstar.listeFermee.size());
	}

	@Test
	@Ignore
	public void testMeilleur_noeud() {
		assertEquals("1:3", ClientAstar.meilleur_noeud(ClientAstar.listeOuverte)); //on veut le cout le plus bas
	}

	@Test
	@Ignore
	public void testDistance() {
		assertTrue(4.47<=ClientAstar.distance(0,0,2,4));
		assertTrue(4.48>=ClientAstar.distance(0,0,2,4));
		assertTrue(4.47<=ClientAstar.distance(2,4,0,0));
		assertTrue(4.48>=ClientAstar.distance(2,4,0,0));
		assertTrue(4.47<=ClientAstar.distance(0,0,-2,-4));
		assertTrue(4.48>=ClientAstar.distance(0,0,-2,-4));
		assertTrue(4.47<=ClientAstar.distance(-2,-4,0,0));
		assertTrue(4.48>=ClientAstar.distance(-2,-4,0,0));
	}

	@Test
	@Ignore
	public void testDeja_present_dans_liste() {
		ClientAstar.listeOuverte.put("1:1", new Noeud());
		assertTrue(ClientAstar.deja_present_dans_liste("1:1", ClientAstar.listeOuverte));
		
		assertTrue(ClientAstar.deja_present_dans_liste(new String("1:1"), ClientAstar.listeOuverte));
		
		assertFalse(ClientAstar.deja_present_dans_liste("3:1",ClientAstar.listeOuverte));
	}

}
