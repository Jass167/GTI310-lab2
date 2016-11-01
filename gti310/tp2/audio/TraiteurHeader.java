package gti310.tp2.audio;

import java.nio.ByteBuffer;

/**Cette classe fait l'étude complète du header d'un wave file entrant et update 
 * le header du fichier wave résultant.
 * Cette classe travaille en parallèle avec la classe 
 * Convert44100HzTo8000HzAudioFIlter */
public class TraiteurHeader {

	private byte [] headerEnBrut;
	private int checkWave;
	//si stereo =2 si mono =1--on va considerer comme le nombres de canaux
	private int isStereo;
	private int chunkSize;
	private int subchunk2Size;
	private int byteRate;
	private int bitParSample;
	private int sampleRate;

	//constructor
	public TraiteurHeader(byte[] headerEnBrut) {
		this.headerEnBrut = headerEnBrut;	

		//Liste des methodes pour trouver toutes les donnï¿½es qu'on a
		//besoin dans le header
		this.chunkSize = findChunkSize();
		this.subchunk2Size = findSubchunk2Size();
		this.byteRate = findByteRate();
		this.bitParSample = findBitParSample();
		this.sampleRate = findSampleRate();
		this.isStereo = findIsStereo();
		this.checkWave = findCheckWave();
	}

	

	//methode qui fait la somme des bytes a partir d'une position et une
	//longueur sur le old header 
	private int readTheBytesToInteger(int startByte, int longueur){

		byte[] sommeBytes = new byte[longueur];
		int resultat=0;
		//il faut ajouter les bytes de droite a gauche a cause de ENDIAN!!
		//only flip little ENDIAN
		int inverse = 1;
		for(int i = startByte; i<startByte+longueur;i++){
			sommeBytes[longueur-inverse]=headerEnBrut[i];
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
	
	private int findCheckWave() {
		return readTheBytesToInteger(8,4);
	}

	private int findIsStereo() {
		return readTheBytesToInteger(22,2);
	}

	private int findSubchunk2Size() {
		return readTheBytesToInteger(40,4);
	}

	private int findBitParSample() {
		return readTheBytesToInteger(34,2);
	}

	private int findSampleRate() {
		return readTheBytesToInteger(24,4);
	}

	private int findByteRate() {
		return readTheBytesToInteger(28,4);
	}

	private int findChunkSize() {
		return readTheBytesToInteger(4,4);
	}



	public byte[] getHeaderEnBrut() {
		return headerEnBrut;
	}



	public int getCheckWave() {
		return checkWave;
	}



	public int getIsStereo() {
		return isStereo;
	}


	public int getChunkSize() {
		return chunkSize;
	}



	public int getChunk2Size() {
		return subchunk2Size;
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
	
	public byte[] updateDuNewHeader(int newBitParSample, int newIsEstereo, 
			int newNumberOfSamples){
				
		//7 changements en total pour new header 
		//ATTENTION, l'ordre des methodes est important
		newSetNumChannels(newIsEstereo);				//good	
		newSetsampleRate();//fixe a 8000				//good
		newSetbyteRate(newBitParSample,newIsEstereo);	//good
		newSetBlockAlign(newBitParSample,newIsEstereo); //good
		newSetbitParSample(newBitParSample);			//good
		
		newSetSubchunk2Size(newBitParSample,newIsEstereo,newNumberOfSamples);
		newSetchunkSize(newBitParSample,newIsEstereo,newNumberOfSamples);
		
		
		return headerEnBrut;
	}



	private void newSetBlockAlign(int newBitParSample, int newIsEstereo) {

		int resultat= newIsEstereo * (newBitParSample/8);
		
		byte[] newValue =  
				ByteBuffer.allocate(2).putShort((short)resultat).array();
        headerEnBrut[32]=newValue[1];
        headerEnBrut[33]=newValue[0];
	
}



	private void newSetNumChannels(int newIsEstereo) {

		byte[] newValue =  
				ByteBuffer.allocate(2).putShort((short)newIsEstereo).array();
        headerEnBrut[22]=newValue[1];
        headerEnBrut[23]=newValue[0];
	
}



	private void newSetsampleRate() {

		byte[] newValue =  ByteBuffer.allocate(4).putInt(8000).array();
        headerEnBrut[24]=newValue[3];
        headerEnBrut[25]=newValue[2];
        headerEnBrut[26]=newValue[1];
        headerEnBrut[27]=newValue[0];
 
}


	private void newSetbitParSample(int newBitParSample) {

		byte[] newValue =  
				ByteBuffer.allocate(2).putShort((short)newBitParSample).array();
        headerEnBrut[34]=newValue[1];
        headerEnBrut[35]=newValue[0];
	}



	private void newSetbyteRate(int newBitParSample, int newIsEstereo) {
		
		int resultat = 8000 * newIsEstereo * (newBitParSample/8);
		
		byte[] newValue =  ByteBuffer.allocate(4).putInt(resultat).array();
        headerEnBrut[28]=newValue[3];
        headerEnBrut[29]=newValue[2];
        headerEnBrut[30]=newValue[1];
        headerEnBrut[31]=newValue[0];
	}



	private void newSetSubchunk2Size(int newBitParSample,int newIsStereo,
			int newNumberOfSamples) {
		
		int resultat = newNumberOfSamples * newIsStereo * (newBitParSample/8);
		
		byte[] newValue =  ByteBuffer.allocate(4).putInt(resultat).array();
        headerEnBrut[40]=newValue[3];
        headerEnBrut[41]=newValue[2];
        headerEnBrut[42]=newValue[1];
        headerEnBrut[43]=newValue[0];
		
	}

	private void newSetchunkSize(int newBitParSample,int newIsEstereo, 
			int newNumberOfSamples) {
		
			/*ChunkSize:       36 + SubChunk2Size, or more precisely:
                               4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
                               This is the size of the rest of the chunk 
                               following this number.  This is the size of the 
                               entire file in bytes minus 8 bytes for the
                               two fields not included in this count:
                               ChunkID and ChunkSize.*/
		int resultat = 36 + 
				( newNumberOfSamples * newIsEstereo * (newBitParSample/8) );
	
		
		byte[] newValue =  ByteBuffer.allocate(4).putInt(resultat).array();
        headerEnBrut[4]=newValue[3];
        headerEnBrut[5]=newValue[2];
        headerEnBrut[6]=newValue[1];
        headerEnBrut[7]=newValue[0];
	}
}
