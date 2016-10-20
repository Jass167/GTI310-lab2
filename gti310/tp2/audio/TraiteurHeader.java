package gti310.tp2.audio;

import java.nio.ByteBuffer;

//cette clase va faire l'etude complet de le header entrant et l'update de le header sortant
//les validations on va les faire sur la clase Convert44100HzTo8000HzAudioFIlter a travers de gets et sets
//
public class TraiteurHeader {

	private byte [] headerEnBrut;
	private int checkWave;
	//si estereo =2 si mono =1--on va considerer comme le nombres de canaux
	private int isEstereo;
	private int chunkSize;
	private int chunk2Size;
	private int byteRate;
	private int bitParSample;
	private int sampleRate;

	// TODO Auto-generated constructor stub
	public TraiteurHeader(byte[] headerEnBrut) {
		this.headerEnBrut= headerEnBrut;	

		//Liste des methodes pour trouver toutes les donnes qu'on a besoin dans le header
		this.chunkSize= findchunkSize();
		this.chunk2Size= findchunk2Size();
		this.byteRate= findbyteRate();
		this.bitParSample= findbitParSample();
		this.sampleRate= findsampleRate();
		this.isEstereo= findIsEstereo();
			
		this.checkWave= findcheckWave();
	}

	

	//methode qui fait la somme des bytes a partir d'une position et un longuer sur le old header 
	private int readTheBytesToInteger(int startByte, int longeur){

		byte[] sommeBytes = new byte[longeur];
		int resultat=0;
		//il faut ajouter les bytes de droite a gauche a cause de ENDIAN!!
		//only flip little ENDIAN
		int inverse = 1;
		for(int i = startByte; i<startByte+longeur;i++){
			sommeBytes[longeur-inverse]=headerEnBrut[i];
			inverse ++;		
		}

		if(sommeBytes.length==2){
			resultat= ByteBuffer.wrap(sommeBytes).getShort();
		}
		else if(sommeBytes.length == 4){
			resultat=  ByteBuffer.wrap(sommeBytes).getInt();
		}
		return resultat;
	}
	
	private int findcheckWave() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(8,4);
	}

	private int findIsEstereo() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(22,2);
	}

	private int findchunk2Size() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(40,4);
	}

	private int findbitParSample() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(34,2);
	}

	private int findsampleRate() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(24,4);
	}

	private int findbyteRate() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(28,4);
	}

	private int findchunkSize() {
		// TODO Auto-generated method stub
		return readTheBytesToInteger(4,4);
	}



	public byte[] getHeaderEnBrut() {
		return headerEnBrut;
	}



	public int getCheckWave() {
		return checkWave;
	}



	public int getIsEstereo() {
		return isEstereo;
	}


	public int getChunkSize() {
		return chunkSize;
	}



	public int getChunk2Size() {
		return chunk2Size;
	}



	public int getByteRate() {
		return byteRate;
	}



	public int getBitParSample() {
		return bitParSample;
	}
	

	public int getSampleRate() {
		return sampleRate;
	}
//------------------------------------------------------------------------
	
	//On va mettre les updates pour le nouveau header a continuationn...
	
//-------------------------------------------------------------------------
	
	public void updateDuNewHeader(int newBitParSample, int newIsEstereo){
		
		newSetchunkSize();
		newSetchunk2Size();
		newSetbyteRate();
		newSetbitParSample();
		newSetsampleRate();

	}



	private void newSetsampleRate() {
		// TODO Auto-generated method stub
		
	}



	private void newSetbitParSample() {
		// TODO Auto-generated method stub
		
	}



	private void newSetbyteRate() {
		// TODO Auto-generated method stub
		
	}



	private void newSetchunk2Size() {
		// TODO Auto-generated method stub
		
	}



	private void newSetchunkSize() {
		// TODO Auto-generated method stub
		
	}
	
}
